package com.example.PantryTracker

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.RecognitionListener


class MainActivity : AppCompatActivity() {
    private lateinit var model: PantryModel
    private lateinit var listView: ListView
    private lateinit var searchEditText: EditText
    private lateinit var addButton: Button
    private lateinit var statsButton: Button
    private lateinit var totalValueText: TextView
    private lateinit var totalItemsText: TextView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var adView: AdView
    private lateinit var prefs: SharedPreferences
    private lateinit var titleText: TextView
    private var items = ArrayList<PantryItem>()

    private val RECORD_AUDIO_PERMISSION_CODE = 1001
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechIntent: Intent
    private lateinit var micButton: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        model = PantryModel.getInstance()
        model.initialize()
        model.setOnItemsChangedListener {
            refreshList()
            updateStats()
        }

        prefs = getSharedPreferences("PantryPrefs", MODE_PRIVATE)

        listView = findViewById(R.id.itemListView)
        searchEditText = findViewById(R.id.searchEditText)
        addButton = findViewById(R.id.addButton)
        statsButton = findViewById(R.id.statsButton)
        totalValueText = findViewById(R.id.totalValueText)
        totalItemsText = findViewById(R.id.totalItemsText)


        micButton = findViewById(R.id.micButton)
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")

        applyUserPreferences()


        // Set up list view
        refreshList()

        // Search functionality
        val searchButton = findViewById<Button>(R.id.searchButton)
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString()
            items = model.searchItems(query)
            updateListView()
        }

        // Add button
        addButton.setOnClickListener {
            val intent = Intent(this, AddEditActivity::class.java)
            startActivity(intent)
        }

        // Stats button
        statsButton.setOnClickListener {
            val intent = Intent(this, StatsActivity::class.java)
            startActivity(intent)
        }

        // Item click - go to edit
        listView.setOnItemClickListener { _, _, position, _ ->
            val item = items[position]
            val intent = Intent(this, AddEditActivity::class.java)
            intent.putExtra("item_id", item.id)
            intent.putExtra("item_name", item.name)
            intent.putExtra("item_quantity", item.quantity)
            intent.putExtra("item_price", item.price)
            intent.putExtra("item_category", item.category)
            intent.putExtra("item_timestamp", item.timestamp)
            startActivity(intent)
        }

        // Item long click - delete
        listView.setOnItemLongClickListener { _, _, position, _ ->
            val item = items[position]
            model.deleteItem(item.id)
            refreshList()
            true
        }

        micButton.setOnClickListener {
            if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                    arrayOf(android.Manifest.permission.RECORD_AUDIO),
                    RECORD_AUDIO_PERMISSION_CODE
                )
            } else {
                speechRecognizer.startListening(speechIntent)
            }
        }


        // Set up advertising
        setupAdvertising()

        // Update stats
        updateStats()
    }

    override fun onResume() {
        super.onResume()
        refreshList()
        updateStats()
    }

    private fun refreshList() {
        items = model.getItems()
        updateListView()
    }

    private fun updateListView() {
        val displayItems = ArrayList<String>()
        for (item in items) {
            displayItems.add("${item.name} - Qty: ${item.quantity} - $${String.format("%.2f", item.price)}")
        }

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayItems)
        listView.adapter = adapter
    }

    private fun updateStats() {
        val totalValue = model.getTotalValue()
        val totalItems = model.getTotalItems()

        totalValueText.text = "Total Value: $${String.format("%.2f", totalValue)}"
        totalItemsText.text = "Total Items: $totalItems"
    }

    private fun setupAdvertising() {
        adView = AdView(this)
        val adSize = AdSize(AdSize.FULL_WIDTH, AdSize.AUTO_HEIGHT)
        adView.setAdSize(adSize)

        // Test ad unit ID
        val adUnitId = "ca-app-pub-3940256099942544/6300978111"
        adView.adUnitId = adUnitId

        val builder = AdRequest.Builder()
        builder.addKeyword("shopping").addKeyword("grocery")
        val adRequest = builder.build()

        val adLayout = findViewById<LinearLayout>(R.id.ad_view)
        adLayout.addView(adView)

        adView.loadAd(adRequest)
    }

    private fun applyUserPreferences() {
        val theme = prefs.getString("theme", "pink")
        titleText = findViewById(R.id.titleText)

        val bgColor = if (theme == "pink") R.color.pinkTheme else R.color.blueTheme
        findViewById<LinearLayout>(R.id.main).setBackgroundColor(resources.getColor(bgColor))

        val username = prefs.getString("username", "User")
        titleText.text = "$username's Pantry Tracker"


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RECORD_AUDIO_PERMISSION_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            speechRecognizer.startListening(speechIntent)
        }
    }

    override fun onStart() {
        super.onStart()

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.get(0) ?: ""

                // Put the spoken text into the search bar
                searchEditText.setText(spokenText)

                // Trigger your existing search button
                val searchButton = findViewById<Button>(R.id.searchButton)
                searchButton.performClick()
            }

            override fun onError(error: Int) {}
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }



}

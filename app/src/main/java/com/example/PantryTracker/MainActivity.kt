package com.example.PantryTracker

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

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

}

package com.example.PantryTracker

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class StatsActivity : AppCompatActivity() {

    private lateinit var model: PantryModel
    private lateinit var totalValueText: TextView
    private lateinit var totalItemsText: TextView
    private lateinit var categoryListView: ListView
    private lateinit var backButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var progressLabel: TextView

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        // Initialize Model
        model = PantryModel.getInstance()

        // Find views
        totalValueText = findViewById(R.id.totalValueText)
        totalItemsText = findViewById(R.id.totalItemsText)
        categoryListView = findViewById(R.id.categoryListView)
        backButton = findViewById(R.id.backButton)
        progressBar = findViewById(R.id.progressBar)
        progressLabel = findViewById(R.id.progressLabel)

        prefs = getSharedPreferences("PantryPrefs", MODE_PRIVATE)
        applyUserPreferences()

        // Back button
        backButton.setOnClickListener {
            finish()
        }

        // Load statistics
        loadStats()
    }

    private fun loadStats() {
        val items = model.getItems()
        val totalValue = model.getTotalValue()
        val totalItems = model.getTotalItems()

        // Display totals
        totalValueText.text = "Total Value: $${String.format("%.2f", totalValue)}"
        totalItemsText.text = "Total Items: $totalItems"

        // Calculate category statistics
        val categoryMap = HashMap<String, Int>()
        for (item in items) {
            val count = categoryMap.getOrDefault(item.category, 0)
            categoryMap[item.category] = count + item.quantity
        }

        // Display by category
        val categoryList = ArrayList<String>()
        for ((category, count) in categoryMap) {
            categoryList.add("$category: $count items")
        }

        if (categoryList.isEmpty()) {
            categoryList.add("No items in pantry")
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categoryList)
        categoryListView.adapter = adapter

        // Set up progress bar (representing pantry fullness, max 100 items)
        progressBar.max = 100
        progressBar.progress = if (totalItems > 100) 100 else totalItems
        progressLabel.text = "Pantry Fullness: $totalItems / 100"
    }

    private fun applyUserPreferences() {

        val theme = prefs.getString("theme", "pink")

        val bgColor = if (theme == "pink") R.color.pinkTheme else R.color.blueTheme
        findViewById<LinearLayout>(R.id.main).setBackgroundColor(resources.getColor(bgColor))



    }
}

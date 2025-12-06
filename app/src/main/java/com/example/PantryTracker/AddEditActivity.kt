package com.example.PantryTracker

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AddEditActivity : AppCompatActivity() {

    private lateinit var model: PantryModel
    private lateinit var nameEditText: EditText
    private lateinit var quantityEditText: EditText
    private lateinit var priceEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var ratingBar: RatingBar
    private lateinit var seekBar: SeekBar
    private lateinit var seekBarLabel: TextView
    private lateinit var prefs: SharedPreferences

    private var editingItem: PantryItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit)

        // Initialize Model
        model = PantryModel.getInstance()

        // Find views
        nameEditText = findViewById(R.id.nameEditText)
        quantityEditText = findViewById(R.id.quantityEditText)
        priceEditText = findViewById(R.id.priceEditText)
        categorySpinner = findViewById(R.id.categorySpinner)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
        ratingBar = findViewById(R.id.ratingBar)
        seekBar = findViewById(R.id.seekBar)
        seekBarLabel = findViewById(R.id.seekBarLabel)

        prefs = getSharedPreferences("PantryPrefs", MODE_PRIVATE)
        applyUserPreferences()

        // Set up category spinner
        val categories = arrayOf("Dairy", "Vegetables", "Fruits", "Meat", "Grains", "Snacks", "Beverages", "Other")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = spinnerAdapter

        // Set up SeekBar (for quantity slider)
        seekBar.max = 100
        seekBar.progress = 1
        seekBarLabel.text = "Quantity: 1"

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val qty = if (progress == 0) 1 else progress
                seekBarLabel.text = "Quantity: $qty"
                quantityEditText.setText(qty.toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Check if editing existing item
        val item = intent.getSerializableExtra("item") as? PantryItem
        if (item != null) {
            editingItem = item
            loadItemData(item)
        }

        // Save button
        saveButton.setOnClickListener {
            saveItem()
        }

        // Cancel button
        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun loadItemData(item: PantryItem) {
        nameEditText.setText(item.name)
        quantityEditText.setText(item.quantity.toString())
        priceEditText.setText(item.price.toString())

        // Set category spinner
        val categories = arrayOf("Dairy", "Vegetables", "Fruits", "Meat", "Grains", "Snacks", "Beverages", "Other")
        val categoryIndex = categories.indexOf(item.category)
        if (categoryIndex >= 0) {
            categorySpinner.setSelection(categoryIndex)
        }

        // Set seekbar
        seekBar.progress = item.quantity
        seekBarLabel.text = "Quantity: ${item.quantity}"
    }

    private fun saveItem() {
        val name = nameEditText.text.toString().trim()
        val quantityStr = quantityEditText.text.toString().trim()
        val priceStr = priceEditText.text.toString().trim()
        val category = categorySpinner.selectedItem.toString()

        // Validation
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter item name", Toast.LENGTH_SHORT).show()
            return
        }

        if (quantityStr.isEmpty()) {
            Toast.makeText(this, "Please enter quantity", Toast.LENGTH_SHORT).show()
            return
        }

        if (priceStr.isEmpty()) {
            Toast.makeText(this, "Please enter price", Toast.LENGTH_SHORT).show()
            return
        }

        val quantity = quantityStr.toIntOrNull() ?: 0
        val price = priceStr.toDoubleOrNull() ?: 0.0

        if (quantity <= 0) {
            Toast.makeText(this, "Quantity must be greater than 0", Toast.LENGTH_SHORT).show()
            return
        }

        if (price < 0) {
            Toast.makeText(this, "Price cannot be negative", Toast.LENGTH_SHORT).show()
            return
        }

        // Save to model
        if (editingItem != null) {
            // Update existing item
            editingItem!!.name = name
            editingItem!!.quantity = quantity
            editingItem!!.price = price
            editingItem!!.category = category
            model.updateItem(editingItem!!, this)
            Toast.makeText(this, "Item updated", Toast.LENGTH_SHORT).show()
        } else {
            // Add new item
            val newItem = PantryItem(
                id = "",
                name = name,
                quantity = quantity,
                price = price,
                category = category
            )
            model.addItem(newItem, this)
            Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    private fun applyUserPreferences() {

        val theme = prefs.getString("theme", "pink")

        val bgColor = if (theme == "pink") R.color.pinkTheme else R.color.blueTheme
        findViewById<LinearLayout>(R.id.main).setBackgroundColor(resources.getColor(bgColor))



    }
}

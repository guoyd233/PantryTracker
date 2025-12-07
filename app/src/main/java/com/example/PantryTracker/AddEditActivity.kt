package com.example.PantryTracker

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AddEditActivity : AppCompatActivity() {

    private lateinit var model: PantryModel
    private val categories = arrayOf("Dairy", "Vegetables", "Fruits", "Meat", "Grains", "Snacks", "Beverages", "Other")
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

        model = PantryModel.getInstance()

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

        // category spinner
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = spinnerAdapter

        // quantity slidebar (seekbar)
        seekBar.max = 100
        seekBar.progress = 1
        seekBarLabel.text = "Quantity: 1"
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val q = if (progress == 0) 1 else progress
                seekBarLabel.text = "Quantity: $q"
                quantityEditText.setText(q.toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // edit existing item
        val itemId = intent.getStringExtra("item_id")
        if (itemId != null) {
            val item = PantryItem(
                id = itemId,
                name = intent.getStringExtra("item_name") ?: "",
                quantity = intent.getIntExtra("item_quantity", 0),
                price = intent.getDoubleExtra("item_price", 0.0),
                category = intent.getStringExtra("item_category") ?: "",
                timestamp = intent.getLongExtra("item_timestamp", 0L)
            )
            editingItem = item
            loadItemData(item)
        }

        saveButton.setOnClickListener {
            saveItem()
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun loadItemData(item: PantryItem) {
        nameEditText.setText(item.name)
        quantityEditText.setText(item.quantity.toString())
        priceEditText.setText(item.price.toString())

        val categoryIndex = categories.indexOf(item.category)
        if (categoryIndex >= 0) {
            categorySpinner.setSelection(categoryIndex)
        }

        seekBar.progress = item.quantity
        seekBarLabel.text = "Quantity: ${item.quantity}"
    }

    private fun saveItem() {
        val name = nameEditText.text.toString().trim()
        val quantityInput = quantityEditText.text.toString().trim()
        val priceInput = priceEditText.text.toString().trim()
        val category = categorySpinner.selectedItem.toString()

        if (name.isEmpty()) {
            Toast.makeText(this, "item name is empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (quantityInput.isEmpty()) {
            Toast.makeText(this, "quantity is empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (priceInput.isEmpty()) {
            Toast.makeText(this, "price is empty", Toast.LENGTH_SHORT).show()
            return
        }

        val quantity = quantityInput.toIntOrNull() ?: 0
        val price = priceInput.toDoubleOrNull() ?: 0.0

        if (quantity <= 0) {
            Toast.makeText(this, "quantity should > 0", Toast.LENGTH_SHORT).show()
            return
        }
        if (price < 0) {
            Toast.makeText(this, "price should >=0", Toast.LENGTH_SHORT).show()
            return
        }

        // edit existing item
        if (editingItem != null) {
            editingItem!!.name = name
            editingItem!!.quantity = quantity
            editingItem!!.price = price
            editingItem!!.category = category
            model.updateItem(editingItem!!)
            Toast.makeText(this, "updated", Toast.LENGTH_SHORT).show()
        } else { // add new item
            val newItem = PantryItem(
                id = "",
                name = name,
                quantity = quantity,
                price = price,
                category = category
            )
            model.addItem(newItem)
            Toast.makeText(this, "added", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    private fun applyUserPreferences() {

        val theme = prefs.getString("theme", "pink")

        val bgColor = if (theme == "pink") R.color.pinkTheme else R.color.blueTheme
        findViewById<LinearLayout>(R.id.main).setBackgroundColor(resources.getColor(bgColor))



    }
}

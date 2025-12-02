package com.example.PantryTracker

import android.content.Context
import com.google.firebase.database.*

class PantryModel private constructor() {

    private var items = ArrayList<PantryItem>()
    private lateinit var firebaseRef: DatabaseReference

    companion object {
        private var instance: PantryModel? = null

        fun getInstance(): PantryModel {
            if (instance == null) {
                instance = PantryModel()
            }
            return instance!!
        }
    }

    fun initialize(context: Context) {
        // Initialize Firebase
        val firebase = FirebaseDatabase.getInstance()
        firebaseRef = firebase.getReference("pantryItems")

        // Load local data
        loadLocalData(context)

        // Listen to Firebase changes
        firebaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val firebaseItems = ArrayList<PantryItem>()
                for (childSnapshot in snapshot.children) {
                    val item = childSnapshot.getValue(PantryItem::class.java)
                    if (item != null) {
                        firebaseItems.add(item)
                    }
                }
                // Update local items with Firebase data
                if (firebaseItems.isNotEmpty()) {
                    items = firebaseItems
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun addItem(item: PantryItem, context: Context) {
        // Generate ID if needed
        if (item.id.isEmpty()) {
            item.id = System.currentTimeMillis().toString()
        }
        item.timestamp = System.currentTimeMillis()

        items.add(item)

        // Save to Firebase
        firebaseRef.child(item.id).setValue(item)

        // Save to local storage
        saveLocalData(context)
    }

    fun updateItem(item: PantryItem, context: Context) {
        item.timestamp = System.currentTimeMillis()

        val index = items.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            items[index] = item

            // Update Firebase
            firebaseRef.child(item.id).setValue(item)

            // Save to local storage
            saveLocalData(context)
        }
    }

    fun deleteItem(itemId: String, context: Context) {
        items.removeAll { it.id == itemId }

        // Delete from Firebase
        firebaseRef.child(itemId).removeValue()

        // Save to local storage
        saveLocalData(context)
    }

    fun getItems(): ArrayList<PantryItem> {
        return ArrayList(items)
    }

    fun getItemById(id: String): PantryItem? {
        return items.find { it.id == id }
    }

    fun searchItems(query: String): ArrayList<PantryItem> {
        if (query.isEmpty()) return ArrayList(items)  // return all items if quesy is empty

        val results = ArrayList<PantryItem>()
        val lowerQuery = query.lowercase()

        for (item in items) {
            if (item.name.lowercase().contains(lowerQuery)) {
                results.add(item)
            }
        }
        return results
    }

    fun getTotalValue(): Double {
        var total = 0.0
        for (item in items) {
            total += item.price * item.quantity
        }
        return total
    }

    fun getTotalItems(): Int {
        var total = 0
        for (item in items) {
            total += item.quantity
        }
        return total
    }

    private fun saveLocalData(context: Context) {
        val prefs = context.getSharedPreferences("PantryPrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Save each item
        editor.putInt("itemCount", items.size)
        for (i in items.indices) {
            val item = items[i]
            editor.putString("item_${i}_id", item.id)
            editor.putString("item_${i}_name", item.name)
            editor.putInt("item_${i}_quantity", item.quantity)
            editor.putFloat("item_${i}_price", item.price.toFloat())
            editor.putString("item_${i}_category", item.category)
            editor.putLong("item_${i}_timestamp", item.timestamp)
        }
        editor.apply()
    }

    private fun loadLocalData(context: Context) {
        val prefs = context.getSharedPreferences("PantryPrefs", Context.MODE_PRIVATE)
        val count = prefs.getInt("itemCount", 0)

        items.clear()
        for (i in 0 until count) {
            val item = PantryItem(
                id = prefs.getString("item_${i}_id", "") ?: "",
                name = prefs.getString("item_${i}_name", "") ?: "",
                quantity = prefs.getInt("item_${i}_quantity", 0),
                price = prefs.getFloat("item_${i}_price", 0f).toDouble(),
                category = prefs.getString("item_${i}_category", "") ?: "",
                timestamp = prefs.getLong("item_${i}_timestamp", 0L)
            )
            items.add(item)
        }
    }
}

package com.example.PantryTracker

import com.google.firebase.database.*

class PantryModel private constructor() {

    private var items = ArrayList<PantryItem>()
    private lateinit var firebaseRef: DatabaseReference
    private var onItemsChangedListener: (() -> Unit)? = null

    companion object {
        private val pm : PantryModel by lazy { PantryModel() }
        fun getInstance(): PantryModel = pm
    }

    fun initialize() {
        val firebase = FirebaseDatabase.getInstance()
        firebaseRef = firebase.getReference("pantryItems")

        // listen for changes to firebase
        firebaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val firebaseItems = ArrayList<PantryItem>()
                for (childSnapshot in snapshot.children) {
                    val item = childSnapshot.getValue(PantryItem::class.java)
                    if (item != null) {
                        firebaseItems.add(item)
                    }
                }
                items = firebaseItems

                // notify the listener
                onItemsChangedListener?.invoke()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun addItem(item: PantryItem) {
        // use timestamp as id
        if (item.id.isEmpty()) {
            item.id = System.currentTimeMillis().toString()
        }
        item.timestamp = System.currentTimeMillis()
        items.add(item)
        firebaseRef.child(item.id).setValue(item)
    }

    fun updateItem(item: PantryItem) {
        item.timestamp = System.currentTimeMillis()

        val index = items.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            items[index] = item
            firebaseRef.child(item.id).setValue(item)
        }
    }

    fun deleteItem(itemId: String) {
        items.removeAll { it.id == itemId }
        firebaseRef.child(itemId).removeValue()
    }

    fun getItems(): ArrayList<PantryItem> {
        return ArrayList(items)
    }

    fun setOnItemsChangedListener(listener: (() -> Unit)?) {
        onItemsChangedListener = listener
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

}

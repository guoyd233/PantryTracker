package com.example.PantryTracker

import java.io.Serializable

data class PantryItem(
    var id: String = "",
    var name: String = "",
    var quantity: Int = 0,
    var price: Double = 0.0,
    var category: String = "",
    var timestamp: Long = System.currentTimeMillis()
) : Serializable {
    constructor() : this("", "", 0, 0.0, "", System.currentTimeMillis())
}

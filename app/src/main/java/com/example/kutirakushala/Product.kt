package com.example.kutirakushala

data class Product(
    val name: String = "",
    val category: String = "",
    val price: String = "",
    val capacity: String = "",
    val imageUrl: String = "",
    val sellerName: String = "",
    val phone: String = "",
    val sellerUid: String = ""   // links to businesses/ and sellers/ documents
)
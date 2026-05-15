package com.example.kutirakushala

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    onLogout: () -> Unit,
    onViewSellerProfile: (sellerUid: String, sellerName: String, sellerPhone: String) -> Unit
) {
    val context = LocalContext.current
    val productList      = remember { mutableStateListOf<Product>() }
    var searchQuery      by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var isLoading        by remember { mutableStateOf(true) }

    val categories = listOf("All", "Food", "Craft", "Textile", "Other")

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance()
            .collection("products")
            .get()
            .addOnSuccessListener { result ->
                productList.clear()
                for (doc in result) {
                    productList.add(doc.toObject(Product::class.java))
                }
                isLoading = false
            }
            .addOnFailureListener { isLoading = false }
    }

    val filtered = productList.filter { p ->
        val catOk    = selectedCategory == "All" || p.category.equals(selectedCategory, ignoreCase = true)
        val searchOk = searchQuery.isBlank() ||
                p.name.contains(searchQuery, ignoreCase = true) ||
                p.sellerName.contains(searchQuery, ignoreCase = true) ||
                p.category.contains(searchQuery, ignoreCase = true)
        catOk && searchOk
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kutira-Kushala Marketplace") },
                actions = {
                    TextButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        onLogout()
                    }) { Text("Logout") }
                }
            )
        }
    ) { padding ->

        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search products, sellers...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick  = { selectedCategory = cat },
                        label    = { Text(cat) }
                    )
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Scaffold
            }

            if (filtered.isEmpty()) {
                Box(
                    Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (searchQuery.isNotBlank()) "No results for \"$searchQuery\""
                        else "No products in this category yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                return@Scaffold
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filtered) { product ->
                    ProductCard(
                        product = product,
                        onViewProfile = {
                            // Always navigate — pass whatever data we have
                            onViewSellerProfile(
                                product.sellerUid,
                                product.sellerName,
                                product.phone
                            )
                        }
                    )
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onViewProfile: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {

            if (product.imageUrl.isNotBlank()) {
                SubcomposeAsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            Modifier.fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator(modifier = Modifier.size(32.dp)) }
                    },
                    error = {
                        Box(
                            Modifier.fillMaxSize()
                                .background(MaterialTheme.colorScheme.errorContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Image unavailable",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                )
            } else {
                Box(
                    Modifier.fillMaxWidth().height(80.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No image", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    if (product.category.isNotBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = product.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "₹${product.price}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        " / wholesale",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(6.dp))

                if (product.capacity.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "📦 Ready to supply: ${product.capacity} units this week",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))
                Text("Seller: ${product.sellerName}", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(10.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onViewProfile,
                        modifier = Modifier.weight(1f)
                    ) { Text("View Profile") }

                    Button(
                        onClick = {
                            if (product.phone.isNotBlank()) {
                                context.startActivity(
                                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:${product.phone}"))
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("📞 Call") }
                }
            }
        }
    }
}
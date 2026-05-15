package com.example.kutirakushala

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerProfileViewScreen(
    sellerUid: String,       // may be blank for old products
    sellerName: String,      // always available from product card
    sellerPhone: String,     // always available from product card
    onBack: () -> Unit
) {
    val context        = LocalContext.current
    var business       by remember { mutableStateOf<Business?>(null) }
    var sellerProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    var weeklyCapacity by remember { mutableStateOf("") }
    var isLoading      by remember { mutableStateOf(true) }

    LaunchedEffect(sellerUid, sellerName) {
        val db = FirebaseFirestore.getInstance()

        // Load business profile only if we have a uid
        if (sellerUid.isNotBlank()) {
            db.collection("businesses").document(sellerUid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) business = doc.toObject(Business::class.java)
                }

            db.collection("sellers").document(sellerUid).get()
                .addOnSuccessListener { doc ->
                    weeklyCapacity = doc.getString("weeklyCapacity") ?: ""
                }
        }

        // Load products: filter by sellerUid if available, otherwise match by sellerName
        db.collection("products").get()
            .addOnSuccessListener { result ->
                val allProducts = result.map { it.toObject(Product::class.java) }
                sellerProducts = if (sellerUid.isNotBlank()) {
                    // New products have sellerUid stored
                    val byUid = allProducts.filter { it.sellerUid == sellerUid }
                    // Fallback: also match by sellerName in case uid wasn't stored
                    if (byUid.isNotEmpty()) byUid
                    else allProducts.filter {
                        it.sellerName.equals(sellerName, ignoreCase = true)
                    }
                } else {
                    // Old products — match by seller name
                    allProducts.filter {
                        it.sellerName.equals(sellerName, ignoreCase = true)
                    }
                }
                isLoading = false
            }
            .addOnFailureListener { isLoading = false }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (sellerName.isNotBlank()) sellerName else "Seller Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── Business profile card ──
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        // Photo + name row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val photoUrl = business?.teamPhotoUrl ?: ""
                            if (photoUrl.isNotBlank()) {
                                SubcomposeAsyncImage(
                                    model = photoUrl,
                                    contentDescription = "Team photo",
                                    modifier = Modifier.size(72.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    loading = {
                                        Box(
                                            Modifier.size(72.dp).clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                        )
                                    },
                                    error = {
                                        AvatarInitial(
                                            name = business?.ownerName ?: sellerName
                                        )
                                    }
                                )
                            } else {
                                AvatarInitial(name = business?.ownerName ?: sellerName)
                            }

                            Spacer(Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = business?.ownerName?.ifBlank { sellerName } ?: sellerName,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                if (!business?.businessType.isNullOrBlank()) {
                                    Text(
                                        text = business!!.businessType,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(12.dp))

                        // Skill + location badges
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (!business?.skillArea.isNullOrBlank()) {
                                InfoBadge(
                                    text = "🛠 ${business!!.skillArea}",
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    textColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            if (!business?.location.isNullOrBlank()) {
                                InfoBadge(
                                    text = "📍 ${business!!.location}",
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    textColor = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }

                        // Weekly capacity meter
                        val capacityToShow = weeklyCapacity.ifBlank {
                            sellerProducts.firstOrNull()?.capacity ?: ""
                        }
                        if (capacityToShow.isNotBlank()) {
                            Spacer(Modifier.height(10.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "📦 Ready to take orders for $capacityToShow units this week",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        // Direct Connect call button
                        val phoneToCall = business?.phone?.ifBlank { sellerPhone } ?: sellerPhone
                        if (phoneToCall.isNotBlank()) {
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    context.startActivity(
                                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneToCall"))
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("📞 Direct Connect: $phoneToCall")
                            }
                        }

                        // If no profile saved yet, show helpful message
                        if (business == null) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "This seller hasn't set up their full profile yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ── Products by this seller ──
            item {
                Text(
                    text = if (sellerProducts.isNotEmpty())
                        "Products by this seller (${sellerProducts.size})"
                    else
                        "Products",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (sellerProducts.isNotEmpty()) {
                items(sellerProducts) { product ->
                    ProductCard(product = product, onViewProfile = {})
                }
            } else {
                item {
                    Text(
                        "No products listed by this seller yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AvatarInitial(name: String) {
    Box(
        Modifier.size(72.dp).clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(1).uppercase(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun InfoBadge(text: String, containerColor: androidx.compose.ui.graphics.Color,
                      textColor: androidx.compose.ui.graphics.Color) {
    Surface(color = containerColor, shape = MaterialTheme.shapes.small) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
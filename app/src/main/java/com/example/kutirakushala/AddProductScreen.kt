package com.example.kutirakushala

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    onBack: () -> Unit = {}
) {
    val context   = LocalContext.current
    val uid       = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var productName by remember { mutableStateOf("") }
    var category    by remember { mutableStateOf("") }
    var price       by remember { mutableStateOf("") }
    var capacity    by remember { mutableStateOf("") }
    var imageUrl    by remember { mutableStateOf("") }
    var sellerName  by remember { mutableStateOf("") }
    var phone       by remember { mutableStateOf("") }
    var isSaving    by remember { mutableStateOf(false) }
    var expanded    by remember { mutableStateOf(false) }

    val categories = listOf("Food", "Craft", "Textile", "Other")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Add New Product", style = MaterialTheme.typography.headlineMedium)

        // URL tip card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "How to get a working image URL:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "• Right-click any image online → Copy image address\n" +
                            "• Imgur: use i.imgur.com/xxxxx.jpg (direct link)\n" +
                            "• Test URL: https://picsum.photos/400/300",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // Live image preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl.isNotBlank()) {
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = "Preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = { CircularProgressIndicator(modifier = Modifier.size(32.dp)) },
                    error = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Cannot load URL", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error)
                            Text("Try: https://picsum.photos/400/300",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                )
            } else {
                Text("Image preview appears here",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        OutlinedTextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = { Text("Product Image URL") },
            placeholder = { Text("https://picsum.photos/400/300") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = productName,
            onValueChange = { productName = it },
            label = { Text("Product Name *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                categories.forEach { cat ->
                    DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; expanded = false })
                }
            }
        }

        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Wholesale Price (₹) *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = capacity,
            onValueChange = { capacity = it },
            label = { Text("Weekly Capacity (units)") },
            placeholder = { Text("e.g. 500 baskets") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = sellerName,
            onValueChange = { sellerName = it },
            label = { Text("Seller / Business Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(4.dp))

        Button(
            onClick = {
                when {
                    productName.isBlank() ->
                        Toast.makeText(context, "Product Name required", Toast.LENGTH_SHORT).show()
                    category.isBlank() ->
                        Toast.makeText(context, "Select a Category", Toast.LENGTH_SHORT).show()
                    price.isBlank() ->
                        Toast.makeText(context, "Wholesale Price required", Toast.LENGTH_SHORT).show()
                    else -> {
                        isSaving = true
                        val product = Product(
                            name       = productName,
                            category   = category,
                            price      = price,
                            capacity   = capacity,
                            imageUrl   = imageUrl,
                            sellerName = sellerName,
                            phone      = phone,
                            sellerUid  = uid      // links to seller profile + capacity
                        )
                        FirebaseFirestore.getInstance()
                            .collection("products")
                            .add(product)
                            .addOnSuccessListener {
                                isSaving = false
                                Toast.makeText(context, "Product uploaded!", Toast.LENGTH_SHORT).show()
                                onBack()
                            }
                            .addOnFailureListener { e ->
                                isSaving = false
                                Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                }
            },
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.width(8.dp))
            }
            Text(if (isSaving) "Saving..." else "Upload Product")
        }

        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("← Back to Dashboard")
        }
    }
}
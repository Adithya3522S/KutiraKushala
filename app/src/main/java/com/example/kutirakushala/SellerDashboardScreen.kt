package com.example.kutirakushala

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SellerDashboardScreen(
    onNavigateToAddProduct: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val uid     = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var capacityText  by remember { mutableStateOf("") }
    var savedCapacity by remember { mutableStateOf("") }
    var isSaving      by remember { mutableStateOf(false) }

    // Load existing capacity on open
    LaunchedEffect(uid) {
        if (uid.isNotBlank()) {
            FirebaseFirestore.getInstance()
                .collection("sellers").document(uid).get()
                .addOnSuccessListener { doc ->
                    val v = doc.getString("weeklyCapacity") ?: ""
                    capacityText  = v
                    savedCapacity = v
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text("Seller Dashboard", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            "Manage your products, capacity and profile",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))

        // Capacity meter card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "📦 Capacity Meter",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (savedCapacity.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Currently visible to buyers: \"$savedCapacity units this week\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = capacityText,
                    onValueChange = { capacityText = it },
                    label = { Text("Units available this week") },
                    placeholder = { Text("e.g. 500 baskets") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (capacityText.isBlank()) {
                            Toast.makeText(context, "Enter a capacity value", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSaving = true
                        FirebaseFirestore.getInstance()
                            .collection("sellers").document(uid)
                            .set(mapOf("weeklyCapacity" to capacityText))
                            .addOnSuccessListener {
                                isSaving      = false
                                savedCapacity = capacityText
                                Toast.makeText(
                                    context,
                                    "Capacity updated: $capacityText units",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                isSaving = false
                                Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (isSaving) "Saving..." else "Update Capacity")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onNavigateToAddProduct,
            modifier = Modifier.fillMaxWidth()
        ) { Text("➕  Add New Product") }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = onNavigateToProfile,
            modifier = Modifier.fillMaxWidth()
        ) { Text("🏠  Edit Business Profile") }

        Spacer(Modifier.weight(1f))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) { Text("Logout") }
    }
}
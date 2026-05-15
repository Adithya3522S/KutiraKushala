package com.example.kutirakushala

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SaveBusinessScreen() {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var businessName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Save Business Details",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = businessName,
            onValueChange = { businessName = it },
            label = { Text("Business Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = capacity,
            onValueChange = { capacity = it },
            label = { Text("Production Capacity") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {

                val business = hashMapOf(
                    "businessName" to businessName,
                    "category" to category,
                    "capacity" to capacity
                )

                db.collection("businesses")
                    .add(business)
                    .addOnSuccessListener {

                        Toast.makeText(
                            context,
                            "Business Saved Successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        businessName = ""
                        category = ""
                        capacity = ""
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            context,
                            "Failed: ${it.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Business")
        }
    }
}
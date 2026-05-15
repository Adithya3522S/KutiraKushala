package com.example.kutirakushala

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val uid     = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var ownerName    by remember { mutableStateOf("") }
    var businessType by remember { mutableStateOf("") }
    var skillArea    by remember { mutableStateOf("") }
    var location     by remember { mutableStateOf("") }
    var phone        by remember { mutableStateOf("") }
    var teamPhotoUrl by remember { mutableStateOf("") }
    var isSaving     by remember { mutableStateOf(false) }
    var isLoaded     by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        if (uid.isNotBlank()) {
            FirebaseFirestore.getInstance()
                .collection("businesses").document(uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        ownerName    = doc.getString("ownerName")    ?: ""
                        businessType = doc.getString("businessType") ?: ""
                        skillArea    = doc.getString("skillArea")    ?: ""
                        location     = doc.getString("location")     ?: ""
                        phone        = doc.getString("phone")        ?: ""
                        teamPhotoUrl = doc.getString("teamPhotoUrl") ?: ""
                    }
                    isLoaded = true
                }
                .addOnFailureListener { isLoaded = true }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Business Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onLogout) { Text("Logout") }
                }
            )
        }
    ) { padding ->

        if (!isLoaded) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Team photo preview
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (teamPhotoUrl.isNotBlank()) {
                    SubcomposeAsyncImage(
                        model = teamPhotoUrl,
                        contentDescription = "Team photo",
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                Modifier.size(110.dp).clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                        },
                        error = {
                            Box(
                                Modifier.size(110.dp).clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) { Text("No photo", style = MaterialTheme.typography.bodySmall) }
                        }
                    )
                } else {
                    Box(
                        Modifier.size(110.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No photo", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            OutlinedTextField(
                value = teamPhotoUrl,
                onValueChange = { teamPhotoUrl = it },
                label = { Text("Team / Family Photo URL") },
                placeholder = { Text("Paste a public image link") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            HorizontalDivider()
            Text("Business details", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = ownerName,
                onValueChange = { ownerName = it },
                label = { Text("Owner / Family Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = businessType,
                onValueChange = { businessType = it },
                label = { Text("Business Name / Type *") },
                placeholder = { Text("e.g. Lakshmi Basket Weavers") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = skillArea,
                onValueChange = { skillArea = it },
                label = { Text("Skill Area *") },
                placeholder = { Text("e.g. Basket weaving, Agarbatti rolling") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Village / City, State *") },
                placeholder = { Text("e.g. Hubli, Karnataka") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Contact Phone Number *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = {
                    when {
                        ownerName.isBlank()    -> Toast.makeText(context, "Owner name required", Toast.LENGTH_SHORT).show()
                        businessType.isBlank() -> Toast.makeText(context, "Business type required", Toast.LENGTH_SHORT).show()
                        skillArea.isBlank()    -> Toast.makeText(context, "Skill area required", Toast.LENGTH_SHORT).show()
                        location.isBlank()     -> Toast.makeText(context, "Location required", Toast.LENGTH_SHORT).show()
                        phone.isBlank()        -> Toast.makeText(context, "Phone required", Toast.LENGTH_SHORT).show()
                        else -> {
                            isSaving = true
                            FirebaseFirestore.getInstance()
                                .collection("businesses").document(uid)
                                .set(
                                    mapOf(
                                        "ownerName"    to ownerName,
                                        "businessType" to businessType,
                                        "skillArea"    to skillArea,
                                        "location"     to location,
                                        "phone"        to phone,
                                        "teamPhotoUrl" to teamPhotoUrl
                                    )
                                )
                                .addOnSuccessListener {
                                    isSaving = false
                                    Toast.makeText(context, "Profile saved!", Toast.LENGTH_SHORT).show()
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
                Text(if (isSaving) "Saving..." else "Save Profile")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
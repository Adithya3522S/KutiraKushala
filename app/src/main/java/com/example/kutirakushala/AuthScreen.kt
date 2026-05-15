package com.example.kutirakushala

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AuthScreen(
    onLoginSuccess: (String) -> Unit,
    onRegisterClick: () -> Unit
) {
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading  by remember { mutableStateOf(false) }
    val context  = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Kutira Kushala", fontSize = 28.sp, style = MaterialTheme.typography.headlineLarge)
        Text(
            "Micro-Factory Showcase",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Enter email and password", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                loading = true
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { result ->
                        val uid = result.user?.uid ?: ""
                        FirebaseFirestore.getInstance()
                            .collection("users").document(uid).get()
                            .addOnSuccessListener { doc ->
                                loading = false
                                val role = doc.getString("role") ?: "buyer"
                                Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                                onLoginSuccess(role)
                            }
                    }
                    .addOnFailureListener {
                        loading = false
                        Toast.makeText(context, "Login failed: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (loading) "Logging in..." else "Login")
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onRegisterClick) {
            Text("Don't have an account? Register")
        }
    }
}
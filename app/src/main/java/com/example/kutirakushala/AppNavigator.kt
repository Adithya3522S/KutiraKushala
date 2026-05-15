package com.example.kutirakushala

import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigator() {

    var currentScreen    by remember { mutableStateOf("login") }
    var userRole         by remember { mutableStateOf("buyer") }
    var viewingSellerUid   by remember { mutableStateOf("") }
    var viewingSellerName  by remember { mutableStateOf("") }
    var viewingSellerPhone by remember { mutableStateOf("") }

    when (currentScreen) {

        "login" -> AuthScreen(
            onLoginSuccess = { role ->
                userRole = role
                currentScreen = if (role == "seller") "seller_dashboard" else "marketplace"
            },
            onRegisterClick = { currentScreen = "register" }
        )

        "register" -> RegisterScreen(
            onRegisterSuccess = { role ->
                userRole = role
                currentScreen = if (role == "seller") "seller_dashboard" else "marketplace"
            },
            onLoginClick = { currentScreen = "login" }
        )

        "marketplace" -> MarketplaceScreen(
            onLogout = {
                FirebaseAuth.getInstance().signOut()
                currentScreen = "login"
            },
            onViewSellerProfile = { uid, name, phone ->
                viewingSellerUid   = uid
                viewingSellerName  = name
                viewingSellerPhone = phone
                currentScreen = "seller_profile_view"
            }
        )

        "seller_profile_view" -> SellerProfileViewScreen(
            sellerUid   = viewingSellerUid,
            sellerName  = viewingSellerName,
            sellerPhone = viewingSellerPhone,
            onBack      = { currentScreen = "marketplace" }
        )

        "seller_dashboard" -> SellerDashboardScreen(
            onNavigateToAddProduct = { currentScreen = "add_product" },
            onNavigateToProfile    = { currentScreen = "business_profile" },
            onLogout = {
                FirebaseAuth.getInstance().signOut()
                currentScreen = "login"
            }
        )

        "add_product" -> AddProductScreen(
            onBack = { currentScreen = "seller_dashboard" }
        )

        "business_profile" -> BusinessProfileScreen(
            onBack   = { currentScreen = "seller_dashboard" },
            onLogout = {
                FirebaseAuth.getInstance().signOut()
                currentScreen = "login"
            }
        )
    }
}
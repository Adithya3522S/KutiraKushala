# Kutira-Kushala - Micro Factory Showcase

## Problem Statement
Cottage industries such as basket weaving, agarbatti rolling, papad making, and other home-based productions remain "invisible" to bulk buyers. They lack a professional platform to showcase their products, production capacity, and reliability. This keeps them trapped in low-income cycles.

**Kutira-Kushala** solves this by turning every home-based unit into a visible "Micro-Factory" that bulk buyers and vendors can easily discover and trust.

## Vision
A simple yet powerful Android app that helps rural and home-based entrepreneurs create professional profiles, list their products with wholesale prices, show real-time production capacity, and directly connect with buyers.

## Key Features
- Business Profile creation (team photo, skill area, location)
- Product Catalog with images, descriptions, and wholesale pricing
- Live Capacity Meter – easily updatable by the user (e.g., "Ready for 500 units this week")
- Direct Connect (Call / WhatsApp button) for bulk buyers
- Search and Filter by Product Category (Craft, Food, Handicrafts, etc.)
- Firebase Firestore backend for real-time data
- Clean, user-friendly Material Design UI using CardViews

## Tech Stack
- **Language**: Kotlin
- **Architecture**: MVVM
- **UI**: XML with Material Components (RecyclerView, CardView)
- **Backend**: Firebase Firestore
- **Image Storage**: Firebase Storage
- **Build System**: Gradle

## Setup & Installation

### Prerequisites
- Android Studio (Latest version)
- Firebase Project configured
- `google-services.json` file placed in `app/` folder

### Steps to Run
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/KutiraKushala.git

## Firebase Setup (For Developers)

1. Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
2. Add your Android app to the Firebase project
3. Download `google-services.json` and place it in the `app/` folder
4. Enable Firestore Database and Storage

package com.example.kutirakushala

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Coil with OkHttp — required for reliable image loading from any public URL
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .memoryCache {
                    MemoryCache.Builder(this).maxSizePercent(0.25).build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(cacheDir.resolve("image_cache"))
                        .maxSizeBytes(50L * 1024 * 1024)
                        .build()
                }
                .okHttpClient {
                    OkHttpClient.Builder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .addInterceptor { chain ->
                            val req = chain.request().newBuilder()
                                .addHeader(
                                    "User-Agent",
                                    "Mozilla/5.0 (Android; Mobile) AppleWebKit/537.36"
                                )
                                .addHeader("Accept", "image/webp,image/png,image/jpeg,*/*")
                                .build()
                            chain.proceed(req)
                        }
                        .build()
                }
                .crossfade(true)
                .build()
        )

        setContent {
            MaterialTheme {
                AppNavigator()
            }
        }
    }
}
package com.ikgallery

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

class MainActivity : ComponentActivity() {

    private val photosState = mutableStateListOf<Uri>()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) loadImages()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        permissionLauncher.launch(permission)

        setContent {
            // Кастомная розовая тема (Pink Palette)
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFFE91E63),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFF8BBD0),
                    secondary = Color(0xFFAD1457),
                    background = Color(0xFFFFF0F5),
                    surface = Color.White,
                    onSurface = Color(0xFF880E4F)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        topBar = {
                            @OptIn(ExperimentalMaterial3Api::class)
                            TopAppBar(
                                title = { Text("💖 IK-Gallery") },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    titleContentColor = MaterialTheme.colorScheme.secondary
                                )
                            )
                        }
                    ) { padding ->
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(110.dp),
                            modifier = Modifier
                                .padding(padding)
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                            contentPadding = PaddingIdentifiers.gridPadding()
                        ) {
                            items(photosState) { uri ->
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            val intent = Intent(this@MainActivity, EditorActivity::class.java).apply {
                                                putExtra("imageUri", uri.toString())
                                            }
                                            startActivity(intent)
                                        }
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private object PaddingIdentifiers {
        fun gridPadding() = PaddingValues(8.dp)
    }

    private fun loadImages() {
        val list = mutableListOf<Uri>()
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                list.add(contentUri)
            }
        }
        photosState.clear()
        photosState.addAll(list)
    }
}

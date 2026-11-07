package com.ikgallery

import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions

class EditorActivity : ComponentActivity() {

    private var currentBitmap by mutableStateOf<Bitmap?>(null)
    private var imageUri: Uri? = null

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                try {
                    imageUri = uri
                    contentResolver.openInputStream(uri)?.use {
                        currentBitmap = BitmapFactory.decodeStream(it)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Ошибка загрузки фото", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uriString = intent.getStringExtra("imageUri")
        imageUri = uriString?.let { Uri.parse(it) }

        imageUri?.let { uri ->
            try {
                contentResolver.openInputStream(uri)?.use {
                    currentBitmap = BitmapFactory.decodeStream(it)
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Не удалось открыть изображение", Toast.LENGTH_SHORT).show()
            }
        }

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFFE91E63),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFF8BBD0),
                    secondary = Color(0xFFAD1457),
                    background = Color(0xFFFFE4E1), // Нежно-розовый
                    surface = Color.White,
                    onSurface = Color(0xFF880E4F),
                    error = Color(0xFFD32F2F)
                )
            ) {
                var textToApply by remember { mutableStateOf("") }
                var showTextDialog by remember { mutableStateOf(false) }
                var showDeleteDialog by remember { mutableStateOf(false) }

                // Состояния для зума и перемещения фото пальцами
                var scale by remember { mutableStateOf(1f) }
                var offsetX by remember { mutableStateOf(0f) }
                var offsetY by remember { mutableStateOf(0f) }

                val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
                    scale = (scale * zoomChange).coerceIn(1f, 5f)
                    offsetX += offsetChange.x
                    offsetY += offsetChange.y
                }

                // Диалог ввода текста
                if (showTextDialog) {
                    AlertDialog(
                        onDismissRequest = { showTextDialog = false },
                        title = { Text("Добавить текст") },
                        text = {
                            OutlinedTextField(
                                value = textToApply,
                                onValueChange = { textToApply = it },
                                label = { Text("Ваш текст") }
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                showTextDialog = false
                                if (textToApply.isNotEmpty() && currentBitmap != null) {
                                    val mutableBitmap = currentBitmap!!.copy(Bitmap.Config.ARGB_8888, true)
                                    val canvas = Canvas(mutableBitmap)
                                    val paint = Paint().apply {
                                        color = android.graphics.Color.WHITE
                                        textSize = 80f
                                        isAntiAlias = true
                                        setShadowLayer(8f, 4f, 4f, android.graphics.Color.BLACK)
                                    }
                                    canvas.drawText(textToApply, 100f, 200f, paint)
                                    currentBitmap = mutableBitmap
                                    textToApply = ""
                                }
                            }) {
                                Text("Готово")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showTextDialog = false }) {
                                Text("Отмена")
                            }
                        }
                    )
                }

                // Диалог удаления
                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Удалить фото?") },
                        text = { Text("Фото будет навсегда удалено с устройства.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDeleteDialog = false
                                    imageUri?.let { uri ->
                                        try {
                                            contentResolver.delete(uri, null, null)
                                            Toast.makeText(this@EditorActivity, "Успешно удалено", Toast.LENGTH_SHORT).show()
                                            finish()
                                        } catch (e: Exception) {
                                            Toast.makeText(this@EditorActivity, "Ошибка удаления", Toast.JSON_ERROR_FALLBACK)
                                        }
                                    }
                                }
                            ) {
                                Text("Удалить", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text("Отмена")
                            }
                        }
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Кастомный фон с пончиками и кремушками
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            
                            // Рисуем узор из пончиков и капель крем-глазури на розовом фоне
                            for (i in 0..12) {
                                val cx = (i * 190f + 50f) % canvasWidth
                                val cy = ((i * 130f + 70f) % canvasHeight)

                                // Пончик (основа)
                                drawCircle(
                                    color = Color(0xFFD2B48C).copy(alpha = 0.5f),
                                    radius = 45f,
                                    center = Offset(cx, cy)
                                )
                                // Дырка пончика
                                drawCircle(
                                    color = Color(0xFFFFE4E1),
                                    radius = 18f,
                                    center = Offset(cx, cy)
                                )
                                // Глазурь
                                drawCircle(
                                    color = Color(0xFFFFB6C1).copy(alpha = 0.7f),
                                    radius = 40f,
                                    center = Offset(cx, cy)
                                )
                                // Капля крема рядом
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.8f),
                                    radius = 18f,
                                    center = Offset(cx + 55f, cy + 45f)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Верхняя панель с кнопкой назад
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = MaterialTheme.colorScheme.secondary)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Область просмотра фото с поддержкой Pinch-to-Zoom (щипок для зума и панорамирование)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onDoubleTap = {
                                                scale = 1f
                                                offsetX = 0f
                                                offsetY = 0f
                                            }
                                        )
                                    }
                                    .transformable(transformState),
                                contentAlignment = Alignment.Center
                            ) {
                                currentBitmap?.let { bitmap ->
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Editing Image",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer(
                                                scaleX = scale,
                                                scaleY = scale,
                                                translationX = offsetX,
                                                translationY = offsetY
                                            )
                                    )
                                } ?: Text("Нет изображения", color = Color.Gray)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Аккуратная нижняя панель с иконками вместо огромных кнопок
                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Кнопка обрезки
                                    IconButton(onClick = {
                                        imageUri?.let { uri ->
                                            try {
                                                cropImage.launch(CropImageContractOptions(uri, CropImageOptions()))
                                            } catch (e: Exception) {
                                                Toast.makeText(this@EditorActivity, "Ошибка обрезки", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }) {
                                        Icon(Icons.Default.Crop, contentDescription = "Обрезать", tint = MaterialTheme.colorScheme.primary)
                                    }

                                    // Кнопка текста
                                    IconButton(onClick = { showTextDialog = true }) {
                                        Icon(Icons.Default.TextFormat, contentDescription = "Текст", tint = MaterialTheme.colorScheme.primary)
                                    }

                                    // Кнопка Ч/Б фильтра
                                    IconButton(onClick = {
                                        currentBitmap?.let { bitmap ->
                                            val width = bitmap.width
                                            val height = bitmap.height
                                            val bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                            val canvas = Canvas(bmpGray)
                                            val paint = Paint()
                                            val cm = ColorMatrix().apply { setSaturation(0f) }
                                            paint.colorFilter = ColorMatrixColorFilter(cm)
                                            canvas.drawBitmap(bitmap, 0f, 0f, paint)
                                            currentBitmap = bmpGray
                                        }
                                    }) {
                                        Icon(Icons.Default.AutoFixHigh, contentDescription = "Фильтр", tint = MaterialTheme.colorScheme.primary)
                                    }

                                    // Кнопка удаления
                                    IconButton(onClick = { showDeleteDialog = true }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

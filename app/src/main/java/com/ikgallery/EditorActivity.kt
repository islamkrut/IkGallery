package com.ikgallery

import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions

class EditorActivity : ComponentActivity() {

    private var currentBitmap by mutableStateOf<Bitmap?>(null)
    private var imageUri: Uri? = null

    // Безопасный контракт обрезки с обработкой ошибок во избежание вылетов
    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                try {
                    imageUri = uri
                    val inputStream = contentResolver.openInputStream(uri)
                    inputStream?.let {
                        currentBitmap = BitmapFactory.decodeStream(it)
                        it.close()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Ошибка загрузки обрезанного фото", Toast.LENGTH_SHORT).show()
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
                val inputStream = contentResolver.openInputStream(uri)
                inputStream?.let {
                    currentBitmap = BitmapFactory.decodeStream(it)
                    it.close()
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
                    background = Color(0xFFFFF0F5),
                    surface = Color.White,
                    onSurface = Color(0xFF880E4F),
                    error = Color(0xFFD32F2F)
                )
            ) {
                var textToApply by remember { mutableStateOf("") }
                var showDeleteDialog by remember { mutableStateOf(false) }

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
                                            Toast.makeText(this@EditorActivity, "Ошибка удаления", Toast.LENGTH_SHORT).show()
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Область просмотра фото
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            currentBitmap?.let { bitmap ->
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Editing",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } ?: Text("Нет изображения", color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = textToApply,
                            onValueChange = { textToApply = it },
                            label = { Text("Текст на фото") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Панель инструментов (Кнопки)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    imageUri?.let { uri ->
                                        try {
                                            cropImage.launch(CropImageContractOptions(uri, CropImageOptions()))
                                        } catch (e: Exception) {
                                            Toast.makeText(this@EditorActivity, "Ошибка запуска обрезки", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Обрезать")
                            }

                            Button(
                                onClick = {
                                    if (textToApply.isNotEmpty() && currentBitmap != null) {
                                        val mutableBitmap = currentBitmap!!.copy(Bitmap.Config.ARGB_8888, true)
                                        val canvas = Canvas(mutableBitmap)
                                        val paint = Paint().apply {
                                            color = android.graphics.Color.WHITE
                                            textSize = 80f
                                            isAntiAlias = true
                                            setShadowLayer(5f, 2f, 2f, android.graphics.Color.BLACK)
                                        }
                                        canvas.drawText(textToApply, 50f, 150f, paint)
                                        currentBitmap = mutableBitmap
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+ Текст")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Фильтр Черно-белый
                            Button(
                                onClick = {
                                    currentBitmap?.let { bitmap ->
                                        val width = bitmap.width
                                        val height = bitmap.height
                                        val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                        val canvas = Canvas(bmpGrayscale)
                                        val paint = Paint()
                                        val cm = ColorMatrix().apply { setSaturation(0f) }
                                        paint.colorFilter = ColorMatrixColorFilter(cm)
                                        canvas.drawBitmap(bitmap, 0f, 0f, paint)
                                        currentBitmap = bmpGrayscale
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Ч/Б Фильтр")
                            }

                            // Кнопка удаления
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Удалить")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { finish() }
                        ) {
                            Text("Назад в галерею")
                        }
                    }
                }
            }
        }
    }
}

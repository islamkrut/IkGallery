package com.ikgallery

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions

class EditorActivity : ComponentActivity() {

    private var currentBitmap by mutableStateOf<Bitmap?>(null)

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                val inputStream = contentResolver.openInputStream(uri)
                currentBitmap = BitmapFactory.decodeStream(inputStream)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uriString = intent.getStringExtra("imageUri")
        val uri = Uri.parse(uriString)

        val inputStream = contentResolver.openInputStream(uri)
        currentBitmap = BitmapFactory.decodeStream(inputStream)

        setContent {
            var textToApply by remember { mutableStateOf("") }

            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                currentBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Edit Image",
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = textToApply,
                    onValueChange = { textToApply = it },
                    label = { Text("Текст на фото") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = {
                        cropImage.launch(
                            CropImageContractOptions(uri, CropImageOptions())
                        )
                    }) {
                        Text("Обрезать")
                    }

                    Button(onClick = {
                        if (textToApply.isNotEmpty() && currentBitmap != null) {
                            val mutableBitmap = currentBitmap!!.copy(Bitmap.Config.ARGB_8888, true)
                            val canvas = Canvas(mutableBitmap)
                            val paint = Paint().apply {
                                color = Color.WHITE
                                textSize = 80f
                                isAntiAlias = true
                            }
                            canvas.drawText(textToApply, 50f, 100f, paint)
                            currentBitmap = mutableBitmap
                        }
                    }) {
                        Text("+ Текст")
                    }

                    Button(onClick = {
                        Toast.makeText(this@EditorActivity, "Сохранено!", Toast.LENGTH_SHORT).show()
                        finish()
                    }) {
                        Text("Сохранить")
                    }
                }
            }
        }
    }
}

package com.dicoding.asclepius.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import org.tensorflow.lite.task.vision.classifier.Classifications

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var currentImageUri: Uri? = null

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                showToast("Permission request granted")
            } else {
                showToast("Permission request denied")
            }
        }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }
        binding.apply {
            analyzeButton.setOnClickListener{
                currentImageUri?.let {
                    analyzeImage(it)
                }?: showToast("No image selected")
            }
            galleryButton.setOnClickListener{
                startGallery()
            }
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            currentImageUri= uri
            showImage()
        }
    // TODO: Mendapatkan gambar dari Gallery.
    }
    private fun showImage() {
        currentImageUri.let {
            binding.previewImageView.setImageURI(it)
            binding.analyzeButton.visibility = android.view.View.VISIBLE
            binding.galleryButton.apply {
            }
        }

    // TODO: Menampilkan gambar sesuai Gallery yang dipilih.
    }

    private fun analyzeImage(image: Uri) {
        val imageHelper = ImageClassifierHelper(
            context = this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener{
                override fun onError(error: String) {
                    showToast(error)
                }
                override fun onResults(results: List<Classifications>?) {
                    val resultString = results?.joinToString("\n") {
                        val threshold = (it.categories[0].score * 100).toInt()
                        "${it.categories[0].label} : ${threshold}%"
                    }
                    if (resultString != null){
                        moveToResult(image,resultString)
                    }
                }
            }
        )
        imageHelper.classifyStaticImage(image)
        // TODO: Menganalisa gambar yang berhasil ditampilkan.
    }

    private fun moveToResult(image:Uri, result:String) {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra(ResultActivity.EXTRA_IMAGE_URI,image.toString())
        intent.putExtra(ResultActivity.EXTRA_RESULT,result)
        startActivity(intent)

    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
    }
}
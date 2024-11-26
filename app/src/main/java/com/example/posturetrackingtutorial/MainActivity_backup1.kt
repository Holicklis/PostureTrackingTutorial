package com.example.posturetrackingtutorial

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.common.InputImage
import com.example.posturetrackingtutorial.ui.theme.PostureTrackingTutorialTheme
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import android.Manifest


class MainActivity_backup1 : ComponentActivity() {
    private lateinit var previewView: PreviewView

    private val cameraPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
            startCamera()
        } else {
            // Request permission
            cameraPermissionRequest.launch(Manifest.permission.CAMERA)
        }
        setContent {
            PostureTrackingTutorialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // This will show the camera feed
                    CameraPreview(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    // This is where we display the camera preview
    @Composable
    fun CameraPreview(modifier: Modifier = Modifier) {
        AndroidView(
            factory = { context ->
                // Initialize PreviewView
                previewView = PreviewView(context)
                previewView
            },
            modifier = modifier
        )

        startCamera()
    }

    // Initialize CameraX
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Setup CameraX Preview
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // Camera Selector (using back camera)
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            // Bind to lifecycle
            cameraProvider.bindToLifecycle(this, cameraSelector, preview) // This is fixed
        }, ContextCompat.getMainExecutor(this))
    }
}

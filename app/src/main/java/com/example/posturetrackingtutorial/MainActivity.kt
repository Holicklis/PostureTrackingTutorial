package com.example.posturetrackingtutorial

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions

import android.util.DisplayMetrics



class MainActivity : ComponentActivity() {
    fun getScreenResolution(): Pair<Int, Int> {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }


    private lateinit var previewView: PreviewView
    private lateinit var graphicOverlay: GraphicOverlay  // Keep graphicOverlay as a global property

    // Request camera permission
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

        // Check and request camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
            startCamera()
        } else {
            // Request permission
            cameraPermissionRequest.launch(Manifest.permission.CAMERA)
        }

        // Initialize graphicOverlay with the current context
        graphicOverlay = GraphicOverlay(this)

        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                CameraPreviewWithOverlay(modifier = Modifier.fillMaxSize(), graphicOverlay = graphicOverlay)
            }
        }
    }

    @Composable
    fun CameraPreviewWithOverlay(modifier: Modifier = Modifier, graphicOverlay: GraphicOverlay) {
        Box(modifier = modifier) {
            // Camera preview
            AndroidView(
                factory = { ctx ->
                    previewView = PreviewView(ctx).apply {
                        layoutParams = android.widget.RelativeLayout.LayoutParams(
                            android.widget.RelativeLayout.LayoutParams.MATCH_PARENT,
                            android.widget.RelativeLayout.LayoutParams.MATCH_PARENT
                        )
                    }
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Add the GraphicOverlay on top
            AndroidView(
                factory = { graphicOverlay },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(android.util.Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            // Set camera info on the overlay
            graphicOverlay.setCameraInfo(640, 480, CameraSelector.LENS_FACING_BACK)

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                processImage(imageProxy)
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
                preview.setSurfaceProvider(previewView.surfaceProvider)
            } catch (e: Exception) {
                Toast.makeText(this, "Binding failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            val options = PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build()
            val poseDetector = PoseDetection.getClient(options)

            poseDetector.process(inputImage)
                .addOnSuccessListener { pose ->
                    // Use the global graphicOverlay to add the pose graphic
                    val poseGraphic = PoseGraphic(graphicOverlay, pose)
                    graphicOverlay.clear() // Clear previous graphics
                    graphicOverlay.add(poseGraphic) // Add the new pose graphic
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Pose detection failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener {
                    imageProxy.close() // Always close the imageProxy
                }
        }
    }
}

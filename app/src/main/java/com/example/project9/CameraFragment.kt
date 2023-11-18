package com.example.project9

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraX
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraFragment : Fragment(), SensorEventListener {

    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference.child("images")

    // Variables for CameraX
    private val REQUEST_CODE_PERMISSIONS = 10
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    private lateinit var imageCapture: ImageCapture

    // Variables for shake detection
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lastTime: Long = 0
    private var lastX = 0.0f
    private var lastY = 0.0f
    private var lastZ = 0.0f
    private val SHAKE_THRESHOLD = 800

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initializing sensor manager and accelerometer
        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        // Setting up capture button click listener
        captureButton.setOnClickListener {
            takePhoto()
        }
    }

    override fun onResume() {
        super.onResume()
        // Registering the sensor listener
        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onPause() {
        super.onPause()
        // Unregistering the sensor listener
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    override fun onSensorChanged(event: SensorEvent) {
        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - lastTime

        if (timeDifference > 100) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val speed = Math.abs(x + y + z - lastX - lastY - lastZ) / timeDifference * 10000

            if (speed > SHAKE_THRESHOLD) {
                // Shake detected, implementing your logic here (e.g., navigate to the desired fragment)
                Log.d("CameraFragment", "Shake detected")
                navigateToImagesFragment() // Example: Navigating to ImagesFragment after a shake
            }

            lastX = x
            lastY = y
            lastZ = z
            lastTime = currentTime
        }
    }

    private fun navigateToImagesFragment() {
        // Example: Navigating back to the ImagesFragment
        findNavController().navigateUp()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        // Creating an instance of the CameraSelector to specify the camera to be used
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        // Setting up the preview use case
        val preview = Preview.Builder()
            .build()

        // Setting up the image capture use case
        imageCapture = ImageCapture.Builder()
            .build()

        // Binding the use cases to the camera preview
        CameraX.bindToLifecycle(this, cameraSelector, preview, imageCapture)

        // Attaching the preview to the PreviewView
        preview.setSurfaceProvider(viewFinder.createSurfaceProvider())
    }

    private fun takePhoto() {
        // Create a file to save the captured image
        val file =
            File(requireContext().externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")

        // Set up the image capture options
        val metadata = ImageCapture.Metadata().apply {
            // Add any metadata if needed
        }
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file)
            .setMetadata(metadata)
            .build()

        // Capture the image
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    // Handle error during image capture
                    Log.e("CameraFragment", "Error capturing image: ${exc.message}")
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // Image saved successfully
                    Log.d("CameraFragment", "Image saved successfully")

                    // Upload the image to Firebase Storage
                    uploadImageToFirebase(file)
                }
            }
        )
    }

    private fun uploadImageToFirebase(file: File) {
        // Use storageRef to upload the file
        val uploadTask = storageRef.child(file.name).putFile(file.toUri())

        uploadTask.addOnSuccessListener {
            // Image uploaded successfully
            Log.d("CameraFragment", "Image uploaded successfully")
            // TODO: Navigate back to the previous fragment or perform any other necessary actions
            findNavController().navigateUp() // Example: Navigating back to the previous fragment after image capture
        }.addOnFailureListener { exception ->
            // Handle failed upload
            Log.e("CameraFragment", "Image upload failed: ${exception.message}")
        }
    }
}

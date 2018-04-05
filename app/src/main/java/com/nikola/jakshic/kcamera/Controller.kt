@file:Suppress("DEPRECATION")

package com.nikola.jakshic.kcamera

import android.annotation.SuppressLint
import android.app.Activity
import android.content.SharedPreferences
import android.graphics.ImageFormat
import android.hardware.Camera
import android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK
import android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT
import android.hardware.Camera.Parameters.*
import android.preference.PreferenceManager
import android.view.OrientationEventListener
import android.view.Surface
import android.view.ViewGroup

class Controller(
        private val callback: Camera.PictureCallback,
        private val context: Activity?,
        private val container: ViewGroup) : OrientationChangeListener.OrientationListener {

    // Keys for shared prefs
    private val CAMERA_KEY = "cam-id"
    private val FLASH_KEY = "flash-id"

    private var camera: Camera? = null
    private var params: Camera.Parameters? = null
    private var preview: Preview? = null
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    private val orientationListener = OrientationChangeListener(context, this)

    /** Sets necessary parameters and starts the camera */
    fun startCamera() {
        camera = getCameraInstance()
        params = camera?.parameters
        setCameraDisplayOrientation()
        setPreviewSize()
        setPictureSize()
        setPictureFormat()
        setFocusMode()
        setFlashMode()
        setJpegQuality()
        camera?.parameters = params // apply previously configured parameters
        preview = Preview(context, camera)
        container.addView(preview)
        orientationListener.enable()
    }

    /** Release all the resources */
    fun stopCamera() {
        orientationListener.disable()
        camera?.release()
        camera = null
        container.removeView(preview)
        preview = null
    }

    /** Switching between back and front camera */
    fun toggleCamera() {
        var id = prefs.getInt(CAMERA_KEY, CAMERA_FACING_BACK)

        id = if (id == CAMERA_FACING_BACK) CAMERA_FACING_FRONT else CAMERA_FACING_BACK
        prefs.edit { putInt(CAMERA_KEY, id) }

        // reset camera to apply new parameters
        stopCamera()
        startCamera()
    }

    /** Take picture and supply image data to the owner of [callback] */
    fun takePicture() {
        camera?.parameters = params // applying params with latest rotation data from sensor
        camera?.takePicture(null, null, callback)
    }

    /** Returns true if flash mode is supported */
    fun hasFlashMode(): Boolean {
        val modes = params?.supportedFlashModes
        return modes?.containsAll(listOf(FLASH_MODE_AUTO, FLASH_MODE_ON, FLASH_MODE_OFF)) == true
    }

    /** Switching flash modes between AUTO, ON and OFF */
    fun toggleFlashMode() {
        if (hasFlashMode()) {
            var mode = prefs.getString(FLASH_KEY, FLASH_MODE_AUTO)
            mode = when (mode) {
                FLASH_MODE_AUTO -> FLASH_MODE_OFF
                FLASH_MODE_OFF -> FLASH_MODE_ON
                else -> FLASH_MODE_AUTO
            }
            prefs.edit { putString(FLASH_KEY, mode) }
            params?.flashMode = mode
            camera?.parameters = params // apply new mode
        }
    }

    /** Returns current flash mode */
    fun getFlashMode(): String = prefs.getString(FLASH_KEY, FLASH_MODE_AUTO)

    /**
     * Failing to check for exceptions if the camera is in use
     * or does not exist will cause application to be shut down by the system.
     */
    private fun getCameraInstance(): Camera? {
        var camera: Camera? = null
        val id = prefs.getInt(CAMERA_KEY, CAMERA_FACING_BACK)
        try {
            camera = Camera.open(id)
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
            // or permissions are not granted
        }
        return camera
    }

    /**
     * Most camera applications lock the display into landscape mode
     * because that is the natural orientation of the camera sensor.
     * This method lets you change how the preview is displayed
     * without affecting how the image is recorded.
     */
    private fun setCameraDisplayOrientation() {
        val id = prefs.getInt(CAMERA_KEY, CAMERA_FACING_BACK)
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(id, info)
        val rotation = context?.windowManager?.defaultDisplay?.rotation

        val degrees = when (rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> -1
        }

        val result = when (info.facing) {
            CAMERA_FACING_FRONT -> {
                val result = (info.orientation + degrees) % 360
                (360 - result) % 360    // compensate the mirror
            }
            else -> (info.orientation - degrees + 360) % 360    // back-facing
        }
        camera?.setDisplayOrientation(result)
    }

    /** Sets flash mode (if supported), default mode is AUTO */
    private fun setFlashMode() {
        if (hasFlashMode()) {
            val mode = prefs.getString(FLASH_KEY, FLASH_MODE_AUTO)
            params?.flashMode = mode
        }
    }

    /** Sets focus mode that is intended for taking pictures */
    private fun setFocusMode() {
        val modes = params?.supportedFocusModes
        if (modes?.contains(FOCUS_MODE_CONTINUOUS_PICTURE) == true)
            params?.focusMode = FOCUS_MODE_CONTINUOUS_PICTURE
    }

    /** Sets JPEG as a picture format, this format is always supported */
    private fun setPictureFormat() {
        params?.pictureFormat = ImageFormat.JPEG
    }

    /** Sets the highest 4:3 preview resolution */
    private fun setPreviewSize() {
        val sizes = params?.supportedPreviewSizes
        val best = sizes?.filter { it.aspectRatio == 4f / 3 }?.maxBy { it.width }
        params?.setPreviewSize(best?.width ?: 0, best?.height ?: 0)
    }

    /** Sets the highest 4:3 picture resolution */
    private fun setPictureSize() {
        val sizes = params?.supportedPictureSizes
        val best = sizes?.filter { it.aspectRatio == 4f / 3 }?.maxBy { it.width }
        params?.setPictureSize(best?.width ?: 0, best?.height ?: 0)
    }

    /** Sets max JPEG quality of captured picture */
    private fun setJpegQuality() {
        params?.jpegQuality = 100
    }

    /** Rotate the picture to match the orientation of what users see */
    override fun onOrientationChanged(orientation: Int) {
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) return

        val id = prefs.getInt(CAMERA_KEY, CAMERA_FACING_BACK)
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(id, info)

        val fixedOrientation = (orientation + 45) / 90 * 90

        val rotation = if (info.facing == CAMERA_FACING_FRONT) {
            (info.orientation - fixedOrientation + 360) % 360
        } else {
            (info.orientation + fixedOrientation) % 360
        }
        params?.setRotation(rotation)
    }

    /** Returns the aspect ratio of this camera size */
    private inline val Camera.Size.aspectRatio get() = width.toFloat() / height

    /** Allows editing of this preference instance with a call to [SharedPreferences.Editor.commit] */
    @SuppressLint("ApplySharedPref")
    private inline fun SharedPreferences.edit(action: SharedPreferences.Editor.() -> Unit) {
        val editor = edit()
        action(editor)
        editor.commit()
    }
}
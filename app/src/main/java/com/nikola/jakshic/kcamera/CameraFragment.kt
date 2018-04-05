@file:Suppress("DEPRECATION")

package com.nikola.jakshic.kcamera

import android.content.pm.ActivityInfo
import android.hardware.Camera
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_camera.*

class CameraFragment : Fragment(), Camera.PictureCallback {

    private lateinit var controller: Controller

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Lock orientation to portrait, because we are using OrientationEventListener
        // to set the proper orientation to picture
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        controller = Controller(this, activity, container)

        // Prevent changing camera parameters while taking picture
        btnTakePicture.setOnClickListener {
            btnTakePicture.isEnabled = false
            btnCamToggle.isEnabled = false
            btnFlashToggle.isEnabled = false
            controller.takePicture() }

        btnCamToggle.setOnClickListener {
            controller.toggleCamera()
            btnFlashToggle.visibility = if (controller.hasFlashMode()) View.VISIBLE else View.GONE
        }

        /** Sets drawable based on flash mode */
        fun setFlashDrawable() =
                when (controller.getFlashMode()) {
                    Camera.Parameters.FLASH_MODE_AUTO -> ContextCompat.getDrawable(context!!, R.drawable.ic_flash_auto)
                    Camera.Parameters.FLASH_MODE_OFF -> ContextCompat.getDrawable(context!!, R.drawable.ic_flash_off)
                    else -> ContextCompat.getDrawable(context!!, R.drawable.ic_flash_on)
                }

        btnFlashToggle.setImageDrawable(setFlashDrawable())
        btnFlashToggle.setOnClickListener {
            controller.toggleFlashMode()
            btnFlashToggle.setImageDrawable(setFlashDrawable())
        }
    }

    override fun onResume() {
        super.onResume()
        controller.startCamera()

        // Has to be in onResume() because Camera.Parameters are available after we get camera instance
        // which is happening in Controller.startCamera
        btnFlashToggle.visibility = if (controller.hasFlashMode()) View.VISIBLE else View.GONE
    }

    override fun onPause() {
        super.onPause()
        controller.stopCamera()
    }

    override fun onPictureTaken(data: ByteArray?, camera: Camera?) {
        Singletons.data = data
        // Show taken picture in ImageFragment
        activity?.supportFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.fragmentContainer, ImageFragment())
                ?.addToBackStack(null)
                ?.commit()
        // Picture has been taken, allow changing camera parameters
        btnTakePicture.isEnabled = true
        btnCamToggle.isEnabled = true
        btnFlashToggle.isEnabled = true
    }
}
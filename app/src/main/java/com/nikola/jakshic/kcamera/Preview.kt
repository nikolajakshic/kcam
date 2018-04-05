@file:Suppress("DEPRECATION")

package com.nikola.jakshic.kcamera

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.IOException

@SuppressLint("ViewConstructor")
class Preview(context: Context?, private val camera: Camera?) : SurfaceView(context), SurfaceHolder.Callback {

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            camera?.setPreviewDisplay(holder)
            camera?.startPreview()
        } catch (e: IOException) {
            Log.d("Preview", "Error starting camera preview : ${e.message}")
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        // preview can not change or rotate, so this can be empty
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        // releasing camera preview is done in Controller.stopCamera
    }
}
package com.nikola.jakshic.kcamera

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class CameraActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        if (savedInstanceState == null)
            startCameraWithPermissionCheck()
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    fun startCamera() {
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, CameraFragment()).commit()
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    fun closeApp() {
        finish()
    }

    @SuppressLint("NeedOnRequestPermissionsResult") // lint bug, will be fixed in next version of permission dispatcher
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        onRequestPermissionsResult(requestCode, grantResults)
    }
}
package com.nikola.jakshic.kcamera

import android.content.Context
import android.view.OrientationEventListener

/** Delegates implementation of [OrientationEventListener.onOrientationChanged] to Controller */
class OrientationChangeListener(
        context: Context?,
        private val listener: OrientationListener) : OrientationEventListener(context) {

    /** For communication between this class and [Controller] */
    interface OrientationListener {
        fun onOrientationChanged(orientation: Int)
    }

    override fun onOrientationChanged(orientation: Int) {
        // Delegate to Controller
        listener.onOrientationChanged(orientation)
    }
}
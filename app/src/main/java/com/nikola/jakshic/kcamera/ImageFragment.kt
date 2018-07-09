package com.nikola.jakshic.kcamera

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_image.*

class ImageFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Previously, in CameraFragment, we locked screen orientation to portrait,
        // now we can allow landscape again
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        return inflater.inflate(R.layout.fragment_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Put taken picture into ImageView(R.id.takenImage)
        Glide.with(this).load(Singletons.data).into(takenImage)

        // Do something with the picture and null out the data from the [Singletons]
    }
}
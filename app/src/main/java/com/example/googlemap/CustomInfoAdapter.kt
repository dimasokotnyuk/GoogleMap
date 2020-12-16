package com.example.googlemap

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomInfoAdapter(private val layoutInflater: LayoutInflater) :
    GoogleMap.InfoWindowAdapter {

    private val popup = layoutInflater.inflate(R.layout.layout_info_window, null)

    val URL_IMAGE =
        "https://upload.wikimedia.org/wikipedia/commons/c/c0/Water_drop_impact_on_a_water-surface_-_%282%29.jpg"

    override fun getInfoWindow(p0: Marker?): View? {
        popup.findViewById<TextView>(R.id.title).text = p0?.snippet + p0?.title
//        Glide
//            .with(layoutInflater.context)
//            .load(URL_IMAGE)
//            .into(popup.findViewById(R.id.imageInfo))
        return popup
    }

    override fun getInfoContents(p0: Marker?): View? {
        return null
    }
}
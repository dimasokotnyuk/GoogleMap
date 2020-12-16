package com.example.googlemap

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.ClusterManager.OnClusterClickListener
import com.google.maps.android.collections.MarkerManager


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private lateinit var map: GoogleMap
    private lateinit var clusterManager: ClusterManager<MyCluster>
    private val TAG = MapsActivity::class.java.simpleName
    private val REQUEST_LOCATION_PERMISSION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val latitude = 48.3876320
        val longitude = 35.0268590
        val zoomLevel = 18f
        val overlaySize = 40f

        val homeLatLng = LatLng(latitude, longitude)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
        map.addMarker(
            MarkerOptions()
                .position(homeLatLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )

        val androidOverlay = GroundOverlayOptions()
            .image(BitmapDescriptorFactory.fromResource(R.drawable.android))
            .position(homeLatLng, overlaySize)
        map.addGroundOverlay(androidOverlay)

        setMapLongClick(map)
        setPoiClick(map)
        setMapStyle(map)
        enableMyLocation()

        setUpCluster()
        setClickCluster(map)
        setClickItemCluster(map)
    }

    private fun setUpCluster() {
        clusterManager = ClusterManager(this, map)
        map.setOnCameraIdleListener(clusterManager)
    }

    private fun setClickItemCluster(map: GoogleMap) {
        clusterManager.setOnClusterItemClickListener(ClusterManager.OnClusterItemClickListener<MyCluster> {
            Log.i("mylog", "click item cluster ${it.position}")
            true
        })
    }

    private fun setClickCluster(map: GoogleMap) {
        clusterManager.setOnClusterClickListener(OnClusterClickListener<MyCluster> {
            val builder = LatLngBounds.Builder()
            it.items.forEach { it ->
                builder.include(LatLng(it.position.latitude, it.position.longitude))
            }
            val bounds = builder.build()
            val padding = 200
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
            map.moveCamera(cameraUpdate)
            map.animateCamera(cameraUpdate)
            true
        })
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            val title = "This is the title"
            val snippet = "${latLng.latitude}   ${latLng.longitude}"
            val offseItem = MyCluster(LatLng(latLng.latitude, latLng.longitude), title, snippet)
            clusterManager.addItem(offseItem)
            clusterManager.cluster()

//            val snippet = String.format(
//                Locale.getDefault(),
//                format = "Lat: %1$.5f, Long: %2$.5f",
//                latLng.latitude,
//                latLng.longitude
//            )
//
//            map.addMarker(
//                MarkerOptions()
//                    .position(latLng)
//                    .title("Моя метка")
//                    .snippet(snippet)
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
//            )
        }
    }

//    private fun addItems() {
//        var lat = 51.5145160
//        var lng = -0.1270060
//        for (i in 0..9) {
//            val offset = i / 60.0
//            lat = lat + offset
//            lng = lng + offset
//            val title = "This is the title"
//            val snippet = "and this is the snippet."
//            val offsetItem = MyCluster(LatLng(lat, lng), title, snippet)
//            clusterManager.addItem(offsetItem)
//        }
//    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.map_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->

            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )
            poiMarker.showInfoWindow()
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        map.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                this,
                R.raw.map_style
            )
        )
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

    override fun onInfoWindowClick(p0: Marker?) {
//        p0?.setIcon(R.drawable.android)
        Toast.makeText(
            this, "Info window clicked",
            Toast.LENGTH_SHORT
        ).show()
        Log.i("mylog", "clickMark")
    }
}
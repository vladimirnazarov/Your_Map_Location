package com.ssrlab.yourmaplocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.ssrlab.yourmaplocation.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var map: GoogleMap
    private var myLocationX: Double = 0.0
    private var myLocationY: Double = 0.0
    private lateinit var cal: Calendar

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1
        )
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            1
        )
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            1
        )

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mBinding.saveCoordinatesButton.setOnClickListener {
            onClick()
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {

        map = googleMap!!

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != 1 && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != 1
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
        }

        f()
    }

    @SuppressLint("SimpleDateFormat")
    private fun onClick() {
        cal = Calendar.getInstance()
        var minutes: Int = cal.get(Calendar.MINUTE)
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        val currentDateAndTime = sdf.format(Date())
        if (minutes < 10) {
            minutes += '0'.code
        }
        mBinding.latValue.setText(myLocationX.toString())
        mBinding.lngValue.setText(myLocationY.toString())

        val name: String = mBinding.nameValue.text.toString() + ".txt"
        val timeT = currentDateAndTime.toString()

        val text = String.format(
            "filename: %s\nx: %s\ny: %s\ntime: %s",
            name,
            myLocationX.toString(),
            myLocationY.toString(),
            timeT
        )
        writeToFile(text, name, applicationContext)
        mBinding.nameValue.setText("")
    }

    private fun writeToFile(data: String, myfile: String, context: Context) {
        val folder = File(context.filesDir, "myLocs")
        if (!folder.exists()) folder.mkdirs()

        println(context.filesDir)

        val file = File(folder, myfile)
        file.printWriter().use { out -> out.println(data) }

        val fileRead = File(folder, myfile)
        FileInputStream(fileRead).use {
            println(String(it.readBytes()))
        }

        Toast.makeText(context, "Запіс зроблен!", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingPermission")
    private fun permGrantedAction(){
        val minsk = LatLng(53.920521, 27.598466)


        map.isMyLocationEnabled = true
        map.addMarker(MarkerOptions().position(minsk).title("Акадэмія навук"))

        map.setOnMyLocationChangeListener(object : OnMyLocationChangeListener {
            var firstLocation = true
            override fun onMyLocationChange(myLocation: Location) {

                // update camera position
                val pos: CameraPosition = map.cameraPosition
                val newPos = CameraPosition.Builder(pos)
                    .target(LatLng(myLocation.latitude, myLocation.longitude))
                    .zoom(17f)
                    .tilt(0f)
                    .build()
                map.animateCamera(CameraUpdateFactory.newCameraPosition(newPos))
                if (firstLocation) {
                    firstLocation = false
                }
                myLocationX = myLocation.latitude
                myLocationY = myLocation.longitude
            }
        })
    }

    private fun f(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            permGrantedAction()
        } else {
            scope.launch {
                delay(1000)
                f()
            }
        }
    }
}
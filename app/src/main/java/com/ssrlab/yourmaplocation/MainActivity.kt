package com.ssrlab.yourmaplocation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Environment
import android.view.View
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
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mBinding: ActivityMainBinding
    private var myLocationX: Double = 0.0
    private var myLocationY: Double = 0.0
    private lateinit var cal: Calendar

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
            onClick(it)
        }
    }

    override fun onMapReady(map: GoogleMap?) {
        val minsk = LatLng(53.920521, 27.598466)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        map?.isMyLocationEnabled = true
        map?.addMarker(MarkerOptions().position(minsk).title("Акадэмія навук"))

        map?.setOnMyLocationChangeListener(object : OnMyLocationChangeListener {
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

    fun onClick(view: View) {
        cal = Calendar.getInstance()
        val hours: Int = cal.get(Calendar.HOUR_OF_DAY)
        var minutes: Int = cal.get(Calendar.MINUTE)
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        val currentDateAndTime = sdf.format(Date())
        if (minutes < 10) {
            minutes = '0'.code + minutes
        }
        mBinding.latValue.setText(myLocationX.toString())
        mBinding.lngValue.setText(myLocationY.toString())

        val name: String = mBinding.nameValue.text.toString() + ".txt"
        val timeT = currentDateAndTime.toString()

        val folder = "myLocs"
        val text = String.format(
            "filename: %s\nx: %s\ny: %s\ntime: %s",
            name,
            myLocationX.toString(),
            myLocationY.toString(),
            timeT
        )
        writeToFile(text, name, folder, applicationContext)
        mBinding.nameValue.setText("")
    }

    private fun writeToFile(data: String, myfile: String, myfolder: String, context: Context) {
        var toastText = "Запіс зроблен"
        val path = Environment.getExternalStorageDirectory()

        val folder = File(path, myfolder)
        var success = true
        if (!folder.exists()) {
            success = folder.mkdirs()
        }

        if (success) {
            val file = File(myfile)

            if (!file.exists()) {
                success = file.mkdirs()

                if (success) {
                    val dest = File(file, myfile)
                    try {
                        // response is the data written to file
                        PrintWriter(dest).use { out -> out.println(data) }
                    } catch (e: Exception) {
                        toastText = "Запіс не зроблен, памылка: " + e.toString()
                        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Памылка", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Немагчама стварыць тэчку", Toast.LENGTH_SHORT).show()
        }
    }
}

//        try {
//            val file = File(folder, myfile)
//            val fileCreated = file.createNewFile()
//            if (!fileCreated) {
//                throw IOException("Unable to create file at specified path. It already exists")
//            }
//
//            val fOut = FileOutputStream(file)
//            val myOutWriter = OutputStreamWriter(fOut)
//            myOutWriter.append(data)
//
//            myOutWriter.close()
//
//            fOut.flush()
//            fOut.close()
//        } catch (e: Exception) {
//            toastText = "Запіс не зроблен, памылка: " + e.toString()
//            println(e.toString())
//        }
//        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
//    }
//}
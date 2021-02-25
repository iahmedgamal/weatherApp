package com.musalaSoft.weatherApp.ui

import Base
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.musalaSoft.weatherApp.R
import com.musalaSoft.weatherApp.databinding.ActivityMainBinding
import com.musalaSoft.weatherApp.helpers.ConstantsUrls
import com.musalaSoft.weatherApp.helpers.MySharedPreferences
import com.musalaSoft.weatherApp.helpers.hasPermission
import com.musalaSoft.weatherApp.helpers.requestPermissionWithRationale
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: com.musalaSoft.weatherApp.databinding.ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    // The Fused Location Provider provides access to location APIs.
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(applicationContext)
    }

    // Allows class to cancel the location request if it exits the activity.
    private var cancellationTokenSource = CancellationTokenSource()

    // If the user denied a previous permission request, but didn't check "Don't ask again", this
    // Snackbar provides an explanation for why user should approve, i.e., the additional rationale.
    private val fineLocationRationalSnackbar by lazy {
        Snackbar.make(
                binding.constraintlayout,
                R.string.fine_location_permission_rationale,
                Snackbar.LENGTH_LONG
        ).setAction(R.string.ok) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        ConstantsUrls.REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE
                )
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.getWeatherSuccessResponse.observe(this, Observer<Base> { weatherData ->
            // Update the UI from MainViewModel
            if (weatherData != null) {
                Log.d("ss", weatherData.toString())
                degreeTV.text = weatherData.main.temp.toString()
                MySharedPreferences.setLastResponse(weatherData.name)
                degreeTV.text = weatherData.main.temp.toString()
                description_tv.text = weatherData.weather[0].description
                feels_like_value_tv.text = weatherData.main.feels_like.toString()
                pressure_value_tv.text = weatherData.main.pressure.toString()

                search_et.setText(weatherData.name.toString())
            }
        })

        setSharedPreferences()
        initSharedPrefrences()

        // search button click
        search_btn.setOnClickListener() {
            val city = search_et.text.toString()
            // get weather data from MainViewModel
            viewModel.getWeatherData(city, MySharedPreferences.getDegree().toString(), null, null)
        }
        locationRequestOnClick(view)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.celsius -> {
                val city = search_et.text.toString()
                MySharedPreferences.setDegree("metric")
                viewModel.getWeatherData(city, "metric", null, null)

                return true
            }
            R.id.fahrenheit -> {
                val city = search_et.text.toString()
                MySharedPreferences.setDegree("imperial")
                viewModel.getWeatherData(city, "imperial", null, null)

                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    fun setSharedPreferences() {
        Log.d("init", "init SharedPreferences")
        MySharedPreferences.setContext(getApplicationContext())
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                val m = StrictMode::class.java.getMethod("disableDeathOnFileUriExposure")
                m.invoke(null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initSharedPrefrences() {
        if (MySharedPreferences.getDegree().toString() != "") {
            var city = MySharedPreferences.getLastResponse().toString()
        }
    }


    override fun onStop() {
        super.onStop()
        // Cancels location request (if in flight).
        cancellationTokenSource.cancel()
    }


    override fun onResume() {
        super.onResume()
        val permissionApproved =
                applicationContext.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

        if (permissionApproved) {
            requestCurrentLocation()

            //check if user's GPS is opened
            locationEnabled()
        }


    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        Log.d(ConstantsUrls.TAG, "onRequestPermissionResult()")

        if (requestCode == ConstantsUrls.REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() ->
                    // If user interaction was interrupted, the permission request
                    // is cancelled and you receive an empty array.
                    Log.d(ConstantsUrls.TAG, "User interaction was cancelled.")

                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    Snackbar.make(
                            binding.constraintlayout,
                            R.string.permission_approved_explanation,
                            Snackbar.LENGTH_LONG
                    )
                            .show()

                else -> {
                    Snackbar.make(
                            binding.constraintlayout,
                            R.string.fine_permission_denied_explanation,
                            Snackbar.LENGTH_LONG
                    )
                            .setAction(R.string.settings) {
                                // Build intent that displays the App settings screen.
                                val intent = Intent()
                                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                val uri = Uri.fromParts(
                                        "package",
                                        packageName,
                                        null
                                )
                                intent.data = uri
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                            }
                            .show()
                }
            }
        }
    }

    fun locationRequestOnClick(view: View) {
        Log.d(ConstantsUrls.TAG, "locationRequestOnClick()")

        val permissionApproved =
                applicationContext.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

        if (permissionApproved) {
            requestCurrentLocation()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissionWithRationale(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        ConstantsUrls.REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE,
                        fineLocationRationalSnackbar
                )
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun requestCurrentLocation() {
        Log.d(ConstantsUrls.TAG, "requestCurrentLocation()")
        if (applicationContext.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {

            // Returns a single current location fix on the device. Unlike getLastLocation() that
            // returns a cached location, this method could cause active location computation on the
            // device. A single fresh location will be returned if the device location can be
            // determined within reasonable time (tens of seconds), otherwise null will be returned.
            //
            // Both arguments are required.
            // PRIORITY type is self-explanatory. (Other options are PRIORITY_BALANCED_POWER_ACCURACY,
            // PRIORITY_LOW_POWER, and PRIORITY_NO_POWER.)
            // The second parameter, [CancellationToken] allows the activity to cancel the request
            // before completion.
            val currentLocationTask: Task<Location> = fusedLocationClient.getCurrentLocation(
                    PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
            )

            currentLocationTask.addOnCompleteListener { task: Task<Location> ->
                val result = if (task.isSuccessful && task.result != null) {
                    val result: Location = task.result
                    "Location (success): ${result.latitude}, ${result.longitude}"
                    viewModel.getWeatherData(null, MySharedPreferences.getDegree().toString(), result.latitude.toString(), result.longitude.toString())
                } else {
                    val exception = task.exception
                    "Location (failure): $exception"
                }

                Log.d(ConstantsUrls.TAG, "getCurrentLocation() result: $result")
            }
        }
    }

    private fun locationEnabled() {
        Log.d("test2", "ss")
        val lm: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gps_enabled = false
        var network_enabled = false
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        if (!gps_enabled && !network_enabled) {
            AlertDialog.Builder(this@MainActivity)
                    .setMessage("GPS Enable")
                    .setPositiveButton("Settings") { dialog, which -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)); }
                    .setNegativeButton("Cancel", null)
                    .show()
        }
    }
}
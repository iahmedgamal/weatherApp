package com.musalaSoft.weatherApp.ui

import Base
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.musalaSoft.weatherApp.R
import com.musalaSoft.weatherApp.databinding.ActivityMainBinding
import com.musalaSoft.weatherApp.helpers.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: com.musalaSoft.weatherApp.databinding.ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var locationViewModel: LocationViewModel

    private val fineLocationRationalSnackbar by lazy {
        Snackbar.make(
                binding.constraintlayout,
                R.string.fine_location_permission_rationale,
                Snackbar.LENGTH_LONG
        ).setAction(R.string.ok) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        Constants.REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE
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
                degreeTV.text = weatherData.main.temp.toString()
                MySharedPreferences.setLastResponse(weatherData.name)
                degreeTV.text = weatherData.main.temp.toString()
                description_tv.text = weatherData.weather[0].description
                feels_like_value_tv.text = weatherData.main.feels_like.toString()
                pressure_value_tv.text = weatherData.main.pressure.toString()
                search_et.setText(weatherData.name.toString())
            }
        })

        viewModel.getWeatherFailResponse.observe(this, Observer<String> {
            if (it != null)
                Snackbar.make(
                        binding.constraintlayout,
                        it,
                        Snackbar.LENGTH_LONG
                )
                        .show()
        })

        locationViewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
        locationViewModel.getLocationResponse.observe(this, Observer<Location> { location ->
            // Update the UI from MainViewModel
            if (location != null) {
                if (NetworkUtil.isNetworkAvailable(this)) {
                    viewModel.getWeatherData(null, MySharedPreferences.getDegree().toString(), location.latitude, location.longitude)
                }
            }
        })

        setSharedPreferences()

        search_btn.setOnClickListener() {
            val city = search_et.text.toString()
            val searchValue: String = search_et.text.toString()
            if (!TextUtils.isEmpty(searchValue)) {
                // get weather data from MainViewModel
                if (NetworkUtil.isNetworkAvailable(this)) {
                    viewModel.getWeatherData(city, MySharedPreferences.getDegree().toString(), null, null)

                } else {
                    Snackbar.make(
                            binding.constraintlayout,
                            "check internet connection",
                            Snackbar.LENGTH_LONG
                    )
                            .show()
                }
            } else {
                search_et.setError("Input required")
            }
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

    override fun onStop() {
        super.onStop()
        // Cancels location request (if in flight).
        locationViewModel.cancellationTokenSource.cancel()
    }


    override fun onResume() {
        super.onResume()
        val permissionApproved =
                applicationContext.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

        if (permissionApproved) {
            locationViewModel.getLocation()
            viewModel
            //check if user's GPS is opened
            locationEnabled()
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        Log.d(Constants.TAG, "onRequestPermissionResult()")

        if (requestCode == Constants.REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() ->
                    // If user interaction was interrupted, the permission request
                    // is cancelled and you receive an empty array.
                    Log.d(Constants.TAG, "User interaction was cancelled.")

                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    locationEnabled()

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
        val permissionApproved =
                applicationContext.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionApproved) {
            locationViewModel.getLocation()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissionWithRationale(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Constants.REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE,
                        fineLocationRationalSnackbar
                )
            }
        }
    }

    private fun locationEnabled() {
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

            settingsRequest()
        }
    }

    // open google maps location dialog
    private fun settingsRequest() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.interval = 10
        mLocationRequest.smallestDisplacement = 10f
        mLocationRequest.fastestInterval = 10
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)

        val task: Task<LocationSettingsResponse> = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())
        task.addOnCompleteListener { task ->
            try {
                val response = task.getResult(ApiException::class.java)

            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        try {
                            val resolvable: ResolvableApiException = exception as ResolvableApiException
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            resolvable.startResolutionForResult(
                                    this@MainActivity,
                                    101)
                        } catch (e: IntentSender.SendIntentException) {
                            // Ignore the error.
                        } catch (e: ClassCastException) {
                            // Ignore, should be an impossible error.
                        }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    }
                }
            }
        }
    }
}
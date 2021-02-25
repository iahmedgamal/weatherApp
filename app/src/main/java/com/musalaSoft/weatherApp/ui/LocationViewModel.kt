package com.musalaSoft.weatherApp.ui

import Coord
import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.net.ConnectivityManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.musalaSoft.weatherApp.helpers.ConstantsUrls
import com.musalaSoft.weatherApp.helpers.MySharedPreferences
import com.musalaSoft.weatherApp.helpers.hasPermission

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    val getLocationResponse = MutableLiveData<Location>().apply { value = null }



    // The Fused Location Provider provides access to location APIs.
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(application)
    }
    // Allows class to cancel the location request if it exits the activity.
     var cancellationTokenSource = CancellationTokenSource()

        @SuppressLint("MissingPermission")
        private fun setCurrentLocation() {
            Log.d(ConstantsUrls.TAG, "requestCurrentLocation()")
                val currentLocationTask: Task<Location> = fusedLocationClient.getCurrentLocation(
                    LocationRequest.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                )

                currentLocationTask.addOnCompleteListener { task: Task<Location> ->
                    val result = if (task.isSuccessful && task.result != null) {
                        val result: Location = task.result
//                        "Location (success): ${result.latitude}, ${result.longitude}"

                        getLocationResponse.value = task.result

//                        viewModel.getWeatherData(null, MySharedPreferences.getDegree().toString(), result.latitude.toString(), result.longitude.toString())
                    } else {
                        val exception = task.exception
                        "Location (failure): $exception"
                    }

                    Log.d(ConstantsUrls.TAG, "getCurrentLocation() result2: $result")
                }

        }

        fun getLocation(){
            setCurrentLocation()
       }



    }

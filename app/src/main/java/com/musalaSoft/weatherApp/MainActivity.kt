package com.musalaSoft.weatherApp

import Base
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
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
import androidx.viewbinding.BuildConfig
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.musalaSoft.weatherApp.helpers.ConstantsUrls
import com.musalaSoft.weatherApp.helpers.MySharedPreferences
import com.musalaSoft.weatherApp.helpers.hasPermission
import com.musalaSoft.weatherApp.helpers.requestPermissionWithRationale
import com.musalaSoft.weatherApp.network.APIInterface
import com.musalaSoft.weatherApp.network.ApiClient
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.musalaSoft.weatherApp.databinding.ActivityMainBinding
import androidx.databinding.DataBindingUtil


class MainActivity : AppCompatActivity() {
    private var service: ApiClient? = null
    private lateinit var binding: com.musalaSoft.weatherApp.databinding.ActivityMainBinding

    // The Fused Location Provider provides access to location APIs.
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(applicationContext)
    }

    // Allows class to cancel the location request if it exits the activity.
    // Typically, you use one cancellation source per lifecycle.
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

//        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSharedPreferences()
        initSharedPrefrences()
        search_btn.setOnClickListener(){
           val city=  search_et.text.toString()
            search(city,MySharedPreferences.getDegree().toString())
        }

        locationRequestOnClick(view)

    }



    private fun search(city:String, unit:String){
        //    For temperature in Fahrenheit use units=imperial
        //    For temperature in Celsius use units=metric
        val apiService = ApiClient.client.create(APIInterface::class.java)
        val call= apiService.getWeather(city, ConstantsUrls.APP_ID,unit)
        // call weather API
        call.enqueue(object : Callback<Base>{
            override fun onFailure(call: Call<Base>, t: Throwable) {
                Log.d("Throwable",t.toString())

            }
            override fun onResponse(call: Call<Base>, response: Response<Base>) {
                Log.d("response",response.toString())
                if (response.code() == 200 ) {
                    Log.d("response", response.body()!!.toString())
                    val finalResponse =  response.body()!!
                    MySharedPreferences.setLastResponse(finalResponse.name)

                    degreeTV.text =  finalResponse.main.temp.toString()
                    description_tv.text = finalResponse.weather[0].description
                    feels_like_value_tv.text =  finalResponse.main.feels_like.toString()
                    pressure_value_tv.text =  finalResponse.main.pressure.toString()
                }
            }

        })
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu to use in the action bar

        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.celsius -> {
                val city=  search_et.text.toString()
                MySharedPreferences.setDegree("metric")
                search(city,"metric")
                return true
            }
            R.id.fahrenheit -> {
                val city=  search_et.text.toString()
                MySharedPreferences.setDegree("imperial")
                search(city,"imperial")
                return true
            }


        }
        return super.onOptionsItemSelected(item)
    }

    //init sharedPreferences
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
        if(MySharedPreferences.getDegree().toString()!=""){
            var city = MySharedPreferences.getLastResponse().toString()
            search(city,MySharedPreferences.getDegree().toString())
            search_et.setText(city)
        }
    }



    override fun onStop() {
        super.onStop()
        // Cancels location request (if in flight).
        cancellationTokenSource.cancel()
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
                                        BuildConfig.LIBRARY_PACKAGE_NAME,
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

    /**
     * Gets current location.
     * Note: The code checks for permission before calling this method, that is, it's never called
     * from a method with a missing permission. Also, I include a second check with my extension
     * function in case devs just copy/paste this code.
     */
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
                } else {
                    val exception = task.exception
                    "Location (failure): $exception"
                }

                Log.d(ConstantsUrls.TAG, "getCurrentLocation() result: $result")
                logOutputToScreen(result)
            }
        }
    }

    private fun logOutputToScreen(outputString: String) {
        val finalOutput = binding.outputTextView.text.toString() + "\n" + outputString
        binding.outputTextView.text = finalOutput
    }





}
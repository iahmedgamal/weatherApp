package com.musalaSoft.weatherApp

import Base
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.musalaSoft.weatherApp.helpers.ConstantsUrls
import com.musalaSoft.weatherApp.helpers.MySharedPreferences
import com.musalaSoft.weatherApp.network.APIInterface
import com.musalaSoft.weatherApp.network.ApiClient
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {
    private var service: ApiClient? = null



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSharedPreferences()
        initSharedPrefrences()
        search_btn.setOnClickListener(){
           val city=  search_et.text.toString()
            search(city,MySharedPreferences.getDegree().toString())
        }

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






}
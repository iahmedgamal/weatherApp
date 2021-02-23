package com.musalaSoft.weatherApp

import Base
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.musalaSoft.weatherApp.network.APIInterface
import com.musalaSoft.weatherApp.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var service: ApiClient? = null

    companion object {
        const val APP_ID = "1a8927de54f779e3daeb1932452a3799"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        search_btn.setOnClickListener(){
           val city=  search_tv.text.toString()
            search(city)
        }

    }


    private fun search(city:String){
        //    For temperature in Fahrenheit use units=imperial
        //    For temperature in Celsius use units=metric
        val apiService = ApiClient.client.create(APIInterface::class.java)
        val call= apiService.getWeather(city,APP_ID,"metric")
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
                    degreeTV.text =  finalResponse.main.temp.toString()
                    description_tv.text=finalResponse.weather[0].description
                }
            }

        })
    }
}
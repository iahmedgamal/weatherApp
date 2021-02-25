package com.musalaSoft.weatherApp.ui

import Base
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.musalaSoft.weatherApp.helpers.ConstantsUrls
import com.musalaSoft.weatherApp.network.APIInterface
import com.musalaSoft.weatherApp.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel : ViewModel() {

    val getWeatherSuccessResponse = MutableLiveData<Base>().apply { value = null }

    private fun search(city: String ?, unit: String, lat : String ?, lon :String?) {
        val apiService = ApiClient.client.create(APIInterface::class.java)
        val call = apiService.getWeather(city, ConstantsUrls.APP_ID, unit, lat, lon)
        // call weather API
        call.enqueue(object : Callback<Base> {
            override fun onFailure(call: Call<Base>, t: Throwable) {
                Log.d("Throwable", t.toString())
            }

            override fun onResponse(call: Call<Base>, response: Response<Base>) {
                Log.d("response", response.toString())
                if (response.code() == 200) {
                    Log.d("response", response.body()!!.toString())
                    val finalResponse = response.body()!!
                    getWeatherSuccessResponse.value = finalResponse

                }
            }

        })
    }

    fun getWeatherData(city: String?, unit: String, lat: String?, lon: String?) {
        search(city, unit, lat, lon)
    }

}
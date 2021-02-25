package com.musalaSoft.weatherApp.network

import Base
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface APIInterface {

  //    For temperature in Fahrenheit use units=imperial
  //    For temperature in Celsius use units=metric
  //    Temperature in Kelvin is used by default
  @GET("data/2.5/weather")
  fun getWeather(
    @Query("q") city: String?,
    @Query("appid") appid: String,
    @Query("units") units: String,
    @Query("lat") lat: String?,
    @Query("lon") lon: String?


  ): Call<Base>

}
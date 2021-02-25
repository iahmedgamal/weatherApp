package com.musalaSoft.weatherApp.network

import Base
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface APIInterface {


  @GET("data/2.5/weather")
  fun getWeather(
    @Query("q") city: String?,
    @Query("appid") appid: String,
    @Query("units") units: String,
    @Query("lat") lat: Double?,
    @Query("lon") lon: Double?


  ): Call<Base>

}
package com.musalaSoft.weatherApp.network

import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

    object ApiClient {
        val BASE_URL = "https://api.openweathermap.org/"
        private var retrofit: Retrofit? = null
        val client: Retrofit
            get() {
                if (retrofit == null) {
                    retrofit = Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                }
                return retrofit!!
            }
    }


//TODO use Interceptor for api-key
//.addQueryParameter("apikey", "your-actual-api-key")

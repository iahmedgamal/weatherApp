package com.musalaSoft.weatherApp.network

import com.musalaSoft.weatherApp.helpers.ConstantsUrls
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

    object ApiClient {
        private var retrofit: Retrofit? = null
        val client: Retrofit
            get() {
                if (retrofit == null) {
                    retrofit = Retrofit.Builder()
                        .baseUrl(ConstantsUrls.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                }
                return retrofit!!
            }
    }


//TODO use Interceptor for api-key
//.addQueryParameter("apikey", "your-actual-api-key")

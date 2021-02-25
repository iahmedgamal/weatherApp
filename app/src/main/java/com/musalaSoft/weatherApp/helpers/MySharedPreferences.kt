package com.musalaSoft.weatherApp.helpers

import android.content.Context
import android.content.SharedPreferences

class MySharedPreferences {

    companion object {
        private val mySharedPreferenceName = "weatherSharedPreference"
        private val PREFERENCE_KEY_DEGREE = "degree"
        private val PREFERENCE_KEY_LAST_RESPONSE = "response"

        private var mAppContext: Context? = null


        fun setContext(context: Context?) {
            mAppContext = context
        }

        fun check(): Context? {
            return mAppContext
        }

        private fun getSharedPreferences(): SharedPreferences? {
            return mAppContext?.getSharedPreferences(
                mySharedPreferenceName,
                Context.MODE_PRIVATE
            )
        }

        fun setDegree(degree: String?) {
            val editor: SharedPreferences.Editor = getSharedPreferences()!!.edit()
            editor.putString(PREFERENCE_KEY_DEGREE, degree).apply()
        }

        fun getDegree(): String? {
            return getSharedPreferences()?.getString(
                PREFERENCE_KEY_DEGREE,
                "metric"
            )
        }

        fun setLastResponse(response:String){
            val editor: SharedPreferences.Editor = getSharedPreferences()!!.edit()
            editor.putString(PREFERENCE_KEY_LAST_RESPONSE, response).apply()
        }

        fun getLastResponse(): String? {
            return getSharedPreferences()?.getString(
                PREFERENCE_KEY_LAST_RESPONSE,
                ""
            )
        }
    }

}
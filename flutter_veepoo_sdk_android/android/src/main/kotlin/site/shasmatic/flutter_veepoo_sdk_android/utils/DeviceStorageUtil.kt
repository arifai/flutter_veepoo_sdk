package site.shasmatic.flutter_veepoo_sdk_android.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class DeviceStorageUtil(private val context: Context) {
    private val name: String = "device_storage"

    fun getPassword(): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)
        return sharedPreferences.getString("password", null)
    }

    fun get24H(): Boolean {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("is24H", true)
    }

    fun saveCredentials(password: String, is24H: Boolean) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        editor.putString("password", password)
        editor.putBoolean("is24H", is24H)
        editor.apply()
    }
}
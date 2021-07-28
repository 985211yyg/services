package com.reemii.services.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings

/**
 * Author: yyg
 * Date: 2019-12-23 10:37
 * Description:
 */

class GpsUtil {

    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @param context
     * @return true 表示开启
     */
    fun isOPen(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        val gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        val network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return if (gps) {
            return true
        } else false
    }

    /**
     * 强制帮用户打开GPS
     *
     * @param context
     */
    fun openGPS(context: Activity, code: Int) {
//        val intent = Intent()
//        intent.action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//        try {
//            context.startActivityForResult(intent, code)
//        } catch (ex: ActivityNotFoundException) { // The Android SDK doc says that the location settings activity
//            intent.action = Settings.ACTION_SETTINGS
//            context.startActivity(intent)
//            context.finish()
//        }
    }

    
    
}
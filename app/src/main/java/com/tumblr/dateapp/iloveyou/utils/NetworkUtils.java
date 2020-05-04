package com.tumblr.dateapp.iloveyou.utils;

/**
 * Created by HS on 2018-01-24.
 */

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {

    public static boolean isAvailable(ConnectivityManager connectivityManager) {
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null;
    }
}

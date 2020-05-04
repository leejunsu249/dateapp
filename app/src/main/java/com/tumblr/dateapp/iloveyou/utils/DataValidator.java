package com.tumblr.dateapp.iloveyou.utils;

/**
 * Created by HS on 2018-01-24.
 */

public class DataValidator {

    public static boolean isUsernameValid(String username) {
        return username.length() < 25;
    }

    public static boolean isBioValid(String bio) {
        return bio.length() < 40;
    }

    public static boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isPasswordValid(String password) {
        return password.length() > 6;
    }

}

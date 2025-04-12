package com.example.pamatek;  // Make sure this matches your app package

import android.content.Context;
import android.content.SharedPreferences;

public class UserPrefs {
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_USER_TYPE = "userType";

    // Save the user type (call this during login)
    public static void setUserType(Context context, String userType) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_USER_TYPE, userType).apply();
    }

    // Get the saved user type
    public static String getUserType(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_TYPE, "");
    }

    // Clear preferences (optional, e.g. on logout)
    public static void clear(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}

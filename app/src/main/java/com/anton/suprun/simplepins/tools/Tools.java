package com.anton.suprun.simplepins.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.anton.suprun.simplepins.data.Constants;

public class Tools {
    private static final String TAG = "Pins_Tools";
    private static Tools mInstance;
    private final Context mContext;
    private final SharedPreferences mPreferences;

    private Tools(Context context) {
        this.mContext = context;
        this.mPreferences = context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);
    }

    public static void createInstance(Context context) {
        if (mInstance == null) {
            mInstance = new Tools(context);
        }
    }

    public static Tools getInstance() {
        return mInstance;
    }

    private static SharedPreferences getPreferences() {
        return getInstance().mPreferences;
    }

    public static void setUserId(String userId) {
        getPreferences().edit().putString(Constants.USER_ID_PREF, userId).apply();
    }

    public static String getUserId() {
        return getPreferences().getString(Constants.USER_ID_PREF, "");
    }

    public static boolean isUserLogged() {
        return !TextUtils.isEmpty(getUserId());
    }

    public static void logout() {
        getPreferences().edit().remove(Constants.USER_ID_PREF).apply();
    }
}

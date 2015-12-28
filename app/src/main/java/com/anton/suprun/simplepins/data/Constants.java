package com.anton.suprun.simplepins.data;

public class Constants {
    // extras and preferences
    public static final String PREFS = "default_preferences";
    public static final String USER_ID_PREF = "user_id_preference";

    // DB related
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "db_simple_pins";
    public static final String TABLE_PINS = "table_pins";
    public static final String FIELD_ID = "_id";
    public static final String FIELD_USER_ID = "user_id";
    public static final String FIELD_LAT = "field_lat";
    public static final String FIELD_LON = "field_lon";
    public static final String FIELD_TITLE = "field_title";

    // Facebook communication
    public static final String FB_CLIENT_ID = "421460128043420";
    public static final String FB_BASIC_HOST = "mbasic.facebook.com";
    public static final String FB_SUCCESS_URL = "https://www.facebook.com/connect/login_success.html";
    public static final String FB_AUTH_URL = "https://www.facebook.com/dialog/oauth" +
            "?client_id=" + FB_CLIENT_ID +
            "&redirect_uri=" + FB_SUCCESS_URL +
            "&response_type=token";
    public static final String FB_GET_USER_ID_URL = "https://graph.facebook.com/me?fields=id&access_token=%s";
    public static final String FB_SUCCESS_PAGE = "login_success.html";
}

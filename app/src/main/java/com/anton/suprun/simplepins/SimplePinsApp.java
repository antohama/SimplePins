package com.anton.suprun.simplepins;

import android.app.Application;

import com.anton.suprun.simplepins.data.PinsDBHelper;
import com.anton.suprun.simplepins.tools.Tools;

public class SimplePinsApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PinsDBHelper.createInstance(this);
        Tools.createInstance(this);
    }
}

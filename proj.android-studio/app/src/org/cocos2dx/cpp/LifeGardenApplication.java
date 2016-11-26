package org.cocos2dx.cpp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class LifeGardenApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ApplicationHelper.startSensorStreamer(this);
    }
}

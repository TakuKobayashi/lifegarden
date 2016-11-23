package org.cocos2dx.cpp;

import android.app.Application;

public class LifeGardenApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SensorStreamer sensorStreamer = SensorStreamer.getInstance(SensorStreamer.class);
        sensorStreamer.init(this);
        sensorStreamer.startSensor();
    }
}

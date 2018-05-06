package com.xdandroid.sample;

import android.app.*;

import com.xdandroid.hellodaemon.*;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //   응용 프로그램의 onCreate ()에서 DaemonEnv.initialize() 한 번 호출해야합니다.
        DaemonEnv.initialize(this, TraceServiceImpl.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
        TraceServiceImpl.sShouldStopService = false;
        DaemonEnv.startServiceMayBind(TraceServiceImpl.class);
    }
}

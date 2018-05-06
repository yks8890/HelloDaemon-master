package com.xdandroid.hellodaemon;

import android.annotation.*;
import android.app.job.*;
import android.os.*;

/**
 * Android 5.0 이상에서 JobScheduler 사용.
 *: watch 하위 프로세스에서 실행하십시오.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobSchedulerService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        if (!DaemonEnv.sInitialized) return false;
        DaemonEnv.startServiceMayBind(DaemonEnv.sServiceClass);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}

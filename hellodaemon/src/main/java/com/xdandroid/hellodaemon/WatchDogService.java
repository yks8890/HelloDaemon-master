package com.xdandroid.hellodaemon;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;

import java.util.concurrent.TimeUnit;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class WatchDogService extends Service {

    protected static final int HASH_CODE = 2;

    protected static Disposable sDisposable;
    protected static PendingIntent sPendingIntent;

    /**
     * 데몬 서비스 실행 : watch 하위 프로세스
     */
    protected final int onStart(Intent intent, int flags, int startId) {

        if (!DaemonEnv.sInitialized) return START_STICKY;

        if (sDisposable != null && !sDisposable.isDisposed()) return START_STICKY;

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
            startForeground(HASH_CODE, new Notification());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                DaemonEnv.startServiceSafely(new Intent(DaemonEnv.sApp, WatchDogNotificationService.class));
        }

        // 주기적으로 AbsWorkService가 실행 중인지 확인하고 실행 중이 아니면 AbsWorkService를 풀다.
        // Android 5.0 이상에서는 JobScheduler를 사용합니다 (AlarmManager보다 우수함).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobInfo.Builder builder = new JobInfo.Builder(HASH_CODE, new ComponentName(DaemonEnv.sApp, JobSchedulerService.class));
            builder.setPeriodic(DaemonEnv.getWakeUpInterval());
            //Android 7.0 이상에서는 JobScheduler에 새로운 제한이 추가되었습니다. 최소 간격은 아래에 설정된 숫자 일 수 있습니다
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                builder.setPeriodic(JobInfo.getMinPeriodMillis(), JobInfo.getMinFlexMillis());
            builder.setPersisted(true);
            JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            scheduler.schedule(builder.build());
        } else {
            //Android 4.4- AlarmManager 사용
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent i = new Intent(DaemonEnv.sApp, DaemonEnv.sServiceClass);
            sPendingIntent = PendingIntent.getService(DaemonEnv.sApp, HASH_CODE, i, PendingIntent.FLAG_UPDATE_CURRENT);
            am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + DaemonEnv.getWakeUpInterval(), DaemonEnv.getWakeUpInterval(), sPendingIntent);
        }

        // Timed Observable을 사용하여 Android 사용자 정의 시스템 JobScheduler / AlarmManager가 불안정한 간격으로 깨어나는 상황을 피하십시오
        sDisposable = Observable
                .interval(DaemonEnv.getWakeUpInterval(), TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        DaemonEnv.startServiceMayBind(DaemonEnv.sServiceClass);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });

        //데몬은 구성 요소의 활성화 된 상태를 감시하여 MAT와 같은 도구에 의해 비활성화되지 않도록합니다
        getPackageManager().setComponentEnabledSetting(new ComponentName(getPackageName(), DaemonEnv.sServiceClass.getName()),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        return START_STICKY;
    }

    @Override
    public final int onStartCommand(Intent intent, int flags, int startId) {
        return onStart(intent, flags, startId);
    }

    @Override
    public final IBinder onBind(Intent intent) {
        onStart(intent, 0, 0);
        return null;
    }

    protected void onEnd(Intent rootIntent) {
        if (!DaemonEnv.sInitialized) return;
        DaemonEnv.startServiceMayBind(DaemonEnv.sServiceClass);
        DaemonEnv.startServiceMayBind(WatchDogService.class);
    }

    /**
     * 가장 최근의 작업 목록에서 카드가 초과 된 경우의 콜백
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        onEnd(rootIntent);
    }

    /**
     * 설정 - 실행 중에 서비스가 중지되면 콜백
     */
    @Override
    public void onDestroy() {
        onEnd(null);
    }

    /**
     * Job / Alarm / Subscription. 서비스가 필요없는 경우 취소하는 데 사용됩니다.
     * <p>
     * WatchDogService는 : watch 자식 프로세스에서 실행되기 때문에 메인 프로세스에서 직접이 메소드를 호출하지 마십시오.
     * 대신 Action WakeUpReceiver.ACTION_CANCEL_JOB_ALARM_SUB을 사용하여 WakeUpReceiver 브로드 캐스트를 보냅니다.
     */
    public static void cancelJobAlarmSub() {
        if (!DaemonEnv.sInitialized) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler scheduler = (JobScheduler) DaemonEnv.sApp.getSystemService(JOB_SCHEDULER_SERVICE);
            scheduler.cancel(HASH_CODE);
        } else {
            AlarmManager am = (AlarmManager) DaemonEnv.sApp.getSystemService(ALARM_SERVICE);
            if (sPendingIntent != null) am.cancel(sPendingIntent);
        }
        if (sDisposable != null) sDisposable.dispose();
    }

    public static class WatchDogNotificationService extends Service {

        /**
         * API 레벨 18 이상인 Android 시스템에서 알림없이 프론트 엔드 서비스 사용
         * 다음에서 실행 : 시계 하위 프로세스
         */
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(WatchDogService.HASH_CODE, new Notification());
            stopSelf();
            return START_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
}

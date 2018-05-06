package com.xdandroid.hellodaemon;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.support.annotation.*;

public abstract class AbsWorkService extends Service {

    protected static final int HASH_CODE = 1;

    protected boolean mFirstStarted = true;

    /**
     * Job / Alarm / Subscription 서비스 운행이 불필요할 때에는 취소합니다.
     */
    public static void cancelJobAlarmSub() {
        if (!DaemonEnv.sInitialized) return;
        DaemonEnv.sApp.sendBroadcast(new Intent(WakeUpReceiver.ACTION_CANCEL_JOB_ALARM_SUB));
    }

    /**
     *임무가 완성되지 않았는데, 더 이상 서비스 운행이 필요 없습니까?
     * @return 서비스를 중단해야 한다, true; 서비스를 시작해야 한다, false; 판단할 수 없고 아무것도 하지 않는다, null.
     */
    public abstract Boolean shouldStopService(Intent intent, int flags, int startId);
    public abstract void startWork(Intent intent, int flags, int startId);
    public abstract void stopWork(Intent intent, int flags, int startId);
    /**
     *임무는 지금 실행 중입니까?
     * @return 임무가 한창 작동 중이다., true; 임무는 현재 운행 중이다., false; 판단 할 수 없으며, 아무것도하지 못한다, null.
     */
    public abstract Boolean isWorkRunning(Intent intent, int flags, int startId);
    @Nullable public abstract IBinder onBind(Intent intent, Void alwaysNull);
    public abstract void onServiceKilled(Intent rootIntent);

    /**
     * 1. 반복 시작 방지, 임의 호출 가능 DaemonEnv.startServiceMayBind(Class serviceClass);
     * 2. 알림을 표시하지 않고 볼트를 사용하여 포 그라운드 서비스를 시작합니다.
     * 3. 하위 스레드에서 예약 된 작업을 실행하고 실행 전 확인 및 삭제 중에 보존되는 문제를 처리합니다.
     * 4. 데몬 서비스를 시작하십시오.
     * 5. 데몬은 구성 요소의 활성화 된 상태를 보호하여 MAT와 같은 도구에 의해 비활성화되지 않도록합니다.
     */
    protected int onStart(Intent intent, int flags, int startId) {

        //데몬 서비스를 시작하고 : watch 하위 프로세스에서 실행합니다
        DaemonEnv.startServiceMayBind(WatchDogService.class);

        //비즈니스 로직 : 요구 사항에 따라 실제 사용에서는이를 사용자 지정 조건으로 변경하고 서비스를 시작하거나 중지해야하는지 여부를 결정합니다 (작업을 실행해야하는지 여부).
        Boolean shouldStopService = shouldStopService(intent, flags, startId);
        if (shouldStopService != null) {
            if (shouldStopService) stopService(intent, flags, startId); else startService(intent, flags, startId);
        }

        if (mFirstStarted) {
            mFirstStarted = false;
            //알림을 표시하지 않고 프론트 데스크 서비스를 시작한 취약점은 API 레벨 25에서 수정되었으며 모두가 만족합니다!
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
                //API 레벨 17 이하의 Android 시스템에서 알림없이 프론트 엔드 서비스 사용
                startForeground(HASH_CODE, new Notification());
                //API 레벨 18 이상의 Android 시스템에서 취약점을 사용하여 알림을 표시하지 않고 전경 서비스를 시작합니다.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                    DaemonEnv.startServiceSafely(new Intent(getApplication(), WorkNotificationService.class));
            }
            getPackageManager().setComponentEnabledSetting(new ComponentName(getPackageName(), WatchDogService.class.getName()),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }

        return START_STICKY;
    }

    void startService(Intent intent, int flags, int startId) {
        //서비스를 실행할 필요가 없는지 확인하십시오.
        Boolean shouldStopService = shouldStopService(intent, flags, startId);
        if (shouldStopService != null && shouldStopService) return;
        //구독을 취소하지 않은 경우 작업이 계속 실행 중이므로 반복 시작, 직접 반환을 방지합니다.
        Boolean workRunning = isWorkRunning(intent, flags, startId);
        if (workRunning != null && workRunning) return;
        //비즈니스 로직
        startWork(intent, flags, startId);
    }

    /**
     * 서비스를 중지하고 예정된 깨우기를 취소하십시오.
     *
     * 서비스 중지는 Context.stopService (intent name)를 호출하는 대신 가입을 취소하여 수행됩니다. 왜냐하면
     * 1..stopService는 Service.onDestroy ()를 호출하고 AbsWorkService는 연결 유지 처리를 수행하고 서비스를 다시 가져옵니다
     * 2. 우리는 AbsWorkService가 콘솔과 같은 역할을하도록합니다. 즉, AbsWorkService가 항상 실행됩니다 (작업 실행 여부와 관계 없음).，
     * 대신 onStart ()에 정의 된 조건에 따라 서비스 시작 또는 중지 여부가 결정됩니다.
     */
    void stopService(Intent intent, int flags, int startId) {
        //작업 구독 취소
        stopWork(intent, flags, startId);
        // Job / Alarm / Subscription 취소
        cancelJobAlarmSub();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return onStart(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        onStart(intent, 0, 0);
        return onBind(intent, null);
    }

    protected void onEnd(Intent rootIntent) {
        onServiceKilled(rootIntent);
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

    public static class WorkNotificationService extends Service {

        /**
         * API 레벨 18 이상인 Android 시스템에서 알림없이 프론트 엔드 서비스 사용
         */
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(AbsWorkService.HASH_CODE, new Notification());
            stopSelf();
            return START_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
}

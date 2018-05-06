package com.xdandroid.hellodaemon;

import android.content.*;

public class WakeUpReceiver extends BroadcastReceiver {

    /**
     * 서비스가 필요하지 않을 때  Job / Alarm / Subscription.을 취소하려면이 작업과 함께 브로드 캐스트를 WakeUpReceiver로 보내십시오.
     */
    protected static final String ACTION_CANCEL_JOB_ALARM_SUB = "com.xdandroid.hellodaemon.CANCEL_JOB_ALARM_SUB";

    /**
     * 8 개의 시스템 브로드 캐스트 청취 :
     * CONNECTIVITY\_CHANGE, USER\_PRESENT, ACTION\_POWER\_CONNECTED, ACTION\_POWER\_DISCONNECTED,
     * BOOT\_COMPLETED, MEDIA\_MOUNTED, PACKAGE\_ADDED, PACKAGE\_REMOVED.
     * 네트워크 연결이 변경되면 소프트웨어 패키지를 설치 / 제거 할 때 사용자 화면이 잠기지 않고 전원이 연결 / 연결 해제되고 시스템 시동이 완료되고 SD 카드가 마운트되며 서비스가 끌어 올려집니다.
     * 이 서비스는 내부적으로 Service가 이미 실행 중이면 다시 시작되지 않는다고 결정합니다.
     * Run in : 시계 서브 프로세스.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && ACTION_CANCEL_JOB_ALARM_SUB.equals(intent.getAction())) {
            WatchDogService.cancelJobAlarmSub();
            return;
        }
        if (!DaemonEnv.sInitialized) return;
        DaemonEnv.startServiceMayBind(DaemonEnv.sServiceClass);
    }

    public static class WakeUpAutoStartReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!DaemonEnv.sInitialized) return;
            DaemonEnv.startServiceMayBind(DaemonEnv.sServiceClass);
        }
    }
}

package com.xdandroid.sample;

import android.content.*;
import android.os.*;

import com.xdandroid.hellodaemon.*;

import java.util.concurrent.*;

import io.reactivex.*;
import io.reactivex.disposables.*;

public class TraceServiceImpl extends AbsWorkService {

    //임무가 완성되지 않았는데, 더 이상 서비스 운행이 필요 없습니까?
    public static boolean sShouldStopService;
    public static Disposable sDisposable;

    public static void stopService() {
        //우리는 이제 더 이상 서비스 운행이 필요치 않습니다. 로고 위치를 표시합니다. true
        sShouldStopService = true;
        //일과 관련된 구독을 취소하다
        if (sDisposable != null) sDisposable.dispose();
        //취소 Job / Alarm / Subscription
        cancelJobAlarmSub();
    }

    /**
     * 임무 완수 여부, 서비스 실행이 필요 없나요?
     * @return 서비스를 중단해야 한다, true; 서비스를 시작해야 한다, false; 아무것도 하지 않고, 아무것도 하지 않고, null.
     */
    @Override
    public Boolean shouldStopService(Intent intent, int flags, int startId) {
        return sShouldStopService;
    }

    @Override
    public void startWork(Intent intent, int flags, int startId) {
        System.out.println("검사 디스켓에 저장된 데이터가 있는지 없는지 검사하시오");
        sDisposable = Observable
                .interval(3, TimeUnit.SECONDS)
                //취소 시에는 타이머를 취소하고
                .doOnDispose(() -> {
                    System.out.println("데이터를 저장 디스켓에 저장한다.。");
                    cancelJobAlarmSub();
                })
                .subscribe(count -> {
                    System.out.println("매 3초마다 데이터를 수집합니다.... count = " + count);
                    if (count > 0 && count % 18 == 0) System.out.println("데이터를 저장 디스켓에 저장한다.。 saveCount = " + (count / 18 - 1));
                });
    }

    @Override
    public void stopWork(Intent intent, int flags, int startId) {
        stopService();
    }

    /**
     * 임무는 지금 실행 중입니까?
     * @return 임무가 한창 작동 중이다., true; 임무는 현재 운행 중이다., false;판단할 수 없고 아무것도 하지 않는다 null.
     */
    @Override
    public Boolean isWorkRunning(Intent intent, int flags, int startId) {
        //아직까지 구독을 취소하지 않았다면 임무를 수행하고 있음을 설명할 수 있다.
        return sDisposable != null && !sDisposable.isDisposed();
    }

    @Override
    public IBinder onBind(Intent intent, Void v) {
        return null;
    }

    @Override
    public void onServiceKilled(Intent rootIntent) {
        System.out.println("데이터를 저장 디스켓에 저장한다。");
    }
}

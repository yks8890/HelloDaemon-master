package com.xdandroid.sample;

import android.app.*;
import android.os.*;
import android.view.*;

import com.xdandroid.hellodaemon.*;

public class MainActivity extends Activity {

    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                TraceServiceImpl.sShouldStopService = false;
                DaemonEnv.startServiceMayBind(TraceServiceImpl.class);
                break;
            case R.id.btn_white:
                IntentWrapper.whiteListMatters(this, "궤적 추적 서비스의 지속적인 운행");
                break;
            case R.id.btn_stop:
                TraceServiceImpl.stopService();
                break;
        }
    }

    //화웨이가 화이트 리스트에 들어가지 않을 때는 버튼을 누르고 테이블에 올라간 뒤 몇초간 진행되는 것을 방지하기 위해 몇초간 진행된다
    public void onBackPressed() {
        IntentWrapper.onBackPressed(this);
    }
}

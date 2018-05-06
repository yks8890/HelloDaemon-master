package com.xdandroid.hellodaemon;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.support.annotation.NonNull;

import java.util.*;

public class IntentWrapper {

    //Android 7.0+ Doze Mode
    protected static final int DOZE = 98;
    //HUAWEI 자체 관리 시작
    protected static final int HUAWEI = 99;
    //HUAWEI 잠금 화면 청소
    protected static final int HUAWEI_GOD = 100;
    //XIAOMI self start 관리
    protected static final int XIAOMI = 101;
    //XIAOMI_GOD 숨겨진 모드
    protected static final int XIAOMI_GOD = 102;
    //삼성 5.0 / 5.1 자체 응용 프로그램 관리
    protected static final int SAMSUNG_L = 103;
    //MEIZU  자기 관리 시작
    protected static final int MEIZU = 104;
    //MEIZU_GOD 대기 전력 소비 관리
    protected static final int MEIZU_GOD = 105;
    //Oppo 자체 시작 관리
    protected static final int OPPO = 106;
    //Samsung 6.0+ 감독되지 않은 애플리케이션 관리
    protected static final int SAMSUNG_M = 107;
    //Oppo 자체 시작 관리 (이전 버전 시스템)
    protected static final int OPPO_OLD = 108;
    //Vivo 백그라운드에서 높은 전력 소비
    protected static final int VIVO_GOD = 109;
    //GIONEE 응용 프로그램
    protected static final int GIONEE = 110;
    //LeTV 자체 시작 관리
    protected static final int LETV = 111;
    //LeTV 애플리케이션 보호
    protected static final int LETV_GOD = 112;
    //COOLPAD  자체 시작 관리
    protected static final int COOLPAD = 113;
    //Lenovo 백그라운드 관리
    protected static final int LENOVO = 114;
    //Lenovo 백그라운드 전력 소비 최적화
    protected static final int LENOVO_GOD = 115;
    //ZTE 자체 시작 관리
    protected static final int ZTE = 116;
    //ZTE  잠금 화면으로 보호 된 애플리케이션 가속화
    protected static final int ZTE_GOD = 117;

    protected static List<IntentWrapper> sIntentWrapperList;

    public static List<IntentWrapper> getIntentWrapperList() {
        if (sIntentWrapperList == null) {

            if (!DaemonEnv.sInitialized) return new ArrayList<>();

            sIntentWrapperList = new ArrayList<>();

            //Android 7.0+ Doze Mode
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                PowerManager pm = (PowerManager) DaemonEnv.sApp.getSystemService(Context.POWER_SERVICE);
                boolean ignoringBatteryOptimizations = pm.isIgnoringBatteryOptimizations(DaemonEnv.sApp.getPackageName());
                if (!ignoringBatteryOptimizations) {
                    Intent dozeIntent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    dozeIntent.setData(Uri.parse("package:" + DaemonEnv.sApp.getPackageName()));
                    sIntentWrapperList.add(new IntentWrapper(dozeIntent, DOZE));
                }
            }

            //HUAWEI 자체 관리 시작
            Intent huaweiIntent = new Intent();
            huaweiIntent.setAction("huawei.intent.action.HSM_BOOTAPP_MANAGER");
            sIntentWrapperList.add(new IntentWrapper(huaweiIntent, HUAWEI));

            //HUAWEI 화면 잠금
            Intent huaweiGodIntent = new Intent();
            huaweiGodIntent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
            sIntentWrapperList.add(new IntentWrapper(huaweiGodIntent, HUAWEI_GOD));

            //xiaomi 자체 시작 관리
            Intent xiaomiIntent = new Intent();
            xiaomiIntent.setAction("miui.intent.action.OP_AUTO_START");
            xiaomiIntent.addCategory(Intent.CATEGORY_DEFAULT);
            sIntentWrapperList.add(new IntentWrapper(xiaomiIntent, XIAOMI));

            //xiaomi 숨겨진 모드
            Intent xiaomiGodIntent = new Intent();
            xiaomiGodIntent.setComponent(new ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"));
            xiaomiGodIntent.putExtra("package_name", DaemonEnv.sApp.getPackageName());
            xiaomiGodIntent.putExtra("package_label", getApplicationName());
            sIntentWrapperList.add(new IntentWrapper(xiaomiGodIntent, XIAOMI_GOD));

            //Samsung 5.0 / 5.1 자체 응용 프로그램 관리
            Intent samsungLIntent = DaemonEnv.sApp.getPackageManager().getLaunchIntentForPackage("com.samsung.android.sm");
            if (samsungLIntent != null)
                sIntentWrapperList.add(new IntentWrapper(samsungLIntent, SAMSUNG_L));

            //Samsung 6.0+ 감독되지 않은 애플리케이션 관리
            Intent samsungMIntent = new Intent();
            samsungMIntent.setComponent(new ComponentName("com.samsung.android.sm_cn", "com.samsung.android.sm.ui.battery.BatteryActivity"));
            sIntentWrapperList.add(new IntentWrapper(samsungMIntent, SAMSUNG_M));

            //Meizu 자기 관리 시작
            Intent meizuIntent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
            meizuIntent.addCategory(Intent.CATEGORY_DEFAULT);
            meizuIntent.putExtra("packageName", DaemonEnv.sApp.getPackageName());
            sIntentWrapperList.add(new IntentWrapper(meizuIntent, MEIZU));

            //Meizu 대기 전원 관리
            Intent meizuGodIntent = new Intent();
            meizuGodIntent.setComponent(new ComponentName("com.meizu.safe", "com.meizu.safe.powerui.PowerAppPermissionActivity"));
            sIntentWrapperList.add(new IntentWrapper(meizuGodIntent, MEIZU_GOD));

            //Oppo 자체 시작 관리
            Intent oppoIntent = new Intent();
            oppoIntent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
            sIntentWrapperList.add(new IntentWrapper(oppoIntent, OPPO));

            //Oppo 자체 시작 관리 (이전 버전 시스템)
            Intent oppoOldIntent = new Intent();
            oppoOldIntent.setComponent(new ComponentName("com.color.safecenter", "com.color.safecenter.permission.startup.StartupAppListActivity"));
            sIntentWrapperList.add(new IntentWrapper(oppoOldIntent, OPPO_OLD));

            //Vivo   백그라운드에서 높은 전력 소비
            Intent vivoGodIntent = new Intent();
            vivoGodIntent.setComponent(new ComponentName("com.vivo.abe", "com.vivo.applicationbehaviorengine.ui.ExcessivePowerManagerActivity"));
            sIntentWrapperList.add(new IntentWrapper(vivoGodIntent, VIVO_GOD));

            //gionee 응용 프로그램
            Intent gioneeIntent = new Intent();
            gioneeIntent.setComponent(new ComponentName("com.gionee.softmanager", "com.gionee.softmanager.MainActivity"));
            sIntentWrapperList.add(new IntentWrapper(gioneeIntent, GIONEE));

            //LeTV 자체 시작 관리
            Intent letvIntent = new Intent();
            letvIntent.setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"));
            sIntentWrapperList.add(new IntentWrapper(letvIntent, LETV));

            //LeTV 애플리케이션 보호
            Intent letvGodIntent = new Intent();
            letvGodIntent.setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.BackgroundAppManageActivity"));
            sIntentWrapperList.add(new IntentWrapper(letvGodIntent, LETV_GOD));

            //coolpad 자체 시작 관리
            Intent coolpadIntent = new Intent();
            coolpadIntent.setComponent(new ComponentName("com.yulong.android.security", "com.yulong.android.seccenter.tabbarmain"));
            sIntentWrapperList.add(new IntentWrapper(coolpadIntent, COOLPAD));

            //Lenovo 백그라운드 관리
            Intent lenovoIntent = new Intent();
            lenovoIntent.setComponent(new ComponentName("com.lenovo.security", "com.lenovo.security.purebackground.PureBackgroundActivity"));
            sIntentWrapperList.add(new IntentWrapper(lenovoIntent, LENOVO));

            //Lenovo 백그라운드 전력 최적화
            Intent lenovoGodIntent = new Intent();
            lenovoGodIntent.setComponent(new ComponentName("com.lenovo.powersetting", "com.lenovo.powersetting.ui.Settings$HighPowerApplicationsActivity"));
            sIntentWrapperList.add(new IntentWrapper(lenovoGodIntent, LENOVO_GOD));

            //zte 자체 시작 관리
            Intent zteIntent = new Intent();
            zteIntent.setComponent(new ComponentName("com.zte.heartyservice", "com.zte.heartyservice.autorun.AppAutoRunManager"));
            sIntentWrapperList.add(new IntentWrapper(zteIntent, ZTE));

            //zte 잠금 화면으로 보호 된 애플리케이션 가속화
            Intent zteGodIntent = new Intent();
            zteGodIntent.setComponent(new ComponentName("com.zte.heartyservice", "com.zte.heartyservice.setting.ClearAppSettingsActivity"));
            sIntentWrapperList.add(new IntentWrapper(zteGodIntent, ZTE_GOD));
        }
        return sIntentWrapperList;
    }

    protected static String sApplicationName;

    public static String getApplicationName() {
        if (sApplicationName == null) {
            if (!DaemonEnv.sInitialized) return "";
            PackageManager pm;
            ApplicationInfo ai;
            try {
                pm = DaemonEnv.sApp.getPackageManager();
                ai = pm.getApplicationInfo(DaemonEnv.sApp.getPackageName(), 0);
                sApplicationName = pm.getApplicationLabel(ai).toString();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                sApplicationName = DaemonEnv.sApp.getPackageName();
            }
        }
        return sApplicationName;
    }

    /**
     * 화이트리스트 처리.
     *
     * @return  박스형 IntentWrapper.
     */
    @NonNull
    public static List<IntentWrapper> whiteListMatters(final Activity a, String reason) {
        List<IntentWrapper> showed = new ArrayList<>();
        if (reason == null) reason = "핵심 서비스의 지속적인 운영";
        List<IntentWrapper> intentWrapperList = getIntentWrapperList();
        for (final IntentWrapper iw : intentWrapperList) {
            //시스템에서이 인 텐트를 처리 할 수있는 Activity가 없다면 해당 모델이 아니며 다음 사이클을 직접 입력하는 것을 무시합니다.
            if (!iw.doesActivityExists()) continue;
            switch (iw.type) {
                case DOZE:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        PowerManager pm = (PowerManager) a.getSystemService(Context.POWER_SERVICE);
                        if (pm.isIgnoringBatteryOptimizations(a.getPackageName())) break;
                        new AlertDialog.Builder(a)
                                .setCancelable(false)
                                .setTitle("무시할 필요가있다. " + getApplicationName() + " 배터리 최적화")
                                .setMessage(reason + "욕구 " + getApplicationName() + " 배터리 최적화 무시 목록에 추가됨。\n\n" +
                                        "OK 를 클릭하십시오. Battery Optimization 무시 대화 상자에서 예를 선택하십시오")
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface d, int w) {
                                        iw.startActivitySafely(a);
                                    }
                                })
                                .show();
                        showed.add(iw);
                    }
                    break;
                case HUAWEI:
                    new AlertDialog.Builder(a)
                            .setCancelable(false)
                            .setTitle("허가가 필요하다. " + getApplicationName() + " 자동 시작")
                            .setMessage(reason + "허가가 필요하다. " + getApplicationName() + " 자동 시작。\n\n" +
                                    "OK를 클릭하십시오 팝업 self-start manager에서 " + getApplicationName() + " 해당 스위치가 켜져 있습니다.。")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    iw.startActivitySafely(a);
                                }
                            })
                            .show();
                    showed.add(iw);
                    break;
                case ZTE_GOD:
                case HUAWEI_GOD:
                    new AlertDialog.Builder(a)
                            .setCancelable(false)
                            .setTitle(getApplicationName() + " 화이트리스트를 정리하기 위해 잠금 화면에 가입해야합니다.")
                            .setMessage(reason + "욕구 " + getApplicationName() + "잠금 화면에 가입하여 화이트리스트 목록을 정리하십시오。\n\n" +
                                    "확인을 클릭하여 팝업 잠금 화면 정리 목록에서 " + getApplicationName() + " 해당 스위치가 켜져 있습니다.。")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    iw.startActivitySafely(a);
                                }
                            })
                            .show();
                    showed.add(iw);
                    break;
                case XIAOMI_GOD:
                    new AlertDialog.Builder(a)
                            .setCancelable(false)
                            .setTitle("닫을 필요가있다 " + getApplicationName() + " 숨겨진 모드")
                            .setMessage(reason + "닫을 필요가있다. " + getApplicationName() + " 숨겨진 모드。\n\n" +
                                    "팝업 창에서 확인을 클릭하십시오 " + getApplicationName() + " 숨김 모드설정에서 무제한을 선택한 다음 위치 지정 허용을 선택하십시오")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    iw.startActivitySafely(a);
                                }
                            })
                            .show();
                    showed.add(iw);
                    break;
                case SAMSUNG_L:
                    new AlertDialog.Builder(a)
                            .setCancelable(false)
                            .setTitle("허가가 필요하다. " + getApplicationName() + " 자기 출발")
                            .setMessage(reason + "욕구 " + getApplicationName() + " 화면이 꺼지면 계속 실행하십시오。\n\n" +
                                    "확인을 클릭하십시오 스마트관리자가 나타나면 메모리를 클릭하고 응용 프로그램 시작탭을 선택하십시오" + getApplicationName() + " 해당 스위치가 켜져 있습니다。")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    iw.startActivitySafely(a);
                                }
                            })
                            .show();
                    showed.add(iw);
                    break;
                case SAMSUNG_M:
                    new AlertDialog.Builder(a)
                            .setCancelable(false)
                            .setTitle("허가가 필요하다." + getApplicationName() +
                                    "자기 출발")
                            .setMessage(reason + "욕구 " + getApplicationName() +
                                    "화면이 꺼지면 계속 실행하십시오.。\n\n" +
                                    "확인을 클릭하십시오 배터리 페이지가 나타나면 모니터링되지 않는 응용프로그램 -> 응용 프로그램 추가를 클릭하십시오" + getApplicationName() + " 마침을 클릭하십시오.")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    iw.startActivitySafely(a);
                                }
                            })
                            .show();
                    showed.add(iw);
                    break;
                case MEIZU:
                    new AlertDialog.Builder(a)
                            .setCancelable(false)
                            .setTitle(
                                    "허가가 필요하다. " + getApplicationName() + " 백그라운드에서 계속 실행")
                            .setMessage(reason + "욕구 " + getApplicationName() + "백그라운드에서 계속 실행 \n\n" +
                                    "팝업 응용 프로그램 정보 인터페이스에서 백그라운드 관리 옵션을 백그라운드 실행 유지로 변경하려면 확인을 클릭하십시오.")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    iw.startActivitySafely(a);
                                }
                            })
                            .show();
                    showed.add(iw);
                    break;
                case MEIZU_GOD:
                    new AlertDialog.Builder(a)
                            .setCancelable(false)
                            .setTitle(getApplicationName() + " 대기 모드에서 계속 실행해야합니다.")
                            .setMessage(reason + "욕구 " + getApplicationName() + " 대기 모드에서 계속 실행하십시오.。\n\n" +
                                    "팝업되는 Standby Power Consumption Management 에서 OK를 클릭하십시오 " + getApplicationName() + " 해당 스위치가 켜져 있습니다.。")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    iw.startActivitySafely(a);
                                }
                            })
                            .show();
                    showed.add(iw);
                    break;
                case ZTE:
                case LETV:
                case XIAOMI:
                case OPPO:
                case OPPO_OLD:
                    new AlertDialog.Builder(a)
                            .setCancelable(false)
                            .setTitle(
                                    "허가가 필요하다. " + getApplicationName() + " 자기 출발")
                            .setMessage(reason + "욕구 " + getApplicationName() +
                                    "자체 시작 허용 목록에 가입。\n\n" +
                                    "확인을 클릭하십시오 시작관리 대화 상자가 나타나면 " + getApplicationName() + " 해당 스위치가 켜져 있습니다.。")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    iw.startActivitySafely(a);
                                }
                            })
                            .show();
                    showed.add(iw);
                    break;
                case COOLPAD:
                    new AlertDialog.Builder(a)
                            .setCancelable(false)
                            .setTitle("허가가 필요하다. " + getApplicationName() + " 자기 출발")
                            .setMessage(reason + "욕구 " + getApplicationName() + " 자기 출발。\n\n" +
                                    "OK를 클릭하십시오 팝업 coolhouskeeper 에서 Software Management -> Startup Management를 찾아" + getApplicationName() + "，윌 " + getApplicationName() + " 상태가 허용됨으로 변경됩니다.")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    iw.startActivitySafely(a);
                                }
                            })
                            .show();
                    showed.add(iw);
                    break;
                case VIVO_GOD:
                    new AlertDialog.Builder(a)
                            .setCancelable(false)
                            .setTitle(
                                    "허가가 필요하다. " + getApplicationName() + " 백그라운드에서 달리기")
                            .setMessage(reason + "욕구 " + getApplicationName() + " 백그라운드에서 높은 전력 소비로 실행됩니다。\n\n" +
                                    "OK를 클릭하십시오. 팝업 Background High Power Consumption에서 " + getApplicationName() +
                                    "해당 스위치가 켜져 있습니다.。")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    iw.startActivitySafely(a);
                                }
                            })
                            .show();
                    showed.add(iw);
                    break;
                case GIONEE:
                    new AlertDialog.Builder(a)
                            .setCancelable(false)
                            .setTitle(getApplicationName() +
                                    "Kai와 Green Background 화이트리스트의 신청서에 가입해야합니다.")
                            .setMessage(reason + "욕구 " + getApplicationName() +
                                    "자동 시작 및 백그라운드 실행。\n\n" +
                                    "확인을 클릭하십시오 시스템 관리자에서 응용 프로그램 관리 -> 응용 프로그램 및 녹생 배경 -> 화이트리스트 지우기를 찾으십시오 " + getApplicationName() + " 화이트리스트에 추가。")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    iw.startActivitySafely(a);
                                }
                            })
                            .show();
                    showed.add(iw);
                    break;
                case LETV_GOD:
                    new AlertDialog.Builder(a)
                            .setCancelable(false)
                            .setTitle("금지되어야한다 " + getApplicationName() + " 자동 정리")
                            .setMessage(reason + "욕구 " + getApplicationName() +
                                    "자동 정리。\n\n" +
                                    "응용 프로그램 보호 팝업에서 확인을 클릭하십시오" + getApplicationName() + " 해당 스위치가 꺼져 있습니다.。")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    iw.startActivitySafely(a);
                                }
                            })
                            .show();
                    showed.add(iw);
                    break;
                case LENOVO:
                    new AlertDialog.Builder(a)
                            .setCancelable(false)
                            .setTitle(
                                    "허가가 필요하다." + getApplicationName() +
                                            "백그라운드에서 달리기")
                            .setMessage(reason + "욕구 " + getApplicationName() + " 백그라운드 셀프 스타트, 백그라운드 GPS 및 백그라운드 실행.\n\n" +
                                    "OK를 클릭하십시오. 백그라운드 관리에서 백그라운드 자체 , 백그라운드 GPS 및 백그라운드를 찾으십시오" + getApplicationName() + "해당 스위치가 켜져 있습니다")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    iw.startActivitySafely(a);
                                }
                            })
                            .show();
                    showed.add(iw);
                    break;
                case LENOVO_GOD:
                    new AlertDialog.Builder(a)
                            .setCancelable(false)
                            .setTitle(
                                    "닫을 필요가있다. " + getApplicationName() +
                                            "백엔드 전력 소비 최적화")
                            .setMessage(reason + "욕구 " + getApplicationName() + " 백엔드 전력 소비 최적화。\n\n" +
                                    "Background Power Consumption Optimization 팝업 메뉴에서 OK를 클릭하십시오 " + getApplicationName() + " 해당 스위치가 꺼져 있습니다.。")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int w) {
                                    iw.startActivitySafely(a);
                                }
                            })
                            .show();
                    showed.add(iw);
                    break;
            }
        }
        return showed;
    }

    /**
     * Huawei 모델이 허용 목록에 포함되지 않도록하고 뒤로 키를 눌러 바탕 화면으로 돌아가서 프로세스가 종료 된 후 몇 초 동안 화면을 잠급니다.
     */
    public static void onBackPressed(Activity a) {
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_HOME);
        a.startActivity(launcherIntent);
    }

    protected Intent intent;
    protected int type;

    protected IntentWrapper(Intent intent, int type) {
        this.intent = intent;
        this.type = type;
    }

    /**
     * 현재 Intent를 처리 할 수 있는 작업이 Activity에 있는지 확인
     */
    protected boolean doesActivityExists() {
        if (!DaemonEnv.sInitialized) return false;
        PackageManager pm = DaemonEnv.sApp.getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list != null && list.size() > 0;
    }

    /**
     * Activity 안전하게 시작
     */
    protected void startActivitySafely(Activity activityContext) {
        try {
            activityContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

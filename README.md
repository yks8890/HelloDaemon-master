# HelloDaemon
### Android 서비스 유지/상주 (Android Service Daemon)

#### 앱의 핵심 기능만 유지할 수 있도록 유지하는 것이 권장됩니다.

#### 이 예제에서 사용 된 연결 유지 방법은 아래의 블로그 및 라이브러리에서 부분적으로 파생됩니다. D-clock의 Android DaemonService로부터 알림을 표시하지 않고 포어 그라운드 서비스를 시작하고 다른 비 기본 레이어 Keepalive 메소드를 구현합니다.

[안드로이드 프로세스 거주자 (2) - 안드로이드 시스템 메커니즘의 자세한 사용 방법을 유지](http://blog.csdn.net/marswin89/article/details/50890708)

[D-clock / AndroidDaemonService](https://github.com/D-clock/AndroidDaemonService)

##위의 2 개의 링크 대부분이 보관됩니다 :

#### 1.프론트 데스크 서비스로 알림 표시없이 서비스 설정

> D-clock :  
>  
아이디어 1 : API <18, 전경 서비스 시작시 직접 수신되는 new Notification()；
>   
아이디어 2 : API >= 18, 그리고 같은 id를 가진 두 개의 전경 서비스를 동시에 시작한 다음, 나중에 시작된 서비스를 멈추십시오.

//알림을 표시하지 않고 프론트 데스크 서비스를 시작한 취약점은 API 레벨 25에서 수정되었으며 모두가 만족합니다!

프론트 데스크 서비스가 백그라운드 서비스보다 우선 순위의 향상 외에도 다음과 같은 장점이 있습니다.

최근 작업 목록에서 카드가 초과되면 프론트 데스크 서비스가 중단되지 않습니다.

(업데이트 : 테스트 후 AOSP / CM / International이 Framework 계층을 사소하게 변경 한 것은 Android 시스템에서만 발견되었습니다.；
EMUI / MIUI가 화이트리스트에 추가되지 않으면 카드가 횡령되고 프론트 데스크 서비스가 중단됩니다. 화이트리스트를 추가 한 후에 크로스 카드의 동작은 국제 제조업체의 시스템과 유사합니다.)

백그라운드 서비스가 중지되고 나중에 다시 시작됩니다 (onStartCommand는 START_STICKY를 반환합니다).

전경 서비스와 배경 서비스가 교차되면 콜백은 onTaskRemoved 메소드에 있습니다.


onDestroy 메소드는 설정 -> 개발자 옵션 -> 실행중인 서비스에서 서비스가 중지 된 경우에만 다시 호출됩니다.

#### 2. 서비스의 onStartCommand 메소드에서 START_STICKY를 반환합니다.

#### 3. 서비스의 onDestroy / onTaskRemoved 메소드를 덮어 쓰고 데이터를 디스크에 저장 한 다음 서비스를 다시 시작하십시오.

#### 4. 8 개의 시스템 브로드 캐스트 청취 :

CONNECTIVITY\_CHANGE, USER\_PRESENT, ACTION\_POWER\_CONNECTED, ACTION\_POWER\_DISCONNECTED, BOOT\_COMPLETED, PACKAGE\_ADDED, PACKAGE\_REMOVED.

네트워크 연결이 변경되면 소프트웨어 패키지를 설치 / 제거 할 때 사용자 화면의 잠금이 해제되고 전원이 연결 / 연결 해제되고 시스템 시동이 완료되고 서비스가 끌어 올려집니다.

이 서비스는 내부적으로 Service가 이미 실행 중이면 다시 시작되지 않는다고 결정합니다.

#### 5. 데몬 서비스를 켜십시오 : 서비스가 실행 중인지 확인하십시오. 실행 중이 아니면 실행하십시오.

#### 6. 데몬은 구성 요소의 활성화 된 상태를 감시하여 MAT와 같은 도구에 의해 비활성화되지 않도록합니다


자세한 내용은 위의 2 개의 링크를 참조하십시오.

## 구현 증가 :

#### \+ 데몬 서비스 : Android 5.0 이상은 JobScheduler를 사용하며 AlarmManager보다 우수합니다.


JobScheduler를 사용하면 안드로이드 시스템이 자동으로 Force Stop 패키지를 가져올 수 있지만 AlarmManager는 풀업을 할 수 없습니다.

Android 4.4 이하는 AlarmManager를 사용합니다.

#### \+ 사용 시간 Observable : Android 사용자 정의 시스템 사용 안 함 JobScheduler / AlarmManager 깨우기 간격이 안정적이지 않음

#### \+서비스를 중지하고 시간 초과 된 웨이크 업을 취소하는 빠른 방법을 추가하십시오.

#### \+ 서비스가 필요하지 않을 때 Job / Alarm / Subscription 을 취소하는 바로 가기 추가 (브로드 캐스트 작업)

#### \+ 국내 모델의 적응력 향상 : Huawei 모델이 돌아 오는 키를 바탕 화면으로 되돌려 놓고 프로세스가 종료 된 후 몇 초 동안 화면을 잠그지 않도록합니다.

테스트 모델 : Huawei Glory 6 Plus (EMUI 4.0 Android 6.0)에서는 응용 프로그램이 허용 목록에 추가되지 않습니다.

>
관찰 된 :
>  
허용 목록이 없으면 뒤로 단추를 눌러 바탕 화면으로 돌아간 다음 화면을 잠그면 몇 초 후에 프로세스가 종료됩니다.；
>  

그러나 바탕 화면으로 돌아가려면 홈 단추를 눌러도 화면이 잠겨 있어도 프로세스가 종료되지 않습니다.

(
업데이트 : 테스트 후, EMUI 시스템에서 화면이 잠겨 있어도 프로세스가 종료되지는 않습니다. 응용 프로그램 용 카드 만 멀티 태스킹 화면의 첫 번째 화면에서 유효합니다. 두 번째 페이지 이상으로 푸시되면 잠겨집니다. 화면이 프로세스를 죽일 몇 초 후, 화이트리스트에 가입 한 후 바탕 화면으로 돌아온 다음 화면을 잠그면 프로세스가 종료되지 않습니다)

따라서 onBackPressed 메서드는 현재 Activity finish / destroy 대신 바탕 화면으로 만 반환되도록 다시 작성됩니다.

테스트 모델 : Redmi 1S 4G (MIUI 8 Android 4.4.2)는 응용 프로그램이 허용 목록에 추가되지 않습니다.

>  

관찰 된 ::
>  

화이트리스트가 없으면 바탕 화면으로 돌아가서 화면을 잠그면 프로세스가 종료되지 않습니다.
>  

그러나 카드가 잘린 경우 프로세스가 종료되어 더 이상 시작되지 않으며 허용 목록을 추가하면 카드가 제거되고 서비스가 중지되지 않습니다. 이것은 CM의 동작과 유사합니다.

네이티브 킵 얼라이브를 사용하지 않으려는 경우 사용자를 허용 목록으로 유도하는 것이 더 적합한 방법 일 수 있습니다

#### \+  Intent 사용

- Android Doze 패턴
- 화웨이 자체 관리
- 화웨이 잠금 화면 정리
- 밀레 셀프 스타트 관리
- 기장 숨겨진 모드
- 삼성 5.0 / 5.1 자체 실행 응용 프로그램 관리
- Samsung 6.0+ 감독되지 않은 응용 프로그램 관리
Meizu 자기 관리 시작
Meizu 대기 전원 관리
- Oppo 자체 시작 관리
- Vivo의 배경에서 높은 전력 소비
- Kai의 Jin Li 응용 프로그램
- 진리 그린 무대 뒤
- LeTV 셀프 스타트 관리
- LeTV 애플리케이션 보호
- 쿨 스타트 관리
- Lenovo 백그라운드 관리
- Lenovo 백그라운드 전력 최적화
- ZTE 자동화 관리
- ZTE 잠금 화면으로 보호 된 애플리케이션 가속화

android.support.v7.AlertDialog를 사용하여 사용자가 앱을 허용 목록에 추가하도록 안내합니다.

#### \+ 데몬 서비스와 BroadcastReceiver는 : watch 서브 프로세스에서 실행되고, 메인 프로세스와 구분된다.

#### \+ AIDL 또는 다른 IPC 방법을 사용하여 서비스와 통신 할 필요가 없도록 작업 프로세스가 주 프로세스에서 실행됩니다.


Poweramp를 참조하면, 시작된 포 그라운드 서비스는 UI와 동일한 프로세스에서 실행됩니다.

#### \+ 반복되는 시작 서비스를 방지하기위한 프로세스를 수행하고, 임의로 startService(Intent i)


서비스가 아직 실행 중이면 아무 작업도하지 말고 서비스가 실행 중이 아니면 위로。

#### \+ 하위 스레드에서 예약 된 작업 실행, 사전 실행 검사 및 파괴 중에 저장된 문제 처리


작업을 시작하기 전에 먼저 디스크가 마지막으로 삭제되는 동안 저장된 데이터를 확인하고 주기적으로 디스크에 저장하십시오

## 소개하다

### 1. 바이너리 추가

build.gradle 추가

```
compile 'com.xdandroid:hellodaemon:+'
```

### 2. 상속 된 AbsWorkService, 6 개의 추상 메서드 구현

```
/**
 * 작업이 완료되면 서비스 작업이 필요하지 않습니까?
 * @return 서비스를 중지해야합니다., true; 서비스를 시작해야합니다., false; 판단 할 수 없다, null.
 */
Boolean shouldStopService();

/**
 * 작업이 실행 중입니까?
 * @return 작업이 실행 중입니다., true; 작업이 현재 실행되고 있지 않습니다., false; 판단 할 수 없다., null.
 */
Boolean isWorkRunning();

void startWork();

void stopWork();

//Service.onBind(Intent intent)
@Nullable IBinder onBind(Intent intent, Void unused);

//서비스가 종료되면 호출되어 데이터를 저장할 수 있습니다.
void onServiceKilled();
```

Manifest에 서비스를 등록하는 것을 잊지 마십시오.

### 3. Application 사용자 정의


응용 프로그램의 onCreate ()에서 호출 됨

```
DaemonEnv.initialize(
  Context app,  //Application Context.
  Class<? extends AbsWorkService> serviceClass, //방금 작성한 서비스의 Class 오브젝트
  @Nullable Integer wakeUpInterval);  //타이밍 웨이크 업 간격 (ms), 기본값은 6 분입니다.

Context.startService(new Intent(Context app, Class<? extends AbsWorkService> serviceClass));
```

Manifest에서 android : name을 통해 이 사용자 정의 응용 프로그램을 사용하는 것을 잊지 마십시오.

### 4. API 지침

#### Service 시작 :

```
Context.startService(new Intent(Context c, Class<? extends AbsWorkService> serviceClass))
```

#### Service 중지 :


AbsWorkService를 확장하려면`stopService ()`메소드를 추가하십시오 :

1.`shouldStopService ()`가`true`를 리턴하도록 유지하는 플래그를 조작하십시오;

2. 타사 SDK에서 제공하는 API 나 API를 호출하여 작업을 중지하십시오.

3. ```AbsWorkService.cancelJobAlarmSub ()```을 호출하여 Job / Alarm / Subscription을 취소하십시오.


서비스를 중지하려면 extends AbsWorkService에서`stopService ()`를 호출하십시오.

#### 화이트리스트 처리 :


다음 API는 모두 IntentWrapper에 있습니다. :

```
List<IntentWrapper> getIntentWrapperList();

//사용자가 앱을 허용 목록에 추가하도록 안내하는 android.support.v7.AlertDialog 팝업.
void whiteListMatters(Activity a, String reason);

//Huawei 모델이 화이트리스트에 합류하지 못하도록하려면 뒤로 버튼을 눌러 바탕 화면으로 돌아가서 프로세스가 종료 된 후 몇 초 동안 화면을 잠급니다.
//MainActivity.onBackPressed ()를 재정 의하여 다음 API에 대한 호출 만 남깁니다.
void onBackPressed(Activity a);
```

#### 서비스가 더 이상 필요 없으면 사용자의 전원을 저장하기 위해``AbsWorkService.cancelJobAlarmSub ()```을 호출하여 스케줄 된 웨이크 업 Job / Alarm / Subscription,을 취소하고``stopService () ''를 호출하여 서비스를 중지 할 수 있습니다.

     자세한 내용은 코드 및 메모를 참조하십시오.

package jack.com.servicekeep.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.telephony.SmsMessage;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import jack.com.servicekeep.Constant;
import jack.com.servicekeep.MainActivity;
import jack.com.servicekeep.manager.ServiceManager;
import jack.com.servicekeep.utils.LogUtils;


/**
 * 保活Service
 *
 * @Author Jack
 * @Date 2017/9/26 15:17
 * @Copyright:wanmei.com Inc. All rights reserved.
 */
@TargetApi(21)
public class KeepAliveJobSchedulerService extends JobService {

    private MediaPlayer mp;
    private Vibrator vibrator;
    private SMSReceiver smsReceiver;
    private StartLearnReceiver startLearnReceiver;

    //播放方法
    public void play() {
        if (mp != null) {
            mp.pause();
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    //暂停
    public void pause(MediaPlayer mediaplayer) {
        if (mp != null) {
            mp.start();
        }
        if (vibrator != null) {
            vibrator.vibrate(new long[]{0, 1000000000}, 0);
        }
    }

    public static void openDing(Context context) {

        PackageManager packageManager = context.getPackageManager();
        PackageInfo pi = null;
        try {
            pi = packageManager.getPackageInfo("com.alibaba.android.rimet", 0);
        } catch (PackageManager.NameNotFoundException e) {
        }
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(pi.packageName);
        List<ResolveInfo> apps = packageManager.queryIntentActivities(resolveIntent, 0);
        ResolveInfo resolveInfo = apps.iterator().next();
        if (resolveInfo != null ) {
            String className = resolveInfo.activityInfo.name;
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName cn = new ComponentName("com.alibaba.android.rimet", className);
            intent.setComponent(cn);
            context.startActivity(intent);
        }
    }

    final public Handler startLearnHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            if (mp!= null) {
//                mp.stop();
//            }
//            if (vibrator!= null) {
//                vibrator.cancel();
//            }
        }
    };

    final public Handler smsHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            Object[] smsObj = (Object[]) bundle.get("pdus");
            SmsMessage smsMessage = null;
            JSONObject requestMap = new JSONObject();
            for (Object object : smsObj) {
                smsMessage = SmsMessage.createFromPdu((byte[]) object);
                LogUtils.d(TAG, "address:" + smsMessage.getOriginatingAddress() + "   displayAddress:" + smsMessage.getDisplayOriginatingAddress()
                        + "   body:" + smsMessage.getDisplayMessageBody() + "  time:"
                        + smsMessage.getTimestampMillis());
                if (smsMessage.getDisplayMessageBody().contains("钉钉")) {
                    play();
                }
            }

//            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//            startActivity(intent);
//            if (!phoneNumber.isEmpty() && !account.isEmpty()) {
//                Bundle bundle = msg.getData();
//                Object[] smsObj = (Object[]) bundle.get("pdus");
//                SmsMessage smsMessage = null;
//                JSONObject requestMap = new JSONObject();
//                for (Object object : smsObj) {
//                    smsMessage = SmsMessage.createFromPdu((byte[]) object);
//
//                    Log.i("Drake", "address:" + smsMessage.getOriginatingAddress() + "   displayAddress:" + smsMessage.getDisplayOriginatingAddress()
//                            + "   body:" + smsMessage.getDisplayMessageBody() + "  time:"
//                            + smsMessage.getTimestampMillis());
//                    try {
//                        requestMap.put("phoneNumber", phoneNumber);
//                        requestMap.put("msg", smsMessage.getDisplayMessageBody());
//                        requestMap.put("timeStamp", smsMessage.getTimestampMillis());
//                        AppUtil.upload("http://" + host_port + "/phone_message.go", requestMap.toString());
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
        }
    };

    public class SMSReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO: This method is called when the BroadcastReceiver is receiving
            // an Intent broadcast.
            Bundle bundle = intent.getExtras();
            if (null != bundle) {
                Message message = new Message();
                message.setData(bundle);
                smsHandler.sendMessage(message);
            }
        }
    }

    public class StartLearnReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO: This method is called when the BroadcastReceiver is receiving
            // an Intent broadcast.
            startLearnHandler.sendEmptyMessage(0);

        }
    }

    private JobParameters mJobParameters;
    private final String TAG = "KeepAliveJobSchedulerService";
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            // 返回true，表示该工作耗时，同时工作处理完成后需要调用jobFinished销毁
            LogUtils.d(TAG, "KeepAliveJobSchedulerService ------ onStartJob");
            mJobParameters = (JobParameters) msg.obj;
            if (mJobParameters != null) {
                LogUtils.d(TAG, "onStartJob params ---------- " + mJobParameters);
            }
            //执行需要保活的工作
            ServiceManager.INSTANCE.needKeepAlive(getApplicationContext());
            return true;
        }
    });



    @Override
    public boolean onStartJob(JobParameters params) {
        LogUtils.d(TAG, "KeepAliveJobSchedulerService-----------onStartJob");
        Message message = Message.obtain();
        message.obj = params;
        mHandler.sendMessage(message);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        LogUtils.d(TAG, "KeepAliveJobSchedulerService-----------onStopJob");
        mHandler.removeCallbacksAndMessages(null);
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, "KeepAliveJobSchedulerService ------- is onCreate");
        mp = new MediaPlayer();
        try {
            mp.setDataSource(getApplicationContext(), RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));//这里我用的通知声音，还有其他的，大家可以点进去看
            mp.prepare();
            mp.setLooping(true);
//            mp.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        //取得震动服务的句柄
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        smsReceiver = new SMSReceiver();
        IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        intentFilter.setPriority(2147483647);
        registerReceiver(smsReceiver, intentFilter);

//        startLearnReceiver = new StartLearnReceiver();
//        registerReceiver(startLearnReceiver, new IntentFilter(Constant.NORMAL_ACTION));

//        mp = new MediaPlayer();
//        try {
//            mp.setDataSource(this, RingtoneManager
//                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));//这里我用的通知声音，还有其他的，大家可以点进去看
//            mp.prepare();
//            mp.start();
//            mp.setLooping(true);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        //取得震动服务的句柄
//        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
//        //按照指定的模式去震动。
////				vibrator.vibrate(1000);
//        //数组参数意义：第一个参数为等待指定时间后开始震动，震动时间为第二个参数。后边的参数依次为等待震动和震动的时间
//        //第二个参数为重复次数，-1为不重复，0为一直震动
//        vibrator.vibrate( new long[]{0,100000},0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "KeepAliveJobSchedulerService ------- is onDestroy!!!");
//        if (startLearnReceiver != null) {
//            unregisterReceiver(startLearnReceiver);
//        }
        if (smsReceiver != null) {
            unregisterReceiver(smsReceiver);
        }
//        if (mp!= null) {
//            mp.stop();
//        }
//        if (vibrator!= null) {
//            vibrator.cancel();
//        }
    }
}

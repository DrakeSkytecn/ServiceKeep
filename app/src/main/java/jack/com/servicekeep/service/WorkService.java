package jack.com.servicekeep.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.telephony.SmsMessage;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import jack.com.servicekeep.utils.LogUtils;

/**
 * 需要保活的业务服务
 * 完美世界
 *
 * @Author Jack
 * @Date 2017/11/22 18:02
 * @Copyright:wanmei.com Inc. All rights reserved.
 */
public class WorkService extends Service {

    private static final String TAG = "WorkService";
    private final static String ACTION_START = "action_start";

//    private SMSReceiver smsReceiver;
//    private MediaPlayer mp;
//    private Vibrator vibrator;
//
//    final public Handler smsHandler = new Handler() {
//
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            Bundle bundle = msg.getData();
//            Object[] smsObj = (Object[]) bundle.get("pdus");
//            SmsMessage smsMessage = null;
//            JSONObject requestMap = new JSONObject();
//            for (Object object : smsObj) {
//                smsMessage = SmsMessage.createFromPdu((byte[]) object);
//                LogUtils.d(TAG, "address:" + smsMessage.getOriginatingAddress() + "   displayAddress:" + smsMessage.getDisplayOriginatingAddress()
//                        + "   body:" + smsMessage.getDisplayMessageBody() + "  time:"
//                        + smsMessage.getTimestampMillis());
//            }
//            mp = new MediaPlayer();
//            try {
//                mp.setDataSource(getApplicationContext(), RingtoneManager
//                        .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));//这里我用的通知声音，还有其他的，大家可以点进去看
//                mp.prepare();
//                mp.start();
//                mp.setLooping(true);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            //取得震动服务的句柄
//            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
//            //按照指定的模式去震动。
////				vibrator.vibrate(1000);
//            //数组参数意义：第一个参数为等待指定时间后开始震动，震动时间为第二个参数。后边的参数依次为等待震动和震动的时间
//            //第二个参数为重复次数，-1为不重复，0为一直震动
//            vibrator.vibrate(new long[]{0, 100000}, 0);
////            if (!phoneNumber.isEmpty() && !account.isEmpty()) {
////                Bundle bundle = msg.getData();
////                Object[] smsObj = (Object[]) bundle.get("pdus");
////                SmsMessage smsMessage = null;
////                JSONObject requestMap = new JSONObject();
////                for (Object object : smsObj) {
////                    smsMessage = SmsMessage.createFromPdu((byte[]) object);
////
////                    Log.i("Drake", "address:" + smsMessage.getOriginatingAddress() + "   displayAddress:" + smsMessage.getDisplayOriginatingAddress()
////                            + "   body:" + smsMessage.getDisplayMessageBody() + "  time:"
////                            + smsMessage.getTimestampMillis());
////                    try {
////                        requestMap.put("phoneNumber", phoneNumber);
////                        requestMap.put("msg", smsMessage.getDisplayMessageBody());
////                        requestMap.put("timeStamp", smsMessage.getTimestampMillis());
////                        AppUtil.upload("http://" + host_port + "/phone_message.go", requestMap.toString());
////                    } catch (JSONException e) {
////                        e.printStackTrace();
////                    }
////                }
////            }
//        }
//    };
//
//    public class SMSReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            // TODO: This method is called when the BroadcastReceiver is receiving
//            // an Intent broadcast.
//            Bundle bundle = intent.getExtras();
//            if (null != bundle) {
//                Message message = new Message();
//                message.setData(bundle);
//                smsHandler.sendMessage(message);
//            }
//        }
//    }

    /**
     * 停止服务
     *
     * @param context
     */
    public static void stopService(Context context) {
        if (context != null) {
            LogUtils.d(TAG, "WorkService ------- stopService");
            Intent intent = new Intent(context, WorkService.class);
            context.stopService(intent);
        }
    }

    /**
     * 开启PushService
     *
     * @param context
     */
    public static void startService(Context context) {
        LogUtils.d(TAG, "WorkService ------- startService");
        if (context != null) {
            Intent intent = new Intent(context, WorkService.class);
            intent.setAction(ACTION_START);
//            context.startService(intent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.d(TAG, "WorkService -------   onBind");
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //todo 启动子线程执行耗时操作
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
//                LogUtils.d(TAG, "WorkService ---------- onStartCommand Service工作了");
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 1000, 1000);

        return START_STICKY;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, "WorkService ------- is onCreate");
//        smsReceiver = new SMSReceiver();
//        IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
//        intentFilter.setPriority(2147483647);
//        registerReceiver(smsReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (smsReceiver != null) {
//            unregisterReceiver(smsReceiver);
//        }
//        if (mp!= null) {
//            mp.stop();
//        }
//        if (vibrator!= null) {
//            vibrator.cancel();
//        }
        LogUtils.d(TAG, "WorkService ------- is onDestroy!!!");
    }
}

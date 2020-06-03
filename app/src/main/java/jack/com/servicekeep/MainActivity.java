package jack.com.servicekeep;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import jack.com.servicekeep.manager.KeepAliveManager;
import jack.com.servicekeep.service.KeepAliveJobSchedulerService;
import jack.com.servicekeep.utils.AppUtil;

public class MainActivity extends Activity implements View.OnClickListener {

//    private TextView mKillService, mStartService;
final String TAG = "MainActivity";
    private Button startLearnBtn;
    private MediaPlayer mp;
    private Vibrator vibrator;
    Timer playTimer = new Timer();
    TimerTask timerTask = new TimerTask() {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mp != null) {
//                        mp.reset();
                        mp.start();
                    }
                    if (vibrator != null) {
                        vibrator.vibrate(new long[]{0, 1000000000}, 0);
                    }
                }
            });
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "DeviceUUID:"+ AppUtil.getDeviceUUID(this));
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECEIVE_SMS}, 1);
        }
//        mKillService = findViewById(R.id.kill_service);
//        mStartService = findViewById(R.id.start_service);
        startLearnBtn = findViewById(R.id.start_learn);
//        mKillService.setOnClickListener(this);
//        mStartService.setOnClickListener(this);
        startLearnBtn.setOnClickListener(this);
        KeepAliveManager.INSTANCE.startKeepAliveService(MainActivity.this);
//        mp.start();
//        mp = new MediaPlayer();
//        try {
//            mp.setDataSource(getApplicationContext(), RingtoneManager
//                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));//这里我用的通知声音，还有其他的，大家可以点进去看
//            mp.prepare();
//            mp.start();
//            mp.setLooping(true);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        //取得震动服务的句柄
//        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        //按照指定的模式去震动。
//        vibrator.vibrate(1000);
        //数组参数意义：第一个参数为等待指定时间后开始震动，震动时间为第二个参数。后边的参数依次为等待震动和震动的时间
        //第二个参数为重复次数，-1为不重复，0为一直震动
//        vibrator.vibrate(new long[]{0, 10000000}, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mp != null) {
            mp.start();
        }
        if (vibrator != null) {
            vibrator.vibrate(new long[]{0, 1000000000}, 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mp != null) {
            mp.pause();
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
//        playTimer.schedule(timerTask, 5000);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
//            case R.id.kill_service:
//                Toast.makeText(getApplicationContext(), "kill service", Toast.LENGTH_LONG).show();
//                KeepAliveManager.INSTANCE.stopKeepAliveSerice(MainActivity.this);
//                break;
//            case R.id.start_service:
//                Toast.makeText(getApplicationContext(), "start service", Toast.LENGTH_LONG).show();
//                KeepAliveManager.INSTANCE.startKeepAliveService(MainActivity.this);
//                break;
            case R.id.start_learn:
                if (mp != null) {
                    mp.pause();
                }
                if (vibrator != null) {
                    vibrator.cancel();
                }
                KeepAliveJobSchedulerService.openDing(this);
//                playTimer.schedule(timerTask, 5000);
                break;
        }
    }
}

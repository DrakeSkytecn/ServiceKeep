package jack.com.servicekeep.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.app.Instrumentation;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import jack.com.servicekeep.utils.AppUtil;

public class MyAccessibilityService extends AccessibilityService {

    final String TAG = "MyAccessibilityService";
    String host_port = "123.206.57.75";
    public volatile int menuIndex = 0;
    public volatile int page = 0;
    private AccessibilityNodeInfo listview;
    String tag = null;
    Timer playTimer = new Timer();
    long videoDuration = 20000; //2400000 20000
    long heartbeatInterval = 1000;
    private MediaPlayer mp;
    private Vibrator vibrator;
    private String total_time = "111";
    private String current_time = "";

    private void setVolume(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 15, AudioManager.FLAG_PLAY_SOUND);
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 15, AudioManager.FLAG_PLAY_SOUND);
        audioManager.setStreamVolume(AudioManager.STREAM_RING, 15, AudioManager.FLAG_PLAY_SOUND);
    }

    private boolean isPlay(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        return audioManager.isMusicActive();
    }

    public void initPlayer() {
        mp = new MediaPlayer();
        try {
            mp.setDataSource(getApplicationContext(), RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));//这里我用的通知声音，还有其他的，大家可以点进去看
            mp.prepare();
            mp.setLooping(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //取得震动服务的句柄
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    //播放方法
    public void play() {
        setVolume(getApplicationContext());
        if (mp != null) {
            mp.start();
        }
        if (vibrator != null) {
            vibrator.vibrate(new long[]{0, 1000000000}, 0);
        }
    }

    //暂停
    public void pause() {
        if (mp != null) {
            mp.pause();
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    TimerTask timerTask = new TimerTask() {

        @Override
        public void run() {
            Log.d(TAG, "Video isPlaying:" + isPlay(getApplicationContext()));
//            click(49.0f,96.0f);
//            handler.sendEmptyMessageDelayed(0, videoDuration);
            play();
        }
    };

    Handler heartbeat = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            boolean isPlaying = isPlay(getApplicationContext());
            int state = isPlaying ? 1 : 0;
            final JSONObject requestMap = new JSONObject();
            try {
                requestMap.put("phoneId", AppUtil.getDeviceUUID(getApplicationContext()));
                requestMap.put("state", state);
                requestMap.put("secretKey", "YUEksdi6775KNs94STNBXk32");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
//                    AppUtil.heartbeat2("http://" + host_port + "/heart.go/?", requestMap.toString());
                    AppUtil.heartbeat2();
                }
            });
            thread.start();
            if (isPlaying) {
                pause();
            }
            Log.d(TAG, "Video isPlaying:" + isPlay(getApplicationContext()));
            heartbeat.sendEmptyMessageDelayed(0, heartbeatInterval);
        }
    };

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, "暂停播放响铃");
            click(360.0f, 1300.0f);
            pause();
            handler2.sendEmptyMessageDelayed(0, videoDuration);
        }
    };

    Handler handler2 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            click(49.0f, 96.0f);
            play();
            handler.sendEmptyMessageDelayed(0, 10000);
        }
    };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "connected");
        initPlayer();
        heartbeat.sendEmptyMessageDelayed(0, heartbeatInterval);
    }

    private void click(final float x, final float y) {  //模拟手势按位置点击
        Path path = new Path();
        path.moveTo(x, y);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        GestureDescription gesture = builder
                .addStroke(new GestureDescription.StrokeDescription(path, 50, 100))
                .build();
        this.dispatchGesture(gesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.i(TAG, "onCompleted: 手势完成");
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.i(TAG, "onCancelled: 失败");
            }
        }, null);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, event.getPackageName() + "");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
            int eventType = event.getEventType();
            if (rootInActiveWindow != null) {
                List<AccessibilityNodeInfo> nodeInfoList = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.alibaba.android.rimet:id/et_phone_input");
                if (nodeInfoList != null && nodeInfoList.size() > 0) {
                    Bundle arguments = new Bundle();
                    arguments.putCharSequence(
                            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "15603661211");//15603661211 18646586315
                    nodeInfoList.get(0).performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                }
                nodeInfoList = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.alibaba.android.rimet:id/et_pwd_login");
                if (nodeInfoList != null && nodeInfoList.size() > 0) {
                    Bundle arguments = new Bundle();
                    arguments.putCharSequence(
                            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "sitong303");// sitong303 xy141314
                    nodeInfoList.get(0).performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                }
                nodeInfoList = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.alibaba.android.rimet:id/btn_next");
                if (nodeInfoList != null && nodeInfoList.size() > 0) {
                    nodeInfoList.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
                nodeInfoList = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.alibaba.android.rimet:id/home_bottom_tab_button_work");
                if (nodeInfoList != null && nodeInfoList.size() > 0) {
                    if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                        nodeInfoList.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
//                nodeInfoList = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.alibaba.android.rimet:id/menu_current_company");
//                if (nodeInfoList != null && nodeInfoList.size() > 0) {
//                    AccessibilityNodeInfo menu = nodeInfoList.get(0);
//                    if (!menu.getText().toString().equals("哈尔滨四通技工学校")) {
//                        menu.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                    }
//                }
//                nodeInfoList = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.alibaba.android.rimet:id/listview");
//                if (nodeInfoList != null && nodeInfoList.size() > 0) {
//                    if (menuIndex != 2) {
//                        listview = nodeInfoList.get(0);
//                        listview.getChild(2).performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                    }
//                }
//                if (tag == null) {
//                    click(360.0f,1280.0f);
//                }
                nodeInfoList = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.alibaba.android.rimet:id/h5_pc_container");
                if (nodeInfoList != null && nodeInfoList.size() > 0) {
                    try {
                        AccessibilityNodeInfo view = nodeInfoList.get(0).getChild(0).getChild(0).getChild(0).getChild(0).getChild(1).getChild(0).getChild(3).getChild(9).getChild(0);
                        //点击云课堂
                        view.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        tag = "";
                    } catch (Exception e) {

                    }
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                nodeInfoList = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.alibaba.android.rimet:id/h5_pc_container");
                if (nodeInfoList != null && nodeInfoList.size() > 0) {
                    try {
//                        AccessibilityNodeInfo view = nodeInfoList.get(0).getChild(0).getChild(0).getChild(0).getChild(0).getChild(11);
                        AccessibilityNodeInfo view = nodeInfoList.get(0).getChild(0).getChild(0).getChild(0).getChild(0).getChild(12).getChild(0);
                        //点击农产品课程
                        view.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        tag = "继续学习";
                    } catch (Exception e) {

                    }
                }

                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                nodeInfoList = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.alibaba.android.rimet:id/title_bar_name");
                if (nodeInfoList != null && nodeInfoList.size() > 0) {
                    AccessibilityNodeInfo menu = nodeInfoList.get(0);
                    CharSequence title = menu.getText();
                    if (title != null && title.toString().equals("培训详情")) {
                        try {
                            nodeInfoList = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.alibaba.android.rimet:id/h5_pc_container");
                            if (nodeInfoList != null && nodeInfoList.size() > 0) {
                                AccessibilityNodeInfo view = nodeInfoList.get(0).getChild(0).getChild(0).getChild(0).getChild(0).getChild(5).getChild(0);
                                //点击继续学习
                                view.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                pause();
//                                playTimer.cancel();
                                try {
                                    playTimer.schedule(timerTask, videoDuration, videoDuration);
                                } catch (Exception e) {

                                }
                            }
                        } catch (Exception e) {

                        }
                    }
                }

//                if (total_time.equals("111")) {
//                    nodeInfoList = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.alibaba.android.rimet:id/video_controller_total_time");
//                    if (nodeInfoList != null && nodeInfoList.size() > 0) {
//                        try {
//                            AccessibilityNodeInfo view = nodeInfoList.get(0);
//                            total_time = view.getText().toString();
//                            Log.d(TAG, "total_time:"+total_time);
//                        } catch (Exception e) {
//
//                        }
//                    }
//                }
//
//                nodeInfoList = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.alibaba.android.rimet:id/video_controller_current_time");
//                if (nodeInfoList != null && nodeInfoList.size() > 0) {
//                    try {
////                        AccessibilityNodeInfo view = nodeInfoList.get(0).getChild(0).getChild(0).getChild(0).getChild(0).getChild(11);
//                        current_time_view = nodeInfoList.get(0);
//                        current_time = current_time_view.getText().toString();
//                        Log.d(TAG, "current_time:"+current_time);
//                        if (current_time.equals(total_time)) {
//                            click(49.0f,96.0f);//返回上一页
//                            play();
//                        }
//                    } catch (Exception e) {
//
//                    }
//                }

//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                nodeInfoList = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.alibaba.android.rimet:id/h5_pc_container");
//                if (nodeInfoList != null && nodeInfoList.size() > 0) {
//                    try {
//                        AccessibilityNodeInfo view = nodeInfoList.get(0).getChild(0).getChild(0).getChild(0).getChild(1).getChild(6);
//                        //点击开始学习
//                        view.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                        tag = null;
//                        playTimer.schedule(timerTask, 10000);
//                    } catch (Exception e) {
//
//                    }
//                }
            } else if (tag != null && tag.equals("继续学习")) {
                click(360.0f, 1300.0f);
                pause();
//                playTimer.cancel();
                try {
                    playTimer.schedule(timerTask, videoDuration, videoDuration);
                } catch (Exception e) {

                }
                tag = "";
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        }
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        Log.d(TAG, "onKeyEvent");
        int key = event.getKeyCode();
        switch (key) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                Log.d(TAG, "KEYCODE_VOLUME_DOWN");
                pause();
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                Log.d(TAG, "KEYCODE_VOLUME_UP");
                break;
        }
        return super.onKeyEvent(event);
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "onInterrupt");
    }
}
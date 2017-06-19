package com.npu.zhang.smartglasses;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class NotificationListener extends NotificationListenerService {

    private final String MMS = "2";
    private final String QQ = "3";
    private final String WECHAT = "4";
    private final String TEST = "0";



    @Override
    public void onCreate() {
        final NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle("SmartGlasses通知测试")
                .setContentText("这是一次普通的通知测试")
                .setTicker("SmartGlasses通知测试！！！")
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                .setSmallIcon(R.mipmap.ic_launcher);
        final Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("NPU_ACTION_TEST_NOTIFICATION")){
                    manager.notify(100, notification);
                    Toast.makeText(NotificationListener.this, "通知服务正常！", Toast.LENGTH_SHORT).show();
                }
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter("NPU_ACTION_TEST_NOTIFICATION"));
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn.getNotification().tickerText == null){
            return;
        }
//        String text = sbn.getNotification().tickerText.toString();
        String text = "haha";
        String type;
        System.out.println(sbn.getPackageName());
        switch (sbn.getPackageName()){
            case "com.android.mms":
                type = MMS;
                break;
            case "com.tencent.mobileqq":
                type = QQ;
                break;
            case "com.tencent.mm":
                type = WECHAT;
                break;
            case "com.npu.zhang.smartglasses":
                type = TEST;
                break;
            default:
                return;
        }

        Intent intent = new Intent("android.intent.action.BLE_BROADCAST");
        intent.putExtra("msg", text);
        intent.putExtra("type", type);
        sendBroadcast(intent);
        super.onNotificationPosted(sbn);
    }

}

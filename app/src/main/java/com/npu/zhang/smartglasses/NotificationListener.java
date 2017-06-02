package com.npu.zhang.smartglasses;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.widget.Toast;

public class NotificationListener extends NotificationListenerService {

    private final String MMS = "2";
    private final String QQ = "3";
    private final String WECHAT = "4";


    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn.getNotification().tickerText == null){
            return;
        }
        String text = sbn.getNotification().tickerText.toString();
        String type;

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

package com.npu.zhang.smartglasses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by zhang on 2017/5/18.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, CoreService.class);
        intent1.putExtra("addr", "00:15:83:00:A5:79");
        context.startService(intent1);
        System.out.println("守护广播接收器启动啦！！！");
    }
}

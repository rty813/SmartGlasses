package com.npu.zhang.smartglasses;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CoreService extends Service {
    private static BluetoothLeService bluetoothLeService;
    private String addr;

    private boolean isBindBleService = false;
//    private boolean isStoped = false;

    //接收广播，通过蓝牙串口发送
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (bluetoothLeService != null){
//                String str = intent.getStringExtra("type") + intent.getStringExtra("msg") + "\r\n";
                String str = intent.getStringExtra("type");
                System.out.println(str);
                bluetoothLeService.WriteValue(str);
            }
        }
    };

    //蓝牙状态广播接收器
    private BroadcastReceiver stateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("NPU_ACTION_GATT_DISCONNECTED")){
                System.out.println("蓝牙连接中断！");
                if (isBindBleService){
                    unbindService(mServiceConnection);
                }
                isBindBleService = false;
//                isStoped = true;
                stopSelf();
            }
            if (action.equals(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)){
                System.out.println("绑定成功");
                Toast.makeText(CoreService.this, "绑定成功", Toast.LENGTH_LONG).show();
//                new SendTime().start();
//                isStoped = false;
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("CoreService onStartCommand");
        addr = intent.getStringExtra("addr");

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        System.out.println(bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE));
        System.out.println("onCreate: CoreService的onCreate" );

        IntentFilter intentFilter = new IntentFilter("NPU_ACTION_GATT_DISCONNECTED");
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        registerReceiver(stateReceiver, intentFilter);

        intentFilter = new IntentFilter("android.intent.action.BLE_BROADCAST");
        registerReceiver(mReceiver, intentFilter);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("CoreService被摧毁啦！onDestory");
        Toast.makeText(CoreService.this, "蓝牙连接失败！", Toast.LENGTH_SHORT).show();
//        isStoped = true;
        unregisterReceiver(stateReceiver);
        unregisterReceiver(mReceiver);
        if (isBindBleService){
            unbindService(mServiceConnection);
        }
        if(bluetoothLeService != null)
        {
            bluetoothLeService.close();
            bluetoothLeService = null;
        }
//        sendBroadcast(new Intent("com.npu.zhang.smartglasses.destroy"));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //BluetoothLeService回调
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            isBindBleService = true;
            bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!bluetoothLeService.initialize()) {
                return;
            }
            bluetoothLeService.connect(addr);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothLeService = null;
            isBindBleService = false;
            System.out.println("BLEService解绑");
        }
    };

//    private class SendTime extends Thread{
//        @Override
//        public void run() {
//            while(true){
//                if (isStoped){
//                    break;
//                }
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                long time=System.currentTimeMillis();
//                SimpleDateFormat format=new SimpleDateFormat("HH:mm:ss");
//                Date d1=new Date(time);
//                String t1=format.format(d1);
//                Intent intent = new Intent("android.intent.action.BLE_BROADCAST");
//                intent.putExtra("msg", t1);
//                intent.putExtra("type", "1");
//                sendBroadcast(intent);
//            }
//            super.run();
//        }
//    }

    @Override
    public void onLowMemory() {
        System.out.println("CoreService: onLowMemory");
        super.onLowMemory();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        System.out.println("CoreService: onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        System.out.println("CoreService: onRebind");
        super.onRebind(intent);
    }
}

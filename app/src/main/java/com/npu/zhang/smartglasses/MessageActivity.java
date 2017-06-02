package com.npu.zhang.smartglasses;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText editText;
    private Button btn_sendTime;
    private Button btn_listen;
    private Button btn_send;
    private TextView tv_recv;
    private String addr;
    private BluetoothLeService bluetoothLeService;
    private boolean mConnected = false;
    private boolean isTimeSend = false;
    private boolean isListenerStart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        editText = (EditText) findViewById(R.id.editText);
        btn_sendTime = (Button) findViewById(R.id.btn_sendTime);
        tv_recv = (TextView) findViewById(R.id.tv_recv);
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_listen = (Button) findViewById(R.id.btn_listen);
        ((TextView)findViewById(R.id.tv_title)).setText(getIntent().getStringExtra("name"));
        addr = getIntent().getStringExtra("addr");
        tv_recv.setMovementMethod(ScrollingMovementMethod.getInstance());

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        System.out.println(bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE));
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        btn_send.setOnClickListener(this);
        btn_listen.setOnClickListener(this);
        btn_sendTime.setOnClickListener(this);
        Toast.makeText(this, "正在连接", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isTimeSend = false;
        unregisterReceiver(mGattUpdateReceiver);
        unbindService(mServiceConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(bluetoothLeService != null)
        {
            bluetoothLeService.close();
            bluetoothLeService = null;
        }
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {  //连接成功
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) { //断开连接
                mConnected = false;
                invalidateOptionsMenu();
                setBtnEnabled(false);
//                clearUI();
            }else if(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) //可以开始干活了
            {
                mConnected = true;
                Toast.makeText(MessageActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                setBtnEnabled(true);
            }else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) { //收到数据
                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                if (data != null) {
                    tv_recv.append(data);
                    int offset = tv_recv.getLineCount() * tv_recv.getLineHeight();
                    if (offset > tv_recv.getHeight()){
                        tv_recv.scrollTo(0, offset - tv_recv.getHeight());
                    }
                }
            }
        }
    };

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!bluetoothLeService.initialize()) {
                finish();
            }
            bluetoothLeService.connect(addr);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothLeService = null;
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {                        //注册接收的事件
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        return intentFilter;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_send:
                if (editText.length() < 1) {
                    Toast.makeText(MessageActivity.this, "请输入要发送的内容", Toast.LENGTH_SHORT).show();
                    return;
                }
                bluetoothLeService.WriteValue(editText.getText().toString() + "\r\n");
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm.isActive())
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                break;
            case R.id.btn_sendTime:
                if (!isTimeSend){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while(true){
                                if (!isTimeSend){
                                    break;
                                }
                                long time=System.currentTimeMillis();//long now = android.os.SystemClock.uptimeMillis();
                                SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Date d1=new Date(time);
                                String t1=format.format(d1);
                                System.out.println(t1);
                                bluetoothLeService.WriteValue(t1);
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
                }
                isTimeSend = !isTimeSend;
                if (isTimeSend){
                    btn_sendTime.setText("停止发送");
                }
                else{
                    btn_sendTime.setText("发送时间");
                }
                break;
            case R.id.btn_listen:
                Intent intent = new Intent(MessageActivity.this, NotificationListener.class);
                if (isListenerStart){
                    stopService(intent);
                    isListenerStart = false;
                    btn_listen.setText("监听通知");
                }
                else{
                    startService(intent);
                    isListenerStart = true;
                    btn_listen.setText("停止监听");
                }

        }
    }

    private void setBtnEnabled(boolean state){
        btn_listen.setEnabled(state);
        btn_sendTime.setEnabled(state);
        btn_send.setEnabled(state);
    }
}

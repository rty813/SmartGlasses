package com.npu.zhang.smartglasses;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private ListView listView;
    private SimpleAdapter adapter;
    private List<Map<String, String>> list;
    private BluetoothLeScanner scanner;
    private ArrayList<BluetoothDevice> devices;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!notificationListenerEnable()){
            Toast.makeText(this, "请赋予通知监听权限", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        }
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "您的设备不支持BLE", Toast.LENGTH_SHORT).show();
            finish();
        }
        listView = (ListView) findViewById(R.id.listview);
        list = new ArrayList<>();
        devices = new ArrayList<>();
        adapter = new SimpleAdapter(this, list, android.R.layout.simple_list_item_2,
                new String[]{"name", "addr"}, new int[]{android.R.id.text1, android.R.id.text2});
        listView.setAdapter(adapter);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        list.clear();
        devices.clear();
        adapter.notifyDataSetChanged();
        if (!bluetoothAdapter.isEnabled()){
            bluetoothAdapter.enable();
        }
        else{
            scanner = bluetoothAdapter.getBluetoothLeScanner();
            progressBar.setVisibility(View.VISIBLE);
            scanner.startScan(scanCallback);
            findViewById(R.id.btn_scan).setEnabled(false);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanner.stopScan(scanCallback);
                    progressBar.setVisibility(View.GONE);
                    findViewById(R.id.btn_scan).setEnabled(true);
                }
            }, 10000);
        }

        findViewById(R.id.btn_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanner = bluetoothAdapter.getBluetoothLeScanner();
                list.clear();
                devices.clear();
                adapter.notifyDataSetChanged();
                scanner.startScan(scanCallback);
                progressBar.setVisibility(View.VISIBLE);
                findViewById(R.id.btn_scan).setEnabled(false);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scanner.stopScan(scanCallback);
                        progressBar.setVisibility(View.GONE);
                        findViewById(R.id.btn_scan).setEnabled(true);
                    }
                }, 10000);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                scanner.stopScan(scanCallback);
                progressBar.setVisibility(View.GONE);
                findViewById(R.id.btn_scan).setEnabled(true);
                Intent intent = new Intent(MainActivity.this, CoreService.class);
                intent.putExtra("addr", list.get(i).get("addr"));
                startService(intent);
                finish();
            }
        });
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice bluetoothDevice = result.getDevice();
            if (devices.contains(bluetoothDevice)){
                return;
            }
            devices.add(bluetoothDevice);
            Map<String, String> map = new HashMap<String, String>();
            if (bluetoothDevice.getName() == null){
                map.put("name", "未知设备");
            }
            else{
                map.put("name", bluetoothDevice.getName());
            }
            map.put("addr", bluetoothDevice.getAddress());
            list.add(map);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_restartNotificationListener:
                toggleNotificationListenerService();
                Toast.makeText(getApplicationContext(), "重启通知监听服务", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_about:
                new AlertDialog.Builder(this)
                        .setTitle("关于")
                        .setIcon(R.mipmap.ic_launcher)
                        .setMessage("by 西工大")
                        .show();
                break;
            case R.id.menu_testNotification:
                sendBroadcast(new Intent("NPU_ACTION_TEST_NOTIFICATION"));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //重新开启NotificationMonitor
    private void toggleNotificationListenerService() {
        ComponentName thisComponent = new ComponentName(this,  NotificationListener.class);
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    private boolean notificationListenerEnable() {
        boolean enable = false;
        String packageName = getPackageName();
        String flat= Settings.Secure.getString(getContentResolver(),"enabled_notification_listeners");
        if (flat != null) {
            enable= flat.contains(packageName);
        }
        return enable;
    }
}
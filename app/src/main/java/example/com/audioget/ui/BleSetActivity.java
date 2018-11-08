//package example.com.audioget.ui;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Bundle;
//import android.support.v4.content.LocalBroadcastManager;
//import android.support.v7.app.AppCompatActivity;
//import android.util.Log;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.EditText;
//import android.widget.LinearLayout;
//import android.widget.ListView;
//import android.widget.TextView;
//
//import com.ble.ble.constants.BleRegConstants;
//import com.ble.gatt.GattAttributes;
//
//import example.com.audioget.MainActivity;
//import example.com.audioget.R;
//import example.com.audioget.util.LeProxy;
//
///**
// * 示例： 模组参数API的使用 通用uuid的数据交互
// */
//public class BleSetActivity extends AppCompatActivity {
//    private static final String TAG = "BleSetActivity";
//
//    private BleSetAdapter mBleSetAdapter;
//    private String mDeviceAddress;
//    private Ble mBle;
//    private LeProxy mLeProxy;
//
//    private final BroadcastReceiver mLocalReceiver = new BroadcastReceiver() {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            String address = intent.getStringExtra(LeProxy.EXTRA_ADDRESS);
//            if (!address.equals(mDeviceAddress)) return;
//
//            switch (intent.getAction()) {
//                case LeProxy.ACTION_GATT_DISCONNECTED:// 断线
//                    break;
//
//                case LeProxy.ACTION_REG_DATA_AVAILABLE: {
//                    int regFlag = intent.getIntExtra(LeProxy.EXTRA_REG_FLAG, 0);
//                    String regData = intent.getStringExtra(LeProxy.EXTRA_REG_DATA);
//                    handleRegData(regFlag, regData);
//                }
//                break;
//
//                case LeProxy.ACTION_DATA_AVAILABLE: {
//                    String uuid = intent.getStringExtra(LeProxy.EXTRA_UUID);
//                    if (GattAttributes.Device_Name.toString().equals(uuid)) {
//                        byte[] data = intent.getByteArrayExtra(LeProxy.EXTRA_DATA);
//                        mBle.deviceName = new String(data);
//                        mBleSetAdapter.notifyDataSetChanged();
//                    }
//                }
//                break;
//            }
//        }
//    };
//
//
//    private IntentFilter makeFilter() {
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(LeProxy.ACTION_GATT_DISCONNECTED);
//        filter.addAction(LeProxy.ACTION_REG_DATA_AVAILABLE);
//        filter.addAction(LeProxy.ACTION_DATA_AVAILABLE);
//        return filter;
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mDeviceAddress = getIntent().getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);
//        mBleSetAdapter = new BleSetAdapter();
//        mBle = new Ble();
//        initView();
//        mLeProxy = LeProxy.getInstance();
//        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalReceiver, makeFilter());
//    }
//
//    private void initView() {
//        String name = getIntent().getStringExtra(MainActivity.EXTRA_DEVICE_NAME);
//        getSupportActionBar().setTitle(name);
//        getSupportActionBar().setSubtitle(mDeviceAddress);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        ListView listView = new ListView(this);
//        listView.setBackgroundColor(getResources().getColor(R.color.bg_main));
//        listView.setAdapter(mBleSetAdapter);
//        setContentView(listView);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                finish();
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    // 模组参数的数据处理
//    private void handleRegData(int regFlag, String regData) {
//        switch (regFlag) {
//            case BleRegConstants.REG_VERSION:
//                mBle.version = regData;
//                break;
//            case BleRegConstants.REG_BATTERY_LEVEL:
//                mBle.batteryLevel = regData;
//                break;
//            case BleRegConstants.REG_ADV_INTERVAL:
//                mBle.advInterval = regData;
//                break;
//            case BleRegConstants.REG_CONN_INTERVAL:
//                mBle.connInterval = regData;
//                break;
//        }
//        mBleSetAdapter.notifyDataSetChanged();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocalReceiver);
//    }
//
//    private class BleSetAdapter extends BaseAdapter {
//        private String[] mTitles;
//
//        BleSetAdapter() {
//            mTitles = getResources().getStringArray(R.array.ble_set_item_titles);
//        }
//
//        @Override
//        public int getCount() {
//            return mTitles.length;
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return mTitles[position];
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        @Override
//        public View getView(final int position, View convertView, ViewGroup parent) {
//            ViewHolder holder;
//            if (convertView == null) {
//                convertView = getLayoutInflater().inflate(R.layout.item_ble_set, null);
//                holder = new ViewHolder();
//                holder.title = (TextView) convertView.findViewById(R.id.txt_title);
//                holder.value = (TextView) convertView.findViewById(R.id.txt_value);
//                holder.read = (TextView) convertView.findViewById(R.id.btn_read);
//                holder.set = (TextView) convertView.findViewById(R.id.btn_set);
//                holder.editText = (EditText) convertView.findViewById(R.id.editText1);
//                holder.layoutSet = (LinearLayout) convertView.findViewById(R.id.layout_set);
//                convertView.setTag(holder);
//            } else {
//                holder = (ViewHolder) convertView.getTag();
//            }
//
//            final ViewHolder holder2 = holder;
//            holder2.title.setText(mTitles[position]);
//
//            int v = position < 3 ? View.GONE : View.VISIBLE;
//            holder2.layoutSet.setVisibility(v);
//
//            switch (position) {
//                case 0://设备名称
//                    holder2.read.setVisibility(View.VISIBLE);
//                    holder2.value.setText(mBle.deviceName);
//                    break;
//                case 1://固件版本
//                    holder2.read.setVisibility(View.VISIBLE);
//                    holder2.value.setText(mBle.version);
//                    break;
//                case 2://电池电量
//                    holder2.read.setVisibility(View.VISIBLE);
//                    holder2.value.setText(mBle.batteryLevel);
//                    break;
//                case 3://广播间隔
//                    holder2.read.setVisibility(View.VISIBLE);
//                    holder2.value.setText(mBle.advInterval);
//                    holder2.editText.setHint(R.string.ble_set_adv_interval_hint);
//                    break;
//                case 4://连接间隔
//                    holder2.read.setVisibility(View.VISIBLE);
//                    holder2.value.setText(mBle.connInterval);
//                    holder2.editText.setHint(R.string.ble_set_conn_interval_hint);
//                    break;
//            }
//
//            holder2.read.setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    //
//                    switch (position) {
//                        case 0:
//                            // 读取设备名称
//                            boolean success = mLeProxy.readCharacteristic(mDeviceAddress, GattAttributes.Generic_Access, GattAttributes.Device_Name);
//                            Log.i(TAG, "Read device name: " + success);
//                            break;
//                        case 1:
//                            // 读取模组版本号
//                            mLeProxy.readReg(mDeviceAddress, BleRegConstants.REG_VERSION);
//                            break;
//                        case 2:
//                            // 读取模组电量
//                            mLeProxy.readReg(mDeviceAddress, BleRegConstants.REG_BATTERY_LEVEL);
//                            break;
//                        case 3:
//                            // 读取广播间隔
//                            mLeProxy.readReg(mDeviceAddress, BleRegConstants.REG_ADV_INTERVAL);
//                            break;
//                        case 4:
//                            // 读取连接间隔
//                            mLeProxy.readReg(mDeviceAddress, BleRegConstants.REG_CONN_INTERVAL);
//                            break;
//                    }
//                }
//            });
//            holder2.set.setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    try {
//                        String strValue = holder2.editText.getText().toString();
//                        int value = Integer.valueOf(strValue);
//                        switch (position) {
//                            case 3:
//                                // 设置广播间隔
//                                mLeProxy.setReg(mDeviceAddress, BleRegConstants.REG_ADV_INTERVAL, value);
//                                break;
//                            case 4:
//                                // 设置连接间隔
//                                mLeProxy.setReg(mDeviceAddress, BleRegConstants.REG_CONN_INTERVAL, value);
//                                break;
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//
//            return convertView;
//        }
//
//    }
//
//    private static class ViewHolder {
//        TextView title, value, read, set;
//        EditText editText;
//        LinearLayout layoutSet;
//    }
//
//    private static class Ble {
//        String version, batteryLevel, advInterval, connInterval, deviceName;
//    }
//}
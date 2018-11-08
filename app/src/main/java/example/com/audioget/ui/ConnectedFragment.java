//package example.com.audioget.ui;
//
//import android.app.Activity;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothGatt;
//import android.bluetooth.BluetoothGattService;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.support.v4.content.LocalBroadcastManager;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.BaseAdapter;
//import android.widget.CheckBox;
//import android.widget.CompoundButton;
//import android.widget.CompoundButton.OnCheckedChangeListener;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.ListView;
//import android.widget.TextView;
//
//import com.ble.api.DataUtil;
//import com.ble.api.EncodeUtil;
//import com.ble.gatt.GattAttributes;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import example.com.audioget.LeDevice;
//import example.com.audioget.MainActivity;
//import example.com.audioget.R;
//import example.com.audioget.util.HexAsciiWatcher;
//import example.com.audioget.util.LeProxy;
//import example.com.audioget.util.TimeUtil;
//
//public class ConnectedFragment extends Fragment implements View.OnClickListener {
//    private final String TAG = "ConnectedFragment";
//
//    private static final int REQ_HEX_INPUT = 3;
//
//    private List<String> mSelectedAddresses;
//    private ConnectedDeviceListAdapter mDeviceListAdapter;
//    private LeProxy mLeProxy;
//    private HexAsciiWatcher mInputWatcher;
//    private EncodeUtil mEncodeUtil = new EncodeUtil();
//
//    private CheckBox mBoxAscii;
//    private CheckBox mBoxEncrypt;
//    private EditText mEdtInput;
//
//    private final BroadcastReceiver mLocalReceiver = new BroadcastReceiver() {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            String address = intent.getStringExtra(LeProxy.EXTRA_ADDRESS);
//
//            switch (intent.getAction()) {
//                case LeProxy.ACTION_GATT_DISCONNECTED:// 断线
//                    mSelectedAddresses.remove(address);
//                    mDeviceListAdapter.removeDevice(address);
//                    break;
//
//                case LeProxy.ACTION_RSSI_AVAILABLE: {// 更新rssi
//                    LeDevice device = mDeviceListAdapter.getDevice(address);
//                    if (device != null) {
//                        int rssi = intent.getIntExtra(LeProxy.EXTRA_RSSI, 0);
//                        device.setRssi(rssi);
//                        mDeviceListAdapter.notifyDataSetChanged();
//                    }
//                }
//                break;
//
//                case LeProxy.ACTION_DATA_AVAILABLE:// 接收到从机数据
//                    displayRxData(intent);
//                    break;
//            }
//        }
//    };
//
//    private void displayRxData(Intent intent) {
//        String address = intent.getStringExtra(LeProxy.EXTRA_ADDRESS);
//        String uuid = intent.getStringExtra(LeProxy.EXTRA_UUID);
//        byte[] data = intent.getByteArrayExtra(LeProxy.EXTRA_DATA);
//
//        //如果数据加密，这里就解密一下
//        if (mBoxEncrypt.isChecked()) data = mEncodeUtil.decodeMessage(data);
//
//        LeDevice device = mDeviceListAdapter.getDevice(address);
//        if (device != null && data != null) {
//            String dataStr = "timestamp: " + TimeUtil.getTimeStamp() + '\n'
//                    + "uuid: " + uuid + '\n'
//                    + "length: " + data.length + '\n';
//            if (mBoxAscii.isChecked()) {
//                dataStr += "data: " + new String(data);
//            } else {
//
//                dataStr += "data: " + DataUtil.byteArrayToHex(data) + '\n';
//            }
//            device.setRxData(dataStr);
//            mDeviceListAdapter.notifyDataSetChanged();
//        }
//    }
//
//    private IntentFilter makeFilter() {
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(LeProxy.ACTION_GATT_DISCONNECTED);
//        filter.addAction(LeProxy.ACTION_RSSI_AVAILABLE);
//        filter.addAction(LeProxy.ACTION_DATA_AVAILABLE);
//        return filter;
//    }
//
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mDeviceListAdapter = new ConnectedDeviceListAdapter();
//        mSelectedAddresses = new ArrayList<>();
//        mLeProxy = LeProxy.getInstance();
//        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mLocalReceiver, makeFilter());
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_connected, container, false);
//        initView(view);
//        return view;
//    }
//
//    private void initView(View view) {
//        ListView listView = (ListView) view.findViewById(R.id.listView1);
//        listView.setAdapter(mDeviceListAdapter);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                LeDevice device = mDeviceListAdapter.getItem(position);
//                Intent intent = new Intent(getActivity(), BleSetActivity.class);
//                intent.putExtra(MainActivity.EXTRA_DEVICE_ADDRESS, device.getAddress());
//                intent.putExtra(MainActivity.EXTRA_DEVICE_NAME, device.getName());
//                startActivity(intent);
//            }
//        });
//
//        TextView tvInputBytes = (TextView) view.findViewById(R.id.tv_input_bytes);
//        mEdtInput = (EditText) view.findViewById(R.id.edt_msg);
//        mInputWatcher = new HexAsciiWatcher(getActivity());
//        mInputWatcher.setHost(mEdtInput);
//        mInputWatcher.setIndicator(tvInputBytes);
//        mEdtInput.addTextChangedListener(mInputWatcher);
//
//        mBoxEncrypt = (CheckBox) view.findViewById(R.id.cbox_encrypt);
//        mBoxEncrypt.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                updateEditText(false);
//            }
//        });
//
//        mBoxAscii = (CheckBox) view.findViewById(R.id.cbox_ascii);
//        mBoxAscii.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                updateEditText(true);
//            }
//        });
//
//        updateEditText(false);
//
//        view.findViewById(R.id.btn_send).setOnClickListener(this);
//        mEdtInput.setOnClickListener(this);
//        mEdtInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                Log.e(TAG, "onFocusChange() - hasFocus=" + hasFocus);
//                if (hasFocus) goHexInputActivity();
//            }
//        });
//    }
//
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.btn_send:
//                send();//TODO
////				sendLargeMtu();
//                break;
//
//            case R.id.edt_msg:
//                goHexInputActivity();
//                break;
//        }
//    }
//
//
//    private void goHexInputActivity() {
//        if (!mBoxAscii.isChecked()) {
//            Intent intent = new Intent(getActivity(), HexInputActivity.class);
//            intent.putExtra(HexInputActivity.EXTRA_MAX_LENGTH, mInputWatcher.getMaxLength());
//            intent.putExtra(HexInputActivity.EXTRA_HEX_STRING, mEdtInput.getText().toString());
//            startActivityForResult(intent, REQ_HEX_INPUT);
//        }
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQ_HEX_INPUT && resultCode == Activity.RESULT_OK) {
//            String hexStr = data.getStringExtra(HexInputActivity.EXTRA_HEX_STRING);
//            mEdtInput.setText(hexStr);
//            mEdtInput.setSelection(hexStr.length());
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }
//
//    // 向勾选的设备发送数据
//    private void send() {
//        String inputStr = mEdtInput.getText().toString();
//
//        if (inputStr.length() > 0) {
//            byte[] data;
//            if (mBoxAscii.isChecked()) {
//                // 这里将换行符替换成Windows系统的，不过这样统计的字节数就会偏少
//                inputStr = inputStr.replaceAll("\r\n", "\n");
//                inputStr = inputStr.replaceAll("\n", "\r\n");
//                data = inputStr.getBytes();
//            } else {
//                data = DataUtil.hexToByteArray(inputStr);
//            }
//
//            Log.e(TAG, inputStr + " -> " + DataUtil.byteArrayToHex(data));
//
//            for (int i = 0; i < mSelectedAddresses.size(); i++) {
//                mLeProxy.send(mSelectedAddresses.get(i), data, mBoxEncrypt.isChecked());
//            }
//        }
//    }
//
//    private void updateEditText(boolean clearText) {
//        mInputWatcher.setTextType(mBoxAscii.isChecked() ? HexAsciiWatcher.ASCII : HexAsciiWatcher.HEX);
//        int maxLen;//可输入的字符串长度
//        String hintText;
//        if (mBoxAscii.isChecked()) {
//            if (mBoxEncrypt.isChecked()) {
//                maxLen = 17;
//            } else {
//                maxLen = 20;
//            }
//            hintText = getString(R.string.connected_send_ascii_hint, maxLen);
//        } else {
//            if (mBoxEncrypt.isChecked()) {
//                maxLen = 34;
//            } else {
//                maxLen = 40;
//            }
//            hintText = getString(R.string.connected_send_hex_hint, maxLen / 2);
//        }
//        mInputWatcher.setMaxLength(maxLen);
//        mEdtInput.setHint(hintText);
//        if (clearText) {
//            mEdtInput.setText("");
//            mInputWatcher.setIndicatorText(getString(R.string.input_bytes, 0));
//        }
//    }
//
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        Log.i(TAG, "onResume()");
//        mSelectedAddresses.clear();
//        mDeviceListAdapter.clear();
//        List<BluetoothDevice> connectedDevices = mLeProxy.getConnectedDevices();
//        for (int i = 0; i < connectedDevices.size(); i++) {
//            String name = connectedDevices.get(i).getName();
//            String address = connectedDevices.get(i).getAddress();
//            mSelectedAddresses.add(address);
//            mDeviceListAdapter.addDevice(new LeDevice(name, address));
//        }
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mLocalReceiver);
//    }
//
//    private class ConnectedDeviceListAdapter extends BaseAdapter {
//        List<LeDevice> mDevices;
//        LayoutInflater mInflater;
//
//        public ConnectedDeviceListAdapter() {
//            mDevices = new ArrayList<>();
//            mInflater = getActivity().getLayoutInflater();
//        }
//
//        private boolean isOadSupported(String address) {
//            BluetoothGatt gatt = mLeProxy.getBluetoothGatt(address);
//            if (gatt != null) {
//                BluetoothGattService oadService = gatt.getService(GattAttributes.TI_OAD_Service);
//                return oadService != null;
//            }
//            return false;
//        }
//
//        public LeDevice getDevice(String address) {
//            for (LeDevice connectedLeDevice : mDevices) {
//                if (connectedLeDevice.getAddress().equals(address)) {
//                    return connectedLeDevice;
//                }
//            }
//            return null;
//        }
//
//        public void addDevice(LeDevice device) {
//            if (!mDevices.contains(device)) {
//                device.setOadSupported(isOadSupported(device.getAddress()));
//                mDevices.add(device);
//                notifyDataSetChanged();
//            }
//        }
//
//        public void removeDevice(String address) {
//            int location = -1;
//
//            for (int i = 0; i < mDevices.size(); i++) {
//                if (mDevices.get(i).getAddress().equals(address)) {
//                    location = i;
//                    break;
//                }
//            }
//
//            if (location != -1) {
//                mDevices.remove(location);
//                notifyDataSetChanged();
//            }
//        }
//
//        public void clear() {
//            mDevices.clear();
//            notifyDataSetChanged();
//        }
//
//        @Override
//        public int getCount() {
//            return mDevices.size();
//        }
//
//        @Override
//        public LeDevice getItem(int position) {
//            return mDevices.get(position);
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            ViewHolder holder;
//            if (convertView == null) {
//                convertView = mInflater.inflate(R.layout.item_device_list, null);
//                holder = new ViewHolder();
//                holder.tvName = (TextView) convertView.findViewById(R.id.device_name);
//                holder.tvAddress = (TextView) convertView.findViewById(R.id.device_address);
//                holder.tvRxData = (TextView) convertView.findViewById(R.id.txt_rx_data);
//                holder.tvRssi = (TextView) convertView.findViewById(R.id.txt_rssi);
//                holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox1);
//                holder.btnOAD = (TextView) convertView.findViewById(R.id.btn_oad);
//                holder.imageView = (ImageView) convertView.findViewById(R.id.imageView1);
//                holder.disconnect = (TextView) convertView.findViewById(R.id.btn_connect);
//                holder.disconnect.setText(R.string.menu_disconnect);
//                convertView.setTag(holder);
//            } else {
//                holder = (ViewHolder) convertView.getTag();
//            }
//
//            final LeDevice device = mDevices.get(position);
//
//            holder.imageView.setVisibility(View.VISIBLE);
//            holder.checkBox.setOnCheckedChangeListener(null);
//
//            holder.disconnect.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mLeProxy.disconnect(device.getAddress());
//                }
//            });
//            String name = device.getName();
//            if (name == null || device.getName().trim().length() == 0) {
//                holder.tvName.setText(R.string.unknown_device);
//            } else {
//                holder.tvName.setText(name);
//            }
//            holder.tvAddress.setText(device.getAddress());
//            holder.checkBox.setVisibility(View.VISIBLE);
//            holder.tvRxData.setVisibility(View.VISIBLE);
//            holder.tvRxData.setText(device.getRxData());
//            holder.tvRssi.setText("rssi: " + device.getRssi() + "dbm");
//            final String address = device.getAddress();
//            holder.checkBox.setChecked(mSelectedAddresses.contains(address));
//            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    // 勾选设备，发送数据时只给已勾选的设备发送
//                    if (isChecked) {
//                        if (!mSelectedAddresses.contains(address)) {
//                            mSelectedAddresses.add(address);
//                        }
//                    } else {
//                        if (mSelectedAddresses.contains(address)) {
//                            mSelectedAddresses.remove(address);
//                        }
//                    }
//                    Log.i(TAG, "Selected " + mSelectedAddresses.size());
//                }
//            });
//
//            if (device.isOadSupported()) {
//                holder.btnOAD.setVisibility(View.VISIBLE);
//                holder.btnOAD.setOnClickListener(new View.OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        // 进入OAD界面
//                        Intent oadIntent = new Intent(getActivity(), OADActivity.class);
//                        oadIntent.putExtra(MainActivity.EXTRA_DEVICE_NAME, device.getName());
//                        oadIntent.putExtra(MainActivity.EXTRA_DEVICE_ADDRESS, device.getAddress());
//                        startActivity(oadIntent);
//                    }
//                });
//            }
//
//            return convertView;
//        }
//    }
//
//    private static class ViewHolder {
//        TextView tvName, tvAddress, tvRxData, tvRssi;
//        CheckBox checkBox;
//        TextView btnOAD;
//        TextView disconnect;
//        ImageView imageView;
//    }
//}
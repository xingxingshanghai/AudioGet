package example.com.audioget;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ble.api.DataUtil;
import com.ble.api.EncodeUtil;
import com.ble.ble.BleService;

import org.achartengine.GraphicalView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import example.com.audioget.util.LeProxy;
import example.com.audioget.util.TimeUtil;
import example.com.audioget.util.ToastUtil;

import static android.media.AudioTrack.WRITE_BLOCKING;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,CompoundButton.OnCheckedChangeListener,RadioGroup.OnCheckedChangeListener {

	private BluetoothAdapter mBluetoothAdapter;
	//private final static int REQUEST_WRITE_EXTERNAL_STORAGE = 5;
	private boolean mScanning;
	private Handler mHandlerScan = new Handler();
	private static final long SCAN_PERIOD = 20000;
	private LeProxy mLeProxy;
	//新加入部分
	private final static String TAG = MainActivity.class.getSimpleName(),sdPath = Environment.getExternalStorageDirectory()+"/js";
	private ImageButton bt_play, bt_pre, bt_next;
    boolean waveAgain = true;
	private static final int REQUEST_ENABLE_BT = 3;
	private final static int sampleRate = 7880;
	private SeekBar seekBar;
	private Button openMusic,clearMusic;
	private MyHandler mHandler = new MyHandler(this);
	private SimpleDateFormat format = new SimpleDateFormat("mm:ss");
	private TextView currentTimeTxt, totalTimeTxt,musicinfo;
	private MusicServiceDemo.CallBack callBack;
	private boolean mFlag = true;
	private ArrayList<MusicBean> musicBeanList = new ArrayList<MusicBean>();
	private int mProgress;
	public static int value;
	private boolean binderFlag = false;
	private Spinner spinner ;
	private SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
	private FileOutputStream fos;
	private BufferedOutputStream bufos;
	private ChartService mService2;
	private LinearLayout mRightCurveLayout;//存放右图表的布局容器
	private GraphicalView mView2;//左右图表
	private RadioGroup rg,position_rg;
	private Button saveAudio,disfinish;
	private String xingbie = "",nianling = "",additondisease = "",position = "";
	private TextView bluescan;
	//File file = new File(Environment.getExternalStorageDirectory(), "test.txt");
	private EditText otherdis;
	private CheckBox checkButton1,checkButton2,checkButton3,checkButton4,checkButton5,checkButton6,checkButton7,checkButton8,checkButton9,checkButton10,
			checkButton11,checkButton12,checkButton13,checkButton14,checkButton15,checkButton16,checkButton17,checkButton18;
	ArrayList<String> disease=new ArrayList<String>();
	StringBuilder sb = new StringBuilder();
    private double [] JieMaDouble;
    private double[] rdata;
	private static int[] steptab = {7, 8, 9, 10, 11, 12, 13, 14,
			16, 17, 19, 21, 23, 25, 28, 31,
			34, 37, 41, 45, 50, 55, 60, 66,
			73, 80, 88, 97, 107, 118, 130, 143,
			157, 173, 190, 209, 230, 253, 279, 307,
			337, 371, 408, 449, 494, 544, 598, 658,
			724, 796, 876, 963, 1060, 1166, 1282, 1411,
			1552, 1707, 1878, 2066, 2272, 2499, 2749, 3024,
			3327, 3660, 4026, 4428, 4871, 5358, 5894, 6484,
			7132, 7845, 8630, 9493, 10442, 11487, 12635, 13899,
			15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767};
	private static int[] indextab = {-1, -1, -1, -1, 2, 4, 6, 8, -1, -1, -1, -1, 2, 4, 6, 8};
	private List<Double> datas = new LinkedList<Double>();
	byte[] jieduanArray;
	int dataLen = 0,index, iLen,diff = 0;
	short samp0 = 0,sampx = 0;
	char code = 0;
    int iControl = 0;
	boolean odd = true, jieduan = false;
	int bufsize = AudioTrack.getMinBufferSize(7880, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);//一个采样点16比特-2个字节
	AudioTrack trackplayer = new AudioTrack(AudioManager.STREAM_MUSIC, 7880, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufsize * 20, AudioTrack.MODE_STREAM);//

	private int timesTest = 0;
	//播放时间的更新
	private  class MyHandler extends Handler {
		// 弱引用
		private WeakReference<MainActivity> reference;
		public MyHandler(MainActivity activity) {
			reference = new WeakReference<MainActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			//播放时间的更新
			MainActivity activity = reference.get();
			if (activity != null&&activity.callBack!=null) {
				int currentTime = activity.callBack.callCurrentTime();
				int totalTime = activity.callBack.callTotalDate();
				activity.seekBar.setMax(totalTime);
				activity.seekBar.setProgress(currentTime);
				String current = activity.format.format(new Date(currentTime));
				String total = activity.format.format(new Date(totalTime));
				activity.currentTimeTxt.setText(current);
				activity.totalTimeTxt.setText(total);
				//更新刷新波形

			}
		}
	}

	//播放服务的绑定
	private ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			callBack = (MusicServiceDemo.MyBinder)service;
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			callBack = null;
		}
	};

	private final BroadcastReceiver mLocalReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String address = intent.getStringExtra(LeProxy.EXTRA_ADDRESS);
			//接收蓝牙广播服务，并在UI界面进相应的界面显示
            //其中包括数据的基本连接断开的蓝牙的基本状态转换
			switch (intent.getAction()){
				case LeProxy.ACTION_GATT_CONNECTED:
					//蓝牙连接后的UI响应
					//ToastUtil.showMsg(mContext, R.string.scan_connected, address + " ");
					bluescan.setText(R.string.scan_connected);
					break;
				case LeProxy.ACTION_GATT_DISCONNECTED:
					//蓝牙断开后的UI响应
					//ToastUtil.showMsg(mContext, R.string.scan_disconnected, address + " ");
					bluescan.setText(R.string.scan_disconnected);
					break;
				case LeProxy.ACTION_CONNECT_ERROR:
					////蓝牙连接出现异常的UI响应
					//ToastUtil.showMsg(mContext, R.string.scan_connection_error, address + " ");
					bluescan.setText(R.string.scan_connection_error);
					break;
				case LeProxy.ACTION_CONNECT_TIMEOUT:
					//蓝牙连接过程中出现的蓝牙超时处理
					//ToastUtil.showMsg(mContext, R.string.scan_connect_timeout, address + " ");
					bluescan.setText(R.string.scan_connect_timeout);
					break;
				case LeProxy.ACTION_DATA_AVAILABLE:
					//接收蓝牙的数据的notify操作的基本数据
					//在界面显示蓝牙传输的数据
					displayRxData(intent);
					//dataProcessing(intent.getByteArrayExtra(LeProxy.EXTRA_DATA));
					break;
				case LeProxy.ACTION_GATT_SERVICES_DISCOVERED:
					//服务已找到的基本UI显示操作
					//Toast.makeText(mContext, "Services discovered: " + address, Toast.LENGTH_SHORT).show();
					bluescan.setText("发现可用蓝牙服务");
					break;
			}
		}
	};

	private void displayRxData(Intent intent) {
		//String uuid = intent.getStringExtra(LeProxy.EXTRA_UUID);
		//byte[] data = intent.getByteArrayExtra(LeProxy.EXTRA_DATA);
		//如果数据加密，这里就解密一下
		//if (mBoxEncrypt.isChecked()) data = mEncodeUtil.decodeMessage(data);
		//testLength = testLength + data.length;
		//String dataStr = "timestamp: " + TimeUtil.getTimeStamp() + '\n' + "uuid: " + uuid + '\n' + "length: " + testLength + '\n';
		//dataStr += "data: " + DataUtil.byteArrayToHex(data);
//		if (mDataType == 0) {
//			dataStr += "data: " + DataUtil.byteArrayToHex(data);
//		} else {
//			dataStr += "data: " + new String(data);
//		}
		//mTxtRxData.setText(dataStr);
		dataProcessing(intent.getByteArrayExtra(LeProxy.EXTRA_DATA));
	}

	private void dataProcessing(byte[] dataw) {
		//byte[] dataw = data;
		//长度可能超过256字节方案
		byte[] datas = jieduanIs(dataw);
		//开始 AA AA
		if (datas.length == 2 && datas[0] == -86 && datas[1] == -86) {
			//录音开始，清空历史波形显示
			mService2.updateCharts(32000);
			//开启播放流
			trackplayer.play();
			//建立文件夹和文件
			File file1 = new File(sdPath);
			if (!file1.exists()) {
				file1.mkdirs();
			}
			try {
				File filepos1 = new File(Environment.getExternalStorageDirectory() + "/RawAudio.raw");
				fos = new FileOutputStream(filepos1, true);
				bufos = new BufferedOutputStream(fos);
				//Log.e(TAG, "dataProcessing: "+"AA AA");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		//结束录制 FF FF FF FF
		else if (datas.length == 20 && datas[0] == -1 && datas[1] == -1 && datas[2] == -1 && datas[3] == -1) {
			//trackplayer.flush();
			trackplayer.stop();
			byte[] databuffer = new byte[1024];
			try {
				FileInputStream in = new FileInputStream(Environment.getExternalStorageDirectory() + "/RawAudio.raw");
				FileOutputStream out = new FileOutputStream(sdPath + "/FinalAudio.wav");
				long totalAudioLen = in.getChannel().size();
				long totalDataLen = totalAudioLen + 36;
				//long totalDataLen = totalAudioLen + 52;
				int srate = sampleRate;
				//WriteWaveFileHeader(out, totalAudioLen, totalDataLen, 8000, 1, (8000 * 256 * 4) / 505);
				//s = totalAudioLen/(256*channels)*505
				//ADWriteWaveFileHeader(out, totalAudioLen, totalDataLen, srate, 1, srate*256/505,totalAudioLen/256*505);
				WriteWaveFileHeader(out, totalAudioLen, totalDataLen, srate, 1, srate*256/505);
				while (in.read(databuffer) != -1)
					out.write(databuffer);
				in.close();
				out.close();
				//删除蓝牙缓存数据文件
				deleteFile(Environment.getExternalStorageDirectory() + "/RawAudio.raw");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				if(fos != null){
					fos.close();
					bufos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else{
			//写入数据
//			try {
//				bufos.write(dataw,0,dataw.length);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			//解码数据显示
			decodeByte(datas);
//			double [] temp = new double[]{0};
//			mService2.updateCharts(temp, 32000);
//			Log.e(TAG,"shuaxin");
		}
	}
	public  boolean deleteFile(String fileName) {
		File file = new File(fileName);
		// 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
		if (file.exists() && file.isFile()) {
			if (file.delete()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	private byte[] jieduanIs(byte[] data) {
		if (jieduan) {
			//将本次的byte数据和上次截留的byte数据拼接
			byte[] data1 = data;
			data = new byte[data1.length + jieduanArray.length];
			// 合并两个数组
			System.arraycopy(jieduanArray, 0, data, 0, jieduanArray.length);
			System.arraycopy(data1, 0, data, jieduanArray.length, data1.length);
			jieduan = false;
		}
		return data;
	}

	public void ADWriteWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate, int channels, long byteRate,long s) throws IOException {
		byte[] header = new byte[60];
		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 20; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 17; // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = 0;
		header[33] = 1;
		header[34] = 4;
		header[35] = 0;
		header[36] = 2;
		header[37] = 0;
		header[38] = (byte) 249;
		header[39] = 1;
		//添加fact数据
		header[40] = 'f';header[41]='a';header[42] = 'c';header[43] = 't';
		header[44] = 4;
		header[45] = 0;
		header[46] = 0;
		header[47] = 0;
		header[48] = (byte) (s & 0xff);
		header[49] = (byte) ((s >> 8) & 0xff);
		header[50] = (byte) ((s >> 16) & 0xff);
		header[51] = (byte) ((s >> 24) & 0xff) ;

		header[52] = 'd';
		header[53] = 'a';
		header[54] = 't';
		header[55] = 'a';
		header[56] = (byte) (totalAudioLen & 0xff);
		header[57] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[58] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[59] = (byte) ((totalAudioLen >> 24) & 0xff);
		out.write(header, 0, 60);
	}
	public void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate, int channels, long byteRate) throws IOException {
		byte[] header = new byte[44];
		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		//
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		//
		header[20] = 1; // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		//
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (2 * 16 / 8); // block align
		header[33] = 0;
		header[34] = 16; // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
		out.write(header, 0, 44);
	}
	//广播注册服务
	private IntentFilter makeFilter(){
		IntentFilter filter = new IntentFilter();
		//连上广播注册
		filter.addAction(LeProxy.ACTION_GATT_CONNECTED);
		//断开广播注册
		filter.addAction(LeProxy.ACTION_GATT_DISCONNECTED);
		//蓝牙连接异常广播注册
		filter.addAction(LeProxy.ACTION_CONNECT_ERROR);
		//蓝牙连接超时广播注册
		filter.addAction(LeProxy.ACTION_CONNECT_TIMEOUT);
		//寻找蓝牙服务广播注册
		filter.addAction(LeProxy.ACTION_GATT_SERVICES_DISCOVERED);
		//获取蓝牙数据广播注册
		filter.addAction(LeProxy.ACTION_DATA_AVAILABLE);
		return filter;
	}

	private final ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.w(TAG, "onServiceDisconnected()");
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LeProxy.getInstance().setBleService(service);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
			finish();
			return;
		}
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main1);
		//mContext = this;
		mLeProxy = LeProxy.getInstance();
		//查看是否支持手机是否支持BLE蓝牙的基本功能
		checkBLEFeature();
		initView();
		bindService(new Intent(this, BleService.class), mConnection, BIND_AUTO_CREATE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		//设置软键盘
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		//getActionBar().setDisplayHomeAsUpEnabled(true);
		//getSupportActionBar().hide();
		LocalBroadcastManager.getInstance(this).registerReceiver(mLocalReceiver, makeFilter());
	}
	//BLE蓝牙支持检验
	private void checkBLEFeature() {
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			finish();
		}

		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
	}

	private void initView() {
		//mTxtRxData = (TextView) findViewById(R.id.dis_test);
		//state_dis = (TextView) findViewById(R.id.state_dis);

		//
		seekBar = (SeekBar)findViewById(R.id.seek_bar);
		bt_play = (ImageButton)findViewById(R.id.bt_play);
		bt_pre = (ImageButton)findViewById(R.id.bt_pre);
		bt_next = (ImageButton)findViewById(R.id.bt_next);
		currentTimeTxt = (TextView)findViewById(R.id.current_time_txt);
		totalTimeTxt = (TextView)findViewById(R.id.total_time_txt);
		musicinfo = (TextView)findViewById(R.id.musicinfo);
		openMusic = (Button) findViewById(R.id.openMusic);
		clearMusic = (Button) findViewById(R.id.clearMusic);
		spinner = (Spinner) findViewById(R.id.spinner);
		rg = (RadioGroup) findViewById(R.id.rg_sex);
		position_rg = (RadioGroup) findViewById(R.id.position_rg);
		rg.setOnCheckedChangeListener(this );
		position_rg.setOnCheckedChangeListener(this );
		saveAudio = (Button) findViewById(R.id.saveAudio);
		saveAudio.setOnClickListener(this);
		saveAudio.setEnabled(false);
		bluescan = (TextView) findViewById(R.id.bluescan);
		//devicebat = (TextView) findViewById(R.id.devicebat);
		otherdis = (EditText) findViewById(R.id.otherdis);
		otherdis.setEnabled(false);
		checkButton1 = (CheckBox) findViewById(R.id.checkButton1);
		checkButton2 = (CheckBox) findViewById(R.id.checkButton2);
		checkButton3 = (CheckBox) findViewById(R.id.checkButton3);
		checkButton4 = (CheckBox) findViewById(R.id.checkButton4);
		checkButton5 = (CheckBox) findViewById(R.id.checkButton5);
		checkButton6 = (CheckBox) findViewById(R.id.checkButton6);
		checkButton7 = (CheckBox) findViewById(R.id.checkButton7);
		checkButton8 = (CheckBox) findViewById(R.id.checkButton8);
		checkButton9 = (CheckBox) findViewById(R.id.checkButton9);
		checkButton10 = (CheckBox) findViewById(R.id.checkButton10);
		checkButton11 = (CheckBox) findViewById(R.id.checkButton11);
		checkButton12 = (CheckBox) findViewById(R.id.checkButton12);
		checkButton13 = (CheckBox) findViewById(R.id.checkButton13);
		checkButton14 = (CheckBox) findViewById(R.id.checkButton14);
		checkButton15 = (CheckBox) findViewById(R.id.checkButton15);
		checkButton16 = (CheckBox) findViewById(R.id.checkButton16);
		checkButton17 = (CheckBox) findViewById(R.id.checkButton17);
		checkButton18 = (CheckBox) findViewById(R.id.checkButton18);
		checkButton1.setOnCheckedChangeListener(this);
		checkButton2.setOnCheckedChangeListener(this);
		checkButton3.setOnCheckedChangeListener(this);
		checkButton4.setOnCheckedChangeListener(this);
		checkButton5.setOnCheckedChangeListener(this);
		checkButton6.setOnCheckedChangeListener(this);
		checkButton7.setOnCheckedChangeListener(this);
		checkButton8.setOnCheckedChangeListener(this);
		checkButton9.setOnCheckedChangeListener(this);
		checkButton10.setOnCheckedChangeListener(this);
		checkButton11.setOnCheckedChangeListener(this);
		checkButton12.setOnCheckedChangeListener(this);
		checkButton13.setOnCheckedChangeListener(this);
		checkButton14.setOnCheckedChangeListener(this);
		checkButton15.setOnCheckedChangeListener(this);
		checkButton16.setOnCheckedChangeListener(this);
		checkButton17.setOnCheckedChangeListener(this);
		checkButton18.setOnCheckedChangeListener(this);
		disfinish = (Button) findViewById(R.id.disfinish);
		disfinish.setOnClickListener(this);
		bt_play.setOnClickListener(this);
		bt_pre.setOnClickListener(this);
		bt_next.setOnClickListener(this);
		openMusic.setOnClickListener(this);
		clearMusic.setOnClickListener(this);
		//实时显示初始化操作
		initWave();
	}
	private void initWave() {
		mRightCurveLayout = (LinearLayout) findViewById(R.id.left_temperature_curve);
		mService2 = new ChartService(this);
		mService2.setXYMultipleSeriesDataset("心音曲线图");
		mService2.setXYMultipleSeriesRenderer(40000, 1, "实时曲线图", "采样点", "相对幅度", Color.RED, Color.BLACK, Color.BLACK, Color.WHITE);
		mView2 = mService2.getGraphicalView();
		//将图表添加到布局容器中
		mRightCurveLayout.addView(mView2, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		mService2.updateCharts(32000);
		//final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		//mBluetoothAdapter = bluetoothManager.getAdapter();
		final List<String> datas = new ArrayList<String>();
		for (int i = 1; i < 80; i++)  datas.add("" + i);
		MyAdapter adapter = new MyAdapter(this);
		spinner.setAdapter(adapter);
		adapter.setDatas(datas);
		//adapter.setDropDownViewResource(R.layout.spinner_style);
		spinner.setSelection(0, false);
		/**选项选择监听*/
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				nianling = datas.get(position);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}
	private void scanLeDevice(final boolean enable) {
		if (enable) {
			if (mBluetoothAdapter.isEnabled()) {
				if (mScanning)
					return;
				mScanning = true;
				mHandlerScan.postDelayed(mScanRunnable, SCAN_PERIOD);
				mBluetoothAdapter.startLeScan(mLeScanCallback);
			} else {
				//ToastUtil.showMsg(this, R.string.scan_bt_disabled);
			}
		} else {
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			mHandlerScan.removeCallbacks(mScanRunnable);
			mScanning = false;
		}
	}
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (device.getAddress().equals("0C:61:CF:39:A9:00"))
					{
						Log.e("MainActivity",device.getAddress());
						mLeProxy.connect(device.getAddress().trim(), false);// TODO
						scanLeDevice(false);
					}
				}
			});
		}
	};
	/**
	 * 播放音乐通过Binder接口实现
	 */
	public void playerMusicByIBinder() {
		boolean playerState = callBack.isPlayerMusic();
		if (playerState) {
			bt_play.setImageResource(R.drawable.pause);
		} else {
			bt_play.setImageResource(R.drawable.play);
		}
	}
	private final Runnable mScanRunnable = new Runnable() {
		@Override
		public void run() {
			scanLeDevice(false);
		}
	};
	@Override
	protected void onResume() {
		super.onResume();
		//打开蓝牙操作
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		scanLeDevice(true);
		mHandlerScan.postDelayed(new Runnable() {
			@Override
			public void run() {
				scanLeDevice(true);
			}
		}, 100);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_CANCELED) {
			finish();
			return;
		}
		// 根据上面发送过去的请求吗来区别
		switch (requestCode) {
			case Activity.RESULT_CANCELED:
				break;
			case 1:
				/** 接收音乐列表资源 */
				if(data.getIntExtra("isTouch",0)==11){
					if(binderFlag){
						unbindService(conn);
						callBack = null;
					}
					musicBeanList = data.getParcelableArrayListExtra("MUSIC_LIST");
					int currentPosition = data.getIntExtra("CURRENT_POSITION", 0);
					Intent intent = new Intent(this, MusicServiceDemo.class);
					intent.putParcelableArrayListExtra("MUSIC_LIST", musicBeanList);
					intent.putExtra("CURRENT_POSITION", currentPosition);
					//startService(intent);
					bindService(intent, conn, Service.BIND_AUTO_CREATE);
					JieMaDouble = null;
					iControl = 0;
					waveAgain = true;
					binderFlag = true;
					musicinfo.setText(musicBeanList.get(currentPosition).getTitle());
					//打开音频，显示音频的波形图
					try {
						FileInputStream in = new FileInputStream(musicBeanList.get(currentPosition).getMusicPath());
						byte[] databuffer1 = new byte[in.available()];
						while (in.read(databuffer1) != -1) {
						}
						Log.e(TAG, databuffer1.length+"");
						//JieMaDouble = decodeByte(Arrays.copyOfRange(databuffer1,60,databuffer1.length),true);
						JieMaDouble = ByteToDouble(Arrays.copyOfRange(databuffer1,60,databuffer1.length));
						in.close();
						//stringBuffer1 = stringBuffer;
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					seekTime();
					forSeekBar();
				}
				break;
			default:
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	//播放音频的实时流文件的解码数据
	private double[] decodeByte(byte[] data,boolean startFlag) {
		//解析完成后实时显示
		int originalLen = data.length;
		Log.e(TAG,"originalLen:"+originalLen);
		//double [] decodeArray = new double[(originalLen-60)/256*505+(originalLen-60)%256 >0 ?(((originalLen-60)%256-4)*2+1):0];
		//double [] decodeArray = new double[originalLen/256*505];
		double [] decodeArray = new double[0];
		int len = (int) Math.ceil(data.length/256);
		int iloop = 0;
		boolean fin = false;
		while(len!=0){
			Log.e(TAG,"decodeArray.length:"+decodeArray.length+" ");
			byte [] tempByte;
			if(!fin && (iloop+256)<originalLen)
				tempByte = Arrays.copyOfRange(data,iloop,iloop+256);
			else
				tempByte = Arrays.copyOfRange(data,iloop,originalLen);
			//获取其数据后进行相应的存储
			Decode_IMA_ADPCM_4BIT_MONO(tempByte, tempByte.length, true);
			decodeArray = concat(decodeArray,rdata);
			//mService2.updateCharts(Decode_IMA_ADPCM_4BIT_MONO(tempByte, tempByte.length, true), 32000);
			iloop = iloop+256;
			len = len-1;
		}
		return decodeArray;
	}
	double [] concat(double[] a, double[] b) {
		double[] c= new double[a.length+b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}
	//补码字节处理
	public double [] array(String res){
		double []c = new double[res.length()/4];
		int j = 0;
		for(int i = 0;i<res.length();){
			if((i+4)>res.length())
				break;
			String a = res.substring(i+2,i+4)+res.substring(i,i+2);
			int bm = Integer.parseInt(a,16);
			if(bm<32768)
				c[j] = bm/32768.0;
			else
			{
				c[j] = (-65536-(~bm+1))/32768.0;
			}
			i = i+4;
			j = j+1;
		}
		return c;
	}
	//求得byte数组的逻辑长度
	public int getRealLength(byte[] a){
		int i=0;
		for(;i<a.length;i++)
		{
			if(a[i]=='\0')
				break;
		}
		return i;
	}
    /**
     * //解码实时数据流文件
     * @param data
     */
    private void decodeByte(byte[] data) {
        //解析完成后实时显示
        if (dataLen == 0 && (dataLen + data.length) > 2) {
        	timesTest++;
        	Log.e(TAG,"开始:"+timesTest+" "+data.length);
            dataLen = dataLen + data.length;
			Decode_IMA_ADPCM_4BIT_MONO(data, data.length, true);
            mService2.updateCharts(rdata, 32000);
        } else if ((dataLen + data.length) < 256 && (dataLen + data.length) > 2) {
			timesTest++;
			Log.e(TAG,"长度在256内:"+timesTest+" "+data.length);
            dataLen = dataLen + data.length;
			Decode_IMA_ADPCM_4BIT_MONO(data, data.length, false);
            mService2.updateCharts(rdata, 32000);
        } else if ((dataLen + data.length) >= 256) {
            if ((dataLen + data.length) == 256) {
            	timesTest++;
				Log.e(TAG,"长度为256:"+timesTest+" "+data.length);
                dataLen = dataLen + data.length;
				Decode_IMA_ADPCM_4BIT_MONO(data, data.length, false);
                mService2.updateCharts(rdata, 32000);
                dataLen = 0;
            } else {
            	timesTest++;
				Log.e(TAG,"长度大于256:"+timesTest+" "+data.length);
                dataLen = dataLen + data.length;
                //说明两块有重叠
                //step 1
                //将前一块的数据截取出来
                int tempLength = data.length + 256 - dataLen;
                byte[] temp = new byte[tempLength];
                for (int i = 0; i < tempLength; i++)
                    temp[i] = data[i];
				Decode_IMA_ADPCM_4BIT_MONO(temp, tempLength, false);
                mService2.updateCharts(rdata, 32000);
                //step 2
                jieduanArray = new byte[dataLen - 256];
                for (int i = tempLength, j = 0; i < dataLen - 256; j++, i++) {
                    jieduanArray[j] = data[i];
                }
                jieduan = true;
                dataLen = 0;
            }
        }
    }
    //实时播放时显示波形的数据

	/**
	 *
	 * @param bytedata 传入待转化的字节数组文件
	 * @return
	 */
	public double [] ByteToDouble(byte [] bytedata){
    	int dataLength = bytedata.length/2;
		int startValue = 0;
		double temp = 0;
		double reData [] = new double[dataLength];
    	for(int i = 0;i<dataLength;i++){
			startValue = Integer.parseInt(String.format("%02X", bytedata[i*2+1]) + String.format("%02X", bytedata[i*2]), 16);
			if (startValue < 32768)
				temp = (short) startValue/32768.0;
			else
				temp = (short) (-65536 - (~startValue + 1))/32768.0;
			reData[i] = temp;
		}
       return reData;
	}
    /**
     * @param imaData 压缩的数据字节流
     * @param iDataLen 压缩的数据字节流的数据长度
     * @param start_Flag 数据块的传输是否完毕
     * @return
     */
    //adpcm解码测试程序

    public void Decode_IMA_ADPCM_4BIT_MONO(byte[] imaData, int iDataLen, boolean start_Flag) {
        int tl = start_Flag ? ((iDataLen - 4) * 2 + 1) : iDataLen * 2;
        rdata = new double[tl];
        byte[] pcmData = new byte[tl * 2];
        iLen = 0;
        int iLen1 = 0;
        int i = 0;
        odd = true;
        //数据的长度不够4字节说明数据块存在一定的问题
        if (start_Flag) {
            int startValue = Integer.parseInt(String.format("%02X", imaData[1]) + String.format("%02X", imaData[0]), 16);
            if (startValue < 32768)
                samp0 = (short) startValue;
            else
                samp0 = (short) (-65536 - (~startValue + 1));
            //提取索引位置
            index = imaData[2] & 0xFF;
            if (index < 0) index = 0;
            if (index > 88) index = 88;
            sampx = samp0;
            odd = true;
            rdata[iLen++] = sampx / 32768.0;
            //datas.add(sampx / 32768.0);
            pcmData[iLen1++] = imaData[0];
            pcmData[iLen1++] = imaData[1];
            i = i + 4;
        }
        while (i < iDataLen) {
            //周期性取一个字节高低位,先去一个字节的低4位，下次循环再取一个字节的高四位
            char a = ((char) (imaData[i] & 0xFF));
            if (odd)
                code = (char) (a & 0x0F);
            else
                code = (char) (a >> 4);
            diff = 0;
            if ((code & 4) != 0) diff = diff + steptab[index];
            if ((code & 2) != 0) diff = diff + (steptab[index] >> 1);
            if ((code & 1) != 0) diff = diff + (steptab[index] >> 2);
            diff = diff + (steptab[index] >> 3);

            if ((code & 8) != 0) diff = -diff;
            if ((sampx + diff) < -32768)
                sampx = -32768;
            else if ((sampx + diff) > 32767)
                sampx = 32767;
            else sampx = (short) (sampx + diff);
            if (sampx >= 0) {
                pcmData[iLen1++] = (byte) (sampx % 256);
                pcmData[iLen1++] = (byte) (sampx / 256);
            } else {
                pcmData[iLen1++] = (byte) ((sampx + 32768) % 256);
                pcmData[iLen1++] = (byte) ((sampx + 32768) / 256 + 128);
            }
            rdata[iLen++] = sampx / 32768.0;
            //datas.add(sampx / 32768.0);
            index = index + indextab[code - 0];
            //防止数据索引越界
            if (index < 0) index = 0;
            if (index > 88) index = 88;
            odd = !odd;
            //偶数变奇数时，说明取到下一个数据
            if (odd)
                i++;
        }
        //ByteBuffer buff = ByteBuffer.allocate(1024);
        //buff.get(pcmData);
        //trackplayer.write(buff,pcmData.length,WRITE_BLOCKING);
        trackplayer.write(pcmData, 0, pcmData.length);
        //将byte数据写入到Audio.raw文件中
        try {
            //trackplayer.write(pcmData, 0, pcmData.length);
            //trackplayer.flush();
            bufos.write(pcmData, 0, pcmData.length);
            //bufos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

//		try {
//			Thread.sleep(2);
//			trackplayer.write(pcmData, 0, pcmData.length);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
        //return rdata;
    }
	//通知时间的播放的更新过程，主线程的更新UI
    //通知时间的播放的更新过程，主线程的更新UI
    private void seekTime(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mFlag) {
                    if (callBack != null) {
                        mHandler.sendMessage(Message.obtain());
                        try {
                            Thread.sleep(1000);
							if((iControl+7880)<JieMaDouble.length)
							{
								Log.e(TAG,""+iControl+" 1轮"+" "+JieMaDouble.length);
								if(waveAgain){
									//Log.e(TAG,"Start");
									mService2.updateCharts(32000);
									mService2.updateCharts(Arrays.copyOfRange(JieMaDouble, 0, 7880),32000);
									iControl = iControl+7880;
									waveAgain = false;
								}
								else {
									//防止尾部有数据
									if ((iControl + 7880 * 2) > JieMaDouble.length) {
										mService2.updateCharts(Arrays.copyOfRange(JieMaDouble, iControl, JieMaDouble.length), 32000);
										iControl = 0;
										waveAgain = true;
									} else {
										mService2.updateCharts(Arrays.copyOfRange(JieMaDouble, iControl, 7880 + iControl), 32000);
										iControl = iControl + 7880;
									}
								}

							}
							else if(iControl<JieMaDouble.length){
								mService2.updateCharts(Arrays.copyOfRange(JieMaDouble, iControl, JieMaDouble.length), 32000);
								iControl = 0;
								waveAgain = true;
							}
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }).start();
    }

    //seekBar的时间的更新，调取主线程的更新
    private void forSeekBar(){
        mProgress = 0;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (callBack != null) {
                    mProgress = progress;
                    //Toast.makeText(MainActivity.this, ""+mProgress, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (callBack != null) {
                    //音乐服务
                    callBack.iSeekTo(mProgress);
                }
            }
        });
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocalReceiver);
        if(binderFlag){
            unbindService(conn);
            callBack = null;
        }
		unbindService(mConnection);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			// 播放或者暂停
			case R.id.bt_play:
				if (binderFlag) {
					playerMusicByIBinder();
					musicinfo.setText(musicBeanList.get(value).getTitle());
				} else
					Toast.makeText(this, "请打开本地音乐", Toast.LENGTH_SHORT).show();
				break;
			case R.id.bt_pre:
				if (binderFlag) {
					callBack.isPlayPre();
					musicinfo.setText(musicBeanList.get(value).getTitle());
				} else
					Toast.makeText(this, "请打开本地音乐", Toast.LENGTH_SHORT).show();
				break;
			case R.id.bt_next:
				if (binderFlag) {
					callBack.isPlayNext();
					musicinfo.setText(musicBeanList.get(value).getTitle());
				} else
					Toast.makeText(this, "请打开本地音乐", Toast.LENGTH_SHORT).show();
				break;
			case R.id.openMusic:
				Intent musicIntent = new Intent(this, MusicListActivity.class);
				startActivityForResult(musicIntent, 1);
				Intent intent2 = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				intent2.setData(Uri.fromFile(new File(sdPath)));
				MainActivity.this.sendBroadcast(intent2);
				break;
			case R.id.clearMusic:
				deleteFile(sdPath + "/FinalAudio.wav");
				mService2.updateCharts(32000);
				Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				intent.setData(Uri.fromFile(new File(sdPath)));
				MainActivity.this.sendBroadcast(intent);
				break;
			case R.id.otherdis:
				additondisease = otherdis.getText().toString();
				break;
			case R.id.disfinish:
				//将上次保留的选择进行清空初始化
				sb.delete(0, sb.length());
				for (int i = 0; i < disease.size(); i++) {
					//把选择的爱好添加到string尾部
					if (i == (disease.size() - 1))
						sb.append(disease.get(i));
					else
						sb.append(disease.get(i) + ",");

				}
				saveAudio.setEnabled(true);
				Toast.makeText(this, "finish", Toast.LENGTH_SHORT).show();
				break;
			case R.id.saveAudio:
				Toast.makeText(this, "save", Toast.LENGTH_SHORT).show();
				if (position == null || position.length() == 0) {
					Toast.makeText(MainActivity.this, "请选择录制位置选项", Toast.LENGTH_SHORT).show();
				} else if (xingbie == null || xingbie.length() == 0) {
					Toast.makeText(MainActivity.this, "请选择性别选项", Toast.LENGTH_SHORT).show();
				} else if (nianling == null || nianling.length() == 0) {
					Toast.makeText(MainActivity.this, "请选择年龄选项", Toast.LENGTH_SHORT).show();
				} else {
					mService2.updateCharts(32000);
					//oldPath like "mnt/sda/sda1/我.png"
					File file = new File(sdPath + "/FinalAudio.wav");
					file.renameTo(new File(sdPath + "/" + sDateFormat.format(new Date()) + "-" + position + "-" + sb + additondisease + "-" + xingbie + "-" + nianling + ".wav"));
					Intent intent1 = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
					intent1.setData(Uri.fromFile(new File(sdPath)));
					MainActivity.this.sendBroadcast(intent1);
				}
				break;
		}
	}
		@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked){
			//添加到疾病数组
			if(buttonView.getText().toString().trim().equals("其它"))
				otherdis.setEnabled(true);
			else
				disease.add(buttonView.getText().toString().trim());
		}else {
			//从数组中移除
			if(buttonView.getText().toString().trim().equals("其它"))
				otherdis.setEnabled(false);
			else
				disease.remove(buttonView.getText().toString().trim());
		}

	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
     // 选中状态改变时被触发
		switch (checkedId) {
			case R.id.rb_FeMale:
				// 当用户选择女性时
				xingbie = "女";
				break;
			case R.id.rb_Male:
				// 当用户选择男性时
				xingbie = "男";
				break;
			case R.id.position_rb1:
				// 当用户选择录制位置1时
				position = "p1";
				break;
			case R.id.position_rb2:
				// 当用户选择录制位置2时
				position = "p2";
				break;
			case R.id.position_rb3:
				// 当用户选择录制位置3时
				position = "p3";
				break;
			case R.id.position_rb4:
				// 当用户选择录制位置4时
				position = "p4";
				break;
		}
	}
}
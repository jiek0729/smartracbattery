package com.smartracumn.smartracbattery;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class SmartBatteryService extends Service {

	private final String TAG = getClass().getSimpleName();
	private final IBinder mBinder = new MyBinder();
	private ArrayList<BatteryRecord> records = new ArrayList<BatteryRecord>();
	private BatteryRecordsDataSource dataSource;
	private OnRecordUpdate updateCallBack;

	public interface OnRecordUpdate {
		void OnRecordInserted(BatteryRecord newRecord);
	}

	private final BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			status = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		}
	};

	private int rawLevel = -1;
	private int scale = -1;
	private int status = -1;

	@Override
	public void onCreate() {
		super.onCreate();

		dataSource = new BatteryRecordsDataSource(this);

		this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		this.unregisterReceiver(mBatInfoReceiver);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO do something useful
		Log.i(TAG, "service on start command.");
		Calendar c = Calendar.getInstance();
		if (rawLevel >= 0 && scale > 0) {
			BatteryRecord record = dataSource.createComment(c.getTime(),
					rawLevel * 100 / scale, status > 0);
			if (updateCallBack != null) {
				updateCallBack.OnRecordInserted(record);
			}
		}
		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO for communication return IBinder implementation
		return mBinder;
	}

	public List<BatteryRecord> getRecords() {
		dataSource.open();
		List<BatteryRecord> records = dataSource.getAllRecords();
		dataSource.close();
		return records;
	}

	public List<BatteryRecord> getRecordsForDateRange(Date start, Date end) {
		dataSource.open();
		List<BatteryRecord> records = dataSource.getRecordsForDateRange(start,
				end);
		dataSource.close();

		return records;
	}

	public void registerUpdateListener(OnRecordUpdate callback) {
		this.updateCallBack = callback;
	}

	public void unregisterUpdateListener(OnRecordUpdate callback) {
		if (callback.equals(this.updateCallBack)) {
			this.updateCallBack = null;
		}
	}

	public void deleteRecords() {
		dataSource.open();
		dataSource.deleteAll();
		dataSource.close();
	}

	public void deletePrevRecords(Date date) {
		dataSource.open();
		dataSource.deletePrevRecord(date);
		dataSource.close();
	}

	public class MyBinder extends Binder {
		SmartBatteryService getService() {
			return SmartBatteryService.this;
		}
	}
}

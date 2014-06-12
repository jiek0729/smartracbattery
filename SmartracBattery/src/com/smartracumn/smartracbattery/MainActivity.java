package com.smartracumn.smartracbattery;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private SmartBatteryService service;

	private SimpleDateFormat iso8601Format = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	private List<BatteryRecord> recordList;

	BatteryRecordArrayAdapter adapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_activity);
		recordList = new ArrayList<BatteryRecord>();
		adapter = new BatteryRecordArrayAdapter(this, recordList);

		ListView lv = (ListView) findViewById(R.id.record_list);
		Button b = (Button) findViewById(R.id.show_all);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				if (service != null) {
					makeToastText("Number of elements "
							+ service.getRecords().size());
					recordList.clear();
					recordList.addAll(service.getRecords());
					adapter.notifyDataSetChanged();
				}
			}
		});
		Button d = (Button) findViewById(R.id.delete_all);
		d.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (service != null) {
					makeToastText(service.getRecords().size()
							+ "elements deleted");
					// Toast.makeText(
					// this,
					// "Number of elements " + service.getRecords().size(),
					// Toast.LENGTH_SHORT).show();
					recordList.clear();
					service.deleteRecords();
					adapter.notifyDataSetChanged();
				}
			}
		});

		lv.setAdapter(adapter);
		// TODO Auto-generated method stub
	}

	private void makeToastText(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = new Intent(this, SmartBatteryService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unbindService(mConnection);
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			SmartBatteryService.MyBinder b = (SmartBatteryService.MyBinder) binder;
			service = b.getService();
			Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT)
					.show();
		}

		public void onServiceDisconnected(ComponentName className) {
			service = null;
		}
	};

	public class BatteryRecordArrayAdapter extends ArrayAdapter<BatteryRecord> {
		private final Context context;
		private final List<BatteryRecord> values;

		public BatteryRecordArrayAdapter(Context context,
				List<BatteryRecord> values) {
			super(context, R.layout.item, values);
			this.context = context;
			this.values = values;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.item, parent, false);
			TextView timeView = (TextView) rowView.findViewById(R.id.time);
			TextView percentageView = (TextView) rowView
					.findViewById(R.id.percentage);
			TextView chargingView = (TextView) rowView
					.findViewById(R.id.is_charging);
			timeView.setText(iso8601Format.format(values.get(position)
					.getTime()));
			percentageView.setText(values.get(position).getPercentage() + "%");
			chargingView.setText(values.get(position).isCharging() ? "Charging"
					: "Unpluged");

			return rowView;
		}
	}
}

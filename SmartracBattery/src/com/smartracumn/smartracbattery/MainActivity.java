package com.smartracumn.smartracbattery;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
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
	private final String TAG = getClass().getSimpleName();

	private SmartBatteryService service;

	private SimpleDateFormat iso8601Format = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	private List<BatteryRecord> recordList;

	private BatteryRecordArrayAdapter adapter;

	private DialogFragment timePickerFragment;

	private Date selectedDate;

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_activity);
		recordList = new ArrayList<BatteryRecord>();
		adapter = new BatteryRecordArrayAdapter(this, recordList);
		timePickerFragment = new DialogFragment();
		selectedDate = getCurrentDate();

		ListView lv = (ListView) findViewById(R.id.record_list);
		Button b = (Button) findViewById(R.id.show_all);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				if (service != null) {
					List<BatteryRecord> records = service.getRecords();
					makeToastText("Number of elements " + records.size());
					recordList.clear();
					recordList.addAll(records);
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
					recordList.clear();
					service.deleteRecords();
					adapter.notifyDataSetChanged();
				}
			}
		});
		Button p = (Button) findViewById(R.id.pick_date);
		p.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				DialogFragment newFragment = new DatePickerFragment();
				newFragment.show(MainActivity.this.getFragmentManager(),
						"datePicker");
			}
		});

		lv.setAdapter(adapter);

		Button e = (Button) findViewById(R.id.export_button);
		e.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Create DirectoryChooserDialog and register a callback
				DirectoryChooserDialog directoryChooserDialog = new DirectoryChooserDialog(
						MainActivity.this,
						new DirectoryChooserDialog.ChosenDirectoryListener() {

							@Override
							public void onChosenDir(String chosenDir) {
								File dir = new File(chosenDir);

								File file = new File(dir, "smartracBattery-"
										+ dateFormat.format(selectedDate)
										+ ".txt");

								if (!file.exists()) {
									try {
										file.createNewFile();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}

								if (writeToFile(file)) {
									Toast.makeText(
											MainActivity.this,
											file.getName()
													+ " created in directory: "
													+ chosenDir,
											Toast.LENGTH_LONG).show();
								} else {
									Toast.makeText(MainActivity.this,
											"File NOT created",
											Toast.LENGTH_LONG).show();
								}

							}
						});

				directoryChooserDialog.chooseDirectory();
			}
		});
		// TODO Auto-generated method stub
	}

	private boolean writeToFile(File file) {
		try {
			// If file does not exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getPath());
			BufferedWriter bw = new BufferedWriter(fw);
			for (BatteryRecord record : recordList) {
				bw.write(record.toString());
				bw.write(System.getProperty("line.separator"));
			}
			bw.close();
			Log.i(TAG, "Write file sucess");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private void makeToastText(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	public void setSelectedDate(Date date) {
		selectedDate = date;
		TextView tv = (TextView) findViewById(R.id.selected_date);
		tv.setText(dateFormat.format(date));
		if (service != null) {
			Calendar calendar = Calendar.getInstance(TimeZone
					.getTimeZone("UTC"));
			calendar.setTime(date);
			calendar.add(Calendar.DATE, 1);
			Date end = calendar.getTime();
			List<BatteryRecord> records = service.getRecordsForDateRange(date,
					end);
			recordList.clear();
			recordList.addAll(records);
			adapter.notifyDataSetChanged();
		}
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

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		// Save UI state changes to the savedInstanceState.
		// This bundle will be passed to onCreate if the process is
		// killed and restarted.
		Log.i(TAG,
				"Save Instance State: selected date: "
						+ dateFormat.format(selectedDate));
		savedInstanceState.putString("selectedDate",
				dateFormat.format(selectedDate));
		// etc.
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Log.i(TAG, "Restore Instance State.");
		// Restore UI state from the savedInstanceState.
		// This bundle has also been passed to onCreate.
		try {
			selectedDate = dateFormat.parse(savedInstanceState
					.getString("selectedDate"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			selectedDate = getCurrentDate();
		}
	}

	private Date getCurrentDate() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			SmartBatteryService.MyBinder b = (SmartBatteryService.MyBinder) binder;
			service = b.getService();
			Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT)
					.show();
			setSelectedDate(selectedDate);
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

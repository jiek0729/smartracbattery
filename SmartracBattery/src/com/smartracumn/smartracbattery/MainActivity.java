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
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.smartracumn.smartracbattery.SmartBatteryService.OnRecordUpdate;

public class MainActivity extends Activity {
	private final String TAG = getClass().getSimpleName();

	private SmartBatteryService service;

	protected List<BatteryRecord> recordList;

	private DialogFragment timePickerFragment;

	private RecordListFragment recordListFragment;

	private RecordChartFragment recordChartFragment;

	private Date selectedDate;

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_activity);
		recordList = new ArrayList<BatteryRecord>();
		timePickerFragment = new DialogFragment();
		recordListFragment = new RecordListFragment();
		recordChartFragment = new RecordChartFragment();
		selectedDate = getCurrentDate();

		if (savedInstanceState == null) {
			FragmentTransaction ft = getFragmentManager().beginTransaction();

			ft.add(R.id.content, recordListFragment).commit();
		}

		attachCallbacks();
	}

	private void attachCallbacks() {
		Button c = (Button) findViewById(R.id.chart_button);
		c.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ToggleButton b = (ToggleButton) v;
				FragmentTransaction ft = getFragmentManager()
						.beginTransaction();

				if (b.isChecked()) {
					ft.replace(R.id.content, recordChartFragment);
				} else {
					ft.replace(R.id.content, recordListFragment);
				}

				ft.commit();
			}
		});

		// Button b = (Button) findViewById(R.id.show_all);
		// b.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View view) {
		// if (service != null) {
		// List<BatteryRecord> records = service.getRecords();
		// makeToastText("Number of elements " + records.size());
		// recordList.clear();
		// recordList.addAll(records);
		// recordListFragment.notifyDataSetChanged();
		// }
		// }
		// });

		Button p = (Button) findViewById(R.id.pick_date);
		p.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				DialogFragment newFragment = new DatePickerFragment();
				newFragment.show(MainActivity.this.getFragmentManager(),
						"datePicker");
			}
		});
	}

	private void onDeleteAllAction() {
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.delete_all)
				.setMessage(R.string.really_delete)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								// Stop the activity
								new DeleteRecordsTask().execute();
							}

						}).setNegativeButton(R.string.no, null).show();
	}

	private void openFileBrowser() {
		DirectoryChooserDialog directoryChooserDialog = new DirectoryChooserDialog(
				MainActivity.this,
				new DirectoryChooserDialog.ChosenDirectoryListener() {

					@Override
					public void onChosenDir(String chosenDir) {
						File dir = new File(chosenDir);

						File file = new File(dir, "smartracBattery-"
								+ dateFormat.format(selectedDate) + ".txt");

						if (!file.exists()) {
							try {
								file.createNewFile();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

						new ExportTask().execute(file);
					}
				});

		directoryChooserDialog.chooseDirectory();
	}

	private void makeToastText(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	public void setSelectedDate(Date date) {
		selectedDate = date;
		TextView tv = (TextView) findViewById(R.id.selected_date);
		tv.setText(dateFormat.format(date));
		if (service != null) {
			new LoadRecordsTask().execute(date);
		}
	}

	private void updateRecords(List<BatteryRecord> records) {
		recordList.clear();
		for (BatteryRecord record : records) {
			if (recordList.size() == 0
					|| recordList.get(recordList.size() - 1).getPercentage() != record
							.getPercentage()
					|| recordList.get(recordList.size() - 1).isCharging() != record
							.isCharging()) {
				recordList.add(record);
			}
		}
		makeToastText("Number of Records " + records.size());
		recordListFragment.notifyDataSetChanged();
		recordChartFragment.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_export:
			openFileBrowser();
			return true;
		case R.id.action_delete_all:
			onDeleteAllAction();
			return true;
		default:
			return super.onOptionsItemSelected(item);
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
			service.registerUpdateListener(updateCallback);
			Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT)
					.show();
			setSelectedDate(selectedDate);
		}

		public void onServiceDisconnected(ComponentName className) {
			service.unregisterUpdateListener(updateCallback);
			service = null;
		}
	};

	private OnRecordUpdate updateCallback = new OnRecordUpdate() {

		@Override
		public void OnRecordInserted(BatteryRecord newRecord) {
			Date time = newRecord.getTime();
			if (time.getYear() == selectedDate.getYear()
					&& time.getMonth() == selectedDate.getMonth()
					&& time.getDate() == selectedDate.getDate()
					&& (recordList.get(recordList.size() - 1).getPercentage() != newRecord
							.getPercentage() || recordList.get(
							recordList.size() - 1).isCharging() != newRecord
							.isCharging())) {
				makeToastText("New record inserted");
				MainActivity.this.recordList.add(newRecord);
				recordListFragment.notifyDataSetChanged();
				recordChartFragment.notifyDataSetChanged();
			}

		}
	};

	private class LoadRecordsTask extends
			AsyncTask<Date, Integer, List<BatteryRecord>> {

		protected void onProgressUpdate(Integer... progress) {
			setProgress(progress[0]);
		}

		protected void onPostExecute(List<BatteryRecord> records) {
			updateRecords(records);
		}

		@Override
		protected List<BatteryRecord> doInBackground(Date... params) {
			// TODO Auto-generated method stub
			Calendar calendar = Calendar.getInstance(TimeZone
					.getTimeZone("UTC"));
			calendar.setTime(params[0]);
			calendar.add(Calendar.DATE, 1);
			Date end = calendar.getTime();
			return service.getRecordsForDateRange(params[0], end);
		}
	}

	private class DeleteRecordsTask extends AsyncTask<Void, Integer, Boolean> {

		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
		}

		protected void onPostExecute(Boolean params) {
			if (params) {
				// recordList.clear();
				// recordListFragment.notifyDataSetChanged();
				// recordChartFragment.notifyDataSetChanged();

				makeToastText("Records older than " + offset + " is deleted");
			}
		}

		private String offset;

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			if (service != null) {
				Calendar c = Calendar.getInstance();
				c.setTime(selectedDate);
				c.set(Calendar.HOUR, 0);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.MILLISECOND, 0);
				offset = dateFormat.format(c.getTime());
				service.deletePrevRecords(c.getTime());
				return true;
			}

			return false;
		}
	}

	private class ExportTask extends AsyncTask<File, Integer, Boolean> {

		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
		}

		protected void onPostExecute(Boolean params) {
			if (params) {
				Toast.makeText(MainActivity.this,
						file + " created in directory: " + dir,
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(MainActivity.this, "Unable to create file",
						Toast.LENGTH_LONG).show();
			}
		}

		private String file;
		private String dir;

		@Override
		protected Boolean doInBackground(File... params) {
			// TODO Auto-generated method stub
			file = params[0].getName();
			dir = params[0].getParent();

			try {
				// If file does not exists, then create it
				if (!params[0].exists()) {
					params[0].createNewFile();
				}
				FileWriter fw = new FileWriter(params[0].getPath());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write("Time, Percentage, State");
				bw.write(System.getProperty("line.separator"));
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
	}
}

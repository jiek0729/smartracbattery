package com.smartracumn.smartracbattery;

import java.text.SimpleDateFormat;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class RecordListFragment extends Fragment {
	private final String TAG = getClass().getName();

	private List<BatteryRecord> recordList;

	private BatteryRecordArrayAdapter adapter;

	@Override
	public void onAttach(Activity activity) {
		Log.i(TAG, getClass().getSimpleName() + ":entered onAttach()");
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, getClass().getSimpleName() + ":entered onCreate()");
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		MainActivity act = (MainActivity) getActivity();
		recordList = act.recordList;

		return inflater.inflate(R.layout.record_list, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.i(TAG, getClass().getSimpleName() + ":entered onActivityCreated()");
		super.onActivityCreated(savedInstanceState);
		adapter = new BatteryRecordArrayAdapter(getActivity(), this.recordList);
		ListView lv = (ListView) getView().findViewById(R.id.record_list);
		lv.setAdapter(adapter);
	}

	public void notifyDataSetChanged() {
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onStart() {
		Log.i(TAG, getClass().getSimpleName() + ":entered onStart()");
		super.onStart();
	}

	@Override
	public void onResume() {
		Log.i(TAG, getClass().getSimpleName() + ":entered onResume()");
		super.onResume();
	}

	@Override
	public void onPause() {
		Log.i(TAG, getClass().getSimpleName() + ":entered onPause()");
		super.onPause();
	}

	@Override
	public void onStop() {
		Log.i(TAG, getClass().getSimpleName() + ":entered onStop()");
		super.onStop();
	}

	@Override
	public void onDetach() {
		Log.i(TAG, getClass().getSimpleName() + ":entered onDetach()");
		super.onDetach();
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, getClass().getSimpleName() + ":entered onDestroy()");
		super.onDestroy();
	}

	@Override
	public void onDestroyView() {
		Log.i(TAG, getClass().getSimpleName() + ":entered onDestroyView()");
		super.onDestroyView();
	}

	public static class BatteryRecordArrayAdapter extends
			ArrayAdapter<BatteryRecord> {
		private final Context context;
		private final List<BatteryRecord> values;

		private final SimpleDateFormat iso8601Format = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");

		public BatteryRecordArrayAdapter(Context context,
				List<BatteryRecord> values) {
			super(context, R.layout.item, values);
			this.context = context;
			this.values = values;
		}

		@Override
		public boolean isEnabled(int position) {
			return false;
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

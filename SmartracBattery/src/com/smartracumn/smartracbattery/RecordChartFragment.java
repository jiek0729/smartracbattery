package com.smartracumn.smartracbattery;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

public class RecordChartFragment extends Fragment {
	private final String TAG = getClass().getName();

	private List<BatteryRecord> recordList;

	private GraphViewSeries recordSeries;

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
		recordSeries = new GraphViewSeries(getViewData(recordList));
		return inflater.inflate(R.layout.chart, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		GraphView graphView = new LineGraphView(getActivity(), "Battery Usage");
		// set styles
		graphView.getGraphViewStyle().setGridColor(Color.BLUE);
		graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.BLUE);
		graphView.getGraphViewStyle().setVerticalLabelsColor(Color.RED);
		graphView.getGraphViewStyle().setNumHorizontalLabels(5);
		graphView.getGraphViewStyle().setNumVerticalLabels(4);
		graphView.getGraphViewStyle().setVerticalLabelsWidth(70);

		graphView.setCustomLabelFormatter(new CustomLabelFormatter() {
			@Override
			public String formatLabel(double value, boolean isValueX) {
				if (isValueX) {
					Date date = new Date((long) value);
					return String.valueOf(date.getHours()) + ":"
							+ String.valueOf(date.getMinutes());
				} else {
					return String.valueOf((int) Math.round(value)) + "%";
				}
			}
		});

		graphView.addSeries(recordSeries); // data

		LinearLayout layout = (LinearLayout) getView().findViewById(
				R.id.chart_view);
		layout.addView(graphView);
	}

	private GraphViewData[] getViewData(List<BatteryRecord> batteryRecords) {
		GraphViewData[] datas = new GraphViewData[batteryRecords.size()];
		for (int i = 0; i < datas.length; i++) {
			datas[i] = batteryRecords.get(i).getGraphViewData();
		}

		return datas;
	}

	public void notifyDataSetChanged() {
		if (recordSeries != null) {
			recordSeries.resetData(getViewData(recordList));
		}
	}

	@Override
	public void onStart() {
		Log.i(TAG, getClass().getSimpleName() + ":entered onStart()");
		// init example series data
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
}

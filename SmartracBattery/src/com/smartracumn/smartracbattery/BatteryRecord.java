package com.smartracumn.smartracbattery;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.jjoe64.graphview.GraphView.GraphViewData;

public class BatteryRecord {
	private int id;
	private Date time;
	private int percentage;
	private boolean isCharging;
	private GraphViewData graphViewData;
	private final SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	public BatteryRecord(int id, Date time, int percentage, boolean isCharging) {
		this.id = id;
		this.time = time;
		this.percentage = percentage;
		this.isCharging = isCharging;
	}

	public int getId() {
		return this.id;
	}

	public Date getTime() {
		return this.time;
	}

	public int getPercentage() {
		return this.percentage;
	}

	public boolean isCharging() {
		return this.isCharging;
	}

	@Override
	public String toString() {
		return ISO8601FORMAT.format(this.time) + "," + this.percentage + "%,"
				+ (this.isCharging ? "plugged" : "unplugged");
	}

	public GraphViewData getGraphViewData() {
		if (this.graphViewData == null) {
			this.graphViewData = new GraphViewData(this.time.getTime(),
					this.percentage);
		}

		return this.graphViewData;
	}
}

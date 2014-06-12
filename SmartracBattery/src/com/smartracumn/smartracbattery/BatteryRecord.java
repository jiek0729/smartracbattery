package com.smartracumn.smartracbattery;

import java.util.Date;

public class BatteryRecord {
	private int id;
	private Date time;
	private int percentage;
	private boolean isCharging;

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
		return this.time.toString() + ": " + this.percentage + "% "
				+ "is charging: " + this.isCharging;
	}
}

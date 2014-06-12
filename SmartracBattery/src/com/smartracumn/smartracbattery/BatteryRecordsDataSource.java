package com.smartracumn.smartracbattery;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class BatteryRecordsDataSource {

	// Database fields
	private final String TAG = getClass().getSimpleName();
	private final SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
			MySQLiteHelper.COLUMN_TIME, MySQLiteHelper.COLUMN_PERCENTAGE,
			MySQLiteHelper.COLUMN_ISCHARGING };

	public BatteryRecordsDataSource(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public BatteryRecord createComment(Date time, int percentage,
			boolean isCharging) {
		Log.i(TAG, "Add record for " + ISO8601FORMAT.format(time));
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_PERCENTAGE, percentage);
		values.put(MySQLiteHelper.COLUMN_TIME, ISO8601FORMAT.format(time));
		values.put(MySQLiteHelper.COLUMN_ISCHARGING, isCharging ? 1 : 0);
		long insertId = database.insert(MySQLiteHelper.TABLE_BATTERY_RECORDS,
				null, values);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_BATTERY_RECORDS,
				allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		BatteryRecord newRecord = cursorToBatteryRecord(cursor);
		cursor.close();
		return newRecord;
	}

	public void deleteRecord(BatteryRecord record) {
		long id = record.getId();
		Log.i(TAG, "Record deleted with id: " + id);
		database.delete(MySQLiteHelper.TABLE_BATTERY_RECORDS,
				MySQLiteHelper.COLUMN_ID + " = " + id, null);
	}

	public void deleteAll() {
		Log.i(TAG, "Records deleted");
		database.delete(MySQLiteHelper.TABLE_BATTERY_RECORDS, null, null);
	}

	public List<BatteryRecord> getRecordsForDateRange(Date start, Date end) {

		Log.i(TAG, "Get records for " + ISO8601FORMAT.format(start) + " - "
				+ ISO8601FORMAT.format(end));
		List<BatteryRecord> records = new ArrayList<BatteryRecord>();

		Cursor cursor = database.query(MySQLiteHelper.TABLE_BATTERY_RECORDS,
				allColumns, MySQLiteHelper.COLUMN_TIME + " BETWEEN \""
						+ ISO8601FORMAT.format(start) + "\" AND \""
						+ ISO8601FORMAT.format(end) + "\"", null, null, null,
				null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			BatteryRecord record = cursorToBatteryRecord(cursor);
			records.add(record);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return records;
	}

	public List<BatteryRecord> getAllRecords() {
		List<BatteryRecord> records = new ArrayList<BatteryRecord>();

		Cursor cursor = database.query(MySQLiteHelper.TABLE_BATTERY_RECORDS,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			BatteryRecord record = cursorToBatteryRecord(cursor);
			records.add(record);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return records;
	}

	private BatteryRecord cursorToBatteryRecord(Cursor cursor) {

		int id = cursor.getInt(0);
		String dateString = cursor.getString(1);
		Date time = null;
		try {
			time = ISO8601FORMAT.parse(dateString);
		} catch (ParseException e) {
			time = null;
		}
		int percentage = cursor.getInt(2);
		boolean isCharging = cursor.getInt(3) == 0 ? false : true;
		BatteryRecord record = new BatteryRecord(id, time, percentage,
				isCharging);
		return record;
	}
}

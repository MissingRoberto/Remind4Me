package com.swcm.remindme;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class Model {

	private static final String TAG = "DBHelper";

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "reminder.db";
	private static final String REMINDERS_TABLE = "Reminders";

	private Context context;
	private SQLiteDatabase db;
	private MyDBOpenHelper openHelper;

	public Model(Context context) {
		this.context = context;
		this.openHelper = new MyDBOpenHelper(this.context);
	}

	public Model open() {
		this.db = openHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		this.db.close();
	}

	public static final class Reminders implements BaseColumns {
		private Reminders() {
		}

		public static final String NAME = "name";
		public static final String PLACE = "place";
		public static final String DESCRIPTION = "summary";
		public static final String ICON = "icon";
		public static final String LATITUDE = "latitude";
		public static final String LONGITUDE = "longitude";
		public static final String ACTIVE = "active";
	}

	private static class MyDBOpenHelper extends SQLiteOpenHelper {

		MyDBOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + REMINDERS_TABLE + " (" + Reminders._ID
					+ " INTEGER PRIMARY KEY," + Reminders.NAME + " TEXT,"
					+ Reminders.PLACE + " TEXT," + Reminders.DESCRIPTION
					+ " TEXT," + Reminders.ICON + " TEXT," + Reminders.LATITUDE
					+ " REAL," + Reminders.LONGITUDE + " REAL,"
					+ Reminders.ACTIVE + " INTEGER" + ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + REMINDERS_TABLE);
			onCreate(db);
		}
	}

	public Reminder selectReminder(long id) {
		Reminder reminder = null;
		Cursor cursor = db.query(REMINDERS_TABLE, null, Reminders._ID + "=?",
				new String[] { Long.toString(id) }, null, null, null);
		cursor.moveToFirst();
		reminder = new Reminder(cursor.getString(1), cursor.getString(2),
				cursor.getString(3), cursor.getString(4), cursor.getDouble(5),
				cursor.getDouble(6), cursor.getInt(7) != 0);
		reminder.setRow(cursor.getInt(0));
		return reminder;
	}

	public ArrayList<Reminder> selectAllReminders() {
		ArrayList<Reminder> list = new ArrayList<Reminder>();
		Cursor cursor = this.db.query(REMINDERS_TABLE, null, null, null, null,
				null,Reminders._ID + " DESC");
		
		// Reminders.PLACE + " "
		if (cursor.moveToFirst()) {
			do {
				Reminder reminder = new Reminder(cursor.getString(1),
						cursor.getString(2), cursor.getString(3),
						cursor.getString(4), cursor.getDouble(5),
						cursor.getDouble(6), cursor.getInt(7) != 0);
				reminder.setRow(cursor.getInt(0));

				list.add(reminder);
			} while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return list;
	}

	public long insertReminder(Reminder Reminder) {
		ContentValues values = new ContentValues();
		values.put(Reminders.NAME, Reminder.getName());
		values.put(Reminders.PLACE, Reminder.getPlace());
		values.put(Reminders.DESCRIPTION, Reminder.getDescription());
		values.put(Reminders.ICON, Reminder.getIcon());
		values.put(Reminders.LATITUDE, Reminder.getLat());
		values.put(Reminders.LONGITUDE, Reminder.getLng());
		values.put(Reminders.ACTIVE, Reminder.isActive());
		long id = db.insert(REMINDERS_TABLE, null, values);
		return id;
	}

	public void updateReminder(Reminder Reminder) {
		ContentValues values = new ContentValues();
		values.put(Reminders.NAME, Reminder.getName());
		values.put(Reminders.PLACE, Reminder.getPlace());
		values.put(Reminders.DESCRIPTION, Reminder.getDescription());
		values.put(Reminders.ICON, Reminder.getIcon());
		values.put(Reminders.LATITUDE, Reminder.getLat());
		values.put(Reminders.LONGITUDE, Reminder.getLng());
		values.put(Reminders.ACTIVE, Reminder.isActive());
		db.update(REMINDERS_TABLE, values, Reminders._ID + "=?",
				new String[] { Long.toString(Reminder.getRow()) });
	}

	public void deleteReminder(Reminder Reminder) {
		db.delete(REMINDERS_TABLE, Reminders._ID + "=?",
				new String[] { Long.toString(Reminder.getRow()) });
	}

	public void removeAll() {
		db.delete(REMINDERS_TABLE, null, null);

	}

}
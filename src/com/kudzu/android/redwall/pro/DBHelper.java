package com.kudzu.android.redwall.pro;

import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper {

	private static final String DATABASE_NAME = "redwall.sqlite";
	private static final int DATABASE_VERSION = 2;
	private static final String WP_TABLE = "wp";

	public class Wp {
		public static final String ID = "_id";
		public static final String NAME = "name";
		public static final String LOCAL_URI = "local_uri";
		public static final String PERMA_LINK = "perma_link";
		public static final String THUMB = "thumb";

	}

	private Context mContext;
	private SQLiteDatabase mDB;

	public DBHelper(Context context) {
		this.mContext = context;
		DBOpenHelper dbOpenHelper = new DBOpenHelper(context);
		this.mDB = dbOpenHelper.getWritableDatabase();
	}

	public ArrayList<Wallpaper> getAllWp() {

		ArrayList<Wallpaper> list = new ArrayList<Wallpaper>();

		Cursor cursor = this.mDB.query(WP_TABLE, new String[] { Wp.ID, Wp.NAME,
				Wp.LOCAL_URI, Wp.PERMA_LINK, Wp.THUMB }, null, null, null,
				null, " _id ASC");

		if (cursor.moveToFirst()) {
			do {
				Wallpaper wp = new Wallpaper(cursor.getString(cursor
						.getColumnIndex(Wp.NAME)), cursor.getString(cursor
						.getColumnIndex(Wp.LOCAL_URI)), cursor.getString(cursor
						.getColumnIndex(Wp.PERMA_LINK)),
						cursor.getString(cursor.getColumnIndex(Wp.THUMB)));

				list.add(wp);
			} while (cursor.moveToNext());

		}
		cursor.close();
		return list;
	}

	public void deleteWp(String id) {
		this.mDB.delete(WP_TABLE, "_id=?", new String[] { id });
	}

	public void addWp(String name, String local_uri, String perma_link,String thumb) {
		ContentValues values = new ContentValues();
		values.put(Wp.NAME, name);
		values.put(Wp.LOCAL_URI, local_uri);
		values.put(Wp.PERMA_LINK, perma_link);
		values.put(Wp.THUMB, thumb);
		this.mDB.insert(WP_TABLE, null, values);
	}

	public void clearWp() {
		this.mDB.delete(WP_TABLE, null, null);
	}

	public int getDBVersion() {
		return this.mDB.getVersion();
	}

	public void close() {
		if (this.mDB.isOpen()) {
			this.mDB.close();
		}
	}

	private static class DBOpenHelper extends SQLiteOpenHelper {

		private static final String CREATE_WP = "CREATE TABLE IF NOT EXISTS "
				+ WP_TABLE
				+ " (_id INTEGER, name TEXT, local_uri TEXT , perma_link TEXT , thumb TEXT, "
				+ "PRIMARY KEY (_id));";

		private Context mContext;

		DBOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			mContext = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_WP);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + WP_TABLE);
			db.execSQL(CREATE_WP);
		}

	}
}

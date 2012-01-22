package at.ac.tuwien.igw.group9.molog.db;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

// Tutorial: http://coding.smashingmagazine.com/2011/03/28/get-started-developing-for-android-with-eclipse-reloaded/
public class LogData extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "log.db";
	private static final int DATABASE_VERSION = 1;
	public static final String TABLE_NAME = "log";
	public static final String ID = BaseColumns._ID;
	public static final String TIMESTAMP = "timestamp";
	public static final String PULSE_VALUE = "pulse_value";
	public static final String GSR_VALUE = "gsr_value";
	public static final String FILE_NAME = "file_name";

	public LogData(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	private void createDB(SQLiteDatabase db) {
		String sql = "CREATE TABLE " + TABLE_NAME + " (" + ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + TIMESTAMP
				+ " INTEGER, " + PULSE_VALUE + " INTEGER, " + GSR_VALUE
				+ " INTEGER, " + FILE_NAME + " TEXT" + ");";

		db.execSQL(sql);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createDB(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		clearDB(db);
		createDB(db);
	}

	private void clearDB(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
	}

	private Cursor getCursorAll() {
		String[] from = { ID, TIMESTAMP, PULSE_VALUE, GSR_VALUE, FILE_NAME };
		String order = TIMESTAMP;
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, from, null, null, null, null,
				order);

		return cursor;
	}

	public void clearDB() {
		clearDB(getWritableDatabase());
	}

	public void insert(long timestamp, int gsrValue, int pulseValue,
			String fileName) {
		SQLiteDatabase db = getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(TIMESTAMP, timestamp);
		values.put(GSR_VALUE, gsrValue);
		values.put(PULSE_VALUE, pulseValue);
		values.put(FILE_NAME, fileName);

		db.insertOrThrow(TABLE_NAME, null, values);
	}

	public Cursor all() {
		return getCursorAll();
	}

	public Cursor all(Activity activity) {
		Cursor cursor = getCursorAll();
		activity.startManagingCursor(cursor);
		return cursor;
	}

	public int count() {
		SQLiteDatabase db = getReadableDatabase();
		return (int) DatabaseUtils.queryNumEntries(db, TABLE_NAME);
	}

}

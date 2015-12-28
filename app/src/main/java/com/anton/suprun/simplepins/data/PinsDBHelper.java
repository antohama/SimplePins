package com.anton.suprun.simplepins.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import com.anton.suprun.simplepins.tools.Tools;

import java.util.ArrayList;
import java.util.List;

public class PinsDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "Pins_DB";
    private final Context mContext;
    private static PinsDBHelper mInstance = null;

    private PinsDBHelper(Context context) {
        super(context, Constants.DB_NAME, null, Constants.DB_VERSION);
        mContext = context;
    }

    public static void createInstance(Context context) {
        if (null == mInstance) {
            mInstance = new PinsDBHelper(context);
        }
    }

    public static PinsDBHelper getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " +
                Constants.TABLE_PINS +
                " ( " +
                Constants.FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Constants.FIELD_USER_ID + " TEXT, " +
                Constants.FIELD_LAT + " REAL, " +
                Constants.FIELD_LON + " REAL, " +
                Constants.FIELD_TITLE + " TEXT" +
                " );");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_PINS);
            PreferenceManager.getDefaultSharedPreferences(mContext).edit().clear().commit();
            this.onCreate(db);
        }
    }

    public Cursor getCursor() {
        SQLiteDatabase db = getReadableDatabase();
        String selection = Constants.FIELD_USER_ID + " = ?";
        String[] selectionArgs = new String[]{Tools.getUserId()};

        Cursor cursor = db.query(Constants.TABLE_PINS, null, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            return cursor;
        } else {
            return null;
        }
    }

    public List<PinEntity> getPinsList() {
        List<PinEntity> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String selection = Constants.FIELD_USER_ID + " = ?";
        String[] selectionArgs = new String[]{Tools.getUserId()};

        Cursor cursor = db.query(Constants.TABLE_PINS, null, selection, selectionArgs, null, null, null);
        int idCol = cursor.getColumnIndex(Constants.FIELD_ID);
        int latCol = cursor.getColumnIndex(Constants.FIELD_LAT);
        int lonCol = cursor.getColumnIndex(Constants.FIELD_LON);
        int titleCol = cursor.getColumnIndex(Constants.FIELD_TITLE);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            PinEntity pin = new PinEntity(
                    cursor.getDouble(latCol),
                    cursor.getDouble(lonCol),
                    cursor.getString(titleCol)
            );
            pin.setId(cursor.getLong(idCol));
            list.add(pin);
        }

        cursor.close();
        return list;
    }

    public void addPin(PinEntity pin) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(Constants.FIELD_USER_ID, Tools.getUserId());
        cv.put(Constants.FIELD_LAT, pin.getLatitude());
        cv.put(Constants.FIELD_LON, pin.getLongitude());
        cv.put(Constants.FIELD_TITLE, pin.getTitle());

        long id = db.insert(Constants.TABLE_PINS, null, cv);
        pin.setId(id);
    }

    public void removePin(PinEntity pin) {
        SQLiteDatabase db = getReadableDatabase();
        long id = getPinId(pin);
        String selection = Constants.FIELD_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(id)};

        db.delete(Constants.TABLE_PINS, selection, selectionArgs);
    }

    private long getPinId(PinEntity desiredPin) {
        for (PinEntity pin : getPinsList()) {
            if (desiredPin.equals(pin)) {
                return pin.getId();
            }
        }

        return -1;
    }
}

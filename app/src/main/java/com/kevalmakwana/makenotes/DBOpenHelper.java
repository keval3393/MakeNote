package com.kevalmakwana.makenotes;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper{

    //Constants for db name and version
    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 1;

    //Constants for identifying table and columns
    public static final String TABLE_NOTES = "notes";
    public static final String NOTE_ID = "_id";
    public static final String NOTE_TEXT = "noteText";
    public static final String NOTE_CREATED = "noteCreated";

    public static final String TABLE_IMAGE = "imagadata";
    public static final String IMAGE_ID = "imageId";
    public static final String IMAGE = "image";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String NOTE_ID_FK = "noteId";

    public static final String[] ALL_COLUMNS_NOTES =
            {NOTE_ID, NOTE_TEXT, NOTE_CREATED};
    public static final String[] ALL_COLUMNS_IMAGE =
            {IMAGE_ID, IMAGE, LATITUDE, LONGITUDE, NOTE_ID_FK};


    //SQL to create table
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NOTES + " (" +
                    NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    NOTE_TEXT + " TEXT, " +
                    NOTE_CREATED + " TEXT default CURRENT_TIMESTAMP" +
                    ")";
    private static final String IMAGETABLE_CREATE =
            "CREATE TABLE " + TABLE_IMAGE + " (" +
                    IMAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    IMAGE + " BLOB, " +
                    NOTE_ID_FK + " INTEGER, " +
                    LATITUDE + " REAL, " +
                    LONGITUDE + " REAL " +
                    ")";

    public DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
        db.execSQL(IMAGETABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        db.execSQL("DROP TABLE IF EXISTS " + IMAGETABLE_CREATE);
        onCreate(db);
    }
}

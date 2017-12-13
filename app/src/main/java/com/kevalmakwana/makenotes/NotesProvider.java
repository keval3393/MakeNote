package com.kevalmakwana.makenotes;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.widget.Toast;

public class NotesProvider extends ContentProvider{

    private static final String AUTHORITY = "com.example.plainolnotes.notesprovider";

    private static final String BASE_PATH = "notes";
    private static final String IMAGE_PATH = "imagedata";

    public static final Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH );
    public static final Uri IMAGE_CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + IMAGE_PATH );

    // Constant to identify the requested operation
    private static final int NOTES = 1;
    private static final int NOTES_ID = 2;
    private static final int IMAGE = 3;
    private static final int IMAGE_ID = 4;

    private static final UriMatcher uriMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);

    public static final String CONTENT_ITEM_TYPE = "Note";

    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, NOTES);
        uriMatcher.addURI(AUTHORITY, BASE_PATH +  "/#", NOTES_ID);
        uriMatcher.addURI(AUTHORITY, IMAGE_PATH, IMAGE);
        uriMatcher.addURI(AUTHORITY, IMAGE_PATH +  "/#", IMAGE_ID);
    }

    private SQLiteDatabase database;

    @Override
    public boolean onCreate() {

        DBOpenHelper helper = new DBOpenHelper(getContext());
        database = helper.getWritableDatabase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        int uriType = uriMatcher.match(uri);
        String[] columns;
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (uriType) {
            case NOTES_ID:
                queryBuilder.appendWhere(DBOpenHelper.NOTE_ID + "=" + uri.getLastPathSegment());
                //selection = DBOpenHelper.NOTE_ID + "=" + uri.getLastPathSegment();
            case NOTES:
                queryBuilder.setTables(DBOpenHelper.TABLE_NOTES);
                columns = DBOpenHelper.ALL_COLUMNS_NOTES;
                break;
            case IMAGE_ID:
                queryBuilder.appendWhere(DBOpenHelper.NOTE_ID_FK + "=" + uri.getLastPathSegment());
                //selection = DBOpenHelper.IMAGE_ID + "=" + uri.getLastPathSegment();
            case IMAGE:
                queryBuilder.setTables(DBOpenHelper.TABLE_IMAGE);
                columns = DBOpenHelper.ALL_COLUMNS_IMAGE;
                break;
        }

        return queryBuilder.query(database,null,null,null,null,null,null);
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if(uri.getPath().equals("/notes")) {
            long id = database.insert(DBOpenHelper.TABLE_NOTES, null, values);
            if(id<0){
                Toast.makeText(getContext(),"Error saving data...",Toast.LENGTH_SHORT).show();
            }
            return Uri.parse(BASE_PATH + "/" + id);
        }
        else{
            long id = database.insert(DBOpenHelper.TABLE_IMAGE,null,values);
            if(id<0){
                Toast.makeText(getContext(),"Error saving data...",Toast.LENGTH_SHORT).show();
            }
            return Uri.parse(IMAGE_PATH + "/" + id);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if(uri.getPath().equals("/notes")) {
            return database.delete(DBOpenHelper.TABLE_NOTES, selection, selectionArgs);
        }
        else{
            return database.delete(DBOpenHelper.TABLE_IMAGE, selection, selectionArgs);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if(uri.getPath().equals("/notes")) {
            return database.update(DBOpenHelper.TABLE_NOTES, values, selection, selectionArgs);
        }
        else {
            return database.update(DBOpenHelper.TABLE_IMAGE, values, selection, selectionArgs);
        }
    }
}

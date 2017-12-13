package com.kevalmakwana.makenotes;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class EditorActivity extends ActionBarActivity implements LocationListener {

    private String action;
    private EditText editor;
    private String oldText;
    private String noteFilter;
    private String imageFilter;
    private int currentNoteID;
    private int PICK_IMAGE_REQUEST = 1;

    private Context mContext = null;

    // flag for GPS status
    boolean isGPSEnabled = false;

    boolean canGetLocation = false;

    Location location;
    double latitude;
    double longitude;
    protected LocationManager locationManager;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME = 1000 * 60 * 1; // 1 minute


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        mContext = getApplicationContext();
        editor = (EditText) findViewById(R.id.editText);

        Intent intent = getIntent();

        Uri uri = intent.getParcelableExtra(NotesProvider.CONTENT_ITEM_TYPE);

        if (uri == null) {
            action = Intent.ACTION_INSERT;
            setTitle(getString(R.string.new_note));
            insertNote(getString(R.string.new_note));
        } else {
            action = Intent.ACTION_EDIT;
            currentNoteID = Integer.parseInt(uri.getLastPathSegment());
            noteFilter = DBOpenHelper.NOTE_ID + "=" + uri.getLastPathSegment();
            Cursor cursor = getContentResolver().query(uri, DBOpenHelper.ALL_COLUMNS_NOTES, noteFilter, null, null);
            cursor.moveToFirst();
            oldText = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TEXT));
            editor.setText(oldText);


            Uri imageUri = Uri.parse(NotesProvider.IMAGE_CONTENT_URI + "/" + currentNoteID);
            imageFilter = DBOpenHelper.NOTE_ID_FK + "=" + uri.getLastPathSegment();
            Cursor imageCursor = getContentResolver().query(imageUri, DBOpenHelper.ALL_COLUMNS_IMAGE, imageFilter, null, null);
            imageCursor.moveToFirst();

            int cursorLength = imageCursor.getCount();
            while (cursorLength > 0) {
                SpannableStringBuilder ssb = new SpannableStringBuilder(editor.getText());
                byte[] imageByteArray = imageCursor.getBlob(imageCursor.getColumnIndex(DBOpenHelper.IMAGE));
                Bitmap bitmapImage = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
                if (bitmapImage != null) {
                    BitmapDrawable bmDrawable = new BitmapDrawable(bitmapImage);
                    bmDrawable.setBounds(0, 0, 900, 700);
                    String newStr = bmDrawable.toString() + "\n";
                    ssb.append(newStr);
                    ssb.setSpan(new ImageSpan(bmDrawable), ssb.length() - newStr.length(), ssb.length() - "\n".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    editor.setText(ssb);
                    imageCursor.moveToNext();
                    cursorLength--;
                }
            }
        }
        editor.requestFocus();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (action.equals(Intent.ACTION_EDIT)) {
            getMenuInflater().inflate(R.menu.menu_editor, menu);
        }
        if (action.equals(Intent.ACTION_INSERT)) {
            getMenuInflater().inflate(R.menu.menu_insert, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finishEditing();
                break;
            case R.id.action_delete:
                deleteNote();
                break;
            case R.id.action_camera:
                takePhoto();
                break;
            case R.id.action_near_me:
                showMap();
                break;
        }
        return true;
    }

    private void showMap() {
        Intent intent = new Intent(EditorActivity.this, MapsActivity.class);
        Uri uri = Uri.parse(NotesProvider.IMAGE_CONTENT_URI + "/" + currentNoteID);
        intent.putExtra("IMAGE_URI", uri);
        startActivity(intent);
    }

    private void takePhoto() {
        getLocation();
        // Get image from gallery on a different thread for efficient performance
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple application/Gallry available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
                setResult(RESULT_OK);
            }
        });

    }

    // Get user location
    private void getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (!isGPSEnabled ) {
                Toast.makeText(getApplicationContext(), "Turn on GPS", Toast.LENGTH_SHORT).show();
            } else {
                canGetLocation = true;

                // lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, EditorActivity.this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void finishEditing() {
        String newText = editor.getText().toString().trim();

        switch (action) {
            case Intent.ACTION_INSERT:
                if (newText.length() == 0) {
                    setResult(RESULT_CANCELED);
                } else if (!newText.equals(getString(R.string.new_note))) {
                    updateNote(newText);
                }
                break;
            case Intent.ACTION_EDIT:
                if (newText.length() == 0) {
                    deleteNote();
                } else if (oldText.equals(newText)) {
                    setResult(RESULT_CANCELED);
                } else {
                    updateNote(newText);
                }
        }
        finish();
    }

    private void deleteNote() {
        getContentResolver().delete(NotesProvider.CONTENT_URI, noteFilter, null);
        getContentResolver().delete(NotesProvider.IMAGE_CONTENT_URI, imageFilter, null);
        editor.setText("");
        Toast.makeText(this, "Note Deleted", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void updateNote(String noteText) {
        ContentValues values = new ContentValues();

        String findStr = "android";
        int lastIndex = 0;
        int imageCount = 0;

        while(lastIndex != -1){

            lastIndex = noteText.indexOf(findStr,lastIndex);

            if(lastIndex != -1){
                imageCount ++;
                lastIndex += findStr.length();
            }
        }

        while (noteText.contains("android")) {
            if(imageCount==1){
                noteText = noteText.replace(noteText.substring(noteText.indexOf("android"), noteText.length()), "");
            }else {
                noteText = noteText.replace(noteText.substring(noteText.indexOf("android"), noteText.indexOf("@") + 8), "");
            }
            imageCount--;
        }
        noteText = noteText.trim();
        values.put(DBOpenHelper.NOTE_TEXT, noteText);
        noteFilter = DBOpenHelper.NOTE_ID + "=" + currentNoteID;
        this.getContentResolver().update(NotesProvider.CONTENT_URI, values, noteFilter, null);
        Toast.makeText(this, R.string.note_update, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
    }

    private void insertNote(String noteText) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, noteText);
        Uri idURI = getContentResolver().insert(NotesProvider.CONTENT_URI, values);
        currentNoteID = Integer.parseInt(idURI.getLastPathSegment());
        setResult(RESULT_OK);
    }

    @Override
    public void onBackPressed() {
        finishEditing();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data.getData() != null) {
            Log.d("Message", "All is well");
            SpannableStringBuilder ssb = new SpannableStringBuilder(editor.getText());
            Bitmap bitMap = null;
            Bitmap resizedBitmap = null;
            try {
                bitMap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                resizedBitmap = Bitmap.createScaledBitmap(bitMap, 900, 700, true);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitMap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                byte[] byteArray = stream.toByteArray();

                savePhotoAsync(byteArray);

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (resizedBitmap != null) {
                BitmapDrawable bmDrawable = new BitmapDrawable(resizedBitmap);
                bmDrawable.setBounds(0, 0, 900, 700);
                String newStr = bmDrawable.toString() + "\n";
                ssb.append(newStr);
                ssb.setSpan(new ImageSpan(bmDrawable), ssb.length() - newStr.length(), ssb.length() - "\n".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                editor.setText(ssb);
            }
        }
    }

    private void savePhotoAsync(final byte[] byteArray) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                //save image to database asynchronously
                ContentValues values = new ContentValues();
                values.put(DBOpenHelper.IMAGE, byteArray);
                values.put(DBOpenHelper.NOTE_ID_FK, currentNoteID);
                values.put(DBOpenHelper.LATITUDE, latitude);
                values.put(DBOpenHelper.LONGITUDE, longitude);
                getContentResolver().insert(NotesProvider.IMAGE_CONTENT_URI, values);
            }
        });

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

}

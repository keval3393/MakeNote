package com.kevalmakwana.makenotes;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    ArrayList<Double> latitudeList = new ArrayList<Double>();
    ArrayList<Double> longitudeList = new ArrayList<Double>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        Uri uri = intent.getParcelableExtra("IMAGE_URI");
        if(uri != null){
            String imageFilter = DBOpenHelper.NOTE_ID_FK + "=" + uri.getLastPathSegment();
            Cursor imageCursor = getContentResolver().query(uri,DBOpenHelper.ALL_COLUMNS_IMAGE,imageFilter,null,null);
            imageCursor.moveToFirst();

            int cursorLength = imageCursor.getCount();
            while (cursorLength > 0) {
                latitudeList.add(imageCursor.getDouble(imageCursor.getColumnIndex(DBOpenHelper.LATITUDE)));
                longitudeList.add(imageCursor.getDouble(imageCursor.getColumnIndex(DBOpenHelper.LONGITUDE)));
                imageCursor.moveToNext();
                cursorLength--;
                }
            }
        }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng markerToZoom = null;

        // showing markers for all location where the image is taken
        for(int i = 0;i<latitudeList.size();i++){
            LatLng newPosition = new LatLng(latitudeList.get(i),longitudeList.get(i));
            mMap.addMarker(new MarkerOptions().position(newPosition).title("Photo is added here...")).showInfoWindow();
            // Last position to zoom camera inside the map
            markerToZoom = new LatLng(latitudeList.get(latitudeList.size()-1),longitudeList.get(longitudeList.size()-1));
        }

        if(markerToZoom!=null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(markerToZoom));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(markerToZoom)
                    .zoom(17).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        else{
            Toast.makeText(getApplicationContext(),"Please add images to your Note..",Toast.LENGTH_SHORT).show();
        }
    }
}

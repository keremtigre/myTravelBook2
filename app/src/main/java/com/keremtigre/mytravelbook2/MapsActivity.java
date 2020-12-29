package com.keremtigre.mytravelbook2;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    SQLiteDatabase database;
    SQLiteStatement sqLiteStatement;
    LocationManager locationManager;
    LocationListener locationListener;
    Geocoder geocoder;
    List<Address> addressList;
    String address="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null) {
                LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
            }
        }

        Intent intent = getIntent();
        try {

            int idInfo = intent.getIntExtra("id", 1);
            database = this.openOrCreateDatabase("Location", MODE_PRIVATE, null);
            Cursor cursor = database.rawQuery("SELECT * FROM location WHERE id= ?", new String[]{String.valueOf(idInfo)});
            int addressIx = cursor.getColumnIndex("address");
            int latIx = cursor.getColumnIndex("latitude");
            int longIx = cursor.getColumnIndex("longitude");
            try {
                mMap.clear();
                while (cursor.moveToNext()) {
                    String address = cursor.getString(addressIx);
                    double latitude = Double.parseDouble(cursor.getString(latIx));
                    double longitude = Double.parseDouble(cursor.getString(longIx));
                    LatLng latLng = new LatLng(latitude, longitude);
                    mMap.addMarker(new MarkerOptions().position(latLng).title(address));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(grantResults.length>0){
            if(requestCode==1){
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                  locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onMapLongClick(final LatLng latLng) {

        geocoder=new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if(addressList.get(0).getThoroughfare() != null){
                address+=addressList.get(0).getThoroughfare();
                address+=" ";
                if(addressList.get(0).getSubThoroughfare() !=null){
                    address+=addressList.get(0).getSubThoroughfare();
                }
            }
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title(address));
            database = this.openOrCreateDatabase("Location", MODE_PRIVATE, null);
            database.execSQL("CREATE TABLE IF NOT EXISTS location (id INTEGER PRIMARY KEY ,address VARCHAR,latitude VARCHAR ,longitude VARCHAR)");
            AlertDialog.Builder alertbuldier = new AlertDialog.Builder(MapsActivity.this);
            alertbuldier.setTitle("Konum Kayıt");
            alertbuldier.setMessage(address+" Adresi kaydedilsin mi ?" );
            alertbuldier.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String sqlString="INSERT INTO location (address,latitude,longitude) VALUES (?,?,?)";
                    sqLiteStatement=database.compileStatement(sqlString);
                    sqLiteStatement.bindString(1,address);
                    sqLiteStatement.bindString(2,String.valueOf(latLng.latitude));
                    sqLiteStatement.bindString(3,String.valueOf(latLng.longitude));
                    sqLiteStatement.execute();
                    Intent intent=new Intent(MapsActivity.this,MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            });
            alertbuldier.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mMap.clear();
                    addressList.clear();
                }
            });
            alertbuldier.show();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
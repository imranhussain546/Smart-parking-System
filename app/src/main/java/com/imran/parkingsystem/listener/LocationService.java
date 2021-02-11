package com.imran.parkingsystem.listener;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.imran.parkingsystem.MainActivity;

public class LocationService extends Service implements LocationListener {
    private LocationManager locatinmanager;


    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        location();
    }



    @SuppressLint("MissingPermission")
    private void location() {
        locatinmanager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        locatinmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locatinmanager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onLocationChanged(@NonNull Location location)
    {
        Intent intent=new Intent("com.imran.parkingsystem.listener.customlocation");
        intent.putExtra("lat",location.getLatitude());
        intent.putExtra("lon",location.getLongitude());
        sendBroadcast(intent);
    }
}

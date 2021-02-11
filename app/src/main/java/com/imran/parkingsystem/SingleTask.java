package com.imran.parkingsystem;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class SingleTask extends Application
{
private FirebaseAuth mAuth;
private GoogleSignInClient mgoogleSignInClient;
private FirebaseDatabase database;
private StorageReference storage;
private BroadcastReceiver broadcastReceiver;
private double lat, lon;
    private Handler myhaHandler;
    @Override
    public void onCreate() {
        super.onCreate();

        myhaHandler = new Handler();
        myhaHandler.post(new Runnable() {
            @Override
            public void run()
            {
                mAuth=FirebaseAuth.getInstance();
                database=FirebaseDatabase.getInstance();
                storage=FirebaseStorage.getInstance().getReference();
                GoogleSignInOptions gso=new GoogleSignInOptions
                        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                mgoogleSignInClient= GoogleSignIn.getClient(SingleTask.this,gso);
            }
        });


        broadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                lat=intent.getExtras().getDouble("lat");
                lon=intent.getExtras().getDouble("lon");
                Toast.makeText(context, "Current Location : " + lat + "\n" + lon, Toast.LENGTH_SHORT).show();
            }
        };
        registerReceiver(broadcastReceiver,new IntentFilter("com.imran.parkingsystem.listener.customlocation"));
    }

    public FirebaseAuth getFirebaseAuth()
    {
        return mAuth;
    }
    public GoogleSignInClient getgoogleSignInClient()
    {
        return mgoogleSignInClient;
    }
    public BroadcastReceiver getLocationBrodcast() {
        return broadcastReceiver;
    }

    public LatLng getCurrentLocation() {
        return new LatLng(lat, lon);
    }

    public StorageReference getDocumentStroageIdProof()
    {
        return storage.child("idproofs/");
    }
    public DatabaseReference getProfileDatabaseReference()
    {
        return database.getReference("profile");
    }
    public DatabaseReference getParkingDatabaseReference() {
        return database.getReference(" add parkings");
    }
    public DatabaseReference getBookingDatabaseReference() {
        return database.getReference("bookings");
    }
    public DatabaseReference getFeedbackDatabaseReference() {
        return database.getReference("feedbacks");
    }
}

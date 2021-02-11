package com.imran.parkingsystem.ui.addparking;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.imran.parkingsystem.MainActivity;
import com.imran.parkingsystem.R;
import com.imran.parkingsystem.SingleTask;
import com.imran.parkingsystem.listener.LocationService;
import com.imran.parkingsystem.module.AddPark;
import com.imran.parkingsystem.module.Profile;

import java.util.List;

public class AddParkingFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener {
    private EditText tfullname, tmobile, taddress, tchargeph, tvehicle;
    private GoogleMap googleMap;
    private Profile profile;
    private String fullname, mobile, address, charge, vehiclenumber, pincode, cityname;
    private double lat, lon;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.addparking, container, false);
         // getActivity().registerReceiver(((SingleTask) getActivity().getApplication()).getLocationBrodcast(), new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        //getActivity().startService(new Intent(getActivity(), LocationService.class));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        profile = ((MainActivity) getActivity()).getProfiledata();//it get current user data

        initViews(view);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);//it load the map
    }
    private MarkerOptions markerOptions;
    @Override
    @SuppressLint("MissingPermission")
    public void onMapReady(GoogleMap googleMap)
    {
        this.googleMap = googleMap;
        final LatLng currentlocation = ((SingleTask) getActivity().getApplication()).getCurrentLocation();
        Log.e("addparking",currentlocation.toString());
        markerOptions = new MarkerOptions().position(currentlocation).title("Your Current Location");//set marker in current location
        googleMap.addMarker(markerOptions).setDraggable(true);//set drag&drop on marker
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentlocation));
//for zoom & circle radius
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currentlocation).zoom(19f).tilt(70).build();
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        googleMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
        googleMap.setOnMarkerDragListener(this);//event on marker
    }

    private void initViews(View view)
    {
        tfullname = view.findViewById(R.id.add_parking_name);
        tmobile = view.findViewById(R.id.add_parking_mobile);
        taddress = view.findViewById(R.id.add_parking_address);
        tchargeph = view.findViewById(R.id.add_parking_charge_per_hour);
        tvehicle = view.findViewById(R.id.add_parking_vehicle);

        if (profile != null) {
            tfullname.setText(profile.getName());
            tmobile.setText(profile.getMobile());
            taddress.setText(profile.getAddress());
            tchargeph.setText(profile.getChargeperhour());
            tvehicle.setText(profile.getVehiclenumber());
        } else {
            Toast.makeText(getActivity(), "Please Fill Profile Details First", Toast.LENGTH_SHORT).show();
        }

        Button addbutton = view.findViewById(R.id.add_parking_button);
        addbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (valid())
                {
                    if (cityname != null) {
                        addParkingDetaildHere(); //if cityname get then set parking detail
                    } else {
                        Toast.makeText(getActivity(), "Please select marker to set location", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void addParkingDetaildHere()
    {
        AddPark addPark = new AddPark();
        addPark.setCityname(cityname);
        addPark.setOwneruid(profile.getUid());
        addPark.setPincode(pincode);
        addPark.setBookstatus(true);
        addPark.setLat(lat);
        addPark.setLon(lon);

        ((SingleTask) getActivity().getApplication()).getParkingDatabaseReference().child(cityname).child(profile.getUid()).setValue(addPark)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) 
                    {
                        if (task.isSuccessful()) {
                        updateProfileData();
                    }

                    }
                });
    }

    private void updateProfileData()
    {
        profile.setAddress(address);
        profile.setCityname(cityname);
        ((SingleTask) getActivity().getApplication()).getProfileDatabaseReference().child(profile.getUid()).setValue(profile)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Successfully Added", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean valid()
    {
        fullname = tfullname.getText().toString();
        mobile = tmobile.getText().toString();
        address = taddress.getText().toString();
        charge = tchargeph.getText().toString();
        vehiclenumber = tvehicle.getText().toString();

        if (TextUtils.isEmpty(fullname)) {
            Toast.makeText(getActivity(), "Please enter full name", Toast.LENGTH_SHORT).show();
            tfullname.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(mobile)) {
            Toast.makeText(getActivity(), "Please enter mobile number", Toast.LENGTH_SHORT).show();
            tmobile.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(address)) {
            Toast.makeText(getActivity(), "Please select map to get address", Toast.LENGTH_SHORT).show();
            taddress.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(charge)) {
            Toast.makeText(getActivity(), "Please enter charge per hour", Toast.LENGTH_SHORT).show();
            tchargeph.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(vehiclenumber)) {
            Toast.makeText(getActivity(), "Please enter vehicle number", Toast.LENGTH_SHORT).show();
            tvehicle.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        setDetailsInUi(marker); //set detail on marker end
    }

    private void setDetailsInUi(Marker marker)
    {
        try {
            LatLng latLng = marker.getPosition();
            Geocoder geocoder = new Geocoder(getActivity());

            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);//get list type of address
            if (addressList.size() > 0) {
                Address address = addressList.get(0);//get address from address list
                markerOptions = new MarkerOptions().position(latLng).title(address.getAddressLine(0)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                googleMap.addMarker(markerOptions);
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                googleMap.setOnMarkerDragListener(this);

                pincode = address.getPostalCode(); //get pincode from address
                cityname = address.getLocality();//get city from pincode
                lat = latLng.latitude;
                lon = latLng.longitude;
                Log.e("error", address.getPostalCode());
                Log.e("error", address.getAddressLine(0));
                Log.e("error", address.getLocality());

                Log.e("error", address.getFeatureName());
                Log.e("error", address.getAdminArea());
                Log.e("error", address.getSubLocality());
                taddress.setText(address.getAddressLine(0));


            } else {
                Toast.makeText(getActivity(), "Try Again", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("error", e.toString());
        }
    }


}
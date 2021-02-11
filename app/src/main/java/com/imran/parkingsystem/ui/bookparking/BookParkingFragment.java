package com.imran.parkingsystem.ui.bookparking;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.imran.parkingsystem.MainActivity;
import com.imran.parkingsystem.R;
import com.imran.parkingsystem.SingleTask;
import com.imran.parkingsystem.adapter.CustomFeedbackAdapter;
import com.imran.parkingsystem.module.AddPark;
import com.imran.parkingsystem.module.Book;
import com.imran.parkingsystem.module.Feedback;
import com.imran.parkingsystem.module.Profile;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class BookParkingFragment extends Fragment implements OnMapReadyCallback {
    private LatLng currentlocation;
    private Profile profile;
    private GoogleMap googleMap;
    private SharedPreferences sharedPreferences;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.bookparking, container, false);
        sharedPreferences = getActivity().getSharedPreferences("session", Context.MODE_PRIVATE);//create sharedpreferences object
        currentlocation = ((SingleTask) getActivity().getApplication()).getCurrentLocation();//get current location
        profile = ((MainActivity) getActivity()).getProfiledata();//get profile
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);//it load the map
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    /*   googleMap.addMarker(new MarkerOptions().position(currentlocation).title("Marker in Sydney"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentlocation));*/

        googleMap.getUiSettings().setZoomControlsEnabled(false);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currentlocation).zoom(19f).tilt(70).build();
        //googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));

        getParkingBasedOnCurrentLocationCityName();
    }


    //show parking on current city location based
    private List<AddPark> currentparkings; //create list of add parking becoz may be in same location more than one parking add

    private void getParkingBasedOnCurrentLocationCityName()
    {
        currentparkings = new ArrayList<>();
        if (currentlocation != null) //if current location is not null then work below code
        {
            try {
                Geocoder geocoder = new Geocoder(getActivity());
                final List<Address> addressList = geocoder.getFromLocation(currentlocation.latitude, currentlocation.longitude, 1); //from geocoder get current address
                if (addressList.size() > 0) {
                    String currentCityname = addressList.get(0).getLocality();//from address get current city name
                    ((SingleTask) getActivity().getApplication()).getParkingDatabaseReference().child(currentCityname).addListenerForSingleValueEvent(new ValueEventListener() //get parking folder then get current city folder from firebase
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Iterator<DataSnapshot> it = snapshot.getChildren().iterator();
                            while (it.hasNext()) //create loop for get data city folder
                            {
                                DataSnapshot ds = it.next();
                                Log.e("error", ds.toString());
                                currentparkings.add(ds.getValue(AddPark.class));//convert data in add park and add in current parking list
                            }
                            setParkingLocationOnMap(currentparkings);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            } catch (Exception e) {
                Log.e("error", e.toString());
            }
        }


    }

    private void setParkingLocationOnMap(final List<AddPark> currentparkings) {
        if (currentparkings.size() > 0) //if parking is avilable in current location then run IF code /not avilable then ELSE code
        {
            for (final AddPark addPark : currentparkings) //create loop for get add parking avilable
            {
                final String owneruid = addPark.getOwneruid();//get owner uid from add park
                //add marker in different location
                googleMap.addMarker(new MarkerOptions().position(new LatLng(addPark.getLat(), addPark.getLon())).title("parking").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));//get location of parking and set marker and change marker color
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() //when click  on marker run below code
                {
                    @Override
                    public boolean onMarkerClick(Marker marker) {

                        if (addPark.isBookstatus())//if book status is return true then parking as avilable on particular owner if is return false then parking is allready booked run ELSE code
                        {
                            fetchOwnerDetail(owneruid);//fetch owner detail
                        }
                        else {
                            //Toast.makeText(getActivity(), "Sorry ", Toast.LENGTH_SHORT).show();
                            bookbutton.setText("Already Booked");
                            bookbutton.setEnabled(false);
                        }
                        return false;
                    }
                });
            }
        } else {
            Toast.makeText(getActivity(), "No Parking Available In This Place.", Toast.LENGTH_SHORT).show();
        }
    }


    private TextView toname, tomobile, toaddress, tocharge;
    private EditText tcvehicle;
    private Button feedbutton, bookbutton;
    private void initViews(View view)
    {
        toname = view.findViewById(R.id.book_owner_name);
        tomobile = view.findViewById(R.id.book_owner_mobile);
        toaddress = view.findViewById(R.id.book_owner_address);
        tocharge = view.findViewById(R.id.book_owner_charge_per_hour);
        tcvehicle = view.findViewById(R.id.book_vehicle_number);

        feedbutton = view.findViewById(R.id.book_feedback_button);

        bookbutton = view.findViewById(R.id.book_now_button);
        feedbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOwnerFeedbackHere();
            }
        });
        bookbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //book button code here
                if (ownerdetail != null) {
                    Calendar calendar = Calendar.getInstance();
                    Book book = new Book();//create Book class object to set booking data
                    book.setDate(new Date(System.currentTimeMillis()) + "");//set current date
                    book.setHour(String.valueOf(calendar.get(Calendar.HOUR)));//set current time
                    book.setMinute(String.valueOf(calendar.get(Calendar.MINUTE)));//set current min
                    book.setOwneruid(ownerdetail.getUid());//get owner id
                    book.setTotalprice("0.0");//set total price
                    book.setUseruid(profile.getUid());//set user id

                    final DatabaseReference dr = ((SingleTask) getActivity().getApplication()).getBookingDatabaseReference().child(profile.getUid()).push();
                    dr.setValue(book).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                sharedPreferences.edit().putString("status", ownerdetail.getUid()).commit();
                                sharedPreferences.edit().putString("pushkey", dr.getKey()).commit();
                                parkingBookStatus(false);

                            }

                        }
                    });


                } else {
                    Toast.makeText(getActivity(), "Please Select a Marker", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void parkingBookStatus(boolean status)
    {
        //update parking status here
        ((SingleTask) getActivity().getApplication()).getParkingDatabaseReference().child(ownerdetail.getCityname()).child(ownerdetail.getUid() + "/bookstatus").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() //change the owner book status in firebase
        {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Successfully Booked", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getOwnerFeedbackHere() {
        final List<Feedback> feedbackList = new ArrayList<>();
        if (ownerdetail != null) {

            ((SingleTask) getActivity().getApplication()).getFeedbackDatabaseReference().child(ownerdetail.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getChildrenCount() > 0) {
                        Iterator<DataSnapshot> it = snapshot.getChildren().iterator();
                        while (it.hasNext()) {
                            DataSnapshot ds = it.next();
                            Log.e("error", ds.toString());
                            Feedback feedback = ds.getValue(Feedback.class);
                            feedbackList.add(feedback);

                        }

                        View v = getActivity().getLayoutInflater().inflate(R.layout.feedback_list, null, false);
                        //alert dialog of feedback here
                        AlertDialog.Builder a = new AlertDialog.Builder(getActivity());
                        final AlertDialog alertDialog = a.create();
                        alertDialog.setView(v);
                        alertDialog.setCancelable(false);
                        alertDialog.show();

                        //initialize reylcer object here
                        RecyclerView recyclerView = v.findViewById(R.id.myfeedbackrecycler);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                        recyclerView.setAdapter(new CustomFeedbackAdapter(feedbackList, ownerdetail));

                        v.findViewById(R.id.close_icon).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                alertDialog.dismiss();
                            }
                        });

                    } else {
                        Toast.makeText(getActivity(), "No Reviews are Available.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        } else {
            Toast.makeText(getActivity(), "Please Select A Parking From Map Marker.", Toast.LENGTH_SHORT).show();
        }
    }

    private Profile ownerdetail;
    private void fetchOwnerDetail(String owneruid) {
        ((SingleTask) getActivity().getApplication()).getProfileDatabaseReference().child(owneruid).addListenerForSingleValueEvent(new ValueEventListener() //get owner folder from firebase
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ownerdetail = snapshot.getValue(Profile.class);//get owner detail from profile clas
                Log.e("error", ownerdetail.getAddress());
                setOwnerDetailInUi(ownerdetail); //set owner detail in ui
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //set owner detail in ui
    private void setOwnerDetailInUi(Profile ownerdetail) {
        toname.setText(ownerdetail.getName());//set owner name
        tomobile.setText(ownerdetail.getMobile());//set owner Mobile
        toaddress.setText(ownerdetail.getAddress());//set owner address
        tocharge.setText(ownerdetail.getChargeperhour());//set owner charge per hour
        //set current client vehicle number from current profile
        tcvehicle.setText(profile.getVehiclenumber());
    }


}
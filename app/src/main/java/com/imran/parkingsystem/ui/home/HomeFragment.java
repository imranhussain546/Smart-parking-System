package com.imran.parkingsystem.ui.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.imran.parkingsystem.LoginActivity;
import com.imran.parkingsystem.MainActivity;
import com.imran.parkingsystem.R;
import com.imran.parkingsystem.SingleTask;
import com.imran.parkingsystem.adapter.ModuleAdapter;
import com.imran.parkingsystem.listener.LocationService;
import com.imran.parkingsystem.module.AddPark;
import com.imran.parkingsystem.module.Book;
import com.imran.parkingsystem.module.Feedback;
import com.imran.parkingsystem.module.Module;
import com.imran.parkingsystem.module.Profile;
import com.imran.parkingsystem.util.GpsUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class HomeFragment extends Fragment implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {
    private RecyclerView moduleRecyclerView;
    private List<Module> moduleList;
    private SliderLayout sliderLayout;
    private HashMap<String, Integer> Hash_file_maps;
    private Button logoutbutton;
    private GpsUtils gpsUtils;
    private boolean status;
    private Profile profile;
    private SharedPreferences sharedPreferences;
    @Override

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        sharedPreferences = getActivity().getSharedPreferences("session", Context.MODE_PRIVATE);//create sharedpreferences object
       // profile = ((MainActivity) getActivity()).getProfiledata();

        return root;


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //initialize all views
        initViews(view);
        mySliderImages();
        //custom method for add module
        myModules();
        gpsUtils = new GpsUtils(getActivity());
        gpsEnable();

              //setlayout in recyclerview either grid or list
        moduleRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));
        //give modules list to the custom moduleadapter class
        ModuleAdapter moduleAdapter = new ModuleAdapter(moduleList);
        //call listner on module adapter
        moduleAdapter.setOnMyModuleClickListener(new ModuleAdapter.MyModuleClickListener() {
            @Override
            public void onModuleClick(View view, int position) {
                NavController navController = Navigation.findNavController(view);
                //get Module object from module list
                Module module = moduleList.get(position);
                if (module.getName().equalsIgnoreCase(getResources().getString(R.string.menu_bookparking)))
                {
                    //go to book parking page

                    String owneruid = sharedPreferences.getString("status", null);
                    if (owneruid== null)
                    {
                        Toast.makeText(getActivity(), "Booking", Toast.LENGTH_SHORT).show();
                        navController.navigate(R.id.action_nav_home_to_nav_bookparking);
                    }
                    else {
                        Toast.makeText(getActivity(), "All ready booked", Toast.LENGTH_SHORT).show();
                        checkBookingStatus(owneruid);
                    }


                }
                else if (module.getName().equalsIgnoreCase(getResources().getString(R.string.menu_Profile)))
                {
                    //go to profile page
                    navController.navigate(R.id.action_nav_home_to_nav_profile);
                    Toast.makeText(getActivity(), "Profile", Toast.LENGTH_SHORT).show();
                }
                else if (module.getName().equalsIgnoreCase(getResources().getString(R.string.menu_parkinghistory)))
                {
                    //go to parking history page
                    navController.navigate(R.id.action_nav_home_to_nav_parkinghistory);
                    Toast.makeText(getActivity(), "Parking", Toast.LENGTH_SHORT).show();
                }
                else if (module.getName().equalsIgnoreCase(getResources().getString(R.string.menu_addparking)))
                {
                    //go to add parking page
                    navController.navigate(R.id.action_nav_home_to_nav_addparking);
                    Toast.makeText(getActivity(), "Add Parking", Toast.LENGTH_SHORT).show();
                }
            }
        });
        moduleRecyclerView.setAdapter(moduleAdapter);
        logoutbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

    }
    private Book currentBooking;
    private Profile ownerProfileDetail;
    private float totalamount;
    private void checkBookingStatus(String owneruid)
    {
        View view = getActivity().getLayoutInflater().inflate(R.layout.payment_alert, null, false);//add payment alert layout
        //alert dialog
        AlertDialog.Builder a = new AlertDialog.Builder(getActivity());
        final AlertDialog alertDialog = a.create();
        alertDialog.setView(view);//set alert layout in alert dailog
        alertDialog.setCancelable(false);//cancel false
        alertDialog.show();//show the dailog
        //get current booking detail
        getBookingDetail(owneruid);//get owner uid
        final TextView tstarttime = view.findViewById(R.id.booking_starttime);
        final TextView tendtime = view.findViewById(R.id.booking_endtime);
        final TextView ttotalprice = view.findViewById(R.id.booking_total_price);

        final ImageView closebutton = view.findViewById(R.id.close_icon);
        final Button stopbutton = view.findViewById(R.id.booking_button_stop);
        final Button feedbutton = view.findViewById(R.id.booking_button_feedback);
        feedbutton.setVisibility(View.GONE);
        final Button paybutton = view.findViewById(R.id.booking_button_pay);
        paybutton.setVisibility(View.GONE);
        getOwnerProfileData(owneruid);//get owner data
        //perform stop buttons event here
        stopbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (currentBooking != null)
                {
                    int endhour = Calendar.getInstance().get(Calendar.HOUR);//get end hour
                    int endminute = Calendar.getInstance().get(Calendar.MINUTE);//get wnd min

                    tstarttime.setText(currentBooking.getHour() + ":" + currentBooking.getMinute());//set start hour or start min in ui
                    tendtime.setText(endhour + ":" + endminute);//set end hour or end min in ui

                    int totalstartinminute = Integer.parseInt(currentBooking.getHour()) * 60 + Integer.parseInt(currentBooking.getMinute());//convert start hour in min then + start min then get total start min
                    int totalendinminute = endhour * 60 + endminute;//convert end hour in min then + end min then get total end min
                    //start time - end time
                    int totalduration = totalendinminute - totalstartinminute;//get total duration

                    float perminutecharge = Float.parseFloat(ownerProfileDetail.getChargeperhour()) / 60;//get charge per hour then divide by 60min then get per min charge
                    Log.e("error per minute ",perminutecharge+"");

                    totalamount = totalduration * perminutecharge;//total time multiply by permin charge then get total amount
                    ttotalprice.setText(String.valueOf(totalamount));//set total amount in UI
                    feedbutton.setVisibility(View.VISIBLE);//now feedback botton visible
                    stopbutton.setVisibility(View.GONE);//not visible
                }
            }
        });
        feedbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
//open feedback form after given feedback enable paybutton
                View v = getActivity().getLayoutInflater().inflate(R.layout.feedback_alert, null, false);
                //alert dialog of feedback here
                AlertDialog.Builder a = new AlertDialog.Builder(getActivity());
                final AlertDialog alertDialog = a.create();
                alertDialog.setView(v);
                alertDialog.setCancelable(false);
                alertDialog.show();
                v.findViewById(R.id.feedback_close_icon).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        alertDialog.dismiss();//when click on close button then feedback alert close
                    }
                });
                //initialize all view here
                TextView townernameandmobile = v.findViewById(R.id.ownernameandmobile);
                TextView towneraddress = v.findViewById(R.id.owneraddress);
                final EditText comments = v.findViewById(R.id.commentshere);
                final TextView ratevalue = v.findViewById(R.id.ratevalue);
                final RatingBar ratingBar = v.findViewById(R.id.feedback_rating);

                Button feedsubmit = v.findViewById(R.id.feedback_submit);
                //set details in UI
                townernameandmobile.setText("Mr./Ms. " + ownerProfileDetail.getName() + "\t" + "Mob. " + ownerProfileDetail.getMobile());
                towneraddress.setText(ownerProfileDetail.getAddress());
                //event on feedback submit button
                feedsubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        String message = comments.getText().toString();//get commint and store in msg
                        if (TextUtils.isEmpty(message)) {
                            Toast.makeText(getActivity(), "Please Enter Comments", Toast.LENGTH_SHORT).show();
                            comments.requestFocus();
                        } else if (ratevalue.getText().toString().equalsIgnoreCase("0.0")) {
                            Toast.makeText(getActivity(), "Please Give Star", Toast.LENGTH_SHORT).show();
                            ratingBar.requestFocus();
                        }else {
                            Feedback feedback = new Feedback();//create feedback class obj
                            feedback.setMessage(message);//set comment
                            feedback.setRate(Float.parseFloat(ratevalue.getText().toString()));//set rate
                            feedback.setOwneruid(ownerProfileDetail.getUid());//set uid
                            feedback.setRenteruid(profile.getUid());//set user uid
                            ((SingleTask) getActivity().getApplication()).getFeedbackDatabaseReference().child(ownerProfileDetail.getUid()).push().setValue(feedback).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getActivity(), "Successfully Feedback Given", Toast.LENGTH_SHORT).show();
                                        paybutton.setVisibility(View.VISIBLE);//visible pay button
                                        feedbutton.setVisibility(View.GONE); //feedback not visible
                                        alertDialog.dismiss();//feedback dailog close
                                    }
                                }
                            });
                        }
                    }
                });
                ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float v, boolean b)
                    {
                        ratevalue.setText(String.valueOf(v));
                    }
                });
            }
        });
        //pay button event
        paybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
//update booking status in parking
                //remove from shareprefrences
                //dismiss the alert
                ((SingleTask) getActivity().getApplication()).getParkingDatabaseReference().child(ownerProfileDetail.getCityname()).child(ownerProfileDetail.getUid() + "/bookstatus").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            String key = sharedPreferences.getString("pushkey", null);
                            if (key != null)
                            {
                                ((SingleTask) getActivity().getApplication()).getBookingDatabaseReference().child(profile.getUid()).child(key + "/totalprice").setValue(String.valueOf(totalamount)).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if (task.isSuccessful())
                                        {
                                            Toast.makeText(getActivity(), "Successfully Payment", Toast.LENGTH_SHORT).show();
                                            sharedPreferences.edit().remove("status").commit();
                                            sharedPreferences.edit().remove("pushkey").commit();
                                            alertDialog.dismiss();
                                        }
                                    }
                                });
                            }
                        }

                    }
                });
            }
        });
        closebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }

    private void getOwnerProfileData(String owneruid)
    {
        ((SingleTask) getActivity().getApplication()).getProfileDatabaseReference().child(owneruid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ownerProfileDetail = snapshot.getValue(Profile.class);//set owner detail in ownerprofiledetail
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getBookingDetail(final String owneruid)
    {
        ((SingleTask) getActivity().getApplication()).getBookingDatabaseReference().child(profile.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterator<DataSnapshot> it = snapshot.getChildren().iterator();
                while (it.hasNext()) {
                    DataSnapshot ds = it.next();
                    Log.e("error", ds.getValue() + "");
                    Book book = ds.getValue(Book.class);
                    if (book.getOwneruid().equals(owneruid)) {
                        currentBooking = book;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public boolean gpsEnable() {
        gpsUtils.turnGPSOn(new GpsUtils.OnGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {

                status = isGPSEnable;

            }
        });
        return status;
    }


    private void mySliderImages()
    {
        Hash_file_maps = new HashMap();
        Hash_file_maps.put("Always check your area around.", R.drawable.park1);
        Hash_file_maps.put("There must not be litter on the ground.", R.drawable.park2);
        Hash_file_maps.put("Keep Parking clean to make it disease free.", R.drawable.park3);
        Hash_file_maps.put("Come, join and pledge together to park.", R.drawable.park4);
        Hash_file_maps.put("always follow the lane.", R.drawable.park5);

        for (String name : Hash_file_maps.keySet()) {

            TextSliderView textSliderView = new TextSliderView(getActivity());
            textSliderView
                    .description(name)
                    .image(Hash_file_maps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(this);
            textSliderView.bundle(new Bundle());
            textSliderView.getBundle()
                    .putString("extra", name);
            sliderLayout.addSlider(textSliderView);
        }

        sliderLayout.setPresetTransformer(SliderLayout.Transformer.Accordion);
        sliderLayout.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        sliderLayout.setCustomAnimation(new DescriptionAnimation());
        sliderLayout.setDuration(3000);
        sliderLayout.addOnPageChangeListener(this);
    }

    private void myModules()
    {
        Module m1=new Module(getResources().getString(R.string.menu_bookparking),R.drawable.booking);
        Module m2=new Module(getResources().getString(R.string.menu_Profile),R.drawable.profile);
        Module m3=new Module(getResources().getString(R.string.menu_addparking),R.drawable.add_parking);
        Module m4=new Module(getResources().getString(R.string.menu_parkinghistory),R.drawable.parking_history);

        moduleList=new ArrayList<>();
        moduleList.add(m1);
        moduleList.add(m2);
        moduleList.add(m3);
        moduleList.add(m4);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getProfileData();
    }
    public Profile getProfileData() {

        String currentuid = ((SingleTask) getActivity().getApplication()).getFirebaseAuth().getCurrentUser().getUid();
        if (currentuid != null) {
            ((SingleTask) getActivity().getApplication()).getProfileDatabaseReference().child(currentuid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.e("profiledata", snapshot.toString());
                    profile = snapshot.getValue(Profile.class);
                    Log.e("profiledata", profile + "");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            Log.e("profile data", "please try after some time");
        }
        return profile;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (status==true) {

            getActivity().registerReceiver(((SingleTask) getActivity().getApplication()).getLocationBrodcast(), new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
          getActivity().startService(new Intent(getActivity(), LocationService.class));
        }

        Log.e("Homefragment"," onpause ");
    }

    @Override
    public void onStop() {
        super.onStop();



            getActivity().startService(new Intent(getActivity(), LocationService.class));


        Log.e("Homefragment"," onstop ");
    }



    private void initViews(View view)
    {
        moduleRecyclerView = view.findViewById(R.id.mymodulerecycler);
        sliderLayout = view.findViewById(R.id.slider);
        logoutbutton=view.findViewById(R.id.logoutbutton);
    }
    private void logout()
    {
        ((SingleTask)getActivity().getApplication()).getgoogleSignInClient().signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful()) {
                    ((SingleTask) getActivity().getApplication()).getFirebaseAuth().signOut();
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                }
            }
        });
    }

    @Override
    public void onSliderClick(BaseSliderView slider) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
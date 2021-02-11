package com.imran.parkingsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.imran.parkingsystem.util.GpsUtils;

public class SplashActivity extends AppCompatActivity {
    
private TextView tittle;
private GpsUtils gpsUtils;
private boolean status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_splash);

        gpsUtils = new GpsUtils(this);
        init();

        Animation animation=AnimationUtils.loadAnimation(this,R.anim.slideup);
        tittle.setAnimation(animation);


        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation)
            {

            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
              //  if (gpsEnable()) {

                        FirebaseUser currentuser = ((SingleTask) getApplication()).getFirebaseAuth().getCurrentUser();
                        if (currentuser != null) {
                            gotohomepage();
                        } else {
                            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                            finish();
                        }

              //  }
               // else {
              //      Toast.makeText(SplashActivity.this, "Please Enable Gps", Toast.LENGTH_SHORT).show();
                //}


            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private void gotohomepage()
    {
       Intent intent=new Intent(SplashActivity.this,MainActivity.class);
       startActivity(intent);
       finish();
    }

   /* public boolean gpsEnable() {
        gpsUtils.turnGPSOn(new GpsUtils.OnGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {

                status = isGPSEnable;

            }
        });
        return status;
    }*/

    private void init()
    {
        tittle=findViewById(R.id.splashtitle);
    }
}
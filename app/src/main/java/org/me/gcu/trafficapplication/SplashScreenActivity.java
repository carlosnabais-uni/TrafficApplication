package org.me.gcu.trafficapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import gr.net.maroulis.library.EasySplashScreen;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EasySplashScreen config = new EasySplashScreen(SplashScreenActivity.this)
                .withFullScreen()
                .withTargetActivity(MainActivity.class)
                .withSplashTimeOut(5000)
                .withBackgroundColor(Color.parseColor("#FFFFFF"))
                .withAfterLogoText("Traffic Scotland")
                .withLogo(R.drawable.ic_traffic_cone_small);

        config.getAfterLogoTextView().setTextColor(getResources()
                .getColor(R.color.action_bar_text_color));


        View splashScreen = config.create();
        setContentView(splashScreen);
    }
}

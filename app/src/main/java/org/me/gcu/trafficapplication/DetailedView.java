package org.me.gcu.trafficapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DetailedView extends AppCompatActivity {

    private String title, description,
            endDate, startDate,
            publishedDate, type,
            location, laneClosures,
            works, trafficManagement,
            diversionInfo, lastUpdate;
    private LinearLayout detailedInformationView;
    private TextView detailsTextView, titleTextView, durationTextView;
    private StringBuilder builder;
    private int workDuration;
    private Boolean hasWorkDuration = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_view);

        // START ---- change colour of back arrow
        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.action_bar_text_color), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        // END ---- change colour of back arrow

        title = getIntent().getStringExtra("TITLE");
        description = getIntent().getStringExtra("DESCRIPTION");
        publishedDate = getIntent().getStringExtra("PUBLISHED_DATE");
        lastUpdate = getIntent().getStringExtra("LAST_UPDATE");

        if(getIntent().getStringExtra("WORK_DURATION") != null) {
            workDuration = new Integer(getIntent().getStringExtra("WORK_DURATION"));
            hasWorkDuration = true;
        }

        if(getIntent().getStringExtra("END_DATE") != null) {
            endDate = getIntent().getStringExtra("END_DATE");
        }

        if(getIntent().getStringExtra("START_DATE") != null) {
            startDate = getIntent().getStringExtra("START_DATE");
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(title);

        builder = new StringBuilder();

        detailedInformationView = findViewById(R.id.detailedInformationView);
        detailsTextView = new TextView(this);


        getIntent().describeContents();

        if(getIntent().getStringExtra("TYPE") != null) {
            type = getIntent().getStringExtra("TYPE");
        }

        if (getIntent().getStringExtra("LOCATION") != null) {
            location = getIntent().getStringExtra("LOCATION");
        }

        if (getIntent().getStringExtra("LANE_CLOSURES") != null) {
            laneClosures = getIntent().getStringExtra("LANE_CLOSURES");
        }

        if (getIntent().getStringExtra("WORKS") != null) {
            works = getIntent().getStringExtra("WORKS");
        }

        if (getIntent().getStringExtra("TRAFFIC_MANAGEMENT") != null) {
            trafficManagement = getIntent().getStringExtra("TRAFFIC_MANAGEMENT");
        }

        if (getIntent().getStringExtra("DIVERSION_INFO") != null) {
            diversionInfo = getIntent().getStringExtra("DIVERSION_INFO");
        }

        titleTextView = new TextView(this);
        titleTextView.setText(title);
        titleTextView.setTextColor(getResources().getColor(R.color.action_bar_text_color));
        titleTextView.setTextSize(20f);

        if(startDate != null) {
            builder.append("\n" + startDate).append("\n");
        }

        if(endDate != null) {
            builder.append(endDate).append("\n\n");
        }

        if(type != null) {
            builder.append("Type:\n" + type).append("\n\n");
        }

        if(description != null) {
            builder.append("Description:\n" + description).append("\n\n");
        }

        if(lastUpdate != null) {
            builder.append("Last Update:\n" + lastUpdate).append("\n\n");
        }

        if(location != null) {
            builder.append("Location:\n" + location).append("\n\n");
        }

        if(laneClosures != null) {
            builder.append("Lane Closures:\n" + laneClosures).append("\n\n");
        }

        if(works != null) {
            builder.append("Work being performed:\n" + works).append("\n\n");
        }

        if(trafficManagement != null) {
            builder.append("Traffic Management:\n" + trafficManagement).append("\n\n");
        }

        if(diversionInfo != null) {
            builder.append("Diversion Info:\n" + diversionInfo).append("\n\n");
        }

        detailsTextView.setText(builder);
        detailedInformationView.addView(titleTextView);

        if(hasWorkDuration) {
            durationTextView = new TextView(this);
            durationTextView.setText(
                    "Expected work duration: " +
                            workDuration + (workDuration != 1 ? " days" : " day"));

            if(workDuration <= 1) {
                durationTextView.setTextColor(getResources().getColor(R.color.colorSuccess));
            } else if (workDuration < 4) {
                durationTextView.setTextColor(getResources().getColor(R.color.colorWarning));
            } else {
                durationTextView.setTextColor(getResources().getColor(R.color.colorDanger));
            }

            detailedInformationView.addView(durationTextView);
        }

        detailedInformationView.addView(detailsTextView);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

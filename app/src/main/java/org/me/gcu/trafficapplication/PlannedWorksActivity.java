package org.me.gcu.trafficapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;


public class PlannedWorksActivity extends AppCompatActivity implements View.OnClickListener {

    private String result = "";
    private String urlSource = "https://trafficscotland.org/rss/feeds/plannedroadworks.aspx";
    private String inputDate;
    private LinearLayout plannedWorksLinearLayout;
    private TextView datePickedView;
    private Button updatePlannedWorks;
    private DatePickerDialog.OnDateSetListener datePickerListener;
    private CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planned_works);

        // START ---- change colour of back arrow
        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.action_bar_text_color), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        // END ---- change colour of back arrow

        plannedWorksLinearLayout = findViewById(R.id.plannedWorksLinearLayout);
        datePickedView = findViewById(R.id.datePickedView);
        progressDialog = new CustomProgressDialog(this);

        datePickedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                DatePickerDialog datePicker = new DatePickerDialog(
                        PlannedWorksActivity.this,
                        android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth,
                        datePickerListener, year, month, day);
                datePicker.getWindow().setBackgroundDrawable(new ColorDrawable(Color.GRAY));
                datePicker.show();
            }
        });

        datePickerListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String dayString = "";
                if(dayOfMonth < 10) {
                    dayString = "0" + dayOfMonth;
                } else {
                    dayString = dayOfMonth + "";
                }
                updatePlannedWorks.setEnabled(true);
                inputDate = dayString + "/" + month + "/" + year;
                datePickedView.setText(inputDate);
            }
        };

        updatePlannedWorks = findViewById(R.id.displayPlannedWorks);
        updatePlannedWorks.setOnClickListener(this);
        updatePlannedWorks.setEnabled(false);
    }

    public void onClick(View v) {
        // removes previous results before making a call to the API so to avoid duplicates
        plannedWorksLinearLayout.removeAllViews();
        startProgress();
    }

    public void startProgress() {
        progressDialog.displayLoadingDialog();
        new Thread(new Task(urlSource, inputDate)).start();
    }

    private class Task implements Runnable{
        private String url;
        private String date;

        public Task(String aurl, String inputDate) {
            url = aurl;
            date = inputDate;
        }

        @Override
        public void run() {
            URL aurl;
            URLConnection yc;
            BufferedReader in;
            String inputLine;

            try {
                aurl = new URL(url);
                yc = aurl.openConnection();
                in = new BufferedReader(new InputStreamReader(yc.getInputStream()));

                while ((inputLine = in.readLine()) != null) {
                    result += inputLine;
                }
                in.close();
            }
            catch (IOException ae) {
                Log.e("MyTag", "ioexception");
            }

            PlannedWorksActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        PlannedWorksParser parser = new PlannedWorksParser();
                        ArrayList<PlannedWork> plannedWorks = parser.processPlannedWorks(
                                result, date, "", true);

                        progressDialog.removeLoadingDialog();

                        parser.displayPlannedWorks(PlannedWorksActivity.this,
                                plannedWorks, plannedWorksLinearLayout);
                    } catch (IOException e) {

                    } catch (XmlPullParserException e) {

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

}

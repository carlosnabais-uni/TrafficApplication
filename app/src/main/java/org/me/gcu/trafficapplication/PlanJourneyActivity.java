package org.me.gcu.trafficapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PlanJourneyActivity extends AppCompatActivity {

    private String plannedWorksResult = "";
    private String currentWorksResult = "";
    private String incidentsResult = "";
    private String plannedRoadWorksLink = "https://trafficscotland.org/rss/feeds/plannedroadworks.aspx";
    private String currentRoadWorksLink = "https://trafficscotland.org/rss/feeds/roadworks.aspx";
    private String currentIncidentsLink = "https://trafficscotland.org/rss/feeds/currentincidents.aspx";
    private EditText startPoint, endPoint;
    private Button searchButton;
    private String inputDate, inputStartLocation, inputEndLocation;
    private TextView datePickedView;
    private DatePickerDialog.OnDateSetListener datePickerListener;
    private CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_journey);

        // START ---- change colour of back arrow
        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.action_bar_text_color), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        // END ---- change colour of back arrow

        datePickedView = findViewById(R.id.datePickedView);
        searchButton = findViewById(R.id.searchLocationButton);
        startPoint = findViewById(R.id.startPoint);
        endPoint = findViewById(R.id.endPoint);
        progressDialog = new CustomProgressDialog(this);

        datePickedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                DatePickerDialog datePicker = new DatePickerDialog(
                        PlanJourneyActivity.this,
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
                if(inputStartLocation != "" && inputStartLocation != null &&
                        inputEndLocation != "" && inputEndLocation != null) {
                    searchButton.setEnabled(true);
                } else {
                    searchButton.setEnabled(false);
                }

                inputDate = dayString + "/" + month + "/" + year;
                datePickedView.setText(inputDate);
            }
        };

        endPoint.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(inputDate == null
                        || s.toString().trim().length() == 0
                        || inputStartLocation == null
                        || inputStartLocation == ""
                ) {
                    searchButton.setEnabled(false);
                } else {
                    searchButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                inputEndLocation = endPoint.getText().toString();
            }
        });

        startPoint.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(inputDate == null
                        || s.toString().trim().length() == 0
                        || inputEndLocation == null
                        || inputEndLocation == ""
                ) {
                    searchButton.setEnabled(false);
                } else {
                    searchButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                inputStartLocation = startPoint.getText().toString();
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard(v);
                startProgress();
            }
        });

        searchButton.setEnabled(false);
    }

    public void closeKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void startProgress() {
        progressDialog.displayLoadingDialog();
        new Thread(new Task(currentIncidentsLink, currentRoadWorksLink,
                plannedRoadWorksLink, inputDate)).start();
    }

    private class Task implements Runnable {
        private String currentIncidentsUrl, currentRoadworksUrl, plannedRoadworksUrl;
        private String date;

        public Task(String incidents, String currentRoadworks,
                    String plannedRoadworks, String inputDate)
        {
            currentIncidentsUrl = incidents;
            currentRoadworksUrl = currentRoadworks;
            plannedRoadworksUrl = plannedRoadworks;
            date = inputDate;
        }

        @Override
        public void run() {
            URL incidentsUrl, currentWorksUrl, plannedWorksUrl;
            URLConnection incidentsConnection, currentWorksConnection, plannedWorksConnection;
            final BufferedReader incidentsBuffer, currentWorksBuffer, plannedWorksBuffer;
            String inputLine;

            try {
                incidentsUrl = new URL(currentIncidentsUrl);
                currentWorksUrl = new URL(currentRoadworksUrl);
                plannedWorksUrl = new URL(plannedRoadworksUrl);

                incidentsConnection = incidentsUrl.openConnection();
                currentWorksConnection = currentWorksUrl.openConnection();
                plannedWorksConnection = plannedWorksUrl.openConnection();

                incidentsBuffer = new BufferedReader(new InputStreamReader(
                        incidentsConnection.getInputStream()));

                currentWorksBuffer = new BufferedReader(new InputStreamReader(
                        currentWorksConnection.getInputStream()));

                plannedWorksBuffer = new BufferedReader(new InputStreamReader(
                        plannedWorksConnection.getInputStream()));

                while ((inputLine = incidentsBuffer.readLine()) != null) {
                    incidentsResult += inputLine;
                }
                incidentsBuffer.close();

                while ((inputLine = currentWorksBuffer.readLine()) != null) {
                    currentWorksResult += inputLine;
                }
                currentWorksBuffer.close();

                while ((inputLine = plannedWorksBuffer.readLine()) != null) {
                    plannedWorksResult += inputLine;
                }
                plannedWorksBuffer.close();
            }
            catch (IOException ae) {
                Log.e("MyTag", "ioexception");
            }

            PlanJourneyActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        int i = 0;
                        Intent intent = new Intent(
                                PlanJourneyActivity.this, MapActivity.class);

                        PlannedWorksParser plannedWorksParser = new PlannedWorksParser();
                        ArrayList<PlannedWork> plannedWorks = plannedWorksParser.processPlannedWorks(
                                plannedWorksResult, date, "", true);

                        CurrentWorksParser currentWorksParser = new CurrentWorksParser();
                        ArrayList<CurrentWork> currentWorks = currentWorksParser.processCurrentWorks(
                                currentWorksResult, date, "", true
                        );

                        CurrentIncidentsParser currentIncidentsParser = new CurrentIncidentsParser();
                        ArrayList<CurrentIncident> currentIncidents =
                                currentIncidentsParser.processCurrentIncidents(incidentsResult,
                                        "", false);

                        for(CurrentWork work : currentWorks) {
                            intent.putStringArrayListExtra(
                                    "CURRENT_WORKS_" + i,
                                    new ArrayList<>(Arrays.asList(work.getLatitude(),
                                            work.getLongitude(),
                                            work.getTitle())));
                            i++;
                        }
                        i = 0;


                        for(PlannedWork work : plannedWorks) {
                            intent.putStringArrayListExtra("PLANNED_WORKS_" + i,
                                    new ArrayList<>(Arrays.asList(work.getLatitude(),
                                            work.getLongitude(),
                                            work.getTitle())));
                            i++;
                        }
                        i = 0;

                        for(CurrentIncident work : currentIncidents) {
                            intent.putStringArrayListExtra("CURRENT_INCIDENTS_" + i,
                                    new ArrayList<>(Arrays.asList(work.getLatitude(),
                                            work.getLongitude(),
                                            work.getTitle())));
                            i++;
                        }

                        intent.putExtra("START_POINT", inputStartLocation);
                        intent.putExtra("END_POINT", inputEndLocation);
                        intent.putExtra("IS_PLAN_JOURNEY", "true");
                        intent.putExtra("CURRENT_INCIDENTS",
                                String.valueOf(currentIncidents.size()));
                        intent.putExtra("PLANNED_WORKS",
                                String.valueOf(plannedWorks.size()));
                        intent.putExtra("CURRENT_WORKS",
                                String.valueOf(currentWorks.size()));

                        progressDialog.removeLoadingDialog();

                        startActivity(intent);
                    } catch (IOException e) {

                    } catch (XmlPullParserException e) {

                    }
                }
            });
        }
    }
}

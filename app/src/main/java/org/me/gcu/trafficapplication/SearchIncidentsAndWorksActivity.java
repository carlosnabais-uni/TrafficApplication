package org.me.gcu.trafficapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;

public class SearchIncidentsAndWorksActivity extends AppCompatActivity implements View.OnClickListener {

    private String plannedWorksResult = "";
    private String currentWorksResult = "";
    private String incidentsResult = "";
    private String plannedRoadWorksLink = "https://trafficscotland.org/rss/feeds/plannedroadworks.aspx";
    private String currentRoadWorksLink = "https://trafficscotland.org/rss/feeds/roadworks.aspx";
    private String currentIncidentsLink = "https://trafficscotland.org/rss/feeds/currentincidents.aspx";
    private Button updateList;
    private EditText searchField;
    private String userInput;
    private LinearLayout resultsLinearLayout;
    private CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_incidents_and_works);

        // START ---- change colour of back arrow
        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.action_bar_text_color), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        // END ---- change colour of back arrow

        progressDialog = new CustomProgressDialog(this);
        resultsLinearLayout = findViewById(R.id.searchResultsLinearLayout);
        updateList = findViewById(R.id.searchButton);
        updateList.setEnabled(false);
        searchField = findViewById(R.id.searchField);

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length()==0){
                    updateList.setEnabled(false);
                } else {
                    updateList.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                userInput = searchField.getText().toString();
            }
        });

        updateList.setOnClickListener(this);
    }

    public void closeKeyboard(View view) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void onClick(View v) {
        // removes previews results before making a call to the API so to avoid duplicates
        resultsLinearLayout.removeAllViews();
        closeKeyboard(v);
        startProgress();
    }

    private void startProgress() {
        progressDialog.displayLoadingDialog();
        new Thread(new Task(currentIncidentsLink, currentRoadWorksLink,
                plannedRoadWorksLink, userInput)).start();
    }

    public class Task implements Runnable {
        private String currentIncidentsUrl, currentRoadworksUrl, plannedRoadworksUrl;
        private String searchTerm;

        public Task(String incidents, String currentRoadworks,
                    String plannedRoadworks, String searchText)
        {
            currentIncidentsUrl = incidents;
            currentRoadworksUrl = currentRoadworks;
            plannedRoadworksUrl = plannedRoadworks;
            searchTerm = searchText;
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

            SearchIncidentsAndWorksActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        SearchIncidentsAndWorksParser parser = new SearchIncidentsAndWorksParser();
                        PlannedWorksParser plannedWorksParser = new PlannedWorksParser();
                        ArrayList<PlannedWork> plannedWorks = plannedWorksParser.processPlannedWorks(
                                plannedWorksResult, "", userInput, false);

                        CurrentWorksParser currentWorksParser = new CurrentWorksParser();
                        ArrayList<CurrentWork> currentWorks = currentWorksParser.processCurrentWorks(
                                currentWorksResult, "", userInput, false
                        );

                        CurrentIncidentsParser currentIncidentsParser = new CurrentIncidentsParser();
                        ArrayList<CurrentIncident> currentIncidents =
                                currentIncidentsParser.processCurrentIncidents(incidentsResult,
                                        userInput, true);

                        progressDialog.removeLoadingDialog();
                        parser.displayResultsFound(
                                SearchIncidentsAndWorksActivity.this,
                                currentIncidents, plannedWorks, currentWorks, resultsLinearLayout
                        );
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

package org.me.gcu.trafficapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class CurrentIncidentsActivity extends AppCompatActivity implements View.OnClickListener {

    private String result = "";
    private String urlSource = "https://trafficscotland.org/rss/feeds/currentincidents.aspx";
    private Button updateIncidents;
    private LinearLayout incidentLinearLayout;
    private CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_incidents);

        // START ---- change colour of back arrow
        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.action_bar_text_color), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        // END ---- change colour of back arrow

        progressDialog = new CustomProgressDialog(this);

        incidentLinearLayout = findViewById(R.id.currentIncidentsLinearLayout);
        updateIncidents = findViewById(R.id.displayCurrentIncidents);
        updateIncidents.setOnClickListener(this);
        startProgress();
    }


    public void onClick(View v) {
        // removes previews results before making a call to the API so to avoid duplicates
        incidentLinearLayout.removeAllViews();
        startProgress();
    }

    public void startProgress() {
        progressDialog.displayLoadingDialog();
        new Thread(new Task(urlSource)).start();
    }

    private class Task implements Runnable {
        private String url;

        public Task(String aurl) {
            url = aurl;
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

            CurrentIncidentsActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        CurrentIncidentsParser parser = new CurrentIncidentsParser();
                        ArrayList<CurrentIncident> parsedIncidents =
                                parser.processCurrentIncidents(result, "", false);

                        progressDialog.removeLoadingDialog();

                        parser.displayCurrentIncidents(
                                CurrentIncidentsActivity.this,
                                parsedIncidents, incidentLinearLayout);
                    } catch (IOException e) {

                    } catch (XmlPullParserException e) {

                    }
                }
            });
        }

    }
}

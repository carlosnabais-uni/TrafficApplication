package org.me.gcu.trafficapplication;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CurrentIncidentsParser extends AppCompatActivity {

    public ArrayList processCurrentIncidents(String informationToParse,
                                             String searchTerm, Boolean searchByTerm)
            throws IOException, XmlPullParserException
    {
        ArrayList<CurrentIncident> currentIncidents = new ArrayList<>();

        try {
            XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
            parserFactory.setNamespaceAware(true);
            XmlPullParser xpp = parserFactory.newPullParser();
            xpp.setInput(new StringReader( informationToParse ));

            int eventType = xpp.getEventType();
            CurrentIncident currentIncident = null;

            while(eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = xpp.getName();

                if(eventType == XmlPullParser.START_TAG) {
                    if(tagName.equalsIgnoreCase("item")) {
                        currentIncident = new CurrentIncident();
                    } else if(currentIncident != null) {
                        if(tagName.equalsIgnoreCase("title")) {
                            String text = xpp.nextText();
                            String[] textArray = text.split("-");
                            String titleString = "";
                            currentIncident.setType(
                                    textArray[textArray.length - 1]
                                            .replaceAll("\\s+", ""));
                            for(int i = 0; i < textArray.length; i++) {
                                if(i + 1 == textArray.length) {
                                    titleString = titleString.substring(0, titleString.length() - 2);
                                } else {
                                    titleString += textArray[i] + " - ";
                                }
                            }
                            currentIncident.setTitle(titleString);
                        } else if(tagName.equalsIgnoreCase("description")) {
                            currentIncident.setDescription(xpp.nextText());
                        } else if(tagName.equalsIgnoreCase("point")) {
                            String[] latLonArray = xpp.nextText().split(" ");
                            currentIncident.setLatitude(latLonArray[0]);
                            currentIncident.setLongitude(latLonArray[1]);
                        } else if (tagName.equalsIgnoreCase("pubDate")) {
                            currentIncident.setDate(currentIncident.parseDate(xpp.nextText()));
                        }
                    }
                } else if(eventType == XmlPullParser.END_TAG) {
                    if (tagName.equalsIgnoreCase("item")) {
                        if(!searchByTerm) {
                            currentIncidents.add(currentIncident);
                        } else if(currentIncident.getType().toLowerCase().contains(searchTerm.toLowerCase())
                                || currentIncident.getDescription().toLowerCase().contains(searchTerm.toLowerCase())
                                || currentIncident.getTitle().toLowerCase().contains(searchTerm.toLowerCase())) {
                            currentIncidents.add(currentIncident);
                        }
                    }
                }

                eventType = xpp.next();
            }

        } catch (XmlPullParserException e) {

        } catch (IOException e) {
        }

        return currentIncidents;
    }

    public void displayCurrentIncidents(final Context activityContext, ArrayList<CurrentIncident> parsedIncidents, LinearLayout layout) {
        Button mapButton, detailsButton;
        LinearLayout buttonsLayout;
        StringBuilder builder;
        TextView rowTextView, title;
        int index = 1;

        if(parsedIncidents.size() != 0) {
            for(final CurrentIncident incident : parsedIncidents) {
                rowTextView = new TextView(activityContext);
                title = new TextView(activityContext);
                buttonsLayout = new LinearLayout(activityContext);
                builder = new StringBuilder();

                Map<String, String> mapButtonIntent = new HashMap<>();
                mapButtonIntent.put("LATITUDE", incident.getLatitude());
                mapButtonIntent.put("LONGITUDE", incident.getLongitude());
                mapButtonIntent.put("TITLE", incident.getTitle());

                mapButton = new CustomButton().createCustomButton(
                        "View on Map", activityContext, MapActivity.class,
                        mapButtonIntent, "success");

                Map<String, String> detailsButtonIntent = new HashMap<>();
                detailsButtonIntent.put("TITLE", incident.getTitle());
                detailsButtonIntent.put("DESCRIPTION", incident.getDescription());
                detailsButtonIntent.put("LAST_UPDATE", incident.getDate());
                detailsButtonIntent.put("TYPE", incident.getType());

                detailsButton = new CustomButton().createCustomButton(
                        "View Details", activityContext, DetailedView.class,
                        detailsButtonIntent, "primary");

                // separator between each incident
                View line = new View(activityContext);
                line.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2
                ));
                line.setBackgroundColor(
                        line.getContext().getResources().getColor(R.color.action_bar_top_color));

                buttonsLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
                );
                buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
                buttonsLayout.addView(detailsButton);
                buttonsLayout.addView(mapButton);

                title.setText(incident.getTitle());
                title.setTextSize(20f);
                title.setTextColor(
                        activityContext.getResources().getColor(R.color.action_bar_text_color));

                builder.append("\n" + incident.getType() + "\n");
                rowTextView.setText(builder);
                layout.addView(title);
                layout.addView(rowTextView);
                layout.addView(buttonsLayout);

                if(index != parsedIncidents.size()) {
                    layout.addView(line);
                }
                index++;
            }
        } else {
            builder = new StringBuilder();
            rowTextView = new TextView(activityContext);
            builder.append("There are no active incidents.");
            rowTextView.setText(builder);
            layout.addView(rowTextView);
        }
    }
}

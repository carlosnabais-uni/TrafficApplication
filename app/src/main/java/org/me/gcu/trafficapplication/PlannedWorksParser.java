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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PlannedWorksParser extends AppCompatActivity {

    public ArrayList processPlannedWorks(
            String informationToParse, String inputDate, String searchTerm, Boolean searchByDate)
            throws IOException, XmlPullParserException
    {
        ArrayList<PlannedWork> plannedWorks = new ArrayList<>();

        try {
            XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
            parserFactory.setNamespaceAware(true);
            XmlPullParser xpp = parserFactory.newPullParser();
            xpp.setInput(new StringReader( informationToParse ));

            int eventType = xpp.getEventType();
            PlannedWork plannedWork = null;

            while(eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = xpp.getName();

                if(eventType == XmlPullParser.START_TAG) {
                    if(tagName.equalsIgnoreCase("item")) {
                        plannedWork = new PlannedWork();
                    } else if(plannedWork != null) {
                        if(tagName.equalsIgnoreCase("title")) {
                            plannedWork.setTitle(xpp.nextText());
                        } else if(tagName.equalsIgnoreCase("description")) {
                            String text = xpp.nextText();
                            String[] textArray = text.split("<br />");
                            plannedWork.setStartDate(textArray[0]);
                            plannedWork.setEndDate(textArray[1]);

                            String[] additionalDetails = textArray[2].split(":");

                            if(additionalDetails[0].replaceAll("\\s+", "")
                                    .equalsIgnoreCase("TYPE")) {

                                String finalLocationString = formattedTextString(
                                        additionalDetails[2].split(" "),2, false);

                                plannedWork.setType(additionalDetails[1].split(" ")[1]);
                                plannedWork.setLocation(finalLocationString);
                                plannedWork.setLaneClosures(additionalDetails[3]);
                            } else {
                                String finalTrafficManagementString = "";
                                String finalWorksString = formattedTextString(
                                        additionalDetails[1].split(" "), 1, true);

                                plannedWork.setWorks(finalWorksString);

                                if(additionalDetails.length > 3) {
                                    finalTrafficManagementString = formattedTextString(
                                            additionalDetails[3].split(" "), 1, false);
                                    plannedWork.setTrafficManagement(finalTrafficManagementString);
                                    plannedWork.setDiversionInformation(additionalDetails[3]);
                                } else {
                                    plannedWork.setTrafficManagement(additionalDetails[2]);
                                }
                            }

                        } else if(tagName.equalsIgnoreCase("point")) {
                            String[] latLonArray = xpp.nextText().split(" ");
                            plannedWork.setLatitude(latLonArray[0]);
                            plannedWork.setLongitude(latLonArray[1]);
                        } else if (tagName.equalsIgnoreCase("pubDate")) {
                            String text = xpp.nextText();
                            plannedWork.setParsedDate(plannedWork.parseDate(text));
                            plannedWork.setDate(plannedWork.formattedDate(text, false));
                        }
                    }
                } else if(eventType == XmlPullParser.END_TAG) {
                    if (tagName.equalsIgnoreCase("item")) {
                        if (searchByDate && plannedWork.getDate().equals(inputDate)) {
                            plannedWorks.add(plannedWork);
                        } else if(!searchByDate) {
                            if(plannedWork.getTitle().contains(searchTerm)
                                    || plannedWork.getTrafficManagement() != null &&
                                    plannedWork.getTrafficManagement().toLowerCase().contains(searchTerm.toLowerCase())
                                    || plannedWork.getDiversionInformation() != null &&
                                    plannedWork.getDiversionInformation().toLowerCase().contains(searchTerm.toLowerCase())
                                    || plannedWork.getWorks() != null &&
                                    plannedWork.getWorks().toLowerCase().contains(searchTerm.toLowerCase())
                                    || plannedWork.getLocation() != null &&
                                    plannedWork.getLocation().toLowerCase().contains(searchTerm.toLowerCase())
                                    || plannedWork.getLaneClosures() != null &&
                                    plannedWork.getLaneClosures().toLowerCase().contains(searchTerm.toLowerCase())
                                    || plannedWork.getType() != null &&
                                    plannedWork.getType().toLowerCase().contains(searchTerm.toLowerCase())) {

                                plannedWorks.add(plannedWork);
                            }
                        }
                    }
                }

                eventType = xpp.next();
            }

        } catch (XmlPullParserException e) {

        } catch (IOException e) {
        }

        return plannedWorks;
    }

    public void displayPlannedWorks(final Context activityContext, ArrayList<PlannedWork> parsedPlannedRoadWorks, LinearLayout layout) throws ParseException {
        StringBuilder builder;
        TextView rowTextView, title, expectedCompletionTime;
        LinearLayout buttonsLayout;
        Button mapButton, detailsButton;
        int index = 1;

        if(parsedPlannedRoadWorks.size() != 0) {
            for(final PlannedWork work : parsedPlannedRoadWorks) {
                title = new TextView(activityContext);
                expectedCompletionTime = new TextView(activityContext);
                buttonsLayout = new LinearLayout(activityContext);
                builder = new StringBuilder();

                Map<String, String> mapButtonIntent = new HashMap<>();
                mapButtonIntent.put("LATITUDE", work.getLatitude());
                mapButtonIntent.put("LONGITUDE", work.getLongitude());
                mapButtonIntent.put("TITLE", work.getTitle());

                mapButton = new CustomButton().createCustomButton(
                        "View on Map", activityContext, MapActivity.class,
                        mapButtonIntent, "success");

                int workingDays = calculateWorkingDays(
                        work.formattedDate(work.getStartDate(), true),
                        work.formattedDate(work.getEndDate(), true)
                );

                Map<String, String> detailsButtonIntent = new HashMap<>();
                detailsButtonIntent.put("TITLE", work.getTitle());
                detailsButtonIntent.put("START_DATE", work.getStartDate());
                detailsButtonIntent.put("END_DATE", work.getEndDate());
                detailsButtonIntent.put("PUBLISHED_DATE", work.getDate());
                detailsButtonIntent.put("TYPE", work.getType());
                detailsButtonIntent.put("LOCATION", work.getLocation());
                detailsButtonIntent.put("LANE_CLOSURES", work.getLaneClosures());
                detailsButtonIntent.put("WORKS", work.getWorks());
                detailsButtonIntent.put("TRAFFIC_MANAGEMENT", work.getTrafficManagement());
                detailsButtonIntent.put("DIVERSION_INFO", work.getDiversionInformation());
                detailsButtonIntent.put("WORK_DURATION", String.valueOf(workingDays));


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

                builder.append(work.getTitle()).append("\n\n").
                        append("Start Date: " + work.getParsedDate()).append("\n");

                title.setText(work.getTitle());
                title.setTextSize(20f);
                title.setTextColor(
                        activityContext.getResources().getColor(R.color.action_bar_text_color));

                expectedCompletionTime.setText(
                        "Expected work duration: " + workingDays +
                                (workingDays != 1 ? " days" : " day")
                );
                expectedCompletionTime.setTextSize(10f);

                if(workingDays <= 1) {
                    expectedCompletionTime.setTextColor(
                            activityContext.getResources().getColor(R.color.colorSuccess));
                } else if (workingDays < 4) {
                    expectedCompletionTime.setTextColor(
                            activityContext.getResources().getColor(R.color.colorWarning));
                } else {
                    expectedCompletionTime.setTextColor(
                            activityContext.getResources().getColor(R.color.colorDanger));
                }

                buttonsLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
                buttonsLayout.addView(detailsButton);
                buttonsLayout.addView(mapButton);

                layout.addView(title);
                layout.addView(expectedCompletionTime);
                layout.addView(buttonsLayout);

                if(index != parsedPlannedRoadWorks.size()) {
                    layout.addView(line);
                }
                index++;
            }
        } else {
            builder = new StringBuilder();
            rowTextView = new TextView(activityContext);
            builder.append("There are no planned works for the selected date.");
            rowTextView.setText(builder);
            layout.addView(rowTextView);
        }
    }


    private String formattedTextString(String[] originalString, int wordsToRemove, boolean isWork) {
        String finalString = "";
        List<String> finalStringArray = new ArrayList<>();
        finalStringArray.addAll(Arrays.asList(originalString));

        for(int i = 0; i < finalStringArray.size(); i++) {
            if(finalStringArray.get(i).contains("Traffic") && isWork) {
                String newString = finalStringArray.get(i).replace("Traffic", "");
                finalStringArray.set(i, newString);
            }
        }

        for(int i = 0; i < wordsToRemove; i++) {
            finalStringArray.remove(finalStringArray.size() - 1);
        }

        for(String word : finalStringArray) {
            finalString += word + " ";
        }

        return finalString;
    }

    private int calculateWorkingDays(String startDate, String endDate) throws ParseException {
        SimpleDateFormat startDayCount = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat endDayCount = new SimpleDateFormat("dd/MM/yyyy");

        Date parsedStartDate = startDayCount.parse(startDate);
        Date parsedEndDate = endDayCount.parse(endDate);

        long diff = parsedEndDate.getTime() - parsedStartDate.getTime();

        return (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }
}

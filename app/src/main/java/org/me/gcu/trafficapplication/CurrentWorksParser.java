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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CurrentWorksParser  extends AppCompatActivity {

    public ArrayList processCurrentWorks(
            String informationToParse, String inputDate, String searchTerm, Boolean searchByDate)
            throws IOException, XmlPullParserException
    {
        ArrayList<CurrentWork> currentWorks = new ArrayList<>();

        try {
            XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
            parserFactory.setNamespaceAware(true);
            XmlPullParser xpp = parserFactory.newPullParser();
            xpp.setInput(new StringReader( informationToParse ));

            int eventType = xpp.getEventType();
            CurrentWork currentWork = null;

            while(eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = xpp.getName();

                if(eventType == XmlPullParser.START_TAG) {
                    if(tagName.equalsIgnoreCase("item")) {
                        currentWork = new CurrentWork();
                    } else if(currentWork != null) {
                        if(tagName.equalsIgnoreCase("title")) {
                            currentWork.setTitle(xpp.nextText());
                        } else if(tagName.equalsIgnoreCase("description")) {
                            String[] textArray = xpp.nextText().split("<br />");
                            currentWork.setSearchStartDate(
                                    currentWork.formattedDate(textArray[0],
                                    true));
                            currentWork.setSearchEndDate(
                                    currentWork.formattedDate(textArray[1],
                                            true));
                            currentWork.setStartDate(textArray[0]);
                            currentWork.setEndDate(textArray[1]);

//                            currentWork.setDelayInformation(textArray[2]);
                        } else if(tagName.equalsIgnoreCase("point")) {
                            String[] latLonArray = xpp.nextText().split(" ");
                            currentWork.setLatitude(latLonArray[0]);
                            currentWork.setLongitude(latLonArray[1]);
                        } else if (tagName.equalsIgnoreCase("pubDate")) {
                            String text = xpp.nextText();
                            currentWork.setPublishedDate(currentWork.parseDate(text));
                        }
                    }
                } else if(eventType == XmlPullParser.END_TAG) {
                    if (tagName.equalsIgnoreCase("item")) {
                        if(searchByDate && currentWork.isBetweenDates(inputDate)) {
                            currentWorks.add(currentWork);
                        } else if(!searchByDate) {
                            if(currentWork.getDelayInformation() != null &&
                                    currentWork.getDelayInformation().toLowerCase().contains(searchTerm.toLowerCase())
                                    || currentWork.getDelayInformation() != null &&
                                    currentWork.getTitle().toLowerCase().contains(searchTerm.toLowerCase())) {
                                currentWorks.add(currentWork);
                            }
                        }
                    }
                }

                eventType = xpp.next();
            }

        } catch (XmlPullParserException e) {

        } catch (IOException e) {
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return currentWorks;
    }

    public void displayCurrentWorks(final Context activityContext, ArrayList<CurrentWork> parsedCurrentWorks, LinearLayout layout) throws ParseException {
        StringBuilder builder;
        TextView rowTextView, title, expectedCompletionTime;
        LinearLayout buttonsLayout;
        Button mapButton, detailsButton;
        int index = 1;

        if(parsedCurrentWorks.size() != 0) {
            for(final CurrentWork work : parsedCurrentWorks) {
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
                detailsButtonIntent.put("PUBLISHED_DATE", work.getPublishedDate());
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
                        append("Start Date: " + work.getPublishedDate()).append("\n");

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
                        LinearLayout.LayoutParams.WRAP_CONTENT)
                );
                buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
                buttonsLayout.addView(detailsButton);
                buttonsLayout.addView(mapButton);

                layout.addView(title);
                layout.addView(expectedCompletionTime);
                layout.addView(buttonsLayout);

                if(index != parsedCurrentWorks.size()) {
                    layout.addView(line);
                }
                index++;
            }
        } else {
            builder = new StringBuilder();
            rowTextView = new TextView(activityContext);
            builder.append("There are no current works for the selected date.");
            rowTextView.setText(builder);
            layout.addView(rowTextView);
        }
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

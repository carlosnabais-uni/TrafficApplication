package org.me.gcu.trafficapplication;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SearchIncidentsAndWorksParser {

    public void displayResultsFound(Context activityContext,
                                    ArrayList<CurrentIncident> currentIncidents,
                                    ArrayList<PlannedWork> plannedWorks,
                                    ArrayList<CurrentWork> currentWorks, LinearLayout layout)
            throws ParseException
    {
        if(currentIncidents.size() != 0 || plannedWorks.size() != 0 || currentWorks.size() != 0) {
            handleCurrentIncidents(activityContext, currentIncidents, layout);
            handleCurrentWorks(activityContext, currentWorks, layout);
            handlePlannedWorks(activityContext, plannedWorks, layout);
        } else {
            StringBuilder notFoundString = new StringBuilder();
            TextView notFoundTextView = new TextView(activityContext);
            notFoundString.append("No results matching your criteria were found. Please try again.");
            notFoundTextView.setText(notFoundString);
            layout.addView(notFoundTextView);
        }
    }

    private void handleCurrentIncidents(
            final Context activityContext,
            ArrayList<CurrentIncident> currentIncidents,
            LinearLayout layout)
    {
        Button mapButton, detailsButton;
        LinearLayout buttonsLayout;
        StringBuilder builder;
        TextView rowTextView, title;
        int index = 1;

        if(currentIncidents.size() != 0) {
            for(final CurrentIncident incident : currentIncidents) {
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

                builder.append("Found in: Current Incidents\n");
                rowTextView.setText(builder);
                layout.addView(title);
                layout.addView(rowTextView);
                layout.addView(buttonsLayout);

                if(index != currentIncidents.size()) {
                    layout.addView(line);
                }
                index++;
            }
        }
    }

    private void handleCurrentWorks(
            final Context activityContext,
            ArrayList<CurrentWork> currentWorks,
            LinearLayout layout
    ) throws ParseException {
        StringBuilder builder;
        TextView rowTextView, title, expectedCompletionTime;
        LinearLayout buttonsLayout;
        Button mapButton, detailsButton;
        int index = 1;

        if(currentWorks.size() != 0) {
            for(final CurrentWork work : currentWorks) {
                title = new TextView(activityContext);
                expectedCompletionTime = new TextView(activityContext);
                buttonsLayout = new LinearLayout(activityContext);
                rowTextView = new TextView(activityContext);
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


                builder.append("Found in: Current Road Works\n");
                builder.append("Start Date: " + work.getPublishedDate()).append("\n");

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

                rowTextView.setText(builder);
                layout.addView(title);
                layout.addView(rowTextView);
                layout.addView(expectedCompletionTime);
                layout.addView(buttonsLayout);

                if(index != currentWorks.size()) {
                    layout.addView(line);
                }
                index++;
            }
        }
    }

    private void handlePlannedWorks(
            final Context activityContext,
            ArrayList<PlannedWork> parsedPlannedRoadWorks,
            LinearLayout layout) throws ParseException
    {
        StringBuilder builder;
        TextView rowTextView, title, expectedCompletionTime;
        LinearLayout buttonsLayout;
        Button mapButton, detailsButton;
        int index = 1;

        if(parsedPlannedRoadWorks.size() != 0) {
            for(final PlannedWork work : parsedPlannedRoadWorks) {
                title = new TextView(activityContext);
                expectedCompletionTime = new TextView(activityContext);
                rowTextView = new TextView(activityContext);
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

                builder.append("Found in: Planned Road Works\n");
                builder.append("Start Date: " + work.getParsedDate()).append("\n");

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

                rowTextView.setText(builder);
                layout.addView(title);
                layout.addView(rowTextView);
                layout.addView(expectedCompletionTime);
                layout.addView(buttonsLayout);

                if(index != parsedPlannedRoadWorks.size()) {
                    layout.addView(line);
                }
                index++;
            }
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

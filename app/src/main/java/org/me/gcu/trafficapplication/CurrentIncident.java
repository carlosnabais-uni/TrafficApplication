package org.me.gcu.trafficapplication;

import java.util.Arrays;

public class CurrentIncident {
    private String title, type, description, date, latitude, longitude;

    public String parseDate(String date) {
        String[] dateArray =  date.split(" ");
        String[] dateArrayWithoutZone = Arrays.copyOf(dateArray, 5);
        String formattedDate = "", delimiter = " ";

        for(int i = 0; i <= dateArrayWithoutZone.length; i++) {
            if(i < dateArrayWithoutZone.length) {
                formattedDate += dateArrayWithoutZone[i] + delimiter;
            }
        }

        return formattedDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}

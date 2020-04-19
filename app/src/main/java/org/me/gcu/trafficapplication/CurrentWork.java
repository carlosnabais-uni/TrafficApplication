package org.me.gcu.trafficapplication;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class CurrentWork {
    private String title, startDate, endDate, searchStartDate, searchEndDate,
            delayInformation, latitude, longitude, publishedDate;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getSearchStartDate() {
        return searchStartDate;
    }

    public void setSearchStartDate(String searchStartDate) {
        this.searchStartDate = searchStartDate;
    }

    public String getSearchEndDate() {
        return searchEndDate;
    }

    public void setSearchEndDate(String searchEndDate) {
        this.searchEndDate = searchEndDate;
    }

    public String getDelayInformation() {
        return delayInformation;
    }

    public void setDelayInformation(String delayInformation) {
        this.delayInformation = delayInformation;
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

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public boolean isBetweenDates(String date) throws ParseException {
        SimpleDateFormat startDate = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat endDate = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat inputDate = new SimpleDateFormat("dd/MM/yyyy");

        Date parsedStartDate = startDate.parse(this.searchStartDate);
        Date parsedEndDate = endDate.parse(this.searchEndDate);
        Date parsedInputDate = inputDate.parse(date);

        return parsedStartDate.compareTo(parsedInputDate) * parsedInputDate.compareTo(parsedEndDate) >= 0;
    }

    public String parseDate(String date) {
        String[] dateArrayWithoutZone = Arrays.copyOf(dateSplitter(date), 4);
        String parsedDate = "", delimiter = " ";

        for(int i = 0; i <= dateArrayWithoutZone.length; i++) {
            if(i < dateArrayWithoutZone.length) {
                parsedDate += dateArrayWithoutZone[i] + delimiter;
            }
        }

        return parsedDate;
    }

    public String formattedDate(String date, boolean isDetails) {
        String[] dateArray =  dateSplitter(date);
        String[] newArray = new String[3];
        String formattedDate = "", delimiter = "/";

        if(isDetails) {
            for (int i = 0; i < dateArray.length; i++) {
                if (i == 3 || i == 4 || i == 5) {
                    newArray[i - 3] = dateArray[i];
                }
            }
        } else {
            for(int i = 0; i < dateArray.length; i++) {
                if(i == 1 || i == 2 || i == 3) {
                    newArray[i - 1] = dateArray[i];
                }
            }
        }


        if(newArray[1].equals("Jan") || newArray[1].equals("January")) {
            newArray[1] = "1";
        } else if(newArray[1].equals("Feb") || newArray[1].equals("February")) {
            newArray[1] = "2";
        } else if(newArray[1].equals("Mar") || newArray[1].equals("March")) {
            newArray[1] = "3";
        } else if(newArray[1].equals("Apr") || newArray[1].equals("April")) {
            newArray[1] = "4";
        } else if(newArray[1].equals("May") || newArray[1].equals("May")) {
            newArray[1] = "5";
        } else if(newArray[1].equals("Jun") || newArray[1].equals("June")) {
            newArray[1] = "6";
        } else if(newArray[1].equals("Jul") || newArray[1].equals("July")) {
            newArray[1] = "7";
        } else if(newArray[1].equals("Aug") || newArray[1].equals("August")) {
            newArray[1] = "8";
        } else if(newArray[1].equals("Sept") || newArray[1].equals("Sep") || newArray[1].equals("September")) {
            newArray[1] = "9";
        } else if(newArray[1].equals("Oct") || newArray[1].equals("October")) {
            newArray[1] = "10";
        } else if(newArray[1].equals("Nov") || newArray[1].equals("November")) {
            newArray[1] = "11";
        } else if(newArray[1].equals("Dec") || newArray[1].equals("December")) {
            newArray[1] = "12";
        }

        for(int i = 0; i < newArray.length; i++) {
            if((i + 1) == newArray.length) {
                formattedDate += newArray[i];
            } else {
                formattedDate += newArray[i] + delimiter;
            }
        }

        return formattedDate;
    }

    private String[] dateSplitter(String date) {
        String[] dateArray =  date.split(" ");
        return dateArray;
    }
}

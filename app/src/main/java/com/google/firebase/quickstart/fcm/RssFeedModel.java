package com.google.firebase.quickstart.fcm;

/**
 * Created by L4208412 on 27/4/2018.
 */

public class RssFeedModel {
    public String title;
    public String link;
    public String description;
    public String pubDate;

    public RssFeedModel(String title, String link, String description, String pubDate) {
        this.title = title;
        this.link = link;
        this.description = description;
        this.pubDate = pubDate;
    }
}

package com.google.firebase.quickstart.fcm;

/**
 * Created by L4208412 on 27/4/2018.
 */

public class RssFeedModel2 {
    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String link;

    public String getNewsLink() {
        return news_link;
    }

    public void setNewsLink(String news_link) {
        this.news_link = news_link;
    }

    public String news_link;

    public RssFeedModel2(String link,String news_link) {
        this.link = link;
        this.news_link = news_link;
    }
}

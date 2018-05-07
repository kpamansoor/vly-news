package com.google.firebase.quickstart.fcm;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ImageSpan;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicBoolean;

public class NewsDetailsActivity extends AppCompatActivity {

    private TextView detail_title;
    private TextView detail_desc;
    private TextView detail_date;
    private String title,desc,link,date;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_details);

        detail_title = (TextView)findViewById(R.id.detail_title);
        detail_desc = (TextView)findViewById(R.id.detail_desc);
        detail_date = (TextView)findViewById(R.id.detail_date);
        detail_desc.setMovementMethod(new ScrollingMovementMethod());

        title = getIntent().getStringExtra("title");
        desc = getIntent().getStringExtra("desc");
        link = getIntent().getStringExtra("link");
        date = getIntent().getStringExtra("date");

        detail_title.setText(title);
        detail_date.setText(date.substring(0, date.length() - 5));
        desc = desc.split("<p>")[1];
        desc = desc.substring(0, desc.length() - 5);
        desc.replace("&#8216;","'");
        desc.replace("&#8217;","'");
        desc.replace("[&#8230;]",".......");
        detail_desc.setText(desc);
    }


}

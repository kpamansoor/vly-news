package com.google.firebase.quickstart.fcm;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import java.util.List;

public class NewsDetailsActivity extends AppCompatActivity  {

    private TextView detail_title;
    private TextView detail_desc;
    private TextView detail_date;
    private String title,desc,link,date,link_image;
    private Button btn_full_story,btn_share;
    private ImageView detail_photo;
    private List<RssFeedModel2> image_links;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_details);

        detail_title = (TextView)findViewById(R.id.detail_title);
        detail_desc = (TextView)findViewById(R.id.detail_desc);
        detail_date = (TextView)findViewById(R.id.detail_date);
        btn_full_story = (Button) findViewById(R.id.btn_full_story);
        btn_share = (Button) findViewById(R.id.btn_share);
        detail_photo = (ImageView) findViewById(R.id.detail_photo);
        detail_desc.setMovementMethod(new ScrollingMovementMethod());

        title = getIntent().getStringExtra("title");
        desc = getIntent().getStringExtra("desc");
        link = getIntent().getStringExtra("link");
        date = getIntent().getStringExtra("date");
        link_image = getIntent().getStringExtra("link_image");

        detail_title.setText(title);
        detail_date.setText(date.substring(0, date.length() - 5));
        desc = desc.split("<p>")[1];
        desc = desc.substring(0, desc.length() - 5);
        desc = desc.replace("&#8216;","'");
        desc = desc.replace("&#8217;","'");
        desc = desc.replace("[&#8230;]",".......");
        detail_desc.setText(desc);


        if(link_image == null)
            detail_photo.setImageResource(R.drawable.vlylogo);
        else
            Picasso.get().load(link_image).error(R.drawable.vlylogo).into(detail_photo);

        btn_full_story.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                startActivity(browserIntent);
            }
        });
        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareText();
            }
        });

    }

    private void shareText() {
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        // Add data to the intent, the receiving app will decide
        // what to do with it.
        share.putExtra(Intent.EXTRA_SUBJECT, title);
        share.putExtra(Intent.EXTRA_TEXT, title +"\n\n"+desc + "\n"+link);

        startActivity(Intent.createChooser(share, "Share news!"));
    }




}

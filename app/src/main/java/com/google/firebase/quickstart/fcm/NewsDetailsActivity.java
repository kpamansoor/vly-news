package com.google.firebase.quickstart.fcm;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
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
    private AdView mAdView,mAdView2;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private boolean loadAds = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_details);

//        showAds();

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        fetchConfig();

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

    private void showAds() {
        mAdView = findViewById(R.id.adView);
        mAdView2 = findViewById(R.id.adView2);
        if(loadAds) {
            mAdView.setVisibility(View.VISIBLE);
            mAdView2.setVisibility(View.GONE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
        else{
            mAdView2.setVisibility(View.VISIBLE);
            mAdView.setVisibility(View.GONE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView2.loadAd(adRequest);
        }
//        ca-app-pub-1243068719441957/2429189234
    }

    private void fetchConfig() {
        loadAds = mFirebaseRemoteConfig.getBoolean("load_news_details_ads");

        long cacheExpiration = 3600; // 1 hour in seconds.
        // If your app is using developer mode, cacheExpiration is set to 0, so each fetch will
        // retrieve values from the service.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        // [START fetch_config_with_callback]
        // cacheExpirationSeconds is set to cacheExpiration here, indicating the next fetch request
        // will use fetch data from the Remote Config service, rather than cached parameter values,
        // if cached parameter values are more than cacheExpiration seconds old.
        // See Best Practices in the README for more information.
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                           mFirebaseRemoteConfig.activateFetched();
                           loadAds = mFirebaseRemoteConfig.getBoolean("load_news_details_ads");
                           showAds();
                        }

                    }
                });
        // [END fetch_config_with_callback]
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

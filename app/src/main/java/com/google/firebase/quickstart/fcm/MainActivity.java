package com.google.firebase.quickstart.fcm;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.glide.slider.library.Animations.DescriptionAnimation;
import com.glide.slider.library.SliderLayout;
import com.glide.slider.library.SliderTypes.BaseSliderView;
import com.glide.slider.library.SliderTypes.TextSliderView;
import com.glide.slider.library.Tricks.ViewPagerEx;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements BaseSliderView.OnSliderClickListener,
        ViewPagerEx.OnPageChangeListener{

    private static final String TAG = "MainActivity";
    private static String top5_news = "";
    private RecyclerView rv;
    private List<RssFeedModel> mFeedModelList;
    private List<RssFeedModel2> list2;
    private SwipeRefreshLayout mSwipeLayout;
    private RssFeedListAdapter recyclerAdapter;
    private TextView tv_news_line;
    private LinearLayout ll_top5;
    private SliderLayout mDemoSlider;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    private AdView mAdView,mAdView2;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private boolean loadAds = false;
    LinearLayout splash, content;
    ImageView info,share;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        editor = preferences.edit();

        rv = (RecyclerView)findViewById(R.id.rv);
        ll_top5 = (LinearLayout) findViewById(R.id.ll_top5);
        tv_news_line = (TextView) findViewById(R.id.tv_news_line);
        mDemoSlider = findViewById(R.id.slider);
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new FetchFeedTask().execute((Void) null);
            }
        });


        // Load all the news
        loadNews();

//        String token = FirebaseInstanceId.getInstance().getToken();
        FirebaseApp.initializeApp(this);
        FirebaseMessaging.getInstance().subscribeToTopic("news");
        // Get token
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        fetchConfig();
        splash = findViewById(R.id.splash);
        content = findViewById(R.id.content);
        info = findViewById(R.id.info);
        share = findViewById(R.id.share);

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,AboutActivity.class));
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Stay updated with news from valanchery!!!\nInstall valanchery news app from:\nhttps://play.google.com/store/apps/details?id=com.mansoor.vly.in");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }

        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }
        // [END handle_data_extras]


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


    private void loadNews() {

        if(netCheckin()) {
            new FetchFeedTask().execute();
            new FetchFeedDetails().execute();

            // Show top5 from history
            tv_news_line.setText(preferences.getString("top5", ""));
            ll_top5.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.slide_in_left));
        }else{
            tv_news_line.setText("No internet connection!");
            Snackbar snackbar = Snackbar
                    .make(mDemoSlider, "No internet connection!", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            loadNews();
                        }
                    });

            snackbar.setActionTextColor(Color.RED);

            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.YELLOW);
            snackbar.show();
        }

    }

    @Override
    public void onSliderClick(BaseSliderView baseSliderView) {
        int i = Integer.parseInt(baseSliderView.getBundle().get("extra").toString());
        Intent details_page = new Intent(MainActivity.this,NewsDetailsActivity.class);
        details_page.putExtra("title", mFeedModelList.get(i).title);
        details_page.putExtra("desc", mFeedModelList.get(i).description);
        details_page.putExtra("date", mFeedModelList.get(i).pubDate);
        details_page.putExtra("link", mFeedModelList.get(i).link);
        details_page.putExtra("link_image", list2.get(i).getLink());
        startActivity(details_page);
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {

    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    protected void onStop() {
//        mDemoSlider.stopAutoCycle();
        super.onStop();
    }


    private boolean netCheckin() {
        try {
            ConnectivityManager nInfo = (ConnectivityManager) getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            nInfo.getActiveNetworkInfo().isConnectedOrConnecting();
            ConnectivityManager cm = (ConnectivityManager) getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private class FetchFeedTask extends AsyncTask<Void, Void, Boolean> {

        private String urlLink;

        @Override
        protected void onPreExecute() {
            mSwipeLayout.setRefreshing(true);
            urlLink = "https://www.valanchery.in/feed/";
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
//            if (TextUtils.isEmpty(urlLink))
//                return false;

            try {
                if(!urlLink.startsWith("http://") && !urlLink.startsWith("https://"))
                    urlLink = "http://" + urlLink;

                URL url = new URL(urlLink);
                InputStream inputStream = url.openConnection().getInputStream();
                mFeedModelList = parseFeed(inputStream);
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Error", e);
            } catch (XmlPullParserException e) {
                Log.e(TAG, "Error", e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mSwipeLayout.setRefreshing(false);

            if (success) {
                recyclerAdapter = new RssFeedListAdapter(mFeedModelList,rv);
                rv.setAdapter(recyclerAdapter);
                for (int i = 0; i < 5; i ++){
                    top5_news += mFeedModelList.get(i).title;
                    if(i != 4)
                        top5_news +="  | ";
                }
                tv_news_line.setText(top5_news);
//                ll_top5.setVisibility(View.VISIBLE);
                ll_top5.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.slide_in_left));
                editor.putString("top5", top5_news);
                editor.commit();

                splash.setVisibility(View.GONE);
                content.setVisibility(View.VISIBLE);

            } else {
                Toast.makeText(MainActivity.this,
                        "Couln't load latest news",
                        Toast.LENGTH_LONG).show();
            }
        }
    }


    public List<RssFeedModel> parseFeed(InputStream inputStream) throws XmlPullParserException,
            IOException {
        String title = null;
        String link = null;
        String description = null;
        String pubDate = null;
        boolean isItem = false;
        List<RssFeedModel> items = new ArrayList<>();

        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlPullParser.setInput(inputStream, null);

            xmlPullParser.nextTag();
            while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                int eventType = xmlPullParser.getEventType();

                String name = xmlPullParser.getName();
                if(name == null)
                    continue;

                if(eventType == XmlPullParser.END_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = false;
                    }
                    continue;
                }

                if (eventType == XmlPullParser.START_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = true;
                        continue;
                    }
                }

                Log.d("MyXmlParser", "Parsing name ==> " + name);
                String result = "";
                if (xmlPullParser.next() == XmlPullParser.TEXT) {
                    result = xmlPullParser.getText();
                    xmlPullParser.nextTag();
                }

                if (name.equalsIgnoreCase("title")) {
                    title = result;
                } else if (name.equalsIgnoreCase("link")) {
                    link = result;
                } else if (name.equalsIgnoreCase("description")) {
                    if(!result.equalsIgnoreCase("News around Valanchery, Photos, Blogs, Yellow Pages, Events, Shopping, Reviews and more"))
                        description = result;
                } else if (name.equalsIgnoreCase("pubDate")) {
                    pubDate = result;
                }

                if (title != null && link != null && description != null && pubDate != null) {
                    if(isItem) {
                        RssFeedModel item = new RssFeedModel(title, link, description,pubDate);
                        items.add(item);
                    }
                    else {
                    }

                    title = null;
                    link = null;
                    description = null;
                    pubDate = null;
                    isItem = false;
                }
            }
            return items;
        } finally {
            inputStream.close();
        }
    }

    private class FetchFeedDetails extends AsyncTask<Void, Void, Boolean> {

        private String urlLink;

        @Override
        protected void onPreExecute() {
            urlLink = "https://www.valanchery.in/feed/instant-articles";
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
//            if (TextUtils.isEmpty(urlLink))
//                return false;

            try {
                if(!urlLink.startsWith("http://") && !urlLink.startsWith("https://"))
                    urlLink = "http://" + urlLink;

                URL url = new URL(urlLink);
                InputStream inputStream = url.openConnection().getInputStream();
                list2 = parseFeed2(inputStream);
                String link_string = "";
                String news_link_string = "";
                for(int i = 0 ; i < list2.size(); i++){
                    link_string += list2.get(i).getLink()+",";
                    news_link_string += list2.get(i).getNewsLink()+",";
                }

                editor.putString("links", link_string);
                editor.putString("news_link", news_link_string);

                editor.commit();

                return true;
            } catch (IOException e) {
                Log.e(TAG, "Error", e);
            } catch (XmlPullParserException e) {
                Log.e(TAG, "Error", e);
            }
            return false;
        }



        @Override
        protected void onPostExecute(Boolean success) {

            if(list2 != null && list2.size() > 0)
                initiateImageFlipper();
        }
    }

    private void initiateImageFlipper() {


        RequestOptions requestOptions = new RequestOptions();
        requestOptions
                .centerCrop();
        //.diskCacheStrategy(DiskCacheStrategy.NONE)
        //.placeholder(R.drawable.placeholder)
        //.error(R.drawable.placeholder);

        for (int i = 0; i < 5; i++) {
            TextSliderView sliderView = new TextSliderView(this);
            // if you want show image only / without description text use DefaultSliderView instead

            // initialize SliderLayout
            sliderView
                    .image(list2.get(i).getLink())
                    .description(mFeedModelList.get(i).title)
                    .setRequestOption(requestOptions)
                    .setBackgroundColor(Color.WHITE)
                    .setProgressBarVisible(true)
                    .setOnSliderClickListener(this);

            //add your extra information
            sliderView.bundle(new Bundle());
            sliderView.getBundle().putString("extra", i+"");
            mDemoSlider.addSlider(sliderView);
        }

        // set Slider Transition Animation
        // mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Default);
        mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);

        mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mDemoSlider.setCustomAnimation(new DescriptionAnimation());
        mDemoSlider.setDuration(4000);
        mDemoSlider.addOnPageChangeListener(this);

    }




    public List<RssFeedModel2> parseFeed2(InputStream inputStream) throws XmlPullParserException,
            IOException {
        String title = null;
        String link = null;
        String news_link = null;
        String description = null;
        String content = null;
        boolean isItem = false;
        List<RssFeedModel2> items = new ArrayList<>();
        RssFeedModel2 item;

        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlPullParser.setInput(inputStream, null);

            xmlPullParser.nextTag();
            while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                int eventType = xmlPullParser.getEventType();

                String name = xmlPullParser.getName();
                if(name == null)
                    continue;

                if(eventType == XmlPullParser.END_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = false;
                    }
                    continue;
                }

                if (eventType == XmlPullParser.START_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = true;
                        continue;
                    }
                }

                Log.d("MyXmlParser", "Parsing name ==> " + name);
                String result = "";
                if (xmlPullParser.next() == XmlPullParser.TEXT) {
                    result = xmlPullParser.getText();
                    xmlPullParser.nextTag();
                }

                if (name.equalsIgnoreCase("title")) {
                    title = result;
                }  else if (name.equalsIgnoreCase("link")) {
                    link = result;
                }  else if (name.equalsIgnoreCase("description")) {
                    if(!result.equalsIgnoreCase("News around Valanchery, Photos, Blogs, Yellow Pages, Events, Shopping, Reviews and more"))
                        description = result;
                } else if (name.equalsIgnoreCase("content:encoded")) {
                    content = result;
//                    result.split("<img src=\"")[1].split("\"/>")[0]
                }

                if (title != null && link != null&& content != null && description != null ) {
                    if(isItem) {
                        try {
//                            news_link = content.split("<figure data-feedback=\"fb:likes,fb:comments\"><img src=\"")[1].split("\"/>")[0];
                            news_link = content.split("<img src=\"")[1].split("\"/>")[0];
                            item = new RssFeedModel2(news_link,link);
                            items.add(item);
                        }catch (IndexOutOfBoundsException e){
                            item = new RssFeedModel2(null,link);
                            items.add(item);
                        }


                    }
                    else {
                    }

                    title = null;
                    link = null;
                    content = null;
                    description = null;
                    isItem = false;
                }
            }
            return items;
        } finally {
            inputStream.close();
        }
    }



}

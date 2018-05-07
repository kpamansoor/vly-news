package com.google.firebase.quickstart.fcm;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RecyclerView rv;
    private List<RssFeedModel> mFeedModelList;
    private SwipeRefreshLayout mSwipeLayout;
    private RssFeedListAdapter recyclerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rv = (RecyclerView)findViewById(R.id.rv);
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);

        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new FetchFeedTask().execute((Void) null);
            }
        });
        new FetchFeedTask().execute();

//        recyclerAdapter.setClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int pos = rv.indexOfChild(v);
//                Toast.makeText(MainActivity.this, " pos"+pos, Toast.LENGTH_SHORT).show();
//            }
//        });
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

        Button subscribeButton = findViewById(R.id.subscribeButton);
        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // [START subscribe_topics]
                FirebaseMessaging.getInstance().subscribeToTopic("news");
                // [END subscribe_topics]

                // Log and toast
                String msg = getString(R.string.msg_subscribed);
                Log.d(TAG, msg);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        Button logTokenButton = findViewById(R.id.logTokenButton);
        logTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get token
                String token = FirebaseInstanceId.getInstance().getToken();

                // Log and toast
                String msg = getString(R.string.msg_token_fmt, token);
                Log.d(TAG, msg);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
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
            } else {
                Toast.makeText(MainActivity.this,
                        "Enter a valid Rss feed url",
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
}

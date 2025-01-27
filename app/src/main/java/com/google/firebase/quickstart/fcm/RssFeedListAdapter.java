package com.google.firebase.quickstart.fcm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.util.List;

/**
 * Created by L4208412 on 27/4/2018.
 */

public class RssFeedListAdapter extends RecyclerView.Adapter<RssFeedListAdapter.FeedModelViewHolder> {

    private final RecyclerView recyclerView;
    private List<RssFeedModel> mRssFeedModels;
    private View.OnClickListener mClickListener;
    private int lastPosition = -1;
    String[] linkArray, newsLinkArray;
    public static class FeedModelViewHolder extends RecyclerView.ViewHolder {
        private View rssFeedView;

        public FeedModelViewHolder(View v) {
            super(v);
            rssFeedView = v;
        }
    }

    public RssFeedListAdapter(List<RssFeedModel> rssFeedModels,RecyclerView recyclerView) {
        mRssFeedModels = rssFeedModels;
        this.recyclerView = recyclerView;
    }

    @Override
    public FeedModelViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);
        final FeedModelViewHolder holder = new FeedModelViewHolder(v);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                int pos = recyclerView.indexOfChild(view);
                int pos = holder.getAdapterPosition();
                Intent details_page = new Intent(view.getContext(),NewsDetailsActivity.class);
                details_page.putExtra("title", mRssFeedModels.get(pos).title);
                details_page.putExtra("desc", mRssFeedModels.get(pos).description);
                details_page.putExtra("date", mRssFeedModels.get(pos).pubDate);
                details_page.putExtra("link", mRssFeedModels.get(pos).link);

                try {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(view.getContext());
                    String links = preferences.getString("links", "");
                    String news_link = preferences.getString("news_link", "");

                    if (links.length() > 0 && news_link.length() > 0) {
                        linkArray = links.split(",");
                        newsLinkArray = news_link.split(",");
                        details_page.putExtra("link_image", getImageLink(mRssFeedModels.get(pos).link));
                    }
                }catch (Exception e){}
                view.getContext().startActivity(details_page);

            }

        });
        return holder;
    }

    private String getImageLink(String news_link) {
        for(int i = 0 ; i < linkArray.length; i++){
            if(newsLinkArray[i].equals(news_link)) {
                return linkArray[i];
            }
        }

        return null;
    }


    public void setClickListener(View.OnClickListener callback) {
        mClickListener = callback;
    }

    @Override
    public void onBindViewHolder(FeedModelViewHolder holder, int position) {
        final RssFeedModel rssFeedModel = mRssFeedModels.get(position);
        ((TextView)holder.rssFeedView.findViewById(R.id.title)).setText(rssFeedModel.title);
        ((TextView)holder.rssFeedView.findViewById(R.id.date))
                .setText(rssFeedModel.pubDate.substring(0, rssFeedModel.pubDate.length() - 5));
        setAnimation(holder.itemView, position);
    }
    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(viewToAnimate.getContext(), android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return mRssFeedModels.size();
    }



}

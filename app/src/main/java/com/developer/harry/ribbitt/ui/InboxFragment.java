package com.developer.harry.ribbitt.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.developer.harry.ribbitt.adapters.MessageAdapter;
import com.developer.harry.ribbitt.utils.ParseConstant;
import com.developer.harry.ribbitt.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by harry on 1/4/16.
 */
public class InboxFragment extends ListFragment {

    protected List<ParseObject> mMessages;
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_inbox,
                container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
/*        mSwipeRefreshLayout.setColorScheme(getResources().getColor(R.color.swipe_refresh1),
                getResources().getColor(R.color.swipe_refresh2),
                getResources().getColor(R.color.swipe_refresh3),
                getResources().getColor(R.color.swipe_refresh4));*/
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setProgressBarIndeterminateVisibility(true);
        retrieveMessages();
    }

    private void retrieveMessages() {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstant.CLASS_MESSAGE);
        query.whereEqualTo(ParseConstant.KEY_RECIPIENT_IDS, ParseUser.getCurrentUser().getObjectId());
        query.addDescendingOrder(ParseConstant.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                getActivity().setProgressBarIndeterminateVisibility(false);
                if(mSwipeRefreshLayout.isRefreshing()){
                    mSwipeRefreshLayout.setRefreshing(false);
                }

                if(e==null){
                    //No found message
                    mMessages = list;
                    String[] usernames = new String[mMessages.size()];
                    int i = 0;
                    for (ParseObject message :mMessages) {
                        usernames[i] = message.getString(ParseConstant.KEY_SENDER_NAME);
                        i++;
                    }
                    if(getListView().getAdapter()==null) {
                        MessageAdapter adapter = new MessageAdapter(getListView().getContext(),
                                mMessages);
                        setListAdapter(adapter);
                    } else{
                        //refill the adapter
                        ((MessageAdapter)getListView().getAdapter()).refill(mMessages);
                    }
                }
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        ParseObject message = mMessages.get(position);
        String messageType = message.getString(ParseConstant.KEY_FILE_TYPE);
        ParseFile file = message.getParseFile(ParseConstant.KEY_FILE);
        Uri fileUri  = Uri.parse(file.getUrl());

        if(messageType.equals(ParseConstant.TYPE_IMAGE)){
            Intent intent = new Intent(getActivity(),ViewimageActivity.class);
            intent.setData(fileUri);
            startActivity(intent);
        }else{
            Intent intent = new Intent(Intent.ACTION_VIEW,fileUri);
            intent.setDataAndType(fileUri,"video/*");
            startActivity(intent);
        }

        //Delete it
        List<String> ids =message.getList(ParseConstant.KEY_RECIPIENT_IDS);
        if(ids.size()==1){
            //last recipients
            message.deleteInBackground();
        }else{
            ids.remove(ParseUser.getCurrentUser().getObjectId());
            ArrayList<String> idsToRemove = new ArrayList<String>();
            idsToRemove.add(ParseUser.getCurrentUser().getObjectId());

            message.removeAll(ParseConstant.KEY_RECIPIENT_IDS,idsToRemove);
            message.saveInBackground();
            
        }

    }

    protected SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            retrieveMessages();

        }
    };
}

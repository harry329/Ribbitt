package com.developer.harry.ribbitt.ui;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.developer.harry.ribbitt.utils.ParseConstant;
import com.developer.harry.ribbitt.R;
import com.developer.harry.ribbitt.RibbittApplication;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class EditFriendsActivity extends ListActivity {

    private static final String TAG = EditFriendsActivity.class.getSimpleName();

    protected List<ParseUser> mUsers = new ArrayList<ParseUser>();
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseUser mCurrentUser;
    protected HashMap<String, String> mUsersAndPhoneNos = new HashMap<>();
    SharedPreferences mSharedPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_friends);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mSharedPreference=getSharedPreferences(ParseConstant.SHARED_PREERENCE_NAME, Context.MODE_APPEND);
        mUsersAndPhoneNos = RibbittApplication.mReadContacts;
    }


    @Override
    protected void onResume() {
        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mFriendsRelation = mCurrentUser.getRelation(ParseConstant.KEY_FRIENDS_RELATION);
        setProgressBarIndeterminateVisibility(true);
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.orderByAscending(ParseConstant.KEY_USERNAME);
        query.setLimit(1000);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> userList, ParseException e) {
                setProgressBarIndeterminateVisibility(false);
                if (e == null) {
                    //Success
                    SharedPreferences.Editor editor = mSharedPreference.edit();
                    ArrayList<String> usernames = new ArrayList<String>();
                    String[] phonenos = new String[userList.size()];
                    Set<String> keys = mUsersAndPhoneNos.keySet();
                    int i = 0;
                    for (ParseUser user : userList) {
                        phonenos[i] = (String) user.get(ParseConstant.KEY_PHONE_NO);
                        System.out.println("phoneno got from parser is" + phonenos[i]);
                        if (phonenos[i] != null) {
                            for (String key : keys) {
                                if (key.trim().contains(phonenos[i].trim())) {
                                    usernames.add(mUsersAndPhoneNos.get(key));
                                    editor.putString(user.getUsername(), mUsersAndPhoneNos.get(key));
                                    editor.commit();
                                    mUsers.add(user);
                                }
                            }
                            i++;
                        }
                    }
                    String[] usernameArray = new String[usernames.size()];
                    usernames.toArray(usernameArray);
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(EditFriendsActivity.this,
                            android.R.layout.simple_list_item_checked, (String[]) usernameArray);
                    setListAdapter(adapter);

                   addFriendCheckMarks();
                } else {
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditFriendsActivity.this);
                    builder.setMessage(e.getMessage())
                            .setTitle(R.string.error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    private void addFriendCheckMarks() {
        mFriendsRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {
                if (e == null) {
                    for (int i = 0; i< mUsers.size(); i++) {
                        System.out.println("Size of list is " + mUsers.size());
                        ParseUser user = mUsers.get(i);

                        for (ParseUser friend : list) {
                            if (friend.getObjectId().equals(user.getObjectId())) {
                                getListView().setItemChecked(i, true);
                            }
                        }
                    }
                } else {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (getListView().isItemChecked(position)) {
            mFriendsRelation.add(mUsers.get(position));
        } else {
            //
            mFriendsRelation.remove(mUsers.get(position));

        }
        mCurrentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });

    }

}

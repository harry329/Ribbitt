package com.developer.harry.ribbitt;

import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import com.developer.harry.ribbitt.utils.ParseConstant;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import java.util.HashMap;

/**
 * Created by harry on 12/28/15.
 */
public class RibbittApplication extends Application {

    public static HashMap<String,String> mReadContacts= new HashMap<String,String>();
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "7QjhVSkgC3vxg1tLfqYtG80fULya7J5WFQL5TcBM", "IJcJQX3P0DO6A32xotGCyrtDqDg3oUq2rGtm6u3P");
        ReadFromContacts readFromContacts = new ReadFromContacts();
        ParseInstallation.getCurrentInstallation().saveInBackground();
        readFromContacts.execute();
    }

    public static void updateParseInstallations(ParseUser parseUser){
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put(ParseConstant.KEY_USER_ID,parseUser.getObjectId());
        installation.saveInBackground();
    }

    private class ReadFromContacts extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ContentResolver cr = getContentResolver();
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    null, null, null, null);

            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        System.out.println("name : " + name + ", ID : " + id);

                        // get the phone number
                        Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id}, null);
                        while (pCur.moveToNext()) {
                            String phone = pCur.getString(
                                    pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            System.out.println("phone" + phone);
                            mReadContacts.put(phone, name);
                        }
                        pCur.close();


                    }
                }
            }
            cur.close();
            return null;
        }
    }

}

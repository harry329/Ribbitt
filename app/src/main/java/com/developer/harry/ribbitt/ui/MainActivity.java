package com.developer.harry.ribbitt.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.developer.harry.ribbitt.utils.ParseConstant;
import com.developer.harry.ribbitt.R;
import com.developer.harry.ribbitt.adapters.SectionsPagerAdapter;
import com.parse.ParseUser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int TAKE_PHOTO_REQUEST=0;
    public static final int TAKE_VIDEO_REQUEST=1;
    public static final int CHOOSE_PHOTO_REQUEST=2;
    public static final int CHOOSE_VIDEO_REQUEST=3;

    public static final int MEDIA_TYPE_IMAGE=4;
    public static final int MEDIA_TYPE_VIDEO=5;
    public static final int FILE_SIZE_LIMIT=1024*1024*10; //10MB

    protected Uri mMediaUri;


    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    protected DialogInterface.OnClickListener mDialogListener =
            new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case 0://Take pic
                    Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                    if(mMediaUri == null){
                        Toast.makeText(MainActivity.this, R.string.error_external_storage ,
                                Toast.LENGTH_LONG).show();
                    }else {
                        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                        startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
                    }
                    break;
                case 1: //Take video
                    Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    mMediaUri= getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
                    if(mMediaUri == null){
                        Toast.makeText(MainActivity.this,R.string.error_external_storage ,
                                Toast.LENGTH_LONG).show();
                    }else {
                        videoIntent.putExtra(MediaStore.EXTRA_OUTPUT,mMediaUri);
                        videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,10);
                        videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,0);
                        startActivityForResult(videoIntent,TAKE_VIDEO_REQUEST);
                    }
                    break;
                case 2: // Choose pic
                    Intent choosePhotoIntent =new Intent(Intent.ACTION_GET_CONTENT);
                    choosePhotoIntent.setType("image/*");
                    startActivityForResult(choosePhotoIntent,CHOOSE_PHOTO_REQUEST);
                    break;
                case 3: //Choose video
                    Intent chooseVideoIntent =new Intent(Intent.ACTION_GET_CONTENT);
                    chooseVideoIntent.setType("video/*");
                    Toast.makeText(MainActivity.this, R.string.video_file_size_warning, Toast.LENGTH_LONG).show();
                    startActivityForResult(chooseVideoIntent, CHOOSE_VIDEO_REQUEST);
                    break;
            }
        }
    };

    private Uri getOutputMediaFileUri(int mediaType) {
        if(isExternalStorageAvailable()){
            String appName = MainActivity.this.getString(R.string.app_name);
            File mediaStorageDir = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    appName);
            if(!mediaStorageDir.exists()){
                if(!mediaStorageDir.mkdir()){
                    Log.e(TAG,"Failed to created directory");
                    return null;
                }
            }
            File mediaFile;
            Date date = new Date();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(date);
            String path = mediaStorageDir.getPath()+File.separator;

            if(mediaType==MEDIA_TYPE_IMAGE){
                mediaFile= new File(path +"IMG_"+timeStamp+".jpg");
            }
            else if (mediaType ==MEDIA_TYPE_VIDEO){
                mediaFile = new File(path +"VID_" + timeStamp +".mp4");
            }else{
                return null;
            }
            Log.d(TAG,"Path of file "+Uri.fromFile(mediaFile));
            return Uri.fromFile(mediaFile);
        }
        return null;
    }

    private boolean isExternalStorageAvailable(){
        String state = Environment.getExternalStorageState();
        if(state.equals(Environment.MEDIA_MOUNTED)){
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null){
            navigateToLogin();
        } else {
            Log.i(TAG, currentUser.getUsername());
        }
        mSectionsPagerAdapter = new SectionsPagerAdapter(this,
                getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // For each of the sections in the app, add a tab to the action bar.

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    private void navigateToLogin() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==CHOOSE_PHOTO_REQUEST||requestCode==CHOOSE_VIDEO_REQUEST){
                if(data==null){
                    Toast.makeText(this,R.string.general_error,Toast.LENGTH_LONG).show();
                }else{
                    mMediaUri=data.getData();
                }
                Log.i(TAG,"Media URI is "+mMediaUri);
                if(requestCode==CHOOSE_VIDEO_REQUEST){
                    int fileSize = 0;
                    InputStream inputStream = null;

                    try {
                        inputStream= getContentResolver().openInputStream(mMediaUri);
                        fileSize = inputStream.available();
                    } catch (FileNotFoundException e) {
                        Toast.makeText(this,R.string.error_opening_file,Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                        return;
                    } catch(IOException e){
                        Toast.makeText(this,R.string.error_opening_file,Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                        return;
                    }
                    finally{
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(fileSize>=FILE_SIZE_LIMIT){
                        Toast.makeText(this,R.string.error_file_size_too_large,Toast.LENGTH_LONG).show();
                        return;
                    }
                }

            }else {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(mMediaUri);
                sendBroadcast(mediaScanIntent);
            }
            Intent recipientsIntent = new Intent(this,RecipientsActivity.class);
            recipientsIntent.setData(mMediaUri);
            String fileType;
            if(requestCode == TAKE_PHOTO_REQUEST||requestCode==CHOOSE_PHOTO_REQUEST){
                fileType= ParseConstant.TYPE_IMAGE;
            }else{
                fileType=ParseConstant.TYPE_VIDEO;
            }
            recipientsIntent.putExtra(ParseConstant.KEY_FILE_TYPE,fileType);
            startActivity(recipientsIntent);

        }else if(resultCode!=RESULT_CANCELED){
            Toast.makeText(this,R.string.general_error,Toast.LENGTH_LONG).show();
        }

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id){
            case R.id.logout:
                ParseUser.logOut();
                navigateToLogin();
                return true;
            case R.id.inbox:
                mViewPager.setCurrentItem(0);
                return true;
            case R.id.friends:
                mViewPager.setCurrentItem(1);
                return true;
            case R.id.action_edit_friends:
                Intent intent = new Intent(this,EditFriendsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_camera:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setItems(R.array.camera_choices, mDialogListener);
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

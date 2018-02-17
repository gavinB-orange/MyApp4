package com.example.brebner.myapp4;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static String GOOGLE_LOCATION = "geo:37.422219,-122.08364?z=14";
    public static String ZOOM = "?z=";
    public static int GETPIN = 1;
    public static int GETPERSON = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Verify that we can can open web pages
        ArrayList<Intent> example_intents = new ArrayList<>();
        Intent dummy = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
        example_intents.add(dummy);
        dummy = new Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.GOOGLE_LOCATION));
        example_intents.add(dummy);
        PackageManager packageManager = getPackageManager();
        // for (int i = 0; i < example_intents.size(); i++) {
        for (Intent i : example_intents) {
            List activities = packageManager.queryIntentActivities(i,
                    PackageManager.MATCH_DEFAULT_ONLY);
            boolean isIntentSafe = activities.size() > 0;
            if (isIntentSafe) {
                Log.w("MyApp4", "Have dependent app");
            } else {
                Log.w("MyApp4", "Missing dependent app");
                System.exit(1);
            }
        }
        // grab the number from the SP and use this to fill the value shown
        SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
        String oldnum = sp.getString(getString(R.string.sp_contact_number), "");
        String oldname = sp.getString(getString(R.string.sp_display_name), "");
        TextView shownumtv = (TextView) findViewById(R.id.textViewContactNumber);
        shownumtv.setText(oldnum);
        TextView shownametv = (TextView) findViewById(R.id.textViewDisplayName);
        shownametv.setText(oldname);
    }

    public void onOpenUrl(View view) {
        EditText et = (EditText) findViewById(R.id.editTextUrl);
        String urlstr = et.getText().toString();
        Uri page2open = Uri.parse(urlstr);
        Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, page2open);
        startActivity(openUrlIntent);
    }

    public void onShowOnMap(View view) {
        EditText longet = (EditText) findViewById(R.id.editTextLong);
        EditText latet = (EditText) findViewById(R.id.editTextLat);
        EditText zoomet = (EditText) findViewById(R.id.editTextZoom);
        String longstr = longet.getText().toString();
        String latstr = latet.getText().toString();
        String zoomstr = zoomet.getText().toString();
        String locstr = "geo:" + longstr + ", " + latstr + MainActivity.ZOOM + zoomstr;
        Log.w("MyApp4","My location = " + locstr);
        Uri location = Uri.parse(locstr);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, location);
        startActivityForResult(mapIntent, MainActivity.GETPIN);
    }

    public void onGetPerson(View view) {
        Intent getPersonIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
        getPersonIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(getPersonIntent, MainActivity.GETPERSON);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == MainActivity.GETPIN) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Do something with the contact here (bigger example below)
                Uri datauri = data.getData();
                String data_as_string = datauri.toString();
                Log.w("MyApp4", data_as_string);
            }
        }
        else {
            if (requestCode == MainActivity.GETPERSON) {
                if (resultCode == RESULT_OK) {
                    Uri contactUri = data.getData();
                    String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};
                    String[] nameprojection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};

                    // Perform the query on the contact to get the NUMBER column
                    // We don't need a selection or sort order (there's only one result for the given URI)
                    // CAUTION: The query() method should be called from a separate thread to avoid blocking
                    // your app's UI thread. (For simplicity of the sample, this code doesn't do that.)
                    // Consider using CursorLoader to perform the query.
                    Cursor cursor = getContentResolver()
                            .query(contactUri, projection, null, null, null);
                    cursor.moveToFirst();

                    Cursor namecursor = getContentResolver().query(contactUri, nameprojection, null, null, null);
                    namecursor.moveToFirst();

                    // Retrieve the phone number from the NUMBER column
                    int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    int namecol = namecursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    String number = cursor.getString(column);
                    String name;
                    try {
                        name = namecursor.getString(namecol);
                    }
                    catch (Exception e) {
                        name = "unknown " + e.toString();
                    }
                    SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
                    String oldnum = sp.getString(getString(R.string.sp_contact_number), "");
                    String oldname = sp.getString(getString(R.string.sp_display_name), "");
                    if (! oldnum.equals(number)) {
                        Log.w("MyApp4", "Numbers do not match");
                        Log.w("MyApp4", "  oldnum = " + oldnum);
                        Log.w("MyApp4", "  newnum = " + number);
                    }
                    else {
                        Log.w("MyApp4", "Numbers are the same");
                    }
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString(getString(R.string.sp_contact_number), number);
                    editor.putString(getString(R.string.sp_display_name), name);
                    editor.commit();
                    Log.w("MyApp4", "Number is " + number);
                    TextView shownumtv = (TextView) findViewById(R.id.textViewContactNumber);
                    shownumtv.setText(number);
                    TextView shownametv = (TextView) findViewById(R.id.textViewDisplayName);
                    shownametv.setText(name);

                }
            }
        }
    }
}

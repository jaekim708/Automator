package com.jamjar.automator;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity  {
    private Button mToggle;
    private Boolean mOn = false;
    private static TextView mOnOff;
    static final int REQUEST_PERMISSION_READ_CALENDAR = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mOnOff = (TextView) findViewById(R.id.onOff);
        mToggle = (Button) findViewById(R.id.toggle);
        mOnOff.setText(R.string.off);
        CalAccess.setup(getApplicationContext());

        mToggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mOn) {
                    mOn = false;
                    mOnOff.setText(R.string.off);
                    CalAccess.cancelAlarms();
                } else {
                    mOn = true;
                    mOnOff.setText("Currently on" + " \nNo future events");
                    getCalendarPermissions();
                    CalAccess.update(getApplicationContext());
                }
            }
        });
    }

    private void getCalendarPermissions(){
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_CALENDAR)) {
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_CALENDAR},
                        REQUEST_PERMISSION_READ_CALENDAR);
            }
        }
    }

    public static TextView getText(){
        return mOnOff;
    }

}

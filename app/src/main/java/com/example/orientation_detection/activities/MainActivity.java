package com.example.orientation_detection.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.example.orientation_detection.R;
import com.example.orientation_detection.Services.DataIntentService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    public static final int DISPLAY_ORIENTATION = 1;
    public static String TAG = "MainActivity";

    NavigationView navigationView;
    private ViewPager mViewPager;
    private FragmentPagerAdapter mAdapter;
    private List<Fragment> mFragments;

    private DataIntentService.DataBinder dataBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
        //excuted when the Service is binded to the activity
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            dataBinder = (DataIntentService.DataBinder) service;
        }
    };

    //the loop body in the subthread
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == DISPLAY_ORIENTATION){
                dataBinder.getOrientationData().displaySensor(MainActivity.this);
                dataBinder.getLightData().displaySensor(MainActivity.this);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //let the tool bar transparent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        //hide the floating action bar!
        fab.hide();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        initFragmentViews();//Initialize components
        initFragmentData();//Initialize data

        //start the Service (bind it to activity afterwards in onResume)
        Intent intentService = new Intent(this,DataIntentService.class);
        startService(intentService);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        Intent bindIntent = new Intent(this,DataIntentService.class);   //bind the Service to activity
        bindService(bindIntent,connection,BIND_AUTO_CREATE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(80);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Message message = new Message();
                    message.what = DISPLAY_ORIENTATION;
                    handler.sendMessage(message);
                }
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        unbindService(connection);  //unbind the Service in case ServiceConnection is leaked
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_orientation) {
            mViewPager.setCurrentItem(0);
        } else if (id == R.id.nav_light) {
            mViewPager.setCurrentItem(1);
        } else if (id == R.id.nav_slideshow) {
            mViewPager.setCurrentItem(2);
        } else if (id == R.id.nav_manage) {
            mViewPager.setCurrentItem(3);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initFragmentData() {
        mFragments = new ArrayList<>();
        //Put fragments into the List
        mFragments.add(new OrientationFragment());
        mFragments.add(new LightFragment());
        mFragments.add(new ThirdFragment());
        mFragments.add(new ForthFragment());

        //Initiate the adapter
        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {//从集合中获取对应位置的Fragment
                return mFragments.get(position);
            }
            @Override
            public int getCount() {//获取集合中Fragment的总数
                return mFragments.size();
            }

        };

        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            //When a tap changes
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                navigationView.getMenu().getItem(position).setChecked(true);
//                Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
//                switch (position) {
//                    case 0:
//                        toolbar.setTitle("Orientation");
//                        break;
//                    case 1:
//                        toolbar.setTitle("Light");
//                        break;
//                    case 2:
//                        toolbar.setTitle("Third");
//                        break;
//                    case 3:
//                        toolbar.setTitle("Forth");
//                        break;
//                }
            }
            //When a tab is selected
            @Override
            public void onPageSelected(int position) {
                //resetImgs();
                Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
                switch (position) {
                    case 0:
                        toolbar.setTitle("Orientation");
                        break;
                    case 1:
                        toolbar.setTitle("Light");
                        break;
                    case 2:
                        toolbar.setTitle("Third");
                        break;
                    case 3:
                        toolbar.setTitle("Forth");
                        break;
                }
            }
            @Override
            //When the state of a tap changes
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    //Initialization
    private void initFragmentViews() {
        mViewPager = (ViewPager) findViewById(R.id.id_viewpager);
    }

}

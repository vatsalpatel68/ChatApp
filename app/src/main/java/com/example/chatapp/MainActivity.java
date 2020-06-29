package com.example.chatapp;

import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toolbar;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private ViewPager mViewpager;
    private SectionPagerAdapter mSectionPagerAdapter;

    private TabLayout mTabLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewpager = (ViewPager) findViewById(R.id.tabpager);
        mSectionPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());

        mViewpager.setAdapter(mSectionPagerAdapter);
        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewpager);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        //when No one Login then it should go to the Home page.
        if(user == null)
        {
            startActivity(new Intent(MainActivity.this,HomePage.class));
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.logout)
        {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this,HomePage.class));
        }

        if(item.getItemId() == R.id.profile)
        {
            startActivity(new Intent(MainActivity.this,profileAct.class));
        }

        if(item.getItemId() == R.id.showusers)
        {
            startActivity(new Intent(MainActivity.this,ShowAllUsers.class));
        }
        return super.onOptionsItemSelected(item);
    }
}

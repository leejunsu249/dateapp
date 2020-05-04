package com.tumblr.dateapp.iloveyou.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.tumblr.dateapp.iloveyou.fragments.RegisterFragment;
import com.tumblr.dateapp.iloveyou.fragments.LoginFragment;

public class MainFragmentPagerAdapter extends FragmentPagerAdapter {

    public MainFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        if (position == 0) {
            return LoginFragment.newInstance();
        } else {
            return RegisterFragment.newInstance();
        }

    }

    @Override
    public int getCount() {
        return 2;
    }
}
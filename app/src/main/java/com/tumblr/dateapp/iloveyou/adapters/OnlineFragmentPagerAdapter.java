package com.tumblr.dateapp.iloveyou.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.tumblr.dateapp.iloveyou.fragments.ConversationsFragment;
import com.tumblr.dateapp.iloveyou.fragments.MapFragment;

public class OnlineFragmentPagerAdapter extends FragmentPagerAdapter {

    public OnlineFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:

                return MapFragment.newInstance();
            case 1:
                return ConversationsFragment.newInstance();
            default:
                return MapFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
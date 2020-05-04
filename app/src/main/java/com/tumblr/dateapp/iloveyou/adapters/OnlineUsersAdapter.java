package com.tumblr.dateapp.iloveyou.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tumblr.dateapp.iloveyou.R;
import com.tumblr.dateapp.iloveyou.models.UserProfile;

import java.util.List;

public class OnlineUsersAdapter extends ArrayAdapter<UserProfile> {

    private final int layoutResource;
    private final List<UserProfile> userProfileList;
    private OnAdapterInteractionListener activity;

    public OnlineUsersAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<UserProfile> userProfiles) {
        super(context, resource, userProfiles);

        this.layoutResource = resource;
        this.userProfileList = userProfiles;

        if (context instanceof OnAdapterInteractionListener) {
            activity = (OnAdapterInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAdapterInteractionListener");
        }
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){

        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layoutResource, null);
        }

        final UserProfile user = userProfileList.get(position);

        TextView userName = (TextView) convertView.findViewById(R.id.active_user_name);
       TextView userGender= (TextView) convertView.findViewById(R.id.active_user_gender);

        ImageView userAvatar = (ImageView) convertView.findViewById(R.id.active_user_avatar);

        userName.setText(user.getUserName());
       userGender.setText(user.getGender());
        userAvatar.setImageBitmap(user.getAvatar());

        //convertView.setOnClickListener(v -> activity.mountChatActivity(user.id));

        return convertView;
    }

    public interface OnAdapterInteractionListener {
        
    }
}

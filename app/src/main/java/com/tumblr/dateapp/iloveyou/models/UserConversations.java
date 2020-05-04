package com.tumblr.dateapp.iloveyou.models;


import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

@IgnoreExtraProperties
public class UserConversations {

    private String id;
    private String ownerId;
    private Map<String, Conversation> conversations;


    // required empty constructor for firebase loading
    public UserConversations(){
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Conversation> getConversations() {
        return conversations;
    }

    public void setConversations(Map<String, Conversation> conversations) {
        this.conversations = conversations;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}

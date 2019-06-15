package com.example.androidlastday;

import org.json.JSONObject;

public class Task {

    public static final int CREATED = 0;
    public static final int PENDING = 1;
    public static final int SUCCESSFUL = 2;
    public static final int ERRORED = 3;

    public static final int GET_QUIZ = 5;
    public static final int GET_USERS = 0;
    public static final int POST_USERS = 1;
    public static final int GET_QUESTION = 2;
    public static final int POST_QUESTION = 3;
    public static final int GET_POINTS = 4;

    public int type;
    public int state;
    public JSONObject request;
    public JSONObject response;
    public OnNetworkResult handler;

    public Task(OnNetworkResult handler, int type, JSONObject request) {
        this.handler = handler;
        this.type = type;
        this.state = Task.CREATED;

        this.request = request;
    }
}

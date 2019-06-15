package com.example.androidlastday;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Network {

    public static String baseUrl = null;

    private static final String TAG = "FINALDAY";

    private static void execute(Task task) {
        (new NetworkTask()).execute(task);
    }

    public static void getQuiz(OnNetworkResult handler) {
        JSONObject request = new JSONObject();
        try {
            request.put("method", "GET");
            request.put("url", baseUrl + "/quiz");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Network.execute(new Task(handler, Task.GET_QUIZ, request));
    }

    public static void getUsers(OnNetworkResult handler) {
        JSONObject request = new JSONObject();
        try {
            request.put("method", "GET");
            request.put("url", baseUrl + "/users");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Network.execute(new Task(handler, Task.GET_USERS, request));
    }

    public static void postUsers(OnNetworkResult handler, String name) {
        JSONObject request = new JSONObject();
        try {
            request.put("method", "POST");
            request.put("url", baseUrl + "/users");
            request.put("data", "name=" + name.trim());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Network.execute(new Task(handler, Task.POST_USERS, request));
    }

    public static void getQuestion(OnNetworkResult handler) {
        JSONObject request = new JSONObject();
        try {
            request.put("method", "GET");
            request.put("url", baseUrl + "/question");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Network.execute(new Task(handler, Task.GET_QUESTION, request));
    }

    public static void postQuestion(OnNetworkResult handler, int answer) {
        JSONObject request = new JSONObject();
        try {
            request.put("method", "POST");
            request.put("url", baseUrl + "/question");
            request.put("data", "answer=" + answer);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Network.execute(new Task(handler, Task.POST_QUESTION, request));
    }

    private static class NetworkTask extends AsyncTask<Task, Void, Task> {

        @Override
        protected Task doInBackground(Task... tasks) {
            Task task = tasks[0];
            JSONObject request = task.request;
            task.state = Task.PENDING;

            try {
                Log.d(TAG, "doInBackground: " + request.getString("url"));
                HttpURLConnection connection = (HttpURLConnection) new URL(request.getString("url")).openConnection();
                connection.setRequestMethod(request.getString("method"));
                connection.setConnectTimeout(500);
                connection.setReadTimeout(500);

                if (connection.getRequestMethod().equals("POST")) {
                    OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                    writer.write(request.getString("data"));
                    writer.flush();
                }
                connection.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder builder = new StringBuilder();

                String chunk;
                while ((chunk = reader.readLine()) != null) {
                    builder.append(chunk).append("\n");
                }
                connection.disconnect();
                reader.close();

                task.response = new JSONObject(builder.toString());
                task.state = Task.SUCCESSFUL;

                return task;
            } catch(MalformedURLException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            } catch(JSONException e) {
                e.printStackTrace();
            }
            task.state = Task.ERRORED;
            return task;
        }

        @Override
        protected void onPostExecute(Task task) {
            task.handler.handleResult(task);
        }
    }
}

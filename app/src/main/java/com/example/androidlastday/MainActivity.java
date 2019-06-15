package com.example.androidlastday;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

public class MainActivity extends AppCompatActivity implements OnNetworkResult{

    private static final String TAG = "FINALDAY";

    GetQuiz getQuizThread;

    TextView title;
    EditText host;
    TextView users;
    Button joinButton;
    EditText name;
    boolean hasJoined = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        title = findViewById(R.id.title);
        host = findViewById(R.id.host);
        users = findViewById(R.id.users);
        joinButton = findViewById(R.id.join);
        name = findViewById(R.id.name);

        host.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                Network.baseUrl = host.getText().toString();
            }
        });

        final ObjectAnimator joinButtonFade = ObjectAnimator.ofFloat(joinButton, "alpha", 0);
        joinButtonFade.setDuration(500);
        joinButtonFade.setInterpolator(new LinearInterpolator());
        joinButtonFade.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                joinButton.setVisibility(View.GONE);
                joinButton.setAlpha(1);
            }
        });

        final ObjectAnimator nameFade = ObjectAnimator.ofFloat(name, "alpha",  1);
        nameFade.setDuration(500);
        nameFade.setInterpolator(new LinearInterpolator());

        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hasJoined = true;
                name.setAlpha(0);
                name.setVisibility(View.VISIBLE);
                joinButtonFade.start();
                nameFade.start();
            }
        });

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                Network.postUsers(MainActivity.this, name.getText().toString());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Network.baseUrl = host.getText().toString();
        getQuizThread = new GetQuiz();
        getQuizThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        getQuizThread.done = true;
    }

    @Override
    public void onBackPressed() {
        if (hasJoined) {
            Network.postUsers(this, "");
            hasJoined = false;

            joinButton.setVisibility(View.VISIBLE);
            name.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void handleResult(Task task) {
        switch (task.type) {
            case Task.POST_USERS:
                if (task.state == Task.SUCCESSFUL) {

                } else {

                }
                break;
            case Task.GET_QUIZ:
                if (task.state == Task.SUCCESSFUL) {
                    try {
                        JSONArray usersJSON = task.response.getJSONArray("users");
                        StringBuilder builder = new StringBuilder();
                        for (int i = 0; i < usersJSON.length(); i++) {
                            builder.append(usersJSON.getJSONObject(i).getString("name")).append("\n");
                        }
                        users.setText(builder.toString().trim());

                        if (task.response.getString("state").equals("running")) {
                            Intent intent = new Intent(this, QuestionActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    users.setText("Not connected.");
                }
                break;
        }
    }


    private class GetQuiz extends Thread {

        public boolean done;

        public GetQuiz() {
            done = false;
        }

        @Override
        public void run() {
            while (!done) {
                Network.getQuiz(MainActivity.this);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

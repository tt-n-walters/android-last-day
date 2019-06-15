package com.example.androidlastday;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ScoresActivity extends AppCompatActivity implements OnNetworkResult {

    GetQuestion getQuestionThread;

    TextView title;
    TextView scores;
    TextView scoreChanges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);

        title = findViewById(R.id.title);
        scores = findViewById(R.id.scores);
        scoreChanges = findViewById(R.id.scoreChanges);

        Intent intent = getIntent();
        JSONArray users;
        try {
            users = new JSONArray(intent.getStringExtra("users"));

            title.setText("Scores after question " + users.getJSONObject(0).getJSONArray("scores").length());

            StringBuilder scoreBuilder = new StringBuilder();
            StringBuilder changeBuilder = new StringBuilder();
            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                JSONArray userScores = user.getJSONArray("scores");
                int total = 0;
                for (int j = 0; j < userScores.length(); j++) {
                    total += userScores.getInt(j);
                }
                scoreBuilder.append(String.format("%s:      %d\n", user.getString("name"), total));
                changeBuilder.append("+" + userScores.getInt(userScores.length() - 1) + "\n");
            }
            scores.setText(scoreBuilder.toString().trim());
            scoreChanges.setText(changeBuilder.toString().trim());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        getQuestionThread = new GetQuestion(500);
        getQuestionThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        getQuestionThread.done = true;
    }

    @Override
    public void handleResult(Task task) {
        if (task.state == Task.SUCCESSFUL) {
            try {
                if (task.response.getString("state").equals("running")) {
                    Intent intent = new Intent(this, QuestionActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class GetQuestion extends Thread {

        private boolean done;
        private int delay;

        public GetQuestion(int delay) {
            done = false;
            this.delay = delay;
        }

        @Override
        public void run() {
            while (!done) {
                Network.getQuestion(ScoresActivity.this);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

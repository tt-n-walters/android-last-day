package com.example.androidlastday;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;

public class QuestionActivity extends AppCompatActivity implements OnNetworkResult {

    private static final String TAG = "FINALDAY";

    GetQuestion getQuestionThread;

    Button button1;
    Button button2;
    Button button3;
    Button button4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);

        setAnswerButton(button1, 0);
        setAnswerButton(button2, 1);
        setAnswerButton(button3, 2);
        setAnswerButton(button4, 3);
    }

    private void setAnswerButton(Button button, final int answer) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Network.postQuestion(QuestionActivity.this, answer);
            }
        });
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
        switch (task.type) {
            case Task.GET_QUESTION:
                if (task.state == Task.SUCCESSFUL) {
                    try {
                        if (task.response.getString("state").equals("complete")) {
                            Intent intent = new Intent(this, ScoresActivity.class);
                            intent.putExtra("users", task.response.getJSONArray("users").toString());
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivity(intent);
                        } else {

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case Task.POST_QUESTION:

                break;
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
                Network.getQuestion(QuestionActivity.this);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

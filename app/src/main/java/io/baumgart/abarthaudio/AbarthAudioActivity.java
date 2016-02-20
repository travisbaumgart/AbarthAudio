package io.baumgart.abarthaudio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class AbarthAudioActivity extends AppCompatActivity {

    private BroadcastReceiver receiver;
    private String status = "";
    private TextView statusTextView;
    private Button startButton;
    private Button stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abarth_audio);

        statusTextView = (TextView) findViewById(R.id.statusText);
        statusTextView.setMovementMethod(new ScrollingMovementMethod());

        startButton = (Button)findViewById(R.id.startButton);
        stopButton = (Button)findViewById(R.id.stopButton);

        startButton.setEnabled(Boolean.TRUE);
        stopButton.setEnabled(Boolean.FALSE);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                status = intent.getStringExtra(AbarthAudioService.COPA_MESSAGE);
                statusTextView.setText(statusTextView.getText() + "\n" + status);
            }
        };

    }

    // Start the  service
    public void startNewService(View view) {
        startService(new Intent(this, AbarthAudioService.class));
        startButton.setEnabled(Boolean.FALSE);
        stopButton.setEnabled(Boolean.TRUE);
    }

    // Stop the  service
    public void stopNewService(View view) {
        stopService(new Intent(this, AbarthAudioService.class));
        startButton.setEnabled(Boolean.TRUE);
        stopButton.setEnabled(Boolean.FALSE);
        statusTextView.setText("");
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(AbarthAudioService.COPA_RESULT)
        );
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

}

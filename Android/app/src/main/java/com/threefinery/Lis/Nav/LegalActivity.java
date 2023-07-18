package com.threefinery.Lis.Nav;

import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LegalActivity extends AppCompatActivity {

    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_legal);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        final TextView textView = findViewById(R.id.textViewLegalText);
        textView.setMovementMethod(new ScrollingMovementMethod());

        final Button buttonView = findViewById(R.id.sendButton);
        buttonView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putBoolean("hasAcceptedTerms", true);
                editor.commit();

                finish();
            }
        });
    }
}

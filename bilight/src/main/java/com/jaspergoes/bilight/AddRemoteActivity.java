package com.jaspergoes.bilight;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.jaspergoes.bilight.milight.Controller;

import java.util.regex.Pattern;

public class AddRemoteActivity extends AppCompatActivity {

    private EditText port;
    private EditText ip;
    private Button connect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_remote);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        TextWatcher validate = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                validate();
            }
        };

        ip = (EditText) findViewById(R.id.edit_ipaddress);
        ip.addTextChangedListener(validate);

        port = (EditText) findViewById(R.id.edit_port);
        port.setText(Integer.toString(Controller.defaultMilightPort));
        port.addTextChangedListener(validate);

        connect = (Button) findViewById(R.id.connect);
        connect.setEnabled(false);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Controller.INSTANCE.setDevice(ip.getText().toString().trim(), Integer.parseInt(port.getText().toString().trim()), getApplicationContext());
                    }
                }).start();
                finish();
            }
        });

    }

    private void validate() {

        boolean valid = true;
        String t;

        t = ip.getText().toString().trim();
        Pattern hostPattern = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");
        Pattern ipPattern = Pattern.compile("^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$");
        if (!hostPattern.matcher(t).matches() && !ipPattern.matcher(t).matches()) {
            valid = false;
        }

        t = port.getText().toString().trim();
        try {
            int unused = Integer.parseInt(t);
        } catch (NumberFormatException e) {
            valid = false;
        }

        connect.setEnabled(valid);

    }

}

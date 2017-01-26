package com.jaspergoes.bilight;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;

import com.jaspergoes.bilight.helpers.ColorPickerView;
import com.jaspergoes.bilight.helpers.OnColorChangeListener;
import com.jaspergoes.bilight.helpers.OnProgressChangeListener;
import com.jaspergoes.bilight.milight.Controller;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.ArrayList;
import java.util.List;

public class ControlActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_control);

        Toolbar supportActionBar = (Toolbar) findViewById(R.id.toolbar);
        supportActionBar.setSubtitle(Controller.milightAddress.getHostAddress() + (Controller.milightPort != Controller.defaultMilightPort ? ":" + Integer.toString(Controller.milightPort) : "") + (Controller.networkInterfaceName.length() > 0 ? " " + getString(R.string.via) + " " + Controller.networkInterfaceName : ""));
        setSupportActionBar(supportActionBar);

        setCheckboxes();

        /* Switch on */
        ((Button) findViewById(R.id.switchOn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        Controller.INSTANCE.setOnOff(true);

                    }

                }).start();

            }
        });

        /* Switch off */
        ((Button) findViewById(R.id.switchOff)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        Controller.INSTANCE.setOnOff(false);

                    }

                }).start();

            }
        });

        /* White mode */
        ((Button) findViewById(R.id.setWhite)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        Controller.INSTANCE.setWhite();

                    }

                }).start();

            }
        });

        /* Audio analyzer | just trying out some stuff */
        //MicrophoneAnalyzer mic = new MicrophoneAnalyzer();
        //mic.startRecording();

        /* Color */
        TypedArray array = getTheme().obtainStyledAttributes(new int[]{android.R.attr.colorBackground});
        int backgroundColor = array.getColor(0, 0xFFFFFF);
        array.recycle();

        ColorPickerView colorPicker = (ColorPickerView) findViewById(R.id.colorpicker);
        colorPicker.setParentBackground(backgroundColor);
        colorPicker.setOnColorChangeListener(new OnColorChangeListener() {

            @Override
            public void colorChanged(final int color) {

                if (Controller.newColor != (Controller.newColor = (color + 128) % 256)) {

                    synchronized (Controller.INSTANCE) {
                        Controller.INSTANCE.notify();
                    }

                }

            }

            @Override
            public void refresh() {

                Controller.INSTANCE.refresh();

            }

        });

        DiscreteSeekBar seekbar;

        /* Brightness */
        seekbar = (DiscreteSeekBar) findViewById(R.id.seekbar_brightness);
        seekbar.setProgress(Controller.newBrightness == -1 ? 100 : Controller.newBrightness);
        seekbar.setOnProgressChangeListener(new OnProgressChangeListener() {

            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, final int value, boolean fromUser) {

                if (fromUser && Controller.newBrightness != (Controller.newBrightness = value)) {

                    synchronized (Controller.INSTANCE) {
                        Controller.INSTANCE.notify();
                    }

                }

            }

        });

        /* Saturation */
        seekbar = (DiscreteSeekBar) findViewById(R.id.seekbar_saturation);
        seekbar.setProgress(Controller.newSaturation == -1 ? 100 : 100 - Controller.newSaturation);
        seekbar.setOnProgressChangeListener(new OnProgressChangeListener() {

            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, final int value, boolean fromUser) {

                if (fromUser && Controller.newSaturation != (Controller.newSaturation = 100 - value)) {

                    synchronized (Controller.INSTANCE) {
                        Controller.INSTANCE.notify();
                    }

                }

            }

        });

        /* Temperature */
        seekbar = (DiscreteSeekBar) findViewById(R.id.seekbar_colortemp);
        seekbar.setProgress(Controller.newTemperature == -1 ? 100 : 100 - Controller.newTemperature);
        seekbar.setOnProgressChangeListener(new OnProgressChangeListener() {

            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, final int value, boolean fromUser) {

                if (fromUser && Controller.newTemperature != (Controller.newTemperature = 100 - value)) {

                    synchronized (Controller.INSTANCE) {
                        Controller.INSTANCE.notify();
                    }

                }

            }

        });

    }

    private void setCheckboxes() {

        final AppCompatCheckBox wifi_0 = (AppCompatCheckBox) findViewById(R.id.control_wifi_bridge);
        final AppCompatCheckBox zone_1 = (AppCompatCheckBox) findViewById(R.id.control_zone_1);
        final AppCompatCheckBox zone_2 = (AppCompatCheckBox) findViewById(R.id.control_zone_2);
        final AppCompatCheckBox zone_3 = (AppCompatCheckBox) findViewById(R.id.control_zone_3);
        final AppCompatCheckBox zone_4 = (AppCompatCheckBox) findViewById(R.id.control_zone_4);

         /* Set checkboxes, according to values currently stored in Controller */
        for (int i : Controller.controlDevices) {
            if (i == 0) {
                wifi_0.setChecked(true);
            } else if (i == 7) {
                for (int x : Controller.controlZones) {
                    if (x == -1) {
                        break;
                    } else if (x == 0) {
                        zone_1.setChecked(true);
                        zone_2.setChecked(true);
                        zone_3.setChecked(true);
                        zone_4.setChecked(true);
                    } else {
                        switch (x) {
                            case 1:
                                zone_1.setChecked(true);
                                break;
                            case 2:
                                zone_2.setChecked(true);
                                break;
                            case 3:
                                zone_3.setChecked(true);
                                break;
                            case 4:
                                zone_4.setChecked(true);
                                break;
                        }
                    }
                }
            }
        }

        AppCompatCheckBox.OnCheckedChangeListener changeListener = new AppCompatCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                boolean wifi = wifi_0.isChecked();
                boolean all = zone_1.isChecked() && zone_2.isChecked() && zone_3.isChecked() && zone_4.isChecked();
                if (wifi && all) {
                    Controller.controlDevices = new int[]{8, 7, 0};
                    Controller.controlZones = new int[]{0};
                } else if (all) {
                    Controller.controlDevices = new int[]{8, 7};
                    Controller.controlZones = new int[]{0};
                } else {
                    boolean any = zone_1.isChecked() || zone_2.isChecked() || zone_3.isChecked() || zone_4.isChecked();
                    if (any) {
                        List<Integer> zoneList = new ArrayList<Integer>();

                        if (zone_1.isChecked())
                            zoneList.add(1);
                        if (zone_2.isChecked())
                            zoneList.add(2);
                        if (zone_3.isChecked())
                            zoneList.add(3);
                        if (zone_4.isChecked())
                            zoneList.add(4);

                        if (wifi) {
                            Controller.controlDevices = new int[]{8, 7, 0};
                        } else {
                            Controller.controlDevices = new int[]{8, 7};
                        }

                        int[] ret = new int[zoneList.size()];
                        for (int i = 0; i < ret.length; i++) ret[i] = zoneList.get(i);

                        Controller.controlZones = ret;
                    } else if (wifi) {
                        Controller.controlDevices = new int[]{0};
                        Controller.controlZones = new int[]{-1};
                    } else {
                        /* None selected, at all */
                        Controller.controlDevices = new int[]{};
                        Controller.controlZones = new int[]{-1};
                    }
                }
            }
        };

        wifi_0.setOnCheckedChangeListener(changeListener);
        zone_1.setOnCheckedChangeListener(changeListener);
        zone_2.setOnCheckedChangeListener(changeListener);
        zone_3.setOnCheckedChangeListener(changeListener);
        zone_4.setOnCheckedChangeListener(changeListener);

    }

}

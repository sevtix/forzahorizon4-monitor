package dev.sevtix.horizonmonitor;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    DatagramSocket s;
    byte[] data = new byte[420];
    DatagramPacket p = new DatagramPacket(data, data.length);
    Timer t = new Timer();
    TimerTask timerTask;

    ArrayList<Value> valueList = new ArrayList<>();

    RecyclerView recyclerView;
    ValueAdapter vAdapter;

    boolean gamemode = false;

    float speed = 0.0f;
    float torque = 0.0f;
    float power = 0.0f;
    float boost = 0.0f;
    float rpm = 0.0f;

    float accelerationX = 0.0f;
    float accelerationY = 0.0f;
    float accelerationZ = 0.0f;

    float tire_temp_front_left = 0.0f;
    float tire_temp_front_right = 0.0f;
    float tire_temp_rear_left = 0.0f;
    float tire_temp_rear_right = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        SharedPreferences appSharedPrefs = getSharedPreferences("prefs", MODE_PRIVATE);

        if(appSharedPrefs.contains("views")) {
            Gson gson = new Gson();
            String json = appSharedPrefs.getString("views", null);
            Type type = new TypeToken<List<Value>>() {}.getType();
            valueList = gson.fromJson(json, type);

            if(valueList == null) {
                valueList = new ArrayList<>();
            }
        }

        clearList(valueList);

        vAdapter = new ValueAdapter(valueList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.getItemAnimator().setChangeDuration(0);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(vAdapter);
        vAdapter.notifyDataSetChanged();


        recyclerView.addOnItemTouchListener(new RecyclerOnTouch(getApplicationContext(), recyclerView, new RecyclerOnTouch.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                //Value value = valueList.get(position);
                Toast.makeText(getApplicationContext(), "Halte gedrückt um zu den Optionen zu gelangen", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, final int pos) {

                final int position = recyclerView.getChildLayoutPosition(view);

                try {
                    final Value value = valueList.get(position);

                    final AlertDialog.Builder optionsDialog = new AlertDialog.Builder(MainActivity.this);
                    optionsDialog.setTitle("Optionen zu \"" + value.getTitle() + "\"");
                    optionsDialog.setMessage("Lösche und verschiebe deine Views");
                    optionsDialog.setCancelable(true);

                    optionsDialog.setPositiveButton("Löschen", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Löschen
                            valueList.remove(position);
                            vAdapter.notifyItemRemoved(position);
                        }
                    });

                    optionsDialog.setNegativeButton("Verschieben", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AlertDialog.Builder moveDialog = new AlertDialog.Builder(MainActivity.this);
                            moveDialog.setTitle("Verschieben");
                            moveDialog.setPositiveButton("Nach oben verschieben", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if(position > 0) {
                                        valueList.remove(position);
                                        valueList.add(position - 1, value);
                                        vAdapter.notifyItemMoved(position, position-1);
                                    } else {
                                        Toast.makeText(MainActivity.this, "Diese View ist bereits ganz oben!", Toast.LENGTH_SHORT).show();
                                    }


                                }
                            });

                            moveDialog.setNegativeButton("Nach unten verschieben", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(position < valueList.size() - 1) {
                                        valueList.remove(position);
                                        valueList.add(position + 1, value);
                                        vAdapter.notifyItemMoved(position, position+1);
                                    } else {
                                        Toast.makeText(MainActivity.this, "Diese View ist bereits ganz unten!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            moveDialog.create().show();
                        }
                    });

                    optionsDialog.setNeutralButton("Abbrechen", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    AlertDialog dialog = optionsDialog.create();
                    dialog.show();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                //vAdapter.notifyDataSetChanged();
            }
        }));

        try {
            s = new DatagramSocket(2048);
        } catch ( final Exception ex ){
            ex.printStackTrace();
        }

        timerTask = new TimerTask() {
            @Override
            public void run() {
                receive();
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                t.schedule(timerTask, 0, 2);
            }
        }).run();
    }

    private void receive() {
        try {
            if(s.isBound()) {
                s.receive(p);
            }
            updateViews(data);
        } catch ( final Exception ex ){
            ex.printStackTrace();
        }
    }

    Utils utils = new Utils();

    void updateViews(byte[] array) {
        String values = utils.bytesToHex(array);

        if(utils.stringFromToInString(0, 8, values).equals("01000000")) {
            gamemode = true;

        } else {
            gamemode = false;
        }

        if(gamemode) {
            speed = utils.getFloatOfString(utils.stringFromToInString(512, 520, values));
            rpm = utils.getFloatOfString(utils.stringFromToInString(32, 40, values));

            power = utils.getFloatOfString(utils.stringFromToInString(520, 528, values));
            torque = utils.getFloatOfString(utils.stringFromToInString(528, 536, values));

            accelerationX = utils.getFloatOfString(utils.stringFromToInString(40, 48, values));
            accelerationY = utils.getFloatOfString(utils.stringFromToInString(48, 56, values));
            accelerationZ = utils.getFloatOfString(utils.stringFromToInString(56, 64, values));

            tire_temp_front_left = utils.getFloatOfString(utils.stringFromToInString(536, 544, values));
            tire_temp_front_right = utils.getFloatOfString(utils.stringFromToInString(544, 552, values));
            tire_temp_rear_left = utils.getFloatOfString(utils.stringFromToInString(552, 560, values));
            tire_temp_rear_right = utils.getFloatOfString(utils.stringFromToInString(560, 568, values));
            boost = utils.getFloatOfString(utils.stringFromToInString(568, 576, values));

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    applyValueToType(ValueType.TORQUE, Math.round(torque) + " Nm");
                    applyValueToType(ValueType.POWER, Math.round(power / 1000) + " kW");
                    applyValueToType(ValueType.RPM, Math.round(rpm) + " U/min");
                    applyValueToType(ValueType.SPEED, Math.round(speed*3.6f) + " km/h");
                    applyValueToType(ValueType.BOOST, round(boost / 14.504f, 2) + " Bar");
                    // TIRES
                    applyValueToType(ValueType.TIRE_TEMP_FRONT_LEFT, Math.round((tire_temp_front_left - 32) * 5/9) + " °C");
                    applyValueToType(ValueType.TIRE_TEMP_FRONT_RIGHT, Math.round((tire_temp_front_right - 32) * 5/9) + " °C");
                    applyValueToType(ValueType.TIRE_TEMP_REAR_LEFT, Math.round((tire_temp_rear_left - 32) * 5/9) + " °C");
                    applyValueToType(ValueType.TIRE_TEMP_REAR_RIGHT, Math.round((tire_temp_rear_right - 32) * 5/9) + " °C");

                    applyValueToType(ValueType.ACCELERATION_X, Math.round(accelerationX) + " m/s2");
                    applyValueToType(ValueType.ACCELERATION_Y, Math.round(accelerationY) + " m/s2");
                    applyValueToType(ValueType.ACCELERATION_Z, Math.round(accelerationZ) + " m/s2");

                    for(int i = 0; i <= vAdapter.getItemCount() - 1; i++) {
                        vAdapter.notifyItemChanged(i);
                    }


                    //rpmView.setText(Math.round(rpm) + " r/min");
                    //powerView.setText(Math.round(power/1000) + " kw");
                    //torqueView.setText(Math.round(torque) + " nm");
                    //boostView.setProgress(Math.round(boost * 10));
                }
            });
        }
    }

    void applyValueToType(ValueType type, String applyValue) {
        for(Value v : valueList) {
            if(v.getType() == type) {
                v.setValue(applyValue);
            }
        }
    }

    @Override
    protected void onStop() {
        save();
        super.onStop();
    }

    BigDecimal round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd;
    }

    void save() {
        // SAVE VIEWS
        SharedPreferences appSharedPrefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = appSharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(valueList);
        editor.putString("views", json);
        editor.apply();
    }

    void addView(View v) {
        RecyclerView addViewRecycler;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View content = inflater.inflate(R.layout.add_view_dialog, null);

        builder.setView(content);

        addViewRecycler = (RecyclerView) content.findViewById(R.id.add_view_recycler);
        final List<Value> addViewList = new ArrayList<>();
        ValueAdapter adapter = new ValueAdapter(addViewList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        addViewRecycler.setLayoutManager(mLayoutManager);
        addViewRecycler.setItemAnimator(new DefaultItemAnimator());
        addViewRecycler.setAdapter(adapter);

        builder.setPositiveButton("Fertig", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        addViewRecycler.addOnItemTouchListener(new RecyclerOnTouch(getApplicationContext(), addViewRecycler, new RecyclerOnTouch.ClickListener() {
            @Override
            public void onClick(View view, int position) {

                // ADD VIEW
                Value value = addViewList.get(position);
                
                if(!doesTypeExist(value.getType())) {
                    value.setValue("0.0");
                    valueList.add(value);
                    vAdapter.notifyItemInserted(vAdapter.getItemCount()-1);
                } else {
                    Toast.makeText(MainActivity.this, "Diese View hast du bereit hinzugefügt!", Toast.LENGTH_SHORT).show();
                }
                

            }

            @Override
            public void onLongClick(View view, final int position) {
            }
        }));

        addViewList.add(new Value("Ladedruck","Zeigt den aktuellen Ladedruck an","Bar", ValueType.BOOST));
        addViewList.add(new Value("Drehmoment","Zeigt das aktuelle Drehmoment an","Nm", ValueType.TORQUE));
        addViewList.add(new Value("Leistung","Zeigt die aktuelle Leistung an","kW", ValueType.POWER));
        addViewList.add(new Value("Umdrehungen","Zeigt die Umdrehungen pro Minute an","U/min", ValueType.RPM));
        //addViewList.add(new Value("Gang","Zeigt den aktuellen Gang an","Gang", ValueType.GEAR));
        addViewList.add(new Value("Geschwindigkeit","Zeigt die aktuelle Geschwindigkeit an","km/h", ValueType.SPEED));
        addViewList.sort(new ValueComperator());

        //addViewList.add(new Value("Zurückgelegte Strecke","Zeigt die zurückgelegte Strecke an","m", ValueType.DISTANCE_TRAVALED));
        addViewList.add(new Value("Reifentemperatur vorne Links","Zeigt die aktuelle Reifentemperatur vorne Links an","°", ValueType.TIRE_TEMP_FRONT_LEFT));
        addViewList.add(new Value("Reifentemperatur vorne Rechts","Zeigt die aktuelle Reifentemperatur vorne Rechts an","°", ValueType.TIRE_TEMP_FRONT_RIGHT));
        addViewList.add(new Value("Reifentemperatur hinten Links","Zeigt die aktuelle Reifentemperatur hinten Links an","°", ValueType.TIRE_TEMP_REAR_LEFT));
        addViewList.add(new Value("Reifentemperatur hinten Rechts","Zeigt die aktuelle Reifentemperatur hinten Rechts an","°", ValueType.TIRE_TEMP_REAR_RIGHT));

        addViewList.add(new Value("Beschleunigung X","Zeigt die aktuelle Beschleunigung der X Achse an","m/s2", ValueType.ACCELERATION_X));
        addViewList.add(new Value("Beschleunigung Y","Zeigt die aktuelle Beschleunigung der Y Achse an","m/s2", ValueType.ACCELERATION_Y));
        addViewList.add(new Value("Beschleunigung Z","Zeigt die aktuelle Beschleunigung der Z Achse an","m/s2", ValueType.ACCELERATION_Z));

        adapter.notifyDataSetChanged();

        builder.create().show();
    }


    boolean doesTypeExist(ValueType type) {
        for(Value value : valueList) {
            if(value.getType() == type) {
                return true;
            }
        }
        return false;
    }

    void clearList(List<Value> list) {
        for(Value v : list) {
            v.setValue("0.0");
        }
    }
}

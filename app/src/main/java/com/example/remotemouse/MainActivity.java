package com.example.remotemouse;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;

    private Socket socket;
    private OutputStream outputStream;
    private Button connectButton;
    private boolean isConnected = false;

    float screenWidth = 1920;  // Largeur de l'écran
    float screenHeight = 1080;  // Hauteur de l'écran
    private EditText ipAddressField;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ipAddressField = findViewById(R.id.ipAddressField);
        connectButton = findViewById(R.id.connectButton);
        connectButton.setOnClickListener(v -> {
            if (!isConnected) {
                String ipAddress = ipAddressField.getText().toString().trim();
                if (!ipAddress.isEmpty()) {
                    connectToServer(ipAddress);
                } else {
                    Log.e("Socket", "Adresse IP non valide");
                }
            } else {
                disconnectFromServer();
            }
        });

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        Button leftButton = findViewById(R.id.leftButton);
        Button rightButton = findViewById(R.id.rightButton);

        leftButton.setOnTouchListener((v, event) -> {
            if (isConnected) {
                new Thread(() -> {
                    try {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            outputStream.write("CLICK_LEFT_DOWN\n".getBytes());
                            outputStream.flush();
                            Log.d("Socket", "Clic gauche maintenu envoyé");
                        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                            outputStream.write("CLICK_LEFT_UP\n".getBytes());
                            outputStream.flush();
                            Log.d("Socket", "Clic gauche relâché envoyé");
                        }
                    } catch (Exception e) {
                        Log.e("Socket", "Erreur d'envoi du clic gauche maintenu", e);
                    }
                }).start();
            }
            return true; // Indiquer que l'événement a été traité
        });


        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected) {
                    new Thread(() -> {
                        try {
                            // Code pour indiquer un clic droit
                            outputStream.write("CLICK_RIGHT\n".getBytes());
                            outputStream.flush();
                            Log.d("Socket", "Clic droit envoyé");
                        } catch (Exception e) {
                            Log.e("Socket", "Erreur d'envoi de clic droit", e);
                        }
                    }).start();
                }
            }
        });
    }

    private void connectToServer(String ipAddress) {
        new Thread(() -> {
            try {
                socket = new Socket(ipAddress, 65432); // Utiliser l'adresse IP saisie
                outputStream = socket.getOutputStream();
                runOnUiThread(() -> {
                    isConnected = true;
                    connectButton.setText("Déconnexion");
                    connectButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                });
            } catch (Exception e) {
                Log.e("Socket", "Erreur de connexion", e);
            }
        }).start();
    }

    private void disconnectFromServer() {
        try {
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
            isConnected = false;
            runOnUiThread(() -> {
                connectButton.setText("Connexion");
                connectButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
            });
        } catch (Exception e) {
            Log.e("Socket", "Erreur de déconnexion", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gyroscopeSensor != null) {
            sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        disconnectFromServer(); // Fermer le socket si l'application est mise en pause
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float gyroX = event.values[0];
            float gyroY = event.values[1];
            float gyroZ = event.values[2];

            Log.d("Gyroscope", "X: " + (int)gyroX + ", Y: " + (int)gyroY + ", Z: " + (int)gyroZ);

            if (isConnected) {
                new SendCoordinatesThread(outputStream, gyroZ, gyroX).start();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (isConnected) {
                new Thread(() -> {
                    try {
                        outputStream.write("VOLUME_DOWN\n".getBytes());
                        outputStream.flush();
                    } catch (Exception e) {
                        Log.e("Socket", "Erreur d'envoi de clic gauche", e);
                    }
                }).start();
            }
            return true;
        }
        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            if (isConnected) {
                new Thread(() -> {
                    try {
                        outputStream.write("VOLUME_UP\n".getBytes());
                        outputStream.flush();
                    } catch (Exception e) {
                        Log.e("Socket", "Erreur d'envoi de clic gauche", e);
                    }
                }).start();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Ne rien faire ici
    }
}

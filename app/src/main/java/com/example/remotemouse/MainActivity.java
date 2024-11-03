package com.example.remotemouse;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;

    private Socket socket;
    private OutputStream outputStream;
    private Button connectButton;
    private boolean isConnected = false;
    private float previousX, previousY;

    float screenWidth = 1920;  // Largeur de l'écran
    float screenHeight = 1080;  // Hauteur de l'écran

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectButton = findViewById(R.id.connectButton);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected) {
                    disconnectFromServer();
                } else {
                    connectToServer();
                }
            }
        });

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        previousY = screenHeight / 2;
        previousX = screenWidth / 2;
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                socket = new Socket("192.168.1.57", 65432); // IP de votre ordinateur
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
            if (socket != null) {
                socket.close(); // Fermer le socket
                socket = null;
                outputStream = null;
            }
            runOnUiThread(() -> {
                isConnected = false;
                connectButton.setText("Connexion");
                connectButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
            });
        } catch (Exception e) {
            Log.e("Socket", "Erreur de fermeture", e);
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
            float gyroX = event.values[0]; // Rotation autour de l'axe X
            float gyroY = event.values[1]; // Rotation autour de l'axe Y
            float gyroZ = event.values[2]; // Rotation autour de l'axe Z

            // Log les valeurs du gyroscope
            Log.d("Gyroscope", "X: " + (int)gyroX + ", Y: " + (int)gyroY + ", Z: " + (int)gyroZ);

            // Définir un facteur de sensibilité pour les mouvements
            float sensitivity = 50; // Ajustez cette valeur selon vos préférences

            // Récupérer les valeurs précédentes pour le mouvement
            float newX = previousX - (gyroZ * sensitivity);
            float newY = previousY - (gyroX * sensitivity); // Inverser Y pour correspondre à l'orientation

            // Limiter la position dans les limites de l'écran
            newX = Math.max(0, Math.min(newX, screenWidth));
            newY = Math.max(0, Math.min(newY, screenHeight));

            // Envoyer les coordonnées à l'ordinateur si connecté
            if (isConnected) {
                new SendCoordinatesThread(outputStream, (int) newX, (int) newY).start();
            }

            // Mettre à jour les précédentes valeurs
            previousX = newX;
            previousY = newY;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            // Si le bouton volume bas est pressé, envoyer un clic gauche
            if (isConnected) {
                new Thread(() -> {
                    try {
                        // Code spécifique pour indiquer un clic gauche (à ajuster côté serveur si nécessaire)
                        outputStream.write("CLICK_LEFT\n".getBytes());
                        outputStream.flush();
                        Log.d("Socket", "Clic gauche envoyé");
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

package com.example.remotemouse;

import android.util.Log;

import java.io.OutputStream;

public class SendCoordinatesThread extends Thread {
    private OutputStream outputStream;
    private float newX;
    private float newY;

    public SendCoordinatesThread(OutputStream outputStream, float newX, float newY) {
        this.outputStream = outputStream;
        this.newX = newX;
        this.newY = newY;
    }

    @Override
    public void run() {
        try {
            if (outputStream != null) {
                String message = "COORD_"+newX + "," + newY+"_";
                outputStream.write(message.getBytes());
                outputStream.flush();
            }
        } catch (Exception e) {
            Log.e("Socket", "Erreur d'envoi des données", e);
        }
    }
}
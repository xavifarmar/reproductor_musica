package com.example.spotify;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity; // Asegúrate de importar esta clase

public class PlayActivity extends AppCompatActivity { // Aquí debes extender de AppCompatActivity

    private MediaPlayer mediaPlayer;
    private ImageButton playButton, pauseButton, stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play); // Estableces el layout de la actividad

        // Inicializamos los botones
        playButton = findViewById(R.id.imageButtonPlay);

        // Configurar el MediaPlayer
        //mediaPlayer = MediaPlayer.create(this, R.raw.music_file); // Asegúrate de tener el archivo en res/raw

        // Acción al presionar el botón Play
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();  // Inicia la reproducción
                }
            }
        });

        // Acción al presionar el botón Pause
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();  // Pausa la reproducción
                }
            }
        });

        // Acción al presionar el botón Stop
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying() || mediaPlayer.isPaused()) {
                    mediaPlayer.stop();  // Detiene la reproducción
                    mediaPlayer.prepareAsync();  // Prepara el MediaPlayer para una nueva reproducción
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();  // Libera recursos del MediaPlayer
        }
    }
}

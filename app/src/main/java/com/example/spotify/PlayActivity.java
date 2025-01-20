package com.example.spotify;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class PlayActivity extends AppCompatActivity {

    private static final int PICK_AUDIO_FILE = 1;  // Código para seleccionar el archivo de audio

    private MediaPlayer mediaPlayer;
    private ImageButton playButton, pauseButton, stopButton;
    private SeekBar seekBar;
    private boolean isPaused = false;

    private Handler handler = new Handler();  // Handler para actualizar el SeekBar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_play);

        // Inicializar los botones
        playButton = findViewById(R.id.imageButtonPlay);
        //pauseButton = findViewById(R.id.imageButtonPause);
        //stopButton = findViewById(R.id.imageButtonStop);
        seekBar = findViewById(R.id.seekBar);  // Inicializar el SeekBar

        // Acción al presionar el botón Play
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null) {
                    if (!mediaPlayer.isPlaying()) {
                        if (isPaused) {
                            mediaPlayer.start();
                            isPaused = false;
                            updateSeekBar();  // Comenzamos a actualizar el SeekBar
                            Toast.makeText(PlayActivity.this, "Reproduciendo música", Toast.LENGTH_SHORT).show();
                        } else {
                            mediaPlayer.seekTo(0);
                            mediaPlayer.start();
                            updateSeekBar();  // Comenzamos a actualizar el SeekBar
                            Toast.makeText(PlayActivity.this, "Reproduciendo música", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        // Acción al presionar el botón Pause
        /*pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    isPaused = true;
                    Toast.makeText(PlayActivity.this, "Música en pausa", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Acción al presionar el botón Stop
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    isPaused = false;
                    seekBar.setProgress(0);  // Resetea el SeekBar
                    Toast.makeText(PlayActivity.this, "Música detenida", Toast.LENGTH_SHORT).show();
                }
            }
        });*/

        // Iniciar la selección del archivo cuando se abre la actividad
        selectAudioFile();
    }

    private void selectAudioFile() {
        // Abrir el selector de archivos para que el usuario seleccione un archivo de audio (MP3)
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_TITLE, "Selecciona un archivo de audio");
        startActivityForResult(intent, PICK_AUDIO_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_AUDIO_FILE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedUri = data.getData();

                if (selectedUri != null) {
                    try {
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(getApplicationContext(), selectedUri);
                        mediaPlayer.prepare();  // Preparamos el MediaPlayer
                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                Toast.makeText(PlayActivity.this, "Archivo listo para reproducir", Toast.LENGTH_SHORT).show();
                                seekBar.setMax(mediaPlayer.getDuration());  // Establecer la duración total en el SeekBar
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al cargar el archivo", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void updateSeekBar() {
        // Actualizamos el SeekBar cada segundo mientras la música se reproduce
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();  // Obtiene la posición actual
                    seekBar.setProgress(currentPosition);  // Actualiza el SeekBar
                    handler.postDelayed(this, 1000);  // Vuelve a llamar al Runnable cada segundo
                }
            }
        };
        handler.post(runnable);  // Inicia el proceso de actualización del SeekBar
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}

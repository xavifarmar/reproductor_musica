package com.example.spotify;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlayActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private ImageButton playButton, pauseButton, houseButton, forwardButton, rewindButton,
            nextButton, backButton;
    private SeekBar seekBar;
    private ImageView coverImageView;  // ImageView for displaying the cover image
    private boolean isPaused = false;  // State to track if it's paused
    private Handler handler = new Handler();  // Handler to update the SeekBar
    private TextView title_song;
    private static final int SEEK_INCREMENT = 10000;  // 10 seconds in milliseconds
    private List<Song> songList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_play);

        // Initialize buttons and ImageView for the cover
        playButton = findViewById(R.id.ic_play);  // Play button
        pauseButton = findViewById(R.id.ic_pause);  // Pause button
        seekBar = findViewById(R.id.seekBar);  // Initialize SeekBar
        coverImageView = findViewById(R.id.coverImageView);  // ImageView for the song cover
        houseButton = findViewById(R.id.house_btn);
        title_song = findViewById(R.id.title_song);  // Initialize title_song TextView
        forwardButton = findViewById(R.id.forwardButton);
        rewindButton = findViewById(R.id.rewindButton);

        if (coverImageView != null) {  // Verificar que el ImageView no sea null
            coverImageView.setImageResource(R.drawable.default_cover);  // Establecer la imagen predeterminada
        } else {
            Log.e("PlayActivity", "ImageView is null");
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Solo actualiza la posición de reproducción si el usuario está moviendo la SeekBar
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Detener la actualización automática del SeekBar mientras el usuario lo mueve
                handler.removeCallbacksAndMessages(null);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Reiniciar la actualización automática del SeekBar después de que el usuario haya dejado de moverla
                updateSeekBar();
            }
        });


        // Retrieve song data from the Intent
        Intent intent = getIntent();
        String songPath = intent.getStringExtra("song_path");
        String songName = intent.getStringExtra("song_name");

        if (songName != null) {
            title_song.setText(songName);  // Set song name to title
        }

        String coverFilePath = getIntent().getStringExtra("cover_path");
        Log.d("PlayActivity", "Cover file path: " + coverFilePath);
        if (coverFilePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(coverFilePath);  // Decodificar el archivo desde la ruta
            if (bitmap != null) {
                coverImageView.setImageBitmap(bitmap);  // Mostrar la imagen en el ImageView
            } else {
                Log.e("PlayActivity", "Failed to decode image from path");
                coverImageView.setImageResource(R.drawable.default_cover);  // Establecer imagen por defecto si falla
            }
        }


        // Iniciar la reproducción si el path de la canción no es null
        if (songPath != null) {
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(songPath);
                mediaPlayer.prepare();  // Prepare the MediaPlayer
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        Toast.makeText(PlayActivity.this, "Now Playing: " + songName, Toast.LENGTH_SHORT).show();
                        seekBar.setMax(mediaPlayer.getDuration());  // Set the SeekBar's maximum value to the song's duration
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading the song", Toast.LENGTH_SHORT).show();
            }
        }

        // Play button action
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null) {
                    if (!mediaPlayer.isPlaying()) {
                        // If the music is not playing
                        if (isPaused) {
                            mediaPlayer.start();  // Resume playback
                            isPaused = false;
                            updatePlayPauseButtons();  // Update buttons
                            updateSeekBar();  // Start updating the SeekBar
                            Toast.makeText(PlayActivity.this, "Playing music", Toast.LENGTH_SHORT).show();
                        } else {
                            // If it's the first time playing, start from the beginning
                            mediaPlayer.seekTo(0);
                            mediaPlayer.start();
                            updatePlayPauseButtons();  // Update buttons
                            updateSeekBar();  // Start updating the SeekBar
                            Toast.makeText(PlayActivity.this, "Playing music", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        // Pause button action
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    isPaused = true;
                    updatePlayPauseButtons();  // Update buttons
                    Toast.makeText(PlayActivity.this, "Music paused", Toast.LENGTH_SHORT).show();
                }
            }
        });

        houseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PlayActivity.this, SongsActivity.class);
                startActivity(intent);
            }
        });
        rewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rewind10Seconds();
            }
        });

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forward10Seconds();
            }
        });
        // Iniciar la actualización del SeekBar
        updateSeekBar();
    }

    private void updatePlayPauseButtons() {
        // Change visibility between Play and Pause buttons
        if (isPaused) {
            playButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
        } else {
            playButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
        }
    }

    private void updateSeekBar() {
        // Update SeekBar every second while the music is playing
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();  // Get the current position
                    seekBar.setProgress(currentPosition);  // Update the SeekBar
                    handler.postDelayed(this, 1000);  // Re-run this Runnable every second
                }
            }
        };
        handler.post(runnable);  // Start the SeekBar update process
    }
    private void rewind10Seconds() {
        if (mediaPlayer != null) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            int newPosition = Math.max(currentPosition - SEEK_INCREMENT, 0); // Prevent seeking before 0 ms
            mediaPlayer.seekTo(newPosition);
            seekBar.setProgress(newPosition);
            Toast.makeText(this, "Rewind 10 seconds", Toast.LENGTH_SHORT).show();
        }
    }

    private void forward10Seconds() {
        if (mediaPlayer != null) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            int newPosition = Math.min(currentPosition + SEEK_INCREMENT, mediaPlayer.getDuration()); // Prevent seeking beyond the song
            mediaPlayer.seekTo(newPosition);
            seekBar.setProgress(newPosition);
            Toast.makeText(this, "Forward 10 seconds", Toast.LENGTH_SHORT).show();
        }
    }

    private void nextSong(){

    }

    private void previousSong(){

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();  // Release mediaPlayer resources when activity is destroyed
        }
    }
}

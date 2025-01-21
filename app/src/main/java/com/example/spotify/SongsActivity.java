package com.example.spotify;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SongsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SongsAdapter adapter;

    // Lanzadores de permisos
    private ActivityResultLauncher<String> requestAudioPermissionLauncher;
    private ActivityResultLauncher<String> requestImagesPermissionLauncher;
    private ActivityResultLauncher<String> requestVideoPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_songs);

        // Lanzador de permiso para leer audio
        requestAudioPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean isGranted) {
                        if (isGranted) {
                            // Si el permiso de audio es concedido, solicitar permiso de imágenes
                            requestImagesPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES);
                        } else {
                            Toast.makeText(SongsActivity.this, "Permiso denegado para acceder a audio.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Lanzador de permiso para leer imágenes
        requestImagesPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean isGranted) {
                        if (isGranted) {
                            // Si el permiso de imágenes es concedido, solicitar permiso de video
                            requestVideoPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_VIDEO);
                        } else {
                            Toast.makeText(SongsActivity.this, "Permiso denegado para acceder a imágenes.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Lanzador de permiso para leer videos
        requestVideoPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean isGranted) {
                        if (isGranted) {
                            // Permiso de video concedido, proceder a cargar las canciones
                            loadSongs();
                        } else {
                            Toast.makeText(SongsActivity.this, "Permiso denegado para acceder a videos.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Crear el adaptador y establecerlo en el RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Configurar el botón para navegar a la actividad de reproducción
        ImageButton music_btn = findViewById(R.id.music_btn);
        music_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SongsActivity.this, PlayActivity.class);
                startActivity(intent);
            }
        });

        // Crear el adaptador con el listener para el clic
        adapter = new SongsAdapter(this, new SongsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Song song) {
                String coverFilePath = saveBitmapToFile(song.getCoverBitmap());  // Guardar la imagen y obtener la ruta
                // Convierte el Bitmap en byte array
                Intent intent = new Intent(SongsActivity.this, PlayActivity.class);
                intent.putExtra("song_path", song.getPath());  // Pasar la ruta del archivo
                intent.putExtra("song_name", song.getName());// Pasar el nombre de la canción
                intent.putExtra("cover_path", coverFilePath);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        // Verificar si el permiso para leer medios ya ha sido concedido
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            // Si el permiso de audio ya está concedido, solicitar el permiso de imágenes
            requestImagesPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            // Solicitar el permiso de audio primero
            requestAudioPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_AUDIO);
        }
    }

    // Método para cargar canciones
    private void loadSongs() {
        // Aquí puedes cargar las canciones y notificar al adaptador
        // Ejemplo:
        adapter.notifyDataSetChanged();
    }

    private String saveBitmapToFile(Bitmap bitmap) {
        // Crear un archivo en el directorio de caché
        File cacheDir = getCacheDir();  // Puedes usar getExternalCacheDir() si prefieres el almacenamiento externo
        File file = new File(cacheDir, "album_cover.jpg");  // Asignar un nombre único para evitar sobrescrituras

        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);  // Comprimir la imagen a un 80% de calidad
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file.getAbsolutePath();  // Devolver la ruta al archivo
    }

}

package com.example.spotify;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SongsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SongsAdapter adapter;

    // Lanzador de permisos para leer almacenamiento
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs);

        // Inicialización del lanzador de permisos
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean isGranted) {
                        if (isGranted) {
                            // Permiso concedido, proceder con la carga de canciones
                            adapter.notifyDataSetChanged();
                        } else {
                            // Permiso denegado, mostrar mensaje
                            Toast.makeText(SongsActivity.this, "Permiso denegado. Sin acceso a la música.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Crear el adaptador y establecerlo en el RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Crear el adaptador con el listener para el clic
        adapter = new SongsAdapter(this, new SongsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Song song) {
                Intent intent = new Intent(SongsActivity.this, PlayActivity.class);
                intent.putExtra("song_path", song.getPath());  // Pasar la ruta del archivo
                intent.putExtra("song_name", song.getName());  // Pasar el nombre de la canción
                startActivity(intent);  // Iniciar PlayActivity
            }
        });
        recyclerView.setAdapter(adapter);

        // Verificar si el permiso ya ha sido concedido
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            // Permiso ya concedido, cargar canciones
            adapter.notifyDataSetChanged();
        } else {
            // Solicitar permiso mostrando el popup
            requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }
}

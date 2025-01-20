package com.example.spotify;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SongsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SongsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_songs);

        // Inicialització del RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));  // Establir el LayoutManager

        // Crear i assignar l'adaptador al RecyclerView
        adapter = new SongsAdapter(this);  // Crear l'adaptador
        recyclerView.setAdapter(adapter);  // Assignar l'adaptador al RecyclerView

        // Comprovar si tenim permís per llegir l'emmagatzematge extern
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Si no tenim permís, el sol·licitem
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_AUDIO)) {
                // Mostrar explicació per demanar permís
                Toast.makeText(this, "Aquesta aplicació necessita accedir a l'emmagatzematge per mostrar les cançons.", Toast.LENGTH_LONG).show();
            }
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_AUDIO},
                    1); // El '1' és el codi de petició
        } else {
            // Si ja tenim permís, carregar les cançons
            cargarCanciones();
        }
    }

    // Quan l'usuari respongui a la sol·licitud de permisos, aquest mètode es cridarà
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si el permís és concedit, es carreguen les cançons
                cargarCanciones();
            } else {
                // Si el permís no és concedit, mostrar un missatge d'error
                Toast.makeText(this, "Permís denegat per accedir a les cançons.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Mètode per carregar les cançons
    private void cargarCanciones() {
        // Aquí és on cridaràs el teu codi per carregar les cançons
    }
}

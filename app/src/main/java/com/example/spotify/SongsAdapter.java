package com.example.spotify;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotify.R;
import com.example.spotify.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongViewHolder> {

    private Context context;
    private List<Song> songs;
    private OnItemClickListener onItemClickListener;

    // Constructor actualizado para aceptar un listener
    public SongsAdapter(Context context, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.songs = new ArrayList<>();
        this.onItemClickListener = onItemClickListener;  // Inicializar el listener
        obtenerCanciones();  // Cargar las canciones al inicializar el adaptador
    }

    // ViewHolder para los elementos del RecyclerView
    public static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView songName;
        ImageView coverImageView;

        public SongViewHolder(View itemView) {
            super(itemView);
            songName = itemView.findViewById(R.id.text1);  // Usamos el ID correcto para el TextView
            coverImageView = itemView.findViewById(R.id.coverImageView);  // Usamos el ID correcto para la portada
        }
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflamos el layout personalizado
        View view = LayoutInflater.from(context).inflate(R.layout.simple_list_item_1, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.songName.setText(song.getName());  // Asignamos el nombre de la canción al TextView

        // Obtener la portada de la canción
        loadCoverImage(holder.coverImageView, song.getCoverPath());

        // Manejo del clic en el ítem
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(song);  // Llamamos al listener
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    // Método para obtener las canciones de la carpeta "Downloads"
    private void obtenerCanciones() {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Audio.Media.DISPLAY_NAME,  // Nombre del archivo
                MediaStore.Audio.Media.DATA,           // Ruta del archivo
                MediaStore.Audio.Media.ALBUM_ID       // ID del álbum para la portada
        };

        String selection = MediaStore.Audio.Media.DATA + " LIKE ?";
        String[] selectionArgs = new String[]{"%/Download/%"};

        try {
            Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, null);
            if (cursor != null) {
                int nameColumnIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
                int pathColumnIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                int albumIdColumnIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

                // Verificamos si las columnas existen
                if (nameColumnIndex >= 0 && pathColumnIndex >= 0 && albumIdColumnIndex >= 0) {
                    if (cursor.moveToFirst()) {
                        do {
                            String name = cursor.getString(nameColumnIndex);
                            String path = cursor.getString(pathColumnIndex);
                            long albumId = cursor.getLong(albumIdColumnIndex);

                            // Obtener la ruta de la portada usando el ID del álbum
                            String coverPath = getAlbumCoverPath(albumId);

                            // Solo añadimos canciones que están en la carpeta /Download/
                            if (path != null && path.contains("/Download/")) {
                                songs.add(new Song(name, path, coverPath));  // Crear el objeto Song con coverPath
                            }
                        } while (cursor.moveToNext());
                    }
                    cursor.close();  // Cerrar el cursor
                } else {
                    Log.e("SongsAdapter", "Columnas de datos no encontradas.");
                    Toast.makeText(context, "Error al obtener las canciones", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "No se encontraron canciones en Downloads", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("SongsAdapter", "Error al consultar la base de datos", e);
            Toast.makeText(context, "Ocurrió un error al obtener las canciones", Toast.LENGTH_SHORT).show();
        }
        notifyDataSetChanged();  // Notificar que los datos han cambiado
    }

    // Método para obtener la ruta de la portada del álbum a partir del albumId
    private String getAlbumCoverPath(long albumId) {
        Uri albumUri = Uri.parse("content://media/external/audio/albumart");
        Uri coverUri = Uri.withAppendedPath(albumUri, String.valueOf(albumId));

        try {
            // Intentar obtener la ruta de la portada
            File imgFile = new File(coverUri.getPath());
            if (imgFile.exists()) {
                return coverUri.toString();  // O cualquier lógica para obtener la ruta
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Si no se encuentra la portada, retornamos la ruta de una imagen predeterminada
        return "android.resource://com.example.spotify/" + R.drawable.default_cover;
    }

    // Método para cargar la portada de la canción
    private void loadCoverImage(ImageView imageView, String coverPath) {
        if (coverPath != null) {
            // Si la portada tiene una ruta, la cargamos
            File imgFile = new File(coverPath);
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imageView.setImageBitmap(bitmap);
            }
        } else {
            // Imagen por defecto
            imageView.setImageResource(R.drawable.default_cover);
        }
    }

    // Interfaz para manejar los clics en las canciones
    public interface OnItemClickListener {
        void onItemClick(Song song);
    }
}

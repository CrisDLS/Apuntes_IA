package com.CrisDLS.apuntesia.adapters;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.CrisDLS.apuntesia.databinding.ItemApunteBinding;
import com.CrisDLS.apuntesia.models.Apunte;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ApunteAdapter extends RecyclerView.Adapter<ApunteAdapter.ApunteViewHolder> {

    private List<Apunte> listaApuntes = new ArrayList<>();

    // --- LÓGICA DE REPRODUCCIÓN ---
    private MediaPlayer mediaPlayer;
    // Guardamos la posición que está sonando actualmente (-1 significa que nada suena)
    private int playingPosition = RecyclerView.NO_POSITION;

    // Método para llenar los datos
    public void setApuntes(List<Apunte> apuntes) {
        this.listaApuntes = apuntes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ApunteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemApunteBinding binding = ItemApunteBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ApunteViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ApunteViewHolder holder, int position) {
        Apunte apunte = listaApuntes.get(position);
        Context context = holder.binding.getRoot().getContext();

        // 1. Asignar Textos
        holder.binding.tvApunteTitulo.setText(apunte.getTitulo());
        holder.binding.tvApunteResumen.setText(apunte.getResumen());

        // 2. ESTADO VISUAL DEL BOTÓN DE AUDIO (Prevención de errores por Scroll)
        if (position == playingPosition) {
            // Este es el elemento que está sonando
            holder.binding.btnPlayAudio.setText("Detener");
            holder.binding.btnPlayAudio.setIconResource(android.R.drawable.ic_media_pause);
        } else {
            // Elemento en reposo
            holder.binding.btnPlayAudio.setText("Escuchar");
            holder.binding.btnPlayAudio.setIconResource(android.R.drawable.ic_media_play);
        }

        // 3. Lógica del Click en Reproducir/Detener
        holder.binding.btnPlayAudio.setOnClickListener(v -> {
            // Para evitar errores si tocan rápido mientras se recicla la vista
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            if (currentPosition == playingPosition) {
                // Toca detener el audio actual
                detenerAudio();
            } else {
                // Toca reproducir un nuevo audio
                reproducirAudio(currentPosition, apunte.getRutaAudio(), context);
            }
        });

        // 4. Click en Opciones (3 puntitos)
        holder.binding.btnOpciones.setOnClickListener(v -> {
            Toast.makeText(context, "Opciones del apunte", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return listaApuntes.size();
    }

    // --- MÉTODOS PRIVADOS PARA CONTROL DE AUDIO ---

    private void reproducirAudio(int posicion, String rutaAudio, Context context) {
        try {
            // 1. Detener el anterior si lo hubiera
            if (mediaPlayer != null) {
                mediaPlayer.reset();
            } else {
                mediaPlayer = new MediaPlayer();
            }

            // 2. Configurar la fuente y preparar
            mediaPlayer.setDataSource(rutaAudio);
            mediaPlayer.prepare();
            mediaPlayer.start();

            // 3. Actualizar la UI
            int posicionAnterior = playingPosition;
            playingPosition = posicion;

            // Refrescar el botón de la tarjeta que se acaba de detener (si existía)
            if (posicionAnterior != RecyclerView.NO_POSITION) {
                notifyItemChanged(posicionAnterior);
            }
            // Refrescar el botón de la tarjeta que acaba de empezar
            notifyItemChanged(playingPosition);

            // 4. Escuchar cuando termine por sí solo
            mediaPlayer.setOnCompletionListener(mp -> detenerAudio());

        } catch (IOException e) {
            Log.e("AudioPlayer", "Error al reproducir: " + e.getMessage());
            Toast.makeText(context, "Archivo de audio no encontrado", Toast.LENGTH_SHORT).show();
            detenerAudio(); // Resetear estado por si acaso
        }
    }

    private void detenerAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }

        // Guardamos quién estaba sonando para actualizar solo esa tarjeta
        int posicionAActualizar = playingPosition;
        playingPosition = RecyclerView.NO_POSITION; // Estado: Nada suena

        if (posicionAActualizar != RecyclerView.NO_POSITION) {
            notifyItemChanged(posicionAActualizar);
        }
    }

    // --- MÉTODO PÚBLICO DE LIMPIEZA ---

    /**
     * Llámalo desde el onDestroy() o onPause() de la Activity
     * para liberar la memoria y apagar la música al salir.
     */
    public void liberarAudio() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        playingPosition = RecyclerView.NO_POSITION;
    }

    // ViewHolder interno
    static class ApunteViewHolder extends RecyclerView.ViewHolder {
        final ItemApunteBinding binding;

        ApunteViewHolder(ItemApunteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
package com.CrisDLS.apuntesia.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.CrisDLS.apuntesia.ui.MateriaDetailActivity;
import com.CrisDLS.apuntesia.databinding.ItemSubjectBinding;
import com.CrisDLS.apuntesia.models.Materia;

import java.util.ArrayList;
import java.util.List;

public class MateriaAdapter extends RecyclerView.Adapter<MateriaAdapter.MateriaViewHolder> {

    private List<Materia> listaMaterias = new ArrayList<>();

    // Método para actualizar los datos desde la Activity
    public void setMaterias(List<Materia> materias) {
        this.listaMaterias = materias;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MateriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // REGLA CUMPLIDA: Inflar usando ViewBinding, sin findViewById
        ItemSubjectBinding binding = ItemSubjectBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MateriaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MateriaViewHolder holder, int position) {
        Materia materia = listaMaterias.get(position);

        // Vincular los datos con la vista
        holder.binding.tvSubjectName.setText(materia.getNombre());

        // Por ahora hardcodeamos los apuntes (lo conectaremos a SQLite en el futuro)
        holder.binding.tvRecordingsCount.setText(materia.getCantidadApuntes() + " apuntes guardados");

        // --- NAVEGACIÓN AL DETALLE ---
        holder.binding.getRoot().setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, MateriaDetailActivity.class);
            // Pasamos el ID y el Nombre a la nueva pantalla
            intent.putExtra("EXTRA_MATERIA_ID", materia.getId());
            intent.putExtra("EXTRA_MATERIA_NOMBRE", materia.getNombre());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaMaterias.size();
    }

    // ViewHolder interno usando ViewBinding
    static class MateriaViewHolder extends RecyclerView.ViewHolder {
        final ItemSubjectBinding binding;

        MateriaViewHolder(ItemSubjectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
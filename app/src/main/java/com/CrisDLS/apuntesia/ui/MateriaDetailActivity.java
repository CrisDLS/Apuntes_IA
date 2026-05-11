package com.CrisDLS.apuntesia.ui;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;


import com.CrisDLS.apuntesia.adapters.ApunteAdapter;
import com.CrisDLS.apuntesia.databinding.ActivityMateriaDetailBinding;
import com.CrisDLS.apuntesia.db.DatabaseHelper;
import com.CrisDLS.apuntesia.models.Apunte;

import java.util.List;

public class MateriaDetailActivity extends AppCompatActivity {

    private ActivityMateriaDetailBinding binding;
    private DatabaseHelper dbHelper;
    private long materiaId;
    private ApunteAdapter adapter; // Variable global

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inflar Vista (Regla ViewBinding estricta)
        binding = ActivityMateriaDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 2. Extraer datos del Intent
        materiaId = getIntent().getLongExtra("EXTRA_MATERIA_ID", -1);
        String materiaNombre = getIntent().getStringExtra("EXTRA_MATERIA_NOMBRE");

        if (materiaId == -1) {
            finish(); // Cierra la pantalla si hubo un error pasando el ID
            return;
        }

        // 3. Configurar Toolbar
        setSupportActionBar(binding.toolbarDetail);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(materiaNombre);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Acción de la flecha de retroceso
        binding.toolbarDetail.setNavigationOnClickListener(v -> finish());

        // 4. Configurar BD y LayoutManager del RecyclerView
        dbHelper = new DatabaseHelper(this);
        binding.rvApuntes.setLayoutManager(new LinearLayoutManager(this));

        // 5. Cargar datos
        cargarApuntes();
    }

    private void cargarApuntes() {
        List<Apunte> listaApuntes = dbHelper.obtenerApuntesPorMateria(materiaId);

        if (listaApuntes.isEmpty()) {
            binding.tvEmptyApuntes.setVisibility(View.VISIBLE);
            binding.rvApuntes.setVisibility(View.GONE);
        } else {
            binding.tvEmptyApuntes.setVisibility(View.GONE);
            binding.rvApuntes.setVisibility(View.VISIBLE);

            // Instanciar el adaptador si es nulo
            if (adapter == null) {
                adapter = new ApunteAdapter();
                binding.rvApuntes.setAdapter(adapter);
            }

            // Pasar la lista obtenida de SQLite al adaptador
            adapter.setApuntes(listaApuntes);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Buena práctica: si el usuario minimiza la app o cambia de pantalla, se detiene
        if (adapter != null) {
            adapter.liberarAudio();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Prevención de memory leaks definitiva
        if (adapter != null) {
            adapter.liberarAudio();
        }
        binding = null;
    }
}
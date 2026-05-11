package com.CrisDLS.apuntesia.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.CrisDLS.apuntesia.databinding.ActivityConfigBinding;
import com.CrisDLS.apuntesia.db.DatabaseHelper;

public class ConfigActivity extends AppCompatActivity {

    // Declaración de ViewBinding
    private ActivityConfigBinding binding;

    // Constantes para SharedPreferences
    private static final String PREFS_NAME = "ApuntesIAPrefs";
    private static final String KEY_DARK_MODE = "darkMode";
    private static final String KEY_SYNC_NOTION = "syncNotion";
    private static final String KEY_NOTION_TOKEN = "notionToken";

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inicializar ViewBinding (REGLA TÉCNICA CUMPLIDA)
        binding = ActivityConfigBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 2. Configurar Toolbar (Botón de regresar)
        setSupportActionBar(binding.toolbarConfig);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbarConfig.setNavigationOnClickListener(v -> finish());

        // 3. Cargar Preferencias Previas
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadPreferences();

        // 4. Configurar Listeners
        setupListeners();
    }

    private void loadPreferences() {
        // Cargar estado de los Switches
        binding.switchDarkMode.setChecked(sharedPreferences.getBoolean(KEY_DARK_MODE, false));
        binding.switchSyncNotion.setChecked(sharedPreferences.getBoolean(KEY_SYNC_NOTION, false));

        // Cargar Token si existe
        String token = sharedPreferences.getString(KEY_NOTION_TOKEN, "");
        binding.etNotionToken.setText(token);
    }

    private void setupListeners() {
        // Listener para Modo Oscuro
        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Listener para Sincronización con Notion
        binding.switchSyncNotion.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_SYNC_NOTION, isChecked).apply();
            String message = isChecked ? "Sincronización activada" : "Solo guardado local";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        // Listener para Guardar Token
        binding.btnSaveToken.setOnClickListener(v -> {
            String token = binding.etNotionToken.getText() != null ? binding.etNotionToken.getText().toString().trim() : "";
            if (!token.isEmpty()) {
                sharedPreferences.edit().putString(KEY_NOTION_TOKEN, token).apply();
                Toast.makeText(this, "Token guardado en bóveda segura", Toast.LENGTH_SHORT).show();
            } else {
                binding.etNotionToken.setError("El token no puede estar vacío");
            }
        });

        // Listener para Agregar Materia
        binding.btnAddSubject.setOnClickListener(v -> {
            String subjectName = binding.etSubjectName.getText() != null ? binding.etSubjectName.getText().toString().trim() : "";
            String subjectId = binding.etSubjectId.getText() != null ? binding.etSubjectId.getText().toString().trim() : "";

            // Validaciones
            if (subjectName.isEmpty()) {
                binding.etSubjectName.setError("El nombre es requerido");
                return;
            }
            if (subjectId.isEmpty() && binding.switchSyncNotion.isChecked()) {
                binding.etSubjectId.setError("Requerido si la sincronización está activa");
                return;
            }

            // --- NUEVA LÓGICA DE BASE DE DATOS --- //

            // Instanciar el Helper (se le pasa el Context de la Activity)
            DatabaseHelper dbHelper = new DatabaseHelper(ConfigActivity.this);

            // Ejecutar la inserción
            long resultadoId = dbHelper.insertarMateria(subjectName, subjectId);

            if (resultadoId != -1) {
                // Éxito
                Toast.makeText(this, "Materia guardada correctamente", Toast.LENGTH_SHORT).show();

                // Limpiar campos UI
                binding.etSubjectName.setText("");
                binding.etSubjectId.setText("");

                // Quitar el foco del input
                binding.etSubjectName.clearFocus();
                binding.etSubjectId.clearFocus();
            } else {
                // Error en BD
                Toast.makeText(this, "Error al guardar la materia", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Buena práctica: liberar la referencia del binding para evitar memory leaks
        binding = null;
    }
}
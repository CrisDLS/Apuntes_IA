package com.CrisDLS.apuntesia;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.CrisDLS.apuntesia.databinding.ActivityMainBinding;
import com.CrisDLS.apuntesia.databinding.DialogRecordBinding;
import com.CrisDLS.apuntesia.db.DatabaseHelper;
import com.CrisDLS.apuntesia.models.Materia;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private DatabaseHelper dbHelper;
    private MateriaAdapter adapter;

    // Variables para Grabación
    private AudioRecorder audioRecorder;
    private String rutaAudioTemporal = "";

    // Variables para Cronómetro
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private int secondsElapsed = 0;

    // IP Local de tu computadora
    private static final String SERVER_URL = "http://192.168.1.66:8000/procesar_apunte";

    // Lanzador moderno para solicitar permisos
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    mostrarDialogoGrabacion();
                } else {
                    Toast.makeText(this, "Se requiere permiso de micrófono para grabar", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inicializar ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 2. Inicializar Helper y Adaptador
        dbHelper = new DatabaseHelper(this);
        adapter = new MateriaAdapter();

        // 3. Configurar RecyclerView
        binding.rvSubjects.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSubjects.setAdapter(adapter);

        // 4. Configurar Listeners estáticos
        setupListeners();
    }

    // Este método se llama cada vez que la pantalla vuelve a primer plano
    @Override
    protected void onResume() {
        super.onResume();
        cargarMaterias();
    }

    private void cargarMaterias() {
        // Consultar la base de datos
        List<Materia> materias = dbHelper.obtenerTodasLasMaterias();

        if (materias.isEmpty()) {
            // Mostrar estado vacío, ocultar lista
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
            binding.rvSubjects.setVisibility(View.GONE);
        } else {
            // Ocultar estado vacío, mostrar lista
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.rvSubjects.setVisibility(View.VISIBLE);

            // Pasar datos al adaptador
            adapter.setMaterias(materias);
        }
    }

    private void setupListeners() {
        // Botón superior (Ajustes)
        binding.topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                startActivity(new Intent(this, ConfigActivity.class));
                return true;
            }
            return false;
        });

        // Botón "+ Agregar Materia" del estado vacío
        binding.btnAddSubjectEmpty.setOnClickListener(v -> {
            startActivity(new Intent(this, ConfigActivity.class));
        });

        // FAB de grabación actualizado con Permisos
        binding.fabRecord.setOnClickListener(v -> {
            if (adapter.getItemCount() == 0) {
                Toast.makeText(this, "Crea una materia antes de grabar", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, ConfigActivity.class));
            } else {
                // Comprobar permiso de micrófono
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    mostrarDialogoGrabacion(); // Ya tenemos permiso
                } else {
                    // Pedir permiso
                    requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
                }
            }
        });
    }

    /**
     * Infla, configura y muestra el diálogo de grabación usando ViewBinding.
     */
    private void mostrarDialogoGrabacion() {
        // 1. Inflar la vista del diálogo
        DialogRecordBinding dialogBinding = DialogRecordBinding.inflate(getLayoutInflater());

        // 2. Preparar los datos del Spinner (Drop-down)
        List<Materia> listaMaterias = dbHelper.obtenerTodasLasMaterias();
        List<String> nombresMaterias = new ArrayList<>();
        for (Materia m : listaMaterias) {
            nombresMaterias.add(m.getNombre());
        }

        // Configurar el Adapter para el AutoCompleteTextView
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                nombresMaterias
        );
        dialogBinding.spinnerMaterias.setAdapter(spinnerAdapter);

        // Seleccionar la primera materia por defecto si existe
        if (!nombresMaterias.isEmpty()) {
            dialogBinding.spinnerMaterias.setText(nombresMaterias.get(0), false);
        }

        // 3. Crear el AlertDialog con Material Design
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogBinding.getRoot())
                .setCancelable(false) // No permitir cerrar tocando fuera
                .create();

        // 4. Lógica del Botón de Cerrar (X)
        dialogBinding.btnClose.setOnClickListener(v -> {
            dialog.dismiss();
        });

        // Inicializar grabadora si no existe
        if (audioRecorder == null) {
            audioRecorder = new AudioRecorder(this);
        }

        // 5. Lógica del Botón Iniciar/Detener (Estado Toggle)
        final boolean[] isRecording = {false};

        dialogBinding.btnRecordToggle.setOnClickListener(v -> {
            if (!isRecording[0]) {
                // --- INICIAR GRABACIÓN ---
                isRecording[0] = true;

                // Cambios Visuales UI
                dialogBinding.btnRecordToggle.setText("Detener");
                dialogBinding.btnRecordToggle.setIconResource(android.R.drawable.ic_media_pause);
                dialogBinding.btnRecordToggle.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                dialogBinding.btnRecordToggle.setTextColor(Color.WHITE);
                dialogBinding.spinnerMaterias.setEnabled(false);
                dialogBinding.btnClose.setEnabled(false); // Evitar que cierre mientras graba

                // 1. Generar ruta temporal segura
                rutaAudioTemporal = new File(getCacheDir(), "apunte_" + System.currentTimeMillis() + ".m4a").getAbsolutePath();

                // 2. Iniciar Grabación de Audio
                audioRecorder.startRecording(rutaAudioTemporal);

                // 3. Iniciar Cronómetro
                iniciarCronometro(dialogBinding.tvTimer);

            } else {
                // --- DETENER GRABACIÓN ---
                isRecording[0] = false;

                // Cambios Visuales UI
                dialogBinding.btnRecordToggle.setText("Procesando...");
                dialogBinding.btnRecordToggle.setEnabled(false);
                dialogBinding.btnClose.setEnabled(true);

                // 1. Detener Grabación y Cronómetro
                audioRecorder.stopRecording();
                detenerCronometro();

                // 2. Extraer el nombre de la materia elegida
                String materiaSeleccionada = dialogBinding.spinnerMaterias.getText().toString();

                Toast.makeText(MainActivity.this, "Procesando con IA... Por favor espera", Toast.LENGTH_LONG).show();

                // Cerrar diálogo para que el usuario no se quede bloqueado
                dialog.dismiss();

                // 3. Enviar el audio mediante OkHttp a Python
                NetworkHelper.getInstance().enviarAudio(rutaAudioTemporal, SERVER_URL, new NetworkHelper.TranscriptionCallback() {
                    @Override
                    public void onSuccess(String titulo, String resumenEstructurado) {
                        // OBLIGATORIO: Volver al Hilo Principal (UI Thread) para actualizar la pantalla
                        runOnUiThread(() -> {
                            Log.d("IA_APUNTES", "TÍTULO: " + titulo);
                            Log.d("IA_APUNTES", "RESUMEN: " + resumenEstructurado);

                            Toast.makeText(MainActivity.this, "¡Apunte generado exitosamente!\nTítulo: " + titulo, Toast.LENGTH_LONG).show();

                            // TODO: Guardar esto en SQLite junto con la ruta del audio y la materia_id
                        });
                    }

                    @Override
                    public void onError(String mensaje) {
                        // OBLIGATORIO: Volver al Hilo Principal
                        runOnUiThread(() -> {
                            Log.e("IA_APUNTES", "Error: " + mensaje);
                            Toast.makeText(MainActivity.this, "Fallo al procesar: " + mensaje, Toast.LENGTH_LONG).show();
                        });
                    }
                }); // <-- ¡AQUÍ ESTABA EL ERROR DE LAS LLAVES!
            }
        });

        // Asegurarnos de limpiar recursos si el usuario cancela con la X
        dialog.setOnDismissListener(d -> {
            if (isRecording[0]) {
                audioRecorder.stopRecording();
                detenerCronometro();
            }
        });

        // 6. Mostrar el Diálogo
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null; // Prevenir Memory Leaks
    }

    /**
     * Inicia un hilo que actualiza el TextView cada segundo en formato MM:SS
     */
    private void iniciarCronometro(TextView tvTimer) {
        secondsElapsed = 0;
        tvTimer.setText("00:00");

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                secondsElapsed++;
                int minutos = secondsElapsed / 60;
                int segundos = secondsElapsed % 60;

                // Formatear a dos dígitos (ej. 05:09)
                String tiempoFormateado = String.format("%02d:%02d", minutos, segundos);
                tvTimer.setText(tiempoFormateado);

                // Ejecutar este mismo código en 1 segundo (1000 ms)
                timerHandler.postDelayed(this, 1000);
            }
        };
        // Iniciar el ciclo
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void detenerCronometro() {
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
        secondsElapsed = 0;
    }
}
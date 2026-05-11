package com.CrisDLS.apuntesia.utils;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;
import java.io.IOException;

public class AudioRecorder {

    private MediaRecorder recorder = null;
    private final Context context;

    public AudioRecorder(Context context) {
        this.context = context;
    }

    /**
     * Configura e inicia la grabación de audio.
     * @param filePath Ruta completa donde se guardará el archivo (.m4a)
     */
    public void startRecording(String filePath) {
        // En Android 12 (API 31+), MediaRecorder requiere el Context en el constructor
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            recorder = new MediaRecorder(context);
        } else {
            recorder = new MediaRecorder();
        }

        try {
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            // Formato MPEG_4 es el estándar para generar archivos .m4a
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            // AAC es óptimo para voz, buen balance calidad/peso
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setOutputFile(filePath);

            recorder.prepare();
            recorder.start();
            Log.d("AudioRecorder", "Grabación iniciada en: " + filePath);

        } catch (IOException e) {
            Log.e("AudioRecorder", "Error al preparar la grabación: " + e.getMessage());
        } catch (IllegalStateException e) {
            Log.e("AudioRecorder", "Estado ilegal del MediaRecorder: " + e.getMessage());
        }
    }

    /**
     * Detiene la grabación y libera los recursos de hardware.
     */
    public void stopRecording() {
        if (recorder != null) {
            try {
                recorder.stop();
            } catch (RuntimeException e) {
                // Puede ocurrir si se detiene inmediatamente después de iniciar
                Log.e("AudioRecorder", "Error al detener: " + e.getMessage());
            } finally {
                recorder.release();
                recorder = null;
                Log.d("AudioRecorder", "Grabación detenida y recursos liberados.");
            }
        }
    }
}

package com.CrisDLS.apuntesia;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetworkHelper {

    private static NetworkHelper instance;
    private final OkHttpClient client;

    public interface TranscriptionCallback {
        void onSuccess(String titulo, String resumenEstructurado);
        void onError(String mensaje);
    }

    private NetworkHelper() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public static synchronized NetworkHelper getInstance() {
        if (instance == null) {
            instance = new NetworkHelper();
        }
        return instance;
    }

    public void enviarAudio(String filePath, String serverUrl, TranscriptionCallback callback) {
        File file = new File(filePath);
        if (!file.exists()) {
            callback.onError("El archivo de audio no existe.");
            return;
        }

        RequestBody fileBody = RequestBody.create(file, MediaType.parse("audio/mp4"));

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("audio", file.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(serverUrl)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Error de conexión: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("Error del servidor: Código " + response.code());
                    return;
                }

                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);

                    // --- AQUÍ ESTÁ LA MAGIA CORREGIDA ---
                    // Leemos directamente las claves que definimos en Python
                    String titulo = json.optString("titulo", "Apunte sin título");
                    String resumen = json.optString("resumen", "Resumen no disponible.");

                    callback.onSuccess(titulo, resumen);

                } catch (JSONException e) {
                    callback.onError("Error leyendo respuesta: " + e.getMessage());
                }
            }
        });
    }
}
package com.tecgeo.tecgeo_tecnova;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.chrisbanes.photoview.PhotoView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ViewPhotoActivity extends AppCompatActivity {
    private RequestQueue queue;
    private JSONArray jsonArray;
    private PhotoView photoView;
    private int nu_abrigo_id;
    private String SETfoto;
    private ProgressBar progressBarViewPhoto;
    private SharedPreferences.Editor editor;
    private SharedPreferences dados;
    long agora;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);

        inicializaComponentes();
        obterFoto();
    }

    @Override
    protected void onStart() {
        super.onStart();
        validaTempoLogin();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void inicializaComponentes() {
        nu_abrigo_id = (int) getIntent().getSerializableExtra("nu_abrigo_id");
        photoView = (PhotoView) findViewById(R.id.photo_view);
        progressBarViewPhoto = findViewById(R.id.progressBarViewPhoto);
        progressBarViewPhoto.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
        photoView.setVisibility(View.INVISIBLE);
    }

    public void obterFoto(){
        queue = Volley.newRequestQueue(this);
        String url = "http://192.168.25.218:3000/abrigo/filtrar/" + nu_abrigo_id;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                response -> {

                    try {
                        jsonArray = response.getJSONArray("abrigos");
                        if (jsonArray.length() > 0) {
                            try {
                                SETfoto = jsonArray.getJSONObject(0).getString("foto");
                                if (SETfoto != null) {
                                    byte[] decodedString = Base64.decode(SETfoto, Base64.DEFAULT);
                                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                    photoView.setImageBitmap(decodedByte);
                                    TimeUnit.SECONDS.sleep(3);
                                    progressBarViewPhoto.setVisibility(View.INVISIBLE);
                                    photoView.setVisibility(View.VISIBLE);
                                } else {
                                    photoView.setVisibility(View.VISIBLE);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle("Abrigos");
                            builder.setMessage("Não existe nenhum abrigo cadastrado");
                            builder.setCancelable(false);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(ViewPhotoActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }

                    } catch (JSONException e) {
                        Toast.makeText(
                                ViewPhotoActivity.this,
                                "Não possível carregar os dados!",
                                Toast.LENGTH_SHORT
                        ).show();
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Abrigo");
                        builder.setMessage("Erro ao listar, tente novamente");
                        builder.setCancelable(false);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(ViewPhotoActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                        e.printStackTrace();
                        queue.stop();
                    }
                }, error -> error.printStackTrace())  {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "bearer " + token);
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                return headers;
            }

            @Override
            public byte[] getBody() {
                return null;
            }

        };

        queue.add(request);
    }

    private void validaTempoLogin(){
        dados = getSharedPreferences("user", MODE_PRIVATE);
        int id = dados.getInt("id", -1);
        String name = dados.getString("nome", null);
        String email = dados.getString("email", null);
        int tempoinicial = dados.getInt("tempoInicial", -1);
        int tempoFinal = dados.getInt("tempoFinal", -1);
        token = dados.getString("token", null);

        Date hoje = new Date();
        agora = hoje.getTime() / 1000;

        if (id != -1 && name != null && email != null && tempoinicial!= -1 && tempoFinal != -1 && token != null) {
            if (tempoFinal < agora){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Login");
                builder.setMessage("Tempo expirado, por favor faça o login novamente!");
                builder.setCancelable(false);
                builder.setPositiveButton("OK", (dialog, which) -> {
                    Intent intent = new Intent(this, LoginActivity.class);
                    editor = getSharedPreferences("user", MODE_PRIVATE).edit();
                    editor.clear().apply();
                    startActivity(intent);
                    finish();
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }

}

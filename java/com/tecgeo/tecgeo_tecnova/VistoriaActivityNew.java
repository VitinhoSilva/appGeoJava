package com.tecgeo.tecgeo_tecnova;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class VistoriaActivityNew extends AppCompatActivity {

    private Double pontoSelecionadoNaActMapX, pontoSelecionadoNaActMapY;
    private Point pontoSelecionado;
    private EditText pontollNew, nomeLocallNew, descricaollNew;
    private TextInputLayout nomeLocalNew, descricaolNew, pontolNew;
    private ProgressBar progressBarLonaNEW;
    private RequestQueue queue;
    private String endereco, descricao;
    private Button SalvarLona;
    private int OBJECTID;
    private SharedPreferences.Editor editor;
    private SharedPreferences dados;
    long agora;
    private String token;
    private boolean destruir = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vistoria_new);

        inicializaComponentes();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nova Lona");
        builder.setMessage("Tem certeza que deseja voltar?");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", (dialog, which) -> {
            queue = Volley.newRequestQueue(this);
            String url = "http://192.168.25.218:3000/vistoria/deletarObjId/" + OBJECTID;
            StringRequest dr = new StringRequest(Request.Method.DELETE, url,
                    response -> {
                        Intent intent = new Intent(VistoriaActivityNew.this, VistoriaActivity.class);
                        startActivity(intent);
                        finish();
                        Log.d("---------> Response: ", response);
                    },
                    error -> {
                        Toast.makeText(
                                VistoriaActivityNew.this,
                                "Erro, tente novamente!",
                                Toast.LENGTH_LONG
                        ).show();

                        Log.d("---------> Error: ", error.toString());
                    })  {
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
            queue.add(dr);

        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            dialog.cancel();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        validaTempoLogin();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(destruir){
            deleteVistoria();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        destruir = true;
    }

    private void inicializaComponentes() {
        pontoSelecionadoNaActMapX = (Double) getIntent().getSerializableExtra("pontoSelecionadoX");
        pontoSelecionadoNaActMapY = (Double) getIntent().getSerializableExtra("pontoSelecionadoY");
        OBJECTID = (int) getIntent().getSerializableExtra("OBJECTID");

        pontollNew = findViewById(R.id.pontollNew);
        nomeLocallNew = findViewById(R.id.nomeLocallNew);
        descricaollNew = findViewById(R.id.descricaollNew);
        nomeLocalNew = findViewById(R.id.nomeLocalNew);
        descricaolNew = findViewById(R.id.descricaolNew);
        pontolNew = findViewById(R.id.pontolNew);
        progressBarLonaNEW = findViewById(R.id.progressBarLonaNEW);
        SalvarLona = findViewById(R.id.SalvarLona);
        progressBarLonaNEW.setVisibility(View.INVISIBLE);

        if (pontoSelecionadoNaActMapX != null && pontoSelecionadoNaActMapY != null) {
            pontoSelecionado = new Point(pontoSelecionadoNaActMapX, pontoSelecionadoNaActMapY, SpatialReference.create(31985));
            pontollNew.setText(pontoSelecionado.toString());
            pontollNew.setClickable(false);
            pontollNew.setAlpha(0.5f);
        }
    }

    public void buttonSalvarLona(View view) {
        endereco = nomeLocallNew.getText().toString().trim();
        descricao = descricaollNew.getText().toString().trim();

        if (endereco.isEmpty() || descricao.isEmpty()) {
            Toast.makeText(this,
                    "Preencha todos os campos corretamente!",
                    Toast.LENGTH_LONG).show();
        } else {
            SalvarLona.setVisibility(View.INVISIBLE);
            pontollNew.setVisibility(View.INVISIBLE);
            nomeLocallNew.setVisibility(View.INVISIBLE);
            descricaollNew.setVisibility(View.INVISIBLE);
            nomeLocalNew.setVisibility(View.INVISIBLE);
            descricaolNew.setVisibility(View.INVISIBLE);
            pontolNew.setVisibility(View.INVISIBLE);
            progressBarLonaNEW.setVisibility(View.VISIBLE);

            queue = Volley.newRequestQueue(this);
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("de_descricao", descricao);
                jsonObject.put("de_endereco", endereco);
            } catch (JSONException e) {
                e.getMessage();
            }

            String url = "http://192.168.25.218:3000/vistoria/atualizarObjId/" + OBJECTID;
            JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                    response -> {
                        SalvarLona.setVisibility(View.VISIBLE);
                        pontollNew.setVisibility(View.VISIBLE);
                        nomeLocallNew.setVisibility(View.VISIBLE);
                        descricaollNew.setVisibility(View.VISIBLE);
                        nomeLocalNew.setVisibility(View.VISIBLE);
                        descricaolNew.setVisibility(View.VISIBLE);
                        pontolNew.setVisibility(View.VISIBLE);
                        progressBarLonaNEW.setVisibility(View.INVISIBLE);

                        Toast.makeText(
                                VistoriaActivityNew.this,
                                "Erro, tente novamente!",
                                Toast.LENGTH_LONG
                        ).show();

                        Log.d("---------> Response: ", response.toString());
                    },
                    error -> {
                        Intent intent = new Intent(VistoriaActivityNew.this, VistoriaActivity.class);
                        startActivity(intent);
                        finish();

                        Toast.makeText(
                                VistoriaActivityNew.this,
                                "Adicionado com sucesso!",
                                Toast.LENGTH_LONG
                        ).show();

                        Log.d("---------> Error: ", error.toString());
                    }
            ) {

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

                    try {
                        Log.i("json", jsonObject.toString());
                        return jsonObject.toString().getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };

            queue.add(putRequest);

        }
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

    private void deleteVistoria(){
        queue = Volley.newRequestQueue(this);
        String url = "http://192.168.25.218:3000/vistoria/deletarObjId/" + OBJECTID;
        StringRequest dr = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    if (response.equals("Deletado com sucesso!")){
                        System.out.println("---------> Response: " +  response);
                        Intent intent = new Intent(VistoriaActivityNew.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                },
                error -> {
                    Toast.makeText(
                            VistoriaActivityNew.this,
                            "Erro, tente novamente!",
                            Toast.LENGTH_LONG
                    ).show();

                    Log.d("---------> Error: ", error.toString());
                })  {
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
        queue.add(dr);
    }
}

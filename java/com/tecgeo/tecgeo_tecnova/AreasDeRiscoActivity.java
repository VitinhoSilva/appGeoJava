package com.tecgeo.tecgeo_tecnova;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class AreasDeRiscoActivity extends AppCompatActivity {

    private RequestQueue queue;
    private Vector<String> titulos;
    private ListView listview;
    private JSONObject AreasRisco;
    private JSONArray jsonArray;
    private ArrayAdapter<String> adapter;
    private String ID = null;
    private SharedPreferences.Editor editor;
    private SharedPreferences dados;
    long agora;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.areas_de_risco_activity);

        inicializaComponentes();
    }

    @Override
    protected void onStart() {
        super.onStart();
        listview.setAdapter(null);
        obterDados();
        validaTempoLogin();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AreasDeRiscoActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void inicializaComponentes(){
        listview = findViewById(R.id.listViewPontosDeRisco);
        Toast.makeText(
                AreasDeRiscoActivity.this,
                "Carregando dados...",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void obterDados(){

        queue = Volley.newRequestQueue(this);
        String url = "http://192.168.25.218:3000/arearisco/listar";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        jsonArray = response.getJSONArray("AreasRisco");

                        if (jsonArray.length() > 0) {
                            Toast.makeText(
                                    AreasDeRiscoActivity.this,
                                    "Dados carregados!",
                                    Toast.LENGTH_SHORT
                            ).show();
                            titulos = new Vector<String>(jsonArray.length());
                            for (int i = 0; i < jsonArray.length(); i++) {
                                AreasRisco = jsonArray.getJSONObject(i);
                                titulos.add(AreasRisco.getString("de_local"));
                            }

                            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titulos);
                            listview.setAdapter(adapter);

                            listview.setOnItemClickListener((parent, view, position, id) -> {
                                try {
                                    ID = String.valueOf(((jsonArray.getJSONObject(position).getInt("nu_area_risco_id"))));

                                    Intent intent = new Intent(AreasDeRiscoActivity.this, MapDinamicActivity.class);
                                    intent.putExtra("ID", ID);
                                    intent.putExtra("AreasDeRiscoActivity", "true");
                                    startActivity(intent);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            });

                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle("Ponto de Risco");
                            builder.setMessage("Não existe nenhum ponto de risco cadastrado");
                            builder.setCancelable(false);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(AreasDeRiscoActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }

                    } catch (JSONException e) {
                        Toast.makeText(
                                AreasDeRiscoActivity.this,
                                "Não possível carregar os dados!",
                                Toast.LENGTH_SHORT
                        ).show();
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Ponto de Risco");
                        builder.setMessage("Erro ao listar, tente novamente");
                        builder.setCancelable(false);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(AreasDeRiscoActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                        e.printStackTrace();
                        queue.stop();
                    }
                }, error -> error.printStackTrace()) {
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
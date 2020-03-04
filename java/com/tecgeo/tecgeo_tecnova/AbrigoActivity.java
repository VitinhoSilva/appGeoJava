package com.tecgeo.tecgeo_tecnova;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
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

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class AbrigoActivity extends AppCompatActivity implements Serializable {

    private RequestQueue queue;
    private Vector<String> titulos;
    private ListView listview;
    private ArrayAdapter<String> adapter;
    private JSONObject titleAbrigos;
    private JSONArray jsonArray;
    private JSONArray AbrigoPoint;
    private String SHAPE;
    private String de_nome;
    private String de_endereco;
    private int nu_capacidade;
    private int nu_ocupacao;
    private int nu_abrigo_id;
    private double x, y;
    int id, tempoinicial, tempoFinal;
    private String name, email, token;
    private SharedPreferences.Editor editor;
    private SharedPreferences dados;
    long agora;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abrigo);

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
        Intent intent = new Intent(AbrigoActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void inicializaComponentes(){
        listview = findViewById(R.id.listViewAbrigos);
        Toast.makeText(
                AbrigoActivity.this,
                "Carregando dados...",
                Toast.LENGTH_SHORT
        ).show();
        dados = getSharedPreferences("user", MODE_PRIVATE);
    }

    public void obterDados(){
        queue = Volley.newRequestQueue(this);
        String url = "http://192.168.25.218:3000/abrigo/listar";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {

                    try {
                        jsonArray = response.getJSONArray("abrigos");

                        if (jsonArray.length() > 0) {
                            Toast.makeText(
                                    AbrigoActivity.this,
                                    "Dados carregados!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            titulos = new Vector<String>(jsonArray.length());
                            for (int i = 0; i < jsonArray.length(); i++) {
                                titleAbrigos = jsonArray.getJSONObject(i);
                                titulos.add(titleAbrigos.getString("de_nome"));
                            }

                            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titulos);
                            listview.setAdapter(adapter);

                            listview.setOnItemClickListener((parent, view, position, id) -> {

                                try {

                                    SHAPE = jsonArray.getJSONObject(position).getString("SHAPE");

                                    if (SHAPE.equals("null")) {
                                        Toast.makeText(
                                                AbrigoActivity.this,
                                                "Ponto sem coordenada!",
                                                Toast.LENGTH_LONG
                                        ).show();

                                    } else {
                                        AbrigoPoint = ((jsonArray.getJSONObject(position).getJSONObject("SHAPE").getJSONArray("points")));
                                        for (int i = 0; i < AbrigoPoint.length(); i++) {
                                            JSONObject latlong = AbrigoPoint.getJSONObject(i);
                                            x = latlong.getDouble("x");
                                            y = latlong.getDouble("y");
                                        }

                                        de_nome = jsonArray.getJSONObject(position).getString("de_nome");
                                        de_endereco = jsonArray.getJSONObject(position).getString("de_endereco");
                                        nu_capacidade = jsonArray.getJSONObject(position).getInt("nu_capacidade");
                                        nu_ocupacao = jsonArray.getJSONObject(position).getInt("nu_ocupacao");
                                        nu_abrigo_id = jsonArray.getJSONObject(position).getInt("nu_abrigo_id");

                                        Intent intent = new Intent(AbrigoActivity.this, AbrigoActivityUp.class);
                                        intent.putExtra("pontoXClicadoUP", String.valueOf(x));
                                        intent.putExtra("pontoYClicadoUP", String.valueOf(y));
                                        intent.putExtra("de_nome", de_nome);
                                        intent.putExtra("de_endereco", de_endereco);
                                        intent.putExtra("nu_capacidade", nu_capacidade);
                                        intent.putExtra("nu_ocupacao", nu_ocupacao);
                                        intent.putExtra("nu_abrigo_id", nu_abrigo_id);
                                        startActivity(intent);
                                        finish();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            });

                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle("Abrigos");
                            builder.setMessage("Não existe nenhum abrigo cadastrado");
                            builder.setCancelable(false);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(AbrigoActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }

                    } catch (JSONException e) {
                        Toast.makeText(
                                AbrigoActivity.this,
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
                                Intent intent = new Intent(AbrigoActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                        e.printStackTrace();
                        queue.stop();
                    }
                }, error -> {
            error.printStackTrace();
        }) {
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

    public void buttonAddAbrigo(View view) {
        Intent intent = new Intent(AbrigoActivity.this, MapDinamicEditAcitivity.class);
        intent.putExtra("AbrigoActivity", "true");
        startActivity(intent);
        finish();
    }

    private void validaTempoLogin(){
        id = dados.getInt("id", -1);
        name = dados.getString("nome", null);
        email = dados.getString("email", null);
        tempoinicial = dados.getInt("tempoInicial", -1);
        tempoFinal = dados.getInt("tempoFinal", -1);
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
                    Intent intent = new Intent(AbrigoActivity.this, LoginActivity.class);
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


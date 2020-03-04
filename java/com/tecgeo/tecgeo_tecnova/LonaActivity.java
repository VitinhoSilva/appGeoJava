package com.tecgeo.tecgeo_tecnova;

import android.app.AlertDialog;
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class LonaActivity extends AppCompatActivity {

    private RequestQueue queue;
    private Vector<String> titulos;
    private ListView listview;
    private ArrayAdapter<String> adapter;
    private JSONObject titleLonas;
    private JSONArray jsonArray;
    private JSONArray LonaPoint;
    private String SHAPE;
    private double x, y;
    private int id_solicitacao_lona;
    private SharedPreferences.Editor editor;
    private SharedPreferences dados;
    long agora;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lona);

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
    public void onBackPressed() {
        Intent intent = new Intent(LonaActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void inicializaComponentes() {
        listview = findViewById(R.id.listViewLonas);
        Toast.makeText(
                LonaActivity.this,
                "Carregando dados...",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void obterDados() {
        queue = Volley.newRequestQueue(this);
        String url = "http://192.168.25.218:3000/lona/listar";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {

                    try {
                        jsonArray = response.getJSONArray("Lonas");

                        if (jsonArray.length() > 0) {
                            Toast.makeText(
                                    LonaActivity.this,
                                    "Dados carregados!",
                                    Toast.LENGTH_SHORT
                            ).show();
                            titulos = new Vector<String>(jsonArray.length());
                            for (int i = 0; i < jsonArray.length(); i++) {
                                titleLonas = jsonArray.getJSONObject(i);
                                titulos.add(titleLonas.getString("de_endereco"));
                            }

                            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titulos);
                            listview.setAdapter(adapter);

                            listview.setOnItemClickListener((parent, view, position, id) -> {

                                try {

                                    SHAPE = jsonArray.getJSONObject(position).getString("SHAPE");

                                    if (SHAPE.equals("null")) {
                                        Toast.makeText(
                                                LonaActivity.this,
                                                "Ponto sem coordenada!",
                                                Toast.LENGTH_LONG
                                        ).show();

                                    } else {
                                        LonaPoint = ((jsonArray.getJSONObject(position).getJSONObject("SHAPE").getJSONArray("points")));
                                        for (int i = 0; i < LonaPoint.length(); i++) {
                                            JSONObject latlong = LonaPoint.getJSONObject(i);
                                            x = latlong.getDouble("x");
                                            y = latlong.getDouble("y");
                                        }

                                        id_solicitacao_lona = jsonArray.getJSONObject(position).getInt("id_solicitacao_lona");
                                        String de_endereco = jsonArray.getJSONObject(position).getString("de_endereco");
                                        String de_descricao = jsonArray.getJSONObject(position).getString("de_descricao");
                                        String dt_data = jsonArray.getJSONObject(position).getString("dt_data");

                                        Intent intent = new Intent(LonaActivity.this, LonaActivityUp.class);
                                        intent.putExtra("pontoXClicadoUP", String.valueOf(x));
                                        intent.putExtra("pontoYClicadoUP", String.valueOf(y));
                                        intent.putExtra("id_solicitacao_lona", id_solicitacao_lona);
                                        intent.putExtra("de_endereco", de_endereco);
                                        intent.putExtra("de_descricao", de_descricao);
                                        intent.putExtra("dt_data", dt_data);
                                        startActivity(intent);
                                        finish();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            });

                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle("Lonas");
                            builder.setMessage("Não existe nenhuma lona cadastrada");
                            builder.setCancelable(false);
                            builder.setPositiveButton("OK", (dialog, which) -> {
                                Intent intent = new Intent(LonaActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            });
                        }

                    } catch (JSONException e) {
                        Toast.makeText(
                                LonaActivity.this,
                                "Não possível carregar os dados!",
                                Toast.LENGTH_SHORT
                        ).show();
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Lonas");
                        builder.setMessage("Erro ao listar, tente novamente");
                        builder.setCancelable(false);
                        builder.setPositiveButton("OK", (dialog, which) -> {
                            Intent intent = new Intent(LonaActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        });
                        e.printStackTrace();
                        queue.stop();
                    }
                }, error -> error.printStackTrace() ) {
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

    public void buttonAddLona(View view) {
        Intent intent = new Intent(LonaActivity.this, MapDinamicEditAcitivity.class);
        intent.putExtra("LonaActivity", "true");
        startActivity(intent);
        finish();
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

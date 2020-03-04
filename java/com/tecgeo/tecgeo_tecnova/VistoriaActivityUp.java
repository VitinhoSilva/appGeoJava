package com.tecgeo.tecgeo_tecnova;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class VistoriaActivityUp extends AppCompatActivity {

    private TextInputEditText locall, dataa, pontoll, descricaoll;
    private TextInputLayout local, data, pontol, descricaol;
    private Button UpLona, DeleteLona, VizualizarLona;
    private ProgressBar progressBarLonaUp;
    private RequestQueue queue;
    private String Slocal, Sdata;
    private String pontoLonaX;
    private String pontoLonaY;
    private String SETlocal, SETdata, SETdescricao;
    private int id_solicitacao_lona;
    private Point point;
    private SharedPreferences.Editor editor;
    private SharedPreferences dados;
    long agora;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vistoria_up);

        inicializaComponentes();
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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(VistoriaActivityUp.this, VistoriaActivity.class);
        startActivity(intent);
        finish();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void inicializaComponentes() {
        locall = findViewById(R.id.locall);
        local = findViewById(R.id.local);
        dataa = findViewById(R.id.dataa);
        data = findViewById(R.id.data);
        descricaol = findViewById(R.id.descricaol);
        descricaoll = findViewById(R.id.descricaoll);
        pontoll = findViewById(R.id.pontoll);
        pontol = findViewById(R.id.pontol);
        UpLona = findViewById(R.id.UpLona);
        DeleteLona = findViewById(R.id.DeleteLona);
        VizualizarLona = findViewById(R.id.VizualizarLona);

        progressBarLonaUp = findViewById(R.id.progressBarLonaUp);

        progressBarLonaUp.setVisibility(View.INVISIBLE);

        pontoLonaX = (String) getIntent().getSerializableExtra("pontoXClicadoUP");
        pontoLonaY = (String) getIntent().getSerializableExtra("pontoYClicadoUP");
        id_solicitacao_lona = (int) getIntent().getSerializableExtra("id_solicitacao_lona");
        SETlocal = (String) getIntent().getSerializableExtra("de_endereco");
        SETdescricao = (String) getIntent().getSerializableExtra("de_descricao");
        SETdata = (String) getIntent().getSerializableExtra("dt_data");

        String dataRecebida = SETdata;
        dataRecebida = dataRecebida.substring(0,10);

        try {
            DateFormat formatUS = new SimpleDateFormat("yyyy-mm-dd");
            Date date = formatUS.parse(dataRecebida);
            DateFormat formatBR = new SimpleDateFormat("dd/mm/yyyy");
            String dateFormated = formatBR.format(date);
            dataa.setText(dateFormated);
            locall.setText(SETlocal);
            descricaoll.setText(SETdescricao);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (pontoLonaX != null && pontoLonaY != null) {
            double x = Double.parseDouble(pontoLonaX);
            double y = Double.parseDouble(pontoLonaY);
            point = new Point(x, y, SpatialReference.create(31985));
            pontoll.setText(point.toString());
        }

    }

    public void buttonAtualizarLona(View view) {
        Slocal = locall.getText().toString().trim();
        SETdescricao = descricaoll.getText().toString().trim();

        if (Slocal.isEmpty() || SETdescricao.isEmpty() ) {
            Toast.makeText(this,
                    "Preencha todos os campos corretamente!",
                    Toast.LENGTH_LONG).show();
        } else {
            UpLona.setVisibility(View.INVISIBLE);
            VizualizarLona.setVisibility(View.INVISIBLE);
            DeleteLona.setVisibility(View.INVISIBLE);
            local.setVisibility(View.INVISIBLE);
            locall.setVisibility(View.INVISIBLE);
            data.setVisibility(View.INVISIBLE);
            dataa.setVisibility(View.INVISIBLE);
            descricaol.setVisibility(View.INVISIBLE);
            descricaoll.setVisibility(View.INVISIBLE);
            pontol.setVisibility(View.INVISIBLE);
            pontoll.setVisibility(View.INVISIBLE);
            progressBarLonaUp.setVisibility(View.VISIBLE);

            queue = Volley.newRequestQueue(this);

            final JSONObject jsonObject = new JSONObject();
            try {
                //jsonObject.put("dt_data", dt_data);
                jsonObject.put("de_endereco", Slocal);
                jsonObject.put("de_descricao", SETdescricao);
            } catch (JSONException e) {
                e.getMessage();
            }

            String url = "http://192.168.25.218:3000/vistoria/atualizar/" + id_solicitacao_lona;
            JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                    response -> {
                        UpLona.setVisibility(View.VISIBLE);
                        VizualizarLona.setVisibility(View.VISIBLE);
                        DeleteLona.setVisibility(View.VISIBLE);
                        descricaol.setVisibility(View.VISIBLE);
                        descricaoll.setVisibility(View.VISIBLE);
                        local.setVisibility(View.VISIBLE);
                        locall.setVisibility(View.VISIBLE);
                        data.setVisibility(View.VISIBLE);
                        dataa.setVisibility(View.VISIBLE);
                        pontol.setVisibility(View.VISIBLE);
                        pontoll.setVisibility(View.VISIBLE);
                        progressBarLonaUp.setVisibility(View.INVISIBLE);

                        Toast.makeText(
                                VistoriaActivityUp.this,
                                "Erro, tente novamente!",
                                Toast.LENGTH_LONG
                        ).show();

                        Log.d("---------> Response: ", response.toString());
                    },
                    error -> {
                        Intent intent = new Intent(VistoriaActivityUp.this, VistoriaActivity.class);
                        startActivity(intent);
                        finish();

                        Toast.makeText(
                                VistoriaActivityUp.this,
                                "Atualizado com sucesso!",
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

    public void buttonDeletaLona(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Deletar Lona");
        builder.setMessage("Tem certeza que deseja excluir?");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", (dialog, which) -> {
            UpLona.setVisibility(View.INVISIBLE);
            VizualizarLona.setVisibility(View.INVISIBLE);
            DeleteLona.setVisibility(View.INVISIBLE);
            local.setVisibility(View.INVISIBLE);
            descricaol.setVisibility(View.INVISIBLE);
            descricaoll.setVisibility(View.INVISIBLE);
            locall.setVisibility(View.INVISIBLE);
            data.setVisibility(View.INVISIBLE);
            dataa.setVisibility(View.INVISIBLE);
            pontol.setVisibility(View.INVISIBLE);
            pontoll.setVisibility(View.INVISIBLE);
            progressBarLonaUp.setVisibility(View.VISIBLE);


            queue = Volley.newRequestQueue(this);

            String url = "http://192.168.25.218:3000/vistoria/deletar/" + id_solicitacao_lona;
            StringRequest dr = new StringRequest(Request.Method.DELETE, url,
                    response -> {
                        Intent intent = new Intent(VistoriaActivityUp.this, VistoriaActivity.class);
                        startActivity(intent);
                        finish();

                        Toast.makeText(
                                VistoriaActivityUp.this,
                                "Deletado com sucesso!",
                                Toast.LENGTH_LONG
                        ).show();

                        Log.d("---------> response: ", response);

                    },
                    error -> {
                        Toast.makeText(
                                VistoriaActivityUp.this,
                                "Erro, tente novamente!",
                                Toast.LENGTH_LONG
                        ).show();

                        UpLona.setVisibility(View.VISIBLE);
                        VizualizarLona.setVisibility(View.VISIBLE);
                        DeleteLona.setVisibility(View.VISIBLE);
                        local.setVisibility(View.VISIBLE);
                        descricaol.setVisibility(View.VISIBLE);
                        descricaoll.setVisibility(View.VISIBLE);
                        locall.setVisibility(View.VISIBLE);
                        data.setVisibility(View.VISIBLE);
                        dataa.setVisibility(View.VISIBLE);
                        pontol.setVisibility(View.VISIBLE);
                        pontoll.setVisibility(View.VISIBLE);
                        progressBarLonaUp.setVisibility(View.INVISIBLE);

                        Log.d("---------> Error: ", error.toString());
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
            queue.add(dr);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            // ->
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void buttonVizualizarLona(View view) {
        if (pontoLonaX != null && pontoLonaY != null) {
                if (point != null) {
                    Intent intent = new Intent(VistoriaActivityUp.this, MapDinamicActivity.class);
                    intent.putExtra("pontoXClicadoUP", point.getX());
                    intent.putExtra("pontoYClicadoUP", point.getY());
                    intent.putExtra("VistoriaActivityUp", "true");
                    startActivity(intent);
                    finish();
                }
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
}

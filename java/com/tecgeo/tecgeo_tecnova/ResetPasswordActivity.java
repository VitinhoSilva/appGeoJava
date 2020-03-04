package com.tecgeo.tecgeo_tecnova;

import android.app.AlertDialog;
import android.content.Intent;
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
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputLayout emailReset;
    private Button buttonReset;
    private TextInputEditText emailReset1;
    private String email;
    private ProgressBar progressReset;
    private RequestQueue queue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        inicializaComponentes();
    }

    @Override
    protected void onStart() {
        super.onStart();
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
        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void inicializaComponentes() {
        emailReset = findViewById(R.id.emailReset);
        buttonReset = findViewById(R.id.buttonReset);
        emailReset1 = findViewById(R.id.emailReset1);
        progressReset = findViewById(R.id.progressReset);
        progressReset.setVisibility(View.INVISIBLE);
    }

    public void buttonReset(View view) {
        email = emailReset1.getText().toString().trim();

        if(email.isEmpty()){
            Toast.makeText(this,
                    "Preencha o email corretamente!",
                    Toast.LENGTH_LONG).show();
        } else {
            emailReset.setVisibility(View.INVISIBLE);
            buttonReset.setVisibility(View.INVISIBLE);
            emailReset1.setVisibility(View.INVISIBLE);
            progressReset.setVisibility(View.VISIBLE);

            queue = Volley.newRequestQueue(this);
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("email", email);
            } catch (JSONException e) {
                e.getMessage();
            }

            String url = "http://192.168.25.218:3000/reset";
            JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                    response -> {
                        try {
                            if (String.valueOf(response.get("response")).equals("Usuário não encontrado!")){
                                emailReset.setVisibility(View.VISIBLE);
                                buttonReset.setVisibility(View.VISIBLE);
                                emailReset1.setVisibility(View.VISIBLE);
                                progressReset.setVisibility(View.INVISIBLE);
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder.setTitle("Usuário não cadastrado");
                                builder.setMessage("O usuário " + email + ", não está cadastrado!");
                                builder.setCancelable(false);
                                builder.setPositiveButton("OK", (dialog, which) -> {
                                   dialog.dismiss();
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            } else if (String.valueOf(response.get("response")).equals("Erro, tente novamente!")){
                                emailReset.setVisibility(View.VISIBLE);
                                buttonReset.setVisibility(View.VISIBLE);
                                emailReset1.setVisibility(View.VISIBLE);
                                progressReset.setVisibility(View.INVISIBLE);

                                Toast.makeText(
                                        ResetPasswordActivity.this,
                                        "Erro, tente novamente!",
                                        Toast.LENGTH_LONG
                                ).show();
                            } else if(String.valueOf(response.get("response")).equals("E-mail enviado com sucesso!")){
                                emailReset.setVisibility(View.VISIBLE);
                                buttonReset.setVisibility(View.VISIBLE);
                                emailReset1.setVisibility(View.VISIBLE);
                                progressReset.setVisibility(View.INVISIBLE);
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder.setTitle("E-mail enviado");
                                builder.setMessage("Email enviado para " + email + ", verifique seu e-mail.");
                                builder.setCancelable(false);
                                builder.setPositiveButton("OK", (dialog, which) -> {
                                    Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        emailReset.setVisibility(View.VISIBLE);
                        buttonReset.setVisibility(View.VISIBLE);
                        emailReset1.setVisibility(View.VISIBLE);
                        progressReset.setVisibility(View.INVISIBLE);

                        Log.d("---------> Error: ", error.toString());

                        Toast.makeText(
                                ResetPasswordActivity.this,
                                "Erro, tente novamente!",
                                Toast.LENGTH_LONG
                        ).show();
                    }
            ) {

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
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
}

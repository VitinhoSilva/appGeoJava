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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CadastroLoginActivity extends AppCompatActivity {

    private TextInputLayout emailCadastrar, nomeCadastrar, senhaCadastrar, senhaConfirmeCadastrar;
    private TextInputEditText emailCadastrar1, nomeCadastrar1, senhaCadastrar1, senhaConfirmeCadastrar1;
    private Button buttonCadastrar;
    private ProgressBar progressCadastrar;
    private String email, nome, senha, confirmeSenha;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_login);
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
        Intent intent = new Intent(CadastroLoginActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void inicializaComponentes() {
        emailCadastrar = findViewById(R.id.emailCadastrar);
        nomeCadastrar = findViewById(R.id.nomeCadastrar);
        senhaCadastrar = findViewById(R.id.senhaCadastrar);
        senhaConfirmeCadastrar = findViewById(R.id.senhaConfirmeCadastrar);
        emailCadastrar1 = findViewById(R.id.emailCadastrar1);
        nomeCadastrar1 = findViewById(R.id.nomeCadastrar1);
        senhaCadastrar1 = findViewById(R.id.senhaCadastrar1);
        senhaConfirmeCadastrar1 = findViewById(R.id.senhaConfirmeCadastrar1);
        buttonCadastrar  = findViewById(R.id.buttonCadastrar);
        progressCadastrar = findViewById(R.id.progressCadastrar);
        progressCadastrar.setVisibility(View.INVISIBLE);
    }

    public void buttonCadastrar(View view) {
        email = emailCadastrar1.getText().toString().trim();
        nome = nomeCadastrar1.getText().toString().trim();
        senha = senhaCadastrar1.getText().toString().trim();
        confirmeSenha = senhaConfirmeCadastrar1.getText().toString().trim();

        if(email.isEmpty() || nome.isEmpty() || senha.isEmpty() || confirmeSenha.isEmpty()){
            Toast.makeText(this,
                    "Preencha todos os campos corretamente!",
                    Toast.LENGTH_LONG).show();
        } else if (!validarEmail(email)){
            Toast.makeText(this,
                    "E-mai inválido!",
                    Toast.LENGTH_LONG).show();
        } else if (!senha.equals(confirmeSenha)) {
            Toast.makeText(this,
                    "Senhas diferentes, insira as senhas corretamente!",
                    Toast.LENGTH_LONG).show();
            senhaCadastrar1.setText("");
            senhaConfirmeCadastrar1.setText("");
        } else if (!senhaForte(confirmeSenha)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Regras de senha");
            builder.setMessage(
                    "1 - Mais de 6 caracteres \n" +
                    "2 - Letras maiúsculas e minúsculas \n" +
                    "3 - Números \n" +
                    "4 - Caracteres especiais: @!-_#$%&");
            builder.setCancelable(false);
            builder.setPositiveButton("OK", (dialog, which) -> {
                dialog.dismiss();
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            senhaCadastrar1.setText("");
            senhaConfirmeCadastrar1.setText("");
        } else {
            emailCadastrar.setVisibility(View.INVISIBLE);
            nomeCadastrar.setVisibility(View.INVISIBLE);
            senhaCadastrar.setVisibility(View.INVISIBLE);
            senhaConfirmeCadastrar.setVisibility(View.INVISIBLE);
            emailCadastrar1.setVisibility(View.INVISIBLE);
            nomeCadastrar1.setVisibility(View.INVISIBLE);
            senhaCadastrar1.setVisibility(View.INVISIBLE);
            senhaConfirmeCadastrar1.setVisibility(View.INVISIBLE);
            buttonCadastrar.setVisibility(View.INVISIBLE);
            progressCadastrar.setVisibility(View.VISIBLE);

            queue = Volley.newRequestQueue(this);

            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("name", nome);
                jsonObject.put("email", email);
                jsonObject.put("password", confirmeSenha);
            } catch (JSONException e) {
                e.getMessage();
            }

            String url = "http://192.168.25.218:3000/singup";
            JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                    response -> {
                        try {
                            if (String.valueOf(response.get("response")).equals("Usuário já cadastrado!")){
                                emailCadastrar.setVisibility(View.VISIBLE);
                                nomeCadastrar.setVisibility(View.VISIBLE);
                                senhaCadastrar.setVisibility(View.VISIBLE);
                                senhaConfirmeCadastrar.setVisibility(View.VISIBLE);
                                emailCadastrar1.setVisibility(View.VISIBLE);
                                nomeCadastrar1.setVisibility(View.VISIBLE);
                                senhaCadastrar1.setVisibility(View.VISIBLE);
                                senhaConfirmeCadastrar1.setVisibility(View.VISIBLE);
                                buttonCadastrar.setVisibility(View.VISIBLE);
                                progressCadastrar.setVisibility(View.INVISIBLE);
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder.setTitle("Usuário já cadastrado!");
                                builder.setMessage("Usuário " + email + ", já está cadastrado!");
                                builder.setCancelable(false);
                                builder.setPositiveButton("OK", (dialog, which) -> {
                                    Intent intent = new Intent(CadastroLoginActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            } else if (String.valueOf(response.get("response")).equals("Usuário cadastrado com sucesso!")) {
                                emailCadastrar.setVisibility(View.VISIBLE);
                                nomeCadastrar.setVisibility(View.VISIBLE);
                                senhaCadastrar.setVisibility(View.VISIBLE);
                                senhaConfirmeCadastrar.setVisibility(View.VISIBLE);
                                emailCadastrar1.setVisibility(View.VISIBLE);
                                nomeCadastrar1.setVisibility(View.VISIBLE);
                                senhaCadastrar1.setVisibility(View.VISIBLE);
                                senhaConfirmeCadastrar1.setVisibility(View.VISIBLE);
                                buttonCadastrar.setVisibility(View.VISIBLE);
                                progressCadastrar.setVisibility(View.INVISIBLE);
                                emailCadastrar1.setText("");
                                nomeCadastrar1.setText("");
                                senhaCadastrar1.setText("");
                                senhaConfirmeCadastrar1.setText("");
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder.setTitle("Sucesso");
                                builder.setMessage("Você foi cadastrado com sucesso, faça o login para continuar");
                                builder.setCancelable(false);
                                builder.setPositiveButton("OK", (dialog, which) -> {
                                    Intent intent = new Intent(CadastroLoginActivity.this, LoginActivity.class);
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
                        emailCadastrar.setVisibility(View.VISIBLE);
                        nomeCadastrar.setVisibility(View.VISIBLE);
                        senhaCadastrar.setVisibility(View.VISIBLE);
                        senhaConfirmeCadastrar.setVisibility(View.VISIBLE);
                        emailCadastrar1.setVisibility(View.VISIBLE);
                        nomeCadastrar1.setVisibility(View.VISIBLE);
                        senhaCadastrar1.setVisibility(View.VISIBLE);
                        senhaConfirmeCadastrar1.setVisibility(View.VISIBLE);
                        buttonCadastrar.setVisibility(View.VISIBLE);
                        progressCadastrar.setVisibility(View.INVISIBLE);

                        Log.d("---------> Error: ", error.toString());

                        Toast.makeText(
                                CadastroLoginActivity.this,
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

    public static boolean validarEmail(String email) {
        boolean isEmailIdValid = false;
        if (email != null && email.length() > 0) {
            String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
            Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(email);
            if (matcher.matches()) {
                isEmailIdValid = true;
            }
        }
        return isEmailIdValid;
    }

    public static boolean senhaForte(String senha) {
        if (senha.length() < 6) {
            return false;
        } else {
            boolean achouNumero = false;
            boolean achouMaiuscula = false;
            boolean achouMinuscula = false;
            boolean achouSimbolo = false;

            for (char c : senha.toCharArray()) {
                if (c >= '0' && c <= '9') {
                    achouNumero = true;
                } else if (c >= 'A' && c <= 'Z') {
                    achouMaiuscula = true;
                } else if (c >= 'a' && c <= 'z') {
                    achouMinuscula = true;
                } else {
                    achouSimbolo = true;
                }
            }

            if (achouNumero && achouMaiuscula && achouMinuscula && achouSimbolo) {
                return true;
            }
        }

        return false;
    }
}

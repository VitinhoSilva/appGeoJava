package com.tecgeo.tecgeo_tecnova;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout emailLogin, senhaLogin;
    private Button buttonLogin;
    private TextInputEditText emailLogin1, senhaLogin1;
    private String email, senha;
    private RequestQueue queue;
    private ProgressBar progressLogin;
    private String[] permissoes = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private int id;
    private String nome;
    private String emaill;
    private int tempoInicial;
    private int tempoFinal;
    private String token;
    private SharedPreferences.Editor editor;
    private SharedPreferences dados;
    long agora;
    private TextView textViewCadastrar, textViewReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        inicializaComponentes();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Permissoes.validarPermissoes(permissoes, LoginActivity.this, 1);

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
                 Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                 Toast.makeText(
                         LoginActivity.this,
                         "Bem vindo!",
                         Toast.LENGTH_LONG
                 ).show();
                 startActivity(intent);
                 finish();
          }
   }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void inicializaComponentes() {
        emailLogin = findViewById(R.id.emailLogin);
        senhaLogin = findViewById(R.id.senhaLogin);
        buttonLogin = findViewById(R.id.buttonLogin);
        emailLogin1 = findViewById(R.id.emailLogin1);
        senhaLogin1 = findViewById(R.id.senhaLogin1);
        progressLogin = findViewById(R.id.progressLogin);
        progressLogin.setVisibility(View.INVISIBLE);
        textViewCadastrar = findViewById(R.id.textViewCadastrar);
        textViewReset = findViewById(R.id.textViewReset);
        Permissoes.validarPermissoes(permissoes, LoginActivity.this, 1);
    }

    public void buttonLogin(View view) {
        email = emailLogin1.getText().toString().trim();
        senha = senhaLogin1.getText().toString().trim();

        if ( email.isEmpty() || senha.isEmpty() ) {
            Toast.makeText(this,
                    "Preencha todos os campos corretamente!",
                    Toast.LENGTH_LONG).show();
        } else {
            emailLogin.setVisibility(View.INVISIBLE);
            senhaLogin.setVisibility(View.INVISIBLE);
            buttonLogin.setVisibility(View.INVISIBLE);
            emailLogin1.setVisibility(View.INVISIBLE);
            senhaLogin1.setVisibility(View.INVISIBLE);
            textViewCadastrar.setVisibility(View.INVISIBLE);
            textViewReset.setVisibility(View.INVISIBLE);
            progressLogin.setVisibility(View.VISIBLE);

            queue = Volley.newRequestQueue(this);

            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("email", email);
                jsonObject.put("password", senha);
            } catch (JSONException e) {
                e.getMessage();
            }

            String url = "http://192.168.25.218:3000/singin";
            JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                    response -> {
                        emailLogin.setVisibility(View.INVISIBLE);
                        senhaLogin.setVisibility(View.INVISIBLE);
                        buttonLogin.setVisibility(View.INVISIBLE);
                        emailLogin1.setVisibility(View.INVISIBLE);
                        senhaLogin1.setVisibility(View.INVISIBLE);
                        textViewCadastrar.setVisibility(View.INVISIBLE);
                        textViewReset.setVisibility(View.INVISIBLE);
                        progressLogin.setVisibility(View.VISIBLE);

                        try {
                            if (String.valueOf(response.get("ERRO")).equals("Email/Senha inválidos!")){
                                emailLogin.setVisibility(View.VISIBLE);
                                senhaLogin.setVisibility(View.VISIBLE);
                                buttonLogin.setVisibility(View.VISIBLE);
                                emailLogin1.setVisibility(View.VISIBLE);
                                senhaLogin1.setVisibility(View.VISIBLE);
                                textViewCadastrar.setVisibility(View.VISIBLE);
                                textViewReset.setVisibility(View.VISIBLE);
                                progressLogin.setVisibility(View.INVISIBLE);
                                Toast.makeText(
                                        LoginActivity.this,
                                        "Email/Senha inválidos!",
                                        Toast.LENGTH_LONG
                                ).show();
                                //Log.d("---------> response: ", response.toString());
                            } else if ((String.valueOf(response.get("ERRO")).equals("Usuário não encontrado!"))){
                                emailLogin.setVisibility(View.VISIBLE);
                                senhaLogin.setVisibility(View.VISIBLE);
                                buttonLogin.setVisibility(View.VISIBLE);
                                emailLogin1.setVisibility(View.VISIBLE);
                                senhaLogin1.setVisibility(View.VISIBLE);
                                textViewCadastrar.setVisibility(View.VISIBLE);
                                textViewReset.setVisibility(View.VISIBLE);
                                progressLogin.setVisibility(View.INVISIBLE);
                                Toast.makeText(
                                        LoginActivity.this,
                                        "Usuário não encontrado!",
                                        Toast.LENGTH_LONG
                                ).show();
                                //Log.d("---------> response: ", response.toString());
                            }
                        } catch (JSONException e) {
                            Toast.makeText(
                                    LoginActivity.this,
                                    "Bem vindo!",
                                    Toast.LENGTH_LONG
                            ).show();

                            //Log.d("---------> Dados", String.valueOf(response));
                            try {
                                id = (int) response.get("id");
                                nome = (String) response.get("name");
                                emaill = (String) response.get("email");
                                tempoInicial = (int) response.get("iat");
                                tempoFinal = (int) response.get("exp");
                                token = (String) response.get("token");

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra("LoginActivity", "LoginActivity");
                                intent.putExtra("id", id);
                                intent.putExtra("nome", nome);
                                intent.putExtra("email", emaill);
                                intent.putExtra("tempoInicial", tempoInicial);
                                intent.putExtra("tempoFinal", tempoFinal);
                                intent.putExtra("token", token);
                                intent.putExtra("senha", senha);
                                startActivity(intent);
                                finish();
                            } catch (JSONException ex) {
                                ex.printStackTrace();
                            }
                        }
                    },
                    error -> {
                        emailLogin.setVisibility(View.VISIBLE);
                        senhaLogin.setVisibility(View.VISIBLE);
                        buttonLogin.setVisibility(View.VISIBLE);
                        emailLogin1.setVisibility(View.VISIBLE);
                        senhaLogin1.setVisibility(View.VISIBLE);
                        textViewCadastrar.setVisibility(View.VISIBLE);
                        textViewReset.setVisibility(View.VISIBLE);
                        progressLogin.setVisibility(View.INVISIBLE);

                        Log.d("---------> Error: ", error.toString());

                        Toast.makeText(
                                LoginActivity.this,
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for( int permissaoResultado : grantResults ){
            if( permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao(){
        View view = LayoutInflater.from(LoginActivity.this).inflate(R.layout.permissao, null);
        androidx.appcompat.app.AlertDialog.Builder alertDialog = new androidx.appcompat.app.AlertDialog.Builder(this);

        TextView title = (TextView) view.findViewById(R.id.titleP);
        ImageButton imageButton = (ImageButton) view.findViewById(R.id.imageP);
        title.setText("Permissão");
        imageButton.setImageResource(R.mipmap.permissao);

        alertDialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertDialog.setView(view);
        alertDialog.show();

    }

    public void abrirTelaCadastro(View view) {
        Intent intent = new Intent(LoginActivity.this, CadastroLoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void abrirTelaReset(View view) {
        Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
        startActivity(intent);
        finish();
    }
}

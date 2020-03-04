package com.tecgeo.tecgeo_tecnova;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AbrigoActivityUp extends AppCompatActivity {

    private TextInputEditText de_nome, de_endereco, nu_capacidade, nu_ocupacao, ponto;
    private TextInputLayout de_nome1, de_endereco1, nu_capacidade1, nu_ocupacao1, ponto1;
    private Button UpAbrigo, DeleteAbrigo, VizualizarAbrigo;
    private ProgressBar progressBarAbrigoUp, progressBarAbrigoUpFoto;
    private RequestQueue queue;
    private JSONArray jsonArray;
    private String nome, endereco, capacidade, ocupacao;
    private int capacidadeInt, ocupacaoInt;
    private String pontoAbrigoX;
    private String pontoAbrigoY;
    private String SETde_nome;
    private String SETde_endereco;
    private String SETfoto;
    private int SETnu_capacidade;
    private int SETnu_ocupacao;
    private int nu_abrigo_id;
    private Point point;
    private ImageView fotoAbrigoUp;
    private static final int SELECAO_CAMERA  = 4;
    private static final int SELECAO_GALERIA = 200;
    private Uri localImagemSelecionada;
    private Uri localImagemCapturada;
    private String encodedImage;
    private byte[] dadosImagem;
    private File arquivoFoto = null;
    private int orientation;
    private SharedPreferences.Editor editor;
    private SharedPreferences dados;
    long agora;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abrigo_up);

        inicializaComponentes();
        obterFoto();
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
        Intent intent = new Intent(AbrigoActivityUp.this, AbrigoActivity.class);
        startActivity(intent);
        finish();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void inicializaComponentes() {

        de_nome = findViewById(R.id.de_nomeUP1);
        de_endereco = findViewById(R.id.de_enderecoUP1);
        nu_capacidade = findViewById(R.id.nu_capacidadeUP1);
        nu_ocupacao = findViewById(R.id.nu_ocupacaoUP1);
        ponto = findViewById(R.id.pontoUP1);

        de_nome1 = findViewById(R.id.de_nomeUPl);
        de_endereco1 = findViewById(R.id.de_enderecoUPa);
        nu_capacidade1 = findViewById(R.id.nu_capacidadeUPa);
        nu_ocupacao1 = findViewById(R.id.nu_ocupacaoUPa);
        ponto1 = findViewById(R.id.pontoUPl);

        UpAbrigo = findViewById(R.id.UpAbrigo);
        DeleteAbrigo = findViewById(R.id.DeleteAbrigo);
        VizualizarAbrigo = findViewById(R.id.VizualizarAbrigo);

        progressBarAbrigoUp = findViewById(R.id.progressBarAbrigoUp);
        progressBarAbrigoUpFoto = findViewById(R.id.progressBarAbrigoUpFoto);
        fotoAbrigoUp = findViewById(R.id.imageAbrigoUp);

        fotoAbrigoUp.setVisibility(View.INVISIBLE);
        progressBarAbrigoUp.setVisibility(View.INVISIBLE);

        pontoAbrigoX = (String) getIntent().getSerializableExtra("pontoXClicadoUP");
        pontoAbrigoY = (String) getIntent().getSerializableExtra("pontoYClicadoUP");
        nu_abrigo_id = (int) getIntent().getSerializableExtra("nu_abrigo_id");
        SETde_nome = (String) getIntent().getSerializableExtra("de_nome");
        SETde_endereco = (String) getIntent().getSerializableExtra("de_endereco");
        SETfoto = (String) getIntent().getSerializableExtra("foto");
        SETnu_capacidade = (int) getIntent().getSerializableExtra("nu_capacidade");
        SETnu_ocupacao = (int) getIntent().getSerializableExtra("nu_ocupacao");
        String nu_capacidadeString = String.valueOf(SETnu_capacidade);
        String nu_ocupacaoString = String.valueOf(SETnu_ocupacao);

        if (SETde_nome != null && SETde_endereco != null) {
            de_nome.setText(SETde_nome);
            de_endereco.setText(SETde_endereco);
            nu_capacidade.setText(nu_capacidadeString);
            nu_ocupacao.setText(nu_ocupacaoString);
        }

        if (pontoAbrigoX != null && pontoAbrigoY != null) {
            double x = Double.parseDouble(pontoAbrigoX);
            double y = Double.parseDouble(pontoAbrigoY);
            point = new Point(x, y, SpatialReference.create(31985));
            ponto.setText(point.toString());
        }

    }

    public void buttonAtualizarAbrigo(View view) {
        nome = de_nome.getText().toString().trim();
        endereco = de_endereco.getText().toString().trim();
        capacidade = nu_capacidade.getText().toString().trim();
        ocupacao = nu_ocupacao.getText().toString().trim().trim();

        try {
            capacidadeInt = Integer.parseInt(capacidade);
            ocupacaoInt = Integer.parseInt(ocupacao);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        if (nome.isEmpty() || endereco.isEmpty() || capacidade.isEmpty() || ocupacao.isEmpty()) {
            Toast.makeText(this,
                    "Preencha todos os campos corretamente!",
                    Toast.LENGTH_LONG).show();
        } else if (capacidadeInt > 32767 || ocupacaoInt > 32767) {
            Toast.makeText(this,
                    "Valor muito grande para ocupação/capacidade!",
                    Toast.LENGTH_LONG).show();
        } else {
            UpAbrigo.setVisibility(View.INVISIBLE);
            VizualizarAbrigo.setVisibility(View.INVISIBLE);
            DeleteAbrigo.setVisibility(View.INVISIBLE);
            de_nome.setVisibility(View.INVISIBLE);
            de_endereco.setVisibility(View.INVISIBLE);
            nu_capacidade.setVisibility(View.INVISIBLE);
            nu_ocupacao.setVisibility(View.INVISIBLE);
            ponto.setVisibility(View.INVISIBLE);
            de_nome1.setVisibility(View.INVISIBLE);
            de_endereco1.setVisibility(View.INVISIBLE);
            nu_capacidade1.setVisibility(View.INVISIBLE);
            nu_ocupacao1.setVisibility(View.INVISIBLE);
            ponto1.setVisibility(View.INVISIBLE);
            fotoAbrigoUp.setVisibility(View.INVISIBLE);
            progressBarAbrigoUp.setVisibility(View.VISIBLE);

            queue = Volley.newRequestQueue(this);

            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("de_nome", nome);
                jsonObject.put("de_endereco", endereco);
                jsonObject.put("nu_capacidade", capacidadeInt);
                jsonObject.put("nu_ocupacao", ocupacaoInt);
                if (encodedImage != null) {
                    jsonObject.put("foto", encodedImage);
                }

            } catch (JSONException e) {
                e.getMessage();
            }

            String url = "http://192.168.25.218:3000/abrigo/atualizar/" + nu_abrigo_id;
            JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                    response -> {
                        UpAbrigo.setVisibility(View.VISIBLE);
                        VizualizarAbrigo.setVisibility(View.VISIBLE);
                        DeleteAbrigo.setVisibility(View.VISIBLE);
                        de_nome.setVisibility(View.VISIBLE);
                        de_endereco.setVisibility(View.VISIBLE);
                        nu_capacidade.setVisibility(View.VISIBLE);
                        nu_ocupacao.setVisibility(View.VISIBLE);
                        ponto.setVisibility(View.VISIBLE);
                        de_nome1.setVisibility(View.VISIBLE);
                        de_endereco1.setVisibility(View.VISIBLE);
                        nu_capacidade1.setVisibility(View.VISIBLE);
                        nu_ocupacao1.setVisibility(View.VISIBLE);
                        ponto1.setVisibility(View.VISIBLE);
                        fotoAbrigoUp.setVisibility(View.VISIBLE);
                        progressBarAbrigoUp.setVisibility(View.INVISIBLE);

                        Toast.makeText(
                                AbrigoActivityUp.this,
                                "Erro, tente novamente!",
                                Toast.LENGTH_LONG
                        ).show();

                        Log.d("---------> Response: ", response.toString());

                    }, error -> {
                        Intent intent = new Intent(AbrigoActivityUp.this, AbrigoActivity.class);
                        startActivity(intent);
                        finish();

                        Toast.makeText(
                                AbrigoActivityUp.this,
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

    public void buttonDeletaAbrigo(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Deletar Abrigo");
        builder.setMessage("Tem certeza que deseja excluir?");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", (dialog, which) -> {
            UpAbrigo.setVisibility(View.INVISIBLE);
            VizualizarAbrigo.setVisibility(View.INVISIBLE);
            DeleteAbrigo.setVisibility(View.INVISIBLE);
            de_nome.setVisibility(View.INVISIBLE);
            de_endereco.setVisibility(View.INVISIBLE);
            nu_capacidade.setVisibility(View.INVISIBLE);
            nu_ocupacao.setVisibility(View.INVISIBLE);
            ponto.setVisibility(View.INVISIBLE);
            de_nome1.setVisibility(View.INVISIBLE);
            de_endereco1.setVisibility(View.INVISIBLE);
            nu_capacidade1.setVisibility(View.INVISIBLE);
            nu_ocupacao1.setVisibility(View.INVISIBLE);
            ponto1.setVisibility(View.INVISIBLE);
            fotoAbrigoUp.setVisibility(View.INVISIBLE);
            progressBarAbrigoUpFoto.setVisibility(View.INVISIBLE);
            progressBarAbrigoUp.setVisibility(View.VISIBLE);

            queue = Volley.newRequestQueue(this);

            String url = "http://192.168.25.218:3000/abrigo/deletar/" + nu_abrigo_id;
            StringRequest dr = new StringRequest(Request.Method.DELETE, url,
                    response -> {
                        Intent intent = new Intent(AbrigoActivityUp.this, AbrigoActivity.class);
                        startActivity(intent);
                        finish();

                        Toast.makeText(
                                AbrigoActivityUp.this,
                                "Deletado com sucesso!",
                                Toast.LENGTH_LONG
                        ).show();

                        Log.d("---------> response: ", response);

                    },
                    error -> {
                        Toast.makeText(
                                AbrigoActivityUp.this,
                                "Erro, tente novamente!",
                                Toast.LENGTH_LONG
                        ).show();

                        UpAbrigo.setVisibility(View.VISIBLE);
                        VizualizarAbrigo.setVisibility(View.VISIBLE);
                        DeleteAbrigo.setVisibility(View.VISIBLE);
                        de_nome.setVisibility(View.VISIBLE);
                        de_endereco.setVisibility(View.VISIBLE);
                        nu_capacidade.setVisibility(View.VISIBLE);
                        nu_ocupacao.setVisibility(View.VISIBLE);
                        ponto.setVisibility(View.VISIBLE);
                        de_nome1.setVisibility(View.VISIBLE);
                        de_endereco1.setVisibility(View.VISIBLE);
                        nu_capacidade1.setVisibility(View.VISIBLE);
                        nu_ocupacao1.setVisibility(View.VISIBLE);
                        ponto1.setVisibility(View.VISIBLE);
                        fotoAbrigoUp.setVisibility(View.VISIBLE);
                        progressBarAbrigoUpFoto.setVisibility(View.INVISIBLE);
                        progressBarAbrigoUp.setVisibility(View.INVISIBLE);

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
                                    decodeImageBase64(SETfoto, fotoAbrigoUp);
                                    TimeUnit.SECONDS.sleep(3);
                                    progressBarAbrigoUpFoto.setVisibility(View.INVISIBLE);
                                    fotoAbrigoUp.setVisibility(View.VISIBLE);
                                    encodedImage = SETfoto;
                                } else {
                                    fotoAbrigoUp.setImageResource(R.drawable.padrao);
                                    fotoAbrigoUp.setVisibility(View.VISIBLE);
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
                                    Intent intent = new Intent(AbrigoActivityUp.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }

                    } catch (JSONException e) {
                        Toast.makeText(
                                AbrigoActivityUp.this,
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
                                Intent intent = new Intent(AbrigoActivityUp.this, MainActivity.class);
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

    public void buttonVizualizarAbrigo(View view) {
        if (pontoAbrigoX != null && pontoAbrigoY != null) {
                if (point != null) {
                    Intent intent = new Intent(AbrigoActivityUp.this, MapDinamicActivity.class);
                    intent.putExtra("pontoXClicadoUP", point.getX());
                    intent.putExtra("pontoYClicadoUP", point.getY());
                    intent.putExtra("AbrigoActivityUp", "true");
                    startActivity(intent);
                    finish();
                }
        }
    }

    private File criarArquivo() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File pasta = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imagem = new File(pasta.getPath() + File.separator
                + "JPG_" + timeStamp + ".jpg");
        return imagem;
    }

    public void onClick(View view) {
        escolherImagem(1);
    }

    public void onclickImage(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Foto");
        builder.setMessage("Escolha a opção desejada");
        builder.setCancelable(false);
        builder.setPositiveButton("Ver foto", (dialog, which) -> {
            Intent intent = new Intent(AbrigoActivityUp.this, ViewPhotoActivity.class);
            intent.putExtra("nu_abrigo_id", nu_abrigo_id);
            startActivity(intent);
        });
        builder.setNegativeButton("Atualizar", (dialog, which) -> {
            escolherImagem(1);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void escolherImagem(final int requestCode){
        View view = LayoutInflater.from(AbrigoActivityUp.this).inflate(R.layout.opcaofoto, null);
        androidx.appcompat.app.AlertDialog.Builder alertDialog = new androidx.appcompat.app.AlertDialog.Builder(this);

        TextView title = (TextView) view.findViewById(R.id.titleF);
        ImageButton imageButton = (ImageButton) view.findViewById(R.id.imageF);
        title.setText("Resgistro fotográfico");
        imageButton.setImageResource(R.mipmap.camera);

        alertDialog.setPositiveButton("Galeria", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Abre a galeria
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI );
                if ( i.resolveActivity(getPackageManager()) != null ){
                    startActivityForResult(i, SELECAO_GALERIA );
                }
            }
        });

        alertDialog.setNegativeButton("Tirar foto", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Abre a câmera
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    try {
                        arquivoFoto = criarArquivo();
                    } catch (IOException ex) {
                    }
                    if (arquivoFoto != null) {
                        Uri photoURI = FileProvider.getUriForFile(getBaseContext(),
                                getBaseContext().getApplicationContext().getPackageName() +
                                        ".provider", arquivoFoto);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, SELECAO_CAMERA);
                        localImagemCapturada = photoURI;
                    }
                }
            }
        }).setView(view);
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( resultCode == RESULT_OK ){
            Bitmap imagem = null;
            try {
                switch ( requestCode ){
                    case SELECAO_CAMERA:
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(arquivoFoto)));
                        ExifInterface exif = new ExifInterface(arquivoFoto.getPath());
                        orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemCapturada );
                        exibirImagem();
                        break;
                    case SELECAO_GALERIA:
                        localImagemSelecionada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada );
                        break;
                }

                if (imagem != null){
                    //Encode
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 10, baos);
                    dadosImagem = baos.toByteArray();
                    byte[] dadosImagem = baos.toByteArray();
                    encodedImage = Base64.encodeToString(dadosImagem, Base64.DEFAULT);
                    //Log.d("encodedImage: ", encodedImage);

                    //Decoder
                    decodeImageBase64(encodedImage, fotoAbrigoUp);
                }

            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void decodeImageBase64(String encodedImage, ImageView imageView){
        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        imageView.setImageBitmap(decodedByte);
    }

    private void exibirImagem() {
        int targetW = fotoAbrigoUp.getWidth();
        int targetH = fotoAbrigoUp.getHeight();
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(arquivoFoto.getAbsolutePath(), bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        Bitmap bitmap = BitmapFactory.decodeFile(arquivoFoto.getAbsolutePath(), bmOptions);
        Bitmap bmRotated = rotateBitmap(bitmap, orientation);
        fotoAbrigoUp.setImageBitmap(bmRotated);
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
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

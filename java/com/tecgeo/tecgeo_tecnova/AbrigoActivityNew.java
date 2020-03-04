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

public class AbrigoActivityNew extends AppCompatActivity {

    private TextInputEditText de_nome, de_endereco, nu_capacidade, nu_ocupacao, ponto;
    private TextInputLayout de_nomeUP1, de_enderecoUP1, nu_capacidadeUP1, nu_ocupacaoUP1, pontoUP1;
    private String nome, endereco, capacidade, ocupacao, dataFormatada;
    private Double pontoSelecionadoNaActMapX, pontoSelecionadoNaActMapY;
    private Double localizacaoAtualX, localizacaoAtualY;
    private RequestQueue queue;
    private ProgressBar progressBarAbrigoNEW;
    private Point pontoSelecionado;
    private  int capacidadeInt, ocupacaoInt;
    private Map<String, Object> params;
    private Date hoje;
    private SimpleDateFormat formataData;
    private int OBJECTID;
    private Button AddAbrigo;
    private ImageView fotoAbrigo;
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
    private boolean destruir = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abrigo_new);
        inicializaComponentes();
    }

    @Override
    protected void onStart() {
        super.onStart();
        validaTempoLogin();
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
        if(destruir){
            deleteAbrigo();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Novo Abrigo");
        builder.setMessage("Tem certeza que deseja voltar?");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", (dialog, which) -> {
            deleteAbrigo();
            Intent intent = new Intent(AbrigoActivityNew.this, AbrigoActivity.class);
            startActivity(intent);
            finish();
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            dialog.cancel();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void inicializaComponentes(){
        de_nome = findViewById(R.id.de_nomeUP1);
        de_endereco = findViewById(R.id.de_enderecoUP1);
        nu_capacidade = findViewById(R.id.nu_capacidadeUP1);
        nu_ocupacao = findViewById(R.id.nu_ocupacaoUP1);
        ponto = findViewById(R.id.pontoUP1);

        de_nomeUP1 = findViewById(R.id.de_nomeUP);
        de_enderecoUP1 = findViewById(R.id.de_enderecoUP);
        nu_capacidadeUP1 = findViewById(R.id.nu_capacidadeUP);
        nu_ocupacaoUP1 = findViewById(R.id.nu_ocupacaoUP);
        pontoUP1 = findViewById(R.id.pontoUP);
        AddAbrigo = findViewById(R.id.AddAbrigo);
        progressBarAbrigoNEW = findViewById(R.id.progressBarAbrigoNEW);
        progressBarAbrigoNEW.setVisibility(View.INVISIBLE);
        fotoAbrigo = findViewById(R.id.imageAbrigoA1);


        params  = new HashMap<String, Object>();
        formataData = new SimpleDateFormat("dd/MM/yyyy");
        hoje = new Date();
        dataFormatada = formataData.format(hoje);

        //Ponto selecionado manual
        pontoSelecionadoNaActMapX = (Double) getIntent().getSerializableExtra("pontoSelecionadoX");
        pontoSelecionadoNaActMapY = (Double) getIntent().getSerializableExtra("pontoSelecionadoY");
        OBJECTID = (int) getIntent().getSerializableExtra("OBJECTID");

        //Ponto selecionado pela localização atual
        localizacaoAtualX = (Double) getIntent().getSerializableExtra("locaAtualX");
        localizacaoAtualY = (Double) getIntent().getSerializableExtra("locaAtualY");

        if (pontoSelecionadoNaActMapX != null && pontoSelecionadoNaActMapY != null) {
            pontoSelecionado = new Point(pontoSelecionadoNaActMapX,pontoSelecionadoNaActMapY, SpatialReference.create(31985));
            ponto.setText(pontoSelecionado.toString());
            ponto.setClickable(false);
            ponto.setAlpha(0.5f);
        } else if (localizacaoAtualX != null && localizacaoAtualY != null) {
            pontoSelecionado = new Point(localizacaoAtualX, localizacaoAtualY, SpatialReference.create(31985));
            ponto.setText(pontoSelecionado.toString());
            ponto.setClickable(false);
            ponto.setAlpha(0.5f);
        }
    }

    public void buttonSalvarAbrigo(View view) {
        nome = de_nome.getText().toString().trim();
        endereco = de_endereco.getText().toString().trim();
        capacidade = nu_capacidade.getText().toString().trim();
        ocupacao = nu_ocupacao.getText().toString().trim();

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
        } else if(encodedImage == null){
            Toast.makeText(this,
                    "Insira uma foto!",
                    Toast.LENGTH_LONG).show();
        } else {
            AddAbrigo.setVisibility(View.INVISIBLE);
            de_nome.setVisibility(View.INVISIBLE);
            de_endereco.setVisibility(View.INVISIBLE);
            nu_capacidade.setVisibility(View.INVISIBLE);
            nu_ocupacao.setVisibility(View.INVISIBLE);
            ponto.setVisibility(View.INVISIBLE);
            de_nomeUP1.setVisibility(View.INVISIBLE);
            de_enderecoUP1.setVisibility(View.INVISIBLE);
            nu_capacidadeUP1.setVisibility(View.INVISIBLE);
            nu_ocupacaoUP1.setVisibility(View.INVISIBLE);
            pontoUP1.setVisibility(View.INVISIBLE);
            fotoAbrigo.setVisibility(View.INVISIBLE);
            progressBarAbrigoNEW.setVisibility(View.VISIBLE);

            queue = Volley.newRequestQueue(this);

            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("de_nome", nome);
                jsonObject.put("de_endereco", endereco);
                jsonObject.put("nu_capacidade", capacidadeInt);
                jsonObject.put("nu_ocupacao", ocupacaoInt);
                jsonObject.put("foto", encodedImage);
            } catch (JSONException e) {
                e.getMessage();
            }

            String url = "http://192.168.25.218:3000/abrigo/atualizarObjId/" + OBJECTID;
            JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,
                    response -> {
                        AddAbrigo.setVisibility(View.VISIBLE);
                        de_nome.setVisibility(View.VISIBLE);
                        de_endereco.setVisibility(View.VISIBLE);
                        nu_capacidade.setVisibility(View.VISIBLE);
                        nu_ocupacao.setVisibility(View.VISIBLE);
                        ponto.setVisibility(View.VISIBLE);
                        de_nomeUP1.setVisibility(View.VISIBLE);
                        de_enderecoUP1.setVisibility(View.VISIBLE);
                        nu_capacidadeUP1.setVisibility(View.VISIBLE);
                        nu_ocupacaoUP1.setVisibility(View.VISIBLE);
                        pontoUP1.setVisibility(View.VISIBLE);
                        fotoAbrigo.setVisibility(View.VISIBLE);
                        progressBarAbrigoNEW.setVisibility(View.INVISIBLE);

                        Toast.makeText(
                                AbrigoActivityNew.this,
                                "Erro, tente novamente!",
                                Toast.LENGTH_LONG
                        ).show();

                        Log.d("---------> Response: ", response.toString());
                    },
                    error -> {
                        Intent intent = new Intent(AbrigoActivityNew.this, AbrigoActivity.class);
                        startActivity(intent);
                        finish();

                        Toast.makeText(
                                AbrigoActivityNew.this,
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

    public void escolherImagem(final int requestCode){
        View view = LayoutInflater.from(AbrigoActivityNew.this).inflate(R.layout.opcaofoto, null);
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
                    destruir = false;
                    startActivityForResult(i, SELECAO_GALERIA);
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
                        destruir = false;
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
        destruir = false;
        if ( resultCode == RESULT_OK ){
            Bitmap imagem = null;
            try {
                switch (requestCode){
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
                    decodeImageBase64(encodedImage, fotoAbrigo);
                }

            }catch (Exception e){
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
        int targetW = fotoAbrigo.getWidth();
        int targetH = fotoAbrigo.getHeight();
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
        fotoAbrigo.setImageBitmap(bmRotated);
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

    public void deleteAbrigo() {
        queue = Volley.newRequestQueue(this);
        String url = "http://192.168.25.218:3000/abrigo/deletarObjId/" + OBJECTID;
        StringRequest dr = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    if (response.equals("Deletado com sucesso!")){
                        System.out.println("---------> Response: " +  response);
                        Intent intent = new Intent(AbrigoActivityNew.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                },
                error -> {
                    System.out.println("---------> Error: " +  error.toString());
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



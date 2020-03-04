package com.tecgeo.tecgeo_tecnova;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MapDinamicEditAcitivity extends AppCompatActivity implements Serializable, LocationListener {

    private ArcGISMap map;
    private MapView mMapView2;
    public Point pontoSelecionadoNoMapa;
    private Point pontoConvertido;
    private Point pontoLocalizacaoAtual;
    private Point pontoLocalizacaoAtualConvertido;
    private ServiceFeatureTable mServiceFeatureTableAbrigo;
    private ServiceFeatureTable mServiceFeatureTableLona;
    private ServiceFeatureTable mServiceFeatureTableVistoria;
    private static double lat, longe;
    private LocationManager locationManager;
    private Viewpoint viewpoint;
    private Double pontoSelecionadoNaActMapXEdit, pontoSelecionadoNaActMapYEdit;
    private Point pontoEdit;
    private Map<String, Object> params;
    private Button atual_ponto, marcar_manual;
    private String AbrigoActivity, LonaActivity, VistoriaActivity;
    private int OBJECTID = 0;
    private SharedPreferences.Editor editor;
    private SharedPreferences dados;
    long agora;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_dinamic_edit_acitivity);

        inicializaComponentes();
        marcarPontoNoMapa();
        marcarPontoNoMapaEdit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getLocation();
        validaTempoLogin();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView2.pause();
        locationManager.removeUpdates(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView2.resume();
    }

    @Override
    public void finish() {
        super.finish();
        //android.os.Process.killProcess(android.os.Process.myPid());
        System.out.println("finish()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView2.dispose();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MapDinamicEditAcitivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    private void inicializaComponentes(){
        pontoSelecionadoNaActMapXEdit = (Double) getIntent().getSerializableExtra("pontoSelecionadoXEdit");
        pontoSelecionadoNaActMapYEdit = (Double) getIntent().getSerializableExtra("pontoSelecionadoYEdit");

        mMapView2 = (MapView) findViewById(R.id.mapView2);
        map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, -7.24, -35.90, 12);
        mMapView2.setMap(map);

        mServiceFeatureTableAbrigo = new ServiceFeatureTable(getString(R.string.abrigos_feature_url));
        mServiceFeatureTableAbrigo.loadAsync();
        params  = new HashMap<String, Object>();

        mServiceFeatureTableLona= new ServiceFeatureTable(getString(R.string.lona_feature_url));
        mServiceFeatureTableLona.loadAsync();

        mServiceFeatureTableVistoria = new ServiceFeatureTable(getString(R.string.vistoria_feature_url));
        mServiceFeatureTableVistoria.loadAsync();

        atual_ponto = findViewById(R.id.atual_ponto);
        marcar_manual = findViewById(R.id.marcar_manual);

        AbrigoActivity = (String) getIntent().getSerializableExtra("AbrigoActivity");
        LonaActivity = (String) getIntent().getSerializableExtra("LonaActivity");
        VistoriaActivity = (String) getIntent().getSerializableExtra("VistoriaActivity");
    }

    @SuppressLint("ClickableViewAccessibility")
    private void marcarPontoNoMapa(){
        mMapView2.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView2) {
            @Override public boolean onSingleTapConfirmed(MotionEvent event) {
                android.graphics.Point point = new android.graphics.Point((int) event.getX(), (int) event.getY());

                pontoSelecionadoNoMapa = mMapView2.screenToLocation(point);
                SpatialReference spacRef = SpatialReference.create(31985);
                pontoConvertido = (Point) GeometryEngine.project(pontoSelecionadoNoMapa, spacRef);

                mMapView2.getGraphicsOverlays().clear();
                GraphicsOverlay mGraphicsOverlay;
                mGraphicsOverlay = new GraphicsOverlay();
                mMapView2.getGraphicsOverlays().add(mGraphicsOverlay);
                SimpleMarkerSymbol pointSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.rgb(226, 119, 40), 10.0f);
                pointSymbol.setOutline(new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2.0f));
                Graphic pointGraphic = new Graphic(pontoSelecionadoNoMapa, pointSymbol);
                mGraphicsOverlay.getGraphics().add(pointGraphic);
                viewpoint = new Viewpoint(pontoSelecionadoNoMapa, 2000);
                map.setInitialViewpoint(viewpoint);

                return super.onSingleTapConfirmed(event);
            }
        });

        mMapView2.setMap(map);
    }

    private void marcarPontoNoMapaEdit() {
        if (pontoSelecionadoNaActMapXEdit != null && pontoSelecionadoNaActMapYEdit != null) {
            mMapView2.getGraphicsOverlays().clear();
            pontoEdit = new Point(pontoSelecionadoNaActMapXEdit, pontoSelecionadoNaActMapYEdit, SpatialReference.create(31985));
            GraphicsOverlay mGraphicsOverlay;
            mGraphicsOverlay = new GraphicsOverlay();
            mMapView2.getGraphicsOverlays().add(mGraphicsOverlay);
            SimpleMarkerSymbol pointSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.rgb(226, 119, 40), 10.0f);
            pointSymbol.setOutline(new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2.0f));
            Graphic pointGraphic = new Graphic(pontoEdit, pointSymbol);
            mGraphicsOverlay.getGraphics().add(pointGraphic);
            viewpoint = new Viewpoint(pontoEdit, 2000);
            map.setInitialViewpoint(viewpoint);
        }
    }

    public void buttonMarcarPontoManual(View view) {
        if (AbrigoActivity != null){
            atual_ponto.setVisibility(View.INVISIBLE);
            marcar_manual.setVisibility(View.INVISIBLE);
            pontoLocalizacaoAtual = null;

            if (pontoConvertido == null) {
                atual_ponto.setVisibility(View.VISIBLE);
                marcar_manual.setVisibility(View.VISIBLE);
                mMapView2.getGraphicsOverlays().clear();
                Toast.makeText(
                        MapDinamicEditAcitivity.this,
                        "Selecione o ponto manualmente",
                        Toast.LENGTH_LONG
                ).show();
            } else {
                Feature feature = mServiceFeatureTableAbrigo.createFeature(params, pontoConvertido);

                if (mServiceFeatureTableAbrigo.canAdd()) {
                    mServiceFeatureTableAbrigo.addFeatureAsync(feature).addDoneListener(() -> applyEdits(mServiceFeatureTableAbrigo));
                } else {
                    atual_ponto.setVisibility(View.VISIBLE);
                    marcar_manual.setVisibility(View.VISIBLE);
                    Toast.makeText(this,
                            "Erro: " + R.string.error_cannot_add_to_feature_table,
                            Toast.LENGTH_LONG).show();
                    runOnUiThread(() -> logToUser(true, getString(R.string.error_cannot_add_to_feature_table)));
                }
            }
        }

        if (LonaActivity != null){
            atual_ponto.setVisibility(View.INVISIBLE);
            marcar_manual.setVisibility(View.INVISIBLE);
            pontoLocalizacaoAtual = null;

            if (pontoConvertido == null) {
                atual_ponto.setVisibility(View.VISIBLE);
                marcar_manual.setVisibility(View.VISIBLE);
                mMapView2.getGraphicsOverlays().clear();
                Toast.makeText(
                        MapDinamicEditAcitivity.this,
                        "Selecione o ponto manualmente",
                        Toast.LENGTH_LONG
                ).show();
            } else {
                Feature feature = mServiceFeatureTableLona.createFeature(params, pontoConvertido);

                if (mServiceFeatureTableLona.canAdd()) {
                    mServiceFeatureTableLona.addFeatureAsync(feature).addDoneListener(() -> applyEdits(mServiceFeatureTableLona));
                } else {
                    atual_ponto.setVisibility(View.VISIBLE);
                    marcar_manual.setVisibility(View.VISIBLE);
                    Toast.makeText(this,
                            "Erro: " + R.string.error_cannot_add_to_feature_table,
                            Toast.LENGTH_LONG).show();
                    runOnUiThread(() -> logToUser(true, getString(R.string.error_cannot_add_to_feature_table)));
                }
            }
        }

        if (VistoriaActivity != null){
            atual_ponto.setVisibility(View.INVISIBLE);
            marcar_manual.setVisibility(View.INVISIBLE);
            pontoLocalizacaoAtual = null;

            if (pontoConvertido == null) {
                atual_ponto.setVisibility(View.VISIBLE);
                marcar_manual.setVisibility(View.VISIBLE);
                mMapView2.getGraphicsOverlays().clear();
                Toast.makeText(
                        MapDinamicEditAcitivity.this,
                        "Selecione o ponto manualmente",
                        Toast.LENGTH_LONG
                ).show();
            } else {
                Feature feature = mServiceFeatureTableVistoria.createFeature(params, pontoConvertido);
                if (mServiceFeatureTableVistoria.canAdd()) {
                    mServiceFeatureTableVistoria.addFeatureAsync(feature).addDoneListener(() -> applyEdits(mServiceFeatureTableVistoria));
                } else {
                    atual_ponto.setVisibility(View.VISIBLE);
                    marcar_manual.setVisibility(View.VISIBLE);
                    Toast.makeText(this,
                            "Erro: " + R.string.error_cannot_add_to_feature_table,
                            Toast.LENGTH_LONG).show();
                    runOnUiThread(() -> logToUser(true, getString(R.string.error_cannot_add_to_feature_table)));
                }
            }
        }
    }

    public void buttonLocaAtual(View view) {
        atual_ponto.setVisibility(View.INVISIBLE);
        marcar_manual.setVisibility(View.INVISIBLE);
        pontoConvertido = null;

        if (lat == 0 || longe == 0) {
            mMapView2.getGraphicsOverlays().clear();
            alertaGpsDesabilitado();
            atual_ponto.setVisibility(View.VISIBLE);
            marcar_manual.setVisibility(View.VISIBLE);
        } else {
            pontoLocalizacaoAtual = new Point(longe, lat, SpatialReference.create(4326));
            pontoLocalizacaoAtualConvertido = (Point) GeometryEngine.project(pontoLocalizacaoAtual, SpatialReference.create(31985));
            //System.out.println("=====================pontoConvertido============================> " + pontoLocalizacaoAtualConvertido);

            mMapView2.getGraphicsOverlays().clear();
            GraphicsOverlay mGraphicsOverlay;
            mGraphicsOverlay = new GraphicsOverlay();
            mMapView2.getGraphicsOverlays().add(mGraphicsOverlay);
            SimpleMarkerSymbol pointSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.rgb(226, 119, 40), 10.0f);
            pointSymbol.setOutline(new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2.0f));
            Graphic pointGraphic = new Graphic(pontoLocalizacaoAtualConvertido, pointSymbol);
            mGraphicsOverlay.getGraphics().add(pointGraphic);
            viewpoint = new Viewpoint(pontoLocalizacaoAtualConvertido, 2000);
            map.setInitialViewpoint(viewpoint);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Localização atual");
            builder.setMessage("Confirma a localização atual?");
            builder.setCancelable(false);
            builder.setPositiveButton("Confirmar", (dialog, which) -> {

                if (pontoLocalizacaoAtualConvertido == null) {
                    mMapView2.getGraphicsOverlays().clear();
                    alertaGpsDesabilitado();
                    atual_ponto.setVisibility(View.VISIBLE);
                    marcar_manual.setVisibility(View.VISIBLE);
                } else {
                    if (AbrigoActivity != null) {
                        Feature feature = mServiceFeatureTableAbrigo.createFeature(params, pontoLocalizacaoAtualConvertido);

                        if (mServiceFeatureTableAbrigo.canAdd()) {
                            mServiceFeatureTableAbrigo.addFeatureAsync(feature).addDoneListener(() -> applyEdits(mServiceFeatureTableAbrigo));
                        } else {
                            atual_ponto.setVisibility(View.VISIBLE);
                            marcar_manual.setVisibility(View.VISIBLE);
                            Toast.makeText(this,
                                    "Erro: " + R.string.error_cannot_add_to_feature_table,
                                    Toast.LENGTH_LONG).show();
                            runOnUiThread(() -> logToUser(true, getString(R.string.error_cannot_add_to_feature_table)));
                        }
                    } else if (LonaActivity != null){
                        Feature feature = mServiceFeatureTableLona.createFeature(params, pontoLocalizacaoAtualConvertido);

                        if (mServiceFeatureTableLona.canAdd()) {
                            mServiceFeatureTableLona.addFeatureAsync(feature).addDoneListener(() -> applyEdits(mServiceFeatureTableLona));
                        } else {
                            atual_ponto.setVisibility(View.VISIBLE);
                            marcar_manual.setVisibility(View.VISIBLE);
                            Toast.makeText(this,
                                    "Erro: " + R.string.error_cannot_add_to_feature_table,
                                    Toast.LENGTH_LONG).show();
                            runOnUiThread(() -> logToUser(true, getString(R.string.error_cannot_add_to_feature_table)));
                        }
                    } else if (VistoriaActivity != null) {
                        Feature feature = mServiceFeatureTableVistoria.createFeature(params, pontoLocalizacaoAtualConvertido);

                        if (mServiceFeatureTableVistoria.canAdd()) {
                            mServiceFeatureTableVistoria.addFeatureAsync(feature).addDoneListener(() -> applyEdits(mServiceFeatureTableVistoria));
                        } else {
                            atual_ponto.setVisibility(View.VISIBLE);
                            marcar_manual.setVisibility(View.VISIBLE);
                            Toast.makeText(this,
                                    "Erro: " + R.string.error_cannot_add_to_feature_table,
                                    Toast.LENGTH_LONG).show();
                            runOnUiThread(() -> logToUser(true, getString(R.string.error_cannot_add_to_feature_table)));
                        }
                    }
                }
          });

            builder.setNegativeButton("Negar", (dialog, which) -> {
                atual_ponto.setVisibility(View.VISIBLE);
                marcar_manual.setVisibility(View.VISIBLE);
                mMapView2.getGraphicsOverlays().clear();
                dialog.cancel();
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void applyEdits(ServiceFeatureTable featureTable) {
        final ListenableFuture<List<FeatureEditResult>> editResult = featureTable.applyEditsAsync();
        editResult.addDoneListener(() -> {
            try {
                List<FeatureEditResult> editResults = editResult.get();
                if (editResults != null && !editResults.isEmpty()) {
                    if (!editResults.get(0).hasCompletedWithErrors()) {
                        //System.out.println("=======OBJECT ID DO OBJETO LANÇADO===========" + editResults.get(0).getObjectId());
                        OBJECTID = (int) editResults.get(0).getObjectId();

                        if (AbrigoActivity != null){
                            if (pontoConvertido != null) {
                                Intent intent = new Intent(MapDinamicEditAcitivity.this, AbrigoActivityNew.class);
                                intent.putExtra("pontoSelecionadoX", pontoConvertido.getX());
                                intent.putExtra("pontoSelecionadoY", pontoConvertido.getY());
                                intent.putExtra("OBJECTID", OBJECTID);
                                startActivity(intent);
                                this.finish();
                                //runOnUiThread(() -> logToUser(false, getString(R.string.feature_added)));
                            } else if (pontoLocalizacaoAtualConvertido != null) {
                                Intent intent = new Intent(MapDinamicEditAcitivity.this, AbrigoActivityNew.class);
                                intent.putExtra("pontoSelecionadoX", pontoLocalizacaoAtualConvertido.getX());
                                intent.putExtra("pontoSelecionadoY", pontoLocalizacaoAtualConvertido.getY());
                                intent.putExtra("OBJECTID", OBJECTID);
                                startActivity(intent);
                                this.finish();
                            }
                        }

                        if (LonaActivity != null){
                            if (pontoConvertido != null) {
                                Intent intent = new Intent(MapDinamicEditAcitivity.this, LonaActivityNew.class);
                                intent.putExtra("pontoSelecionadoX", pontoConvertido.getX());
                                intent.putExtra("pontoSelecionadoY", pontoConvertido.getY());
                                intent.putExtra("OBJECTID", OBJECTID);
                                startActivity(intent);
                                this.finish();
                                //runOnUiThread(() -> logToUser(false, getString(R.string.feature_added)));
                            } else if (pontoLocalizacaoAtualConvertido != null) {
                                Intent intent = new Intent(MapDinamicEditAcitivity.this, LonaActivityNew.class);
                                intent.putExtra("pontoSelecionadoX", pontoLocalizacaoAtualConvertido.getX());
                                intent.putExtra("pontoSelecionadoY", pontoLocalizacaoAtualConvertido.getY());
                                intent.putExtra("OBJECTID", OBJECTID);
                                startActivity(intent);
                                this.finish();
                            }
                        }

                        if (VistoriaActivity != null){
                            if (pontoConvertido != null) {
                                Intent intent = new Intent(MapDinamicEditAcitivity.this, VistoriaActivityNew.class);
                                intent.putExtra("pontoSelecionadoX", pontoConvertido.getX());
                                intent.putExtra("pontoSelecionadoY", pontoConvertido.getY());
                                intent.putExtra("OBJECTID", OBJECTID);
                                startActivity(intent);
                                this.finish();
                                //runOnUiThread(() -> logToUser(false, getString(R.string.feature_added)));
                            } else if (pontoLocalizacaoAtualConvertido != null) {
                                Intent intent = new Intent(MapDinamicEditAcitivity.this, VistoriaActivityNew.class);
                                intent.putExtra("pontoSelecionadoX", pontoLocalizacaoAtualConvertido.getX());
                                intent.putExtra("pontoSelecionadoY", pontoLocalizacaoAtualConvertido.getY());
                                intent.putExtra("OBJECTID", OBJECTID);
                                startActivity(intent);
                                this.finish();
                            }
                        }

                    } else {
                        atual_ponto.setVisibility(View.VISIBLE);
                        marcar_manual.setVisibility(View.VISIBLE);
                        Toast.makeText(this,
                                "Erro: " + R.string.error_cannot_add_to_feature_table,
                                Toast.LENGTH_LONG).show();
                        runOnUiThread(() -> logToUser(true, getString(R.string.error_cannot_add_to_feature_table)));
                        throw editResults.get(0).getError();
                    }
                } else {
                    atual_ponto.setVisibility(View.VISIBLE);
                    marcar_manual.setVisibility(View.VISIBLE);
                    Toast.makeText(this,
                            "A feição Não pode ser adicionada!",
                            Toast.LENGTH_LONG).show();
                }
            } catch (InterruptedException | ExecutionException e) {
                atual_ponto.setVisibility(View.VISIBLE);
                marcar_manual.setVisibility(View.VISIBLE);
                e.getCause();
                runOnUiThread(() -> logToUser(true, getString(R.string.error_applying_edits, e.getCause())));
                Toast.makeText(this,
                        "Erro: " + R.string.error_cannot_add_to_feature_table,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void logToUser(boolean isError, String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        if (isError) {
            Log.e("ERROR", message);
        } else {
            Log.d("SUCCESS", message);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        longe = location.getLongitude();

        if (lat != 0 && longe != 0) {
            //System.out.println("=============================> Localização atual: " + lat + "," + longe);
        } else {
            //System.out.println("=============================> GPS DESABILITADO!");
        }

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        alertaGpsDesabilitado();
    }

    public void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void alertaGpsDesabilitado(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("GPS");
        builder.setMessage("Para marcar com a sua localização é necessário que o GPS esteja ativo!");
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void validaTempoLogin(){
        dados = getSharedPreferences("user", MODE_PRIVATE);
        int id = dados.getInt("id", -1);
        String name = dados.getString("nome", null);
        String email = dados.getString("email", null);
        int tempoinicial = dados.getInt("tempoInicial", -1);
        int tempoFinal = dados.getInt("tempoFinal", -1);
        String token = dados.getString("token", null);

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

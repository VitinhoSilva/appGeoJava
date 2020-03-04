package com.tecgeo.tecgeo_tecnova;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MapDinamicActivity extends AppCompatActivity {

    private ArcGISMap map;
    private MapView mMapView1;
    private GraphicsOverlay mGraphicsOverlay;
    private Viewpoint viewpoint;
    private Point point;
    private RequestQueue queue;
    private String IDpoligono = null;
    private int IDint = 0;
    private JSONArray jsonArray;
    private PointCollection points;
    private Polygon polygon;
    private JSONArray pontoRisco;
    private Double pontoXClicadoUP, pontoYClicadoUP;
    private String AbrigoActivity, LonaActivity, VistoriaActivity, PontoDeRiscoActivity;
    private Callout mCallout;
    private SharedPreferences.Editor editor;
    private SharedPreferences dados;
    long agora;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_dinamic);
        inicializaComponentes();

        if (IDpoligono != null) {
            montaPoligono();
        } else if (pontoXClicadoUP != null && pontoYClicadoUP != null) {
            marcaPontoNoMapa(pontoXClicadoUP, pontoYClicadoUP);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        validaTempoLogin();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView1.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView1.resume();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MapDinamicActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView1.dispose();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void inicializaComponentes(){
        mMapView1 = (MapView) findViewById(R.id.mapView1);
        map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, -7.24, -35.90, 12);
        mMapView1.setMap(map);
        IDpoligono = (String) getIntent().getSerializableExtra("ID");
        points = new PointCollection(SpatialReference.create(31985));
        pontoXClicadoUP = (Double) getIntent().getSerializableExtra("pontoXClicadoUP");
        pontoYClicadoUP = (Double) getIntent().getSerializableExtra("pontoYClicadoUP");
        AbrigoActivity = (String) getIntent().getSerializableExtra("AbrigoActivityUp");
        LonaActivity = (String) getIntent().getSerializableExtra("LonaActivityUp");
        VistoriaActivity = (String) getIntent().getSerializableExtra("VistoriaActivityUp");
        PontoDeRiscoActivity = (String) getIntent().getSerializableExtra("AreasDeRiscoActivity");

        if(AbrigoActivity != null) {
            ServiceFeatureTable serviceFeatureTableAbrigos = new ServiceFeatureTable(
                    getResources().getString(R.string.abrigos_url));
            mCallout = mMapView1.getCallout();
            mMapView1.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView1) {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    // remove any existing callouts
                    if (mCallout.isShowing()) {
                        mCallout.dismiss();
                    }
                    // get the point that was clicked and convert it to a point in map coordinates
                    final Point clickPoint = mMapView
                            .screenToLocation(new android.graphics.Point(Math.round(e.getX()), Math.round(e.getY())));
                    // create a selection tolerance
                    int tolerance = 5;
                    double mapTolerance = tolerance * mMapView.getUnitsPerDensityIndependentPixel();
                    // use tolerance to create an envelope to query
                    Envelope envelope = new Envelope(clickPoint.getX() - mapTolerance, clickPoint.getY() - mapTolerance,
                            clickPoint.getX() + mapTolerance, clickPoint.getY() + mapTolerance, map.getSpatialReference());
                    QueryParameters query = new QueryParameters();
                    query.setGeometry(envelope);
                    // request all available attribute fields
                    final ListenableFuture<FeatureQueryResult> future = serviceFeatureTableAbrigos
                            .queryFeaturesAsync(query, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);
                    // add done loading listener to fire when the selection returns
                    future.addDoneListener(() -> {
                        try {
                            //call get on the future to get the result
                            FeatureQueryResult result = future.get();
                            // create an Iterator
                            Iterator<Feature> iterator = result.iterator();
                            // create a TextView to display field values
                            TextView calloutContent = new TextView(getApplicationContext());
                            calloutContent.setTextColor(Color.BLACK);
                            calloutContent.setSingleLine(false);
                            calloutContent.setVerticalScrollBarEnabled(true);
                            calloutContent.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
                            calloutContent.setMovementMethod(new ScrollingMovementMethod());
                            calloutContent.setLines(10);
                            // cycle through selections
                            int counter = 0;
                            Feature feature;
                            while (iterator.hasNext()) {
                                feature = iterator.next();
                                // create a Map of all available attributes as name value pairs
                                Map<String, Object> attr = feature.getAttributes();
                                Set<String> keys = attr.keySet();
                                for (String key : keys) {
                                    Object value = attr.get(key);
                                    // format observed field value as date
                                    if (value instanceof GregorianCalendar) {
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                                        value = simpleDateFormat.format(((GregorianCalendar) value).getTime());
                                        Date dataMaisUm = simpleDateFormat.parse(String.valueOf(value));
                                        Calendar cal = Calendar.getInstance();
                                        cal.setTime(dataMaisUm );
                                        cal.add(Calendar.DATE, 1);
                                        dataMaisUm = cal.getTime();
                                        value = simpleDateFormat.format(dataMaisUm);
                                    }
                                    // append name value pairs to TextView
                                    if (key.equals("de_endereco")) {
                                        calloutContent.append("Endereço: " + value + "\n");
                                    } else if (key.equals("de_nome")){
                                        calloutContent.append("Nome: " + value + "\n");
                                    } else if (key.equals("dt_alteracao_ocupacao")){
                                        calloutContent.append("Data: " + value + "\n");
                                    } else if (key.equals("nu_capacidade")){
                                        calloutContent.append("Capacidade: " + value + "\n");
                                    } else if (key.equals("nu_ocupacao")){
                                        calloutContent.append("Ocupação: " + value + "\n");
                                    }

                                }
                                counter++;
                                // center the mapview on selected feature
                                Envelope envelope1 = feature.getGeometry().getExtent();
                                mMapView.setViewpointGeometryAsync(envelope1, 200);
                                // show CallOut
                                mCallout.setLocation(clickPoint);
                                mCallout.setContent(calloutContent);
                                mCallout.show();
                            }
                        } catch (Exception e1) {
                            Log.e(getResources().getString(R.string.app_name), "Select feature failed: " + e1.getMessage());
                        }
                    });
                    return super.onSingleTapConfirmed(e);
                }
            });
        } else if(LonaActivity != null) {
            ServiceFeatureTable serviceFeatureTableSolicitacaoLona = new ServiceFeatureTable(
                    getResources().getString(R.string.solicitacao_de_lona_url));
            mCallout = mMapView1.getCallout();
            mMapView1.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView1) {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    // remove any existing callouts
                    if (mCallout.isShowing()) {
                        mCallout.dismiss();
                    }
                    // get the point that was clicked and convert it to a point in map coordinates
                    final Point clickPoint = mMapView
                            .screenToLocation(new android.graphics.Point(Math.round(e.getX()), Math.round(e.getY())));
                    // create a selection tolerance
                    int tolerance = 5;
                    double mapTolerance = tolerance * mMapView.getUnitsPerDensityIndependentPixel();
                    // use tolerance to create an envelope to query
                    Envelope envelope = new Envelope(clickPoint.getX() - mapTolerance, clickPoint.getY() - mapTolerance,
                            clickPoint.getX() + mapTolerance, clickPoint.getY() + mapTolerance, map.getSpatialReference());
                    QueryParameters query = new QueryParameters();
                    query.setGeometry(envelope);
                    // request all available attribute fields
                    final ListenableFuture<FeatureQueryResult> future = serviceFeatureTableSolicitacaoLona
                            .queryFeaturesAsync(query, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);
                    // add done loading listener to fire when the selection returns
                    future.addDoneListener(() -> {
                        try {
                            //call get on the future to get the result
                            FeatureQueryResult result = future.get();
                            // create an Iterator
                            Iterator<Feature> iterator = result.iterator();
                            // create a TextView to display field values
                            TextView calloutContent = new TextView(getApplicationContext());
                            calloutContent.setTextColor(Color.BLACK);
                            calloutContent.setSingleLine(false);
                            calloutContent.setVerticalScrollBarEnabled(true);
                            calloutContent.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
                            calloutContent.setMovementMethod(new ScrollingMovementMethod());
                            calloutContent.setLines(5);
                            // cycle through selections
                            int counter = 0;
                            Feature feature;
                            while (iterator.hasNext()) {
                                feature = iterator.next();
                                // create a Map of all available attributes as name value pairs
                                Map<String, Object> attr = feature.getAttributes();
                                Set<String> keys = attr.keySet();
                                for (String key : keys) {
                                    Object value = attr.get(key);
                                    // format observed field value as date
                                    if (value instanceof GregorianCalendar) {
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                                        value = simpleDateFormat.format(((GregorianCalendar) value).getTime());
                                        Date dataMaisUm = simpleDateFormat.parse(String.valueOf(value));
                                        Calendar cal = Calendar.getInstance();
                                        cal.setTime(dataMaisUm );
                                        cal.add(Calendar.DATE, 1);
                                        dataMaisUm = cal.getTime();
                                        value = simpleDateFormat.format(dataMaisUm);
                                    }
                                    // append name value pairs to TextView
                                    if (key.equals("de_endereco")) {
                                        calloutContent.append("Endereço: " + value + "\n");
                                    } else if (key.equals("de_descricao")){
                                        calloutContent.append("Descrição: " + value + "\n");
                                    } else if (key.equals("dt_data")){
                                        calloutContent.append("Data: " + value + "\n");
                                    }

                                }
                                counter++;
                                // center the mapview on selected feature
                                Envelope envelope1 = feature.getGeometry().getExtent();
                                mMapView.setViewpointGeometryAsync(envelope1, 200);
                                // show CallOut
                                mCallout.setLocation(clickPoint);
                                mCallout.setContent(calloutContent);
                                mCallout.show();
                            }
                        } catch (Exception e1) {
                            Log.e(getResources().getString(R.string.app_name), "Select feature failed: " + e1.getMessage());
                        }
                    });
                    return super.onSingleTapConfirmed(e);
                }
            });
        } else if(VistoriaActivity != null) {
            ServiceFeatureTable serviceFeatureTableVistoria = new ServiceFeatureTable(
                    getResources().getString(R.string.vistoria_url));
            mCallout = mMapView1.getCallout();
            mMapView1.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView1) {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    // remove any existing callouts
                    if (mCallout.isShowing()) {
                        mCallout.dismiss();
                    }
                    // get the point that was clicked and convert it to a point in map coordinates
                    final Point clickPoint = mMapView
                            .screenToLocation(new android.graphics.Point(Math.round(e.getX()), Math.round(e.getY())));
                    // create a selection tolerance
                    int tolerance = 5;
                    double mapTolerance = tolerance * mMapView.getUnitsPerDensityIndependentPixel();
                    // use tolerance to create an envelope to query
                    Envelope envelope = new Envelope(clickPoint.getX() - mapTolerance, clickPoint.getY() - mapTolerance,
                            clickPoint.getX() + mapTolerance, clickPoint.getY() + mapTolerance, map.getSpatialReference());
                    QueryParameters query = new QueryParameters();
                    query.setGeometry(envelope);
                    // request all available attribute fields
                    final ListenableFuture<FeatureQueryResult> future = serviceFeatureTableVistoria
                            .queryFeaturesAsync(query, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);
                    // add done loading listener to fire when the selection returns
                    future.addDoneListener(() -> {
                        try {
                            //call get on the future to get the result
                            FeatureQueryResult result = future.get();
                            // create an Iterator
                            Iterator<Feature> iterator = result.iterator();
                            // create a TextView to display field values
                            TextView calloutContent = new TextView(getApplicationContext());
                            calloutContent.setTextColor(Color.BLACK);
                            calloutContent.setSingleLine(false);
                            calloutContent.setVerticalScrollBarEnabled(true);
                            calloutContent.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
                            calloutContent.setMovementMethod(new ScrollingMovementMethod());
                            calloutContent.setLines(5);
                            // cycle through selections
                            int counter = 0;
                            Feature feature;
                            while (iterator.hasNext()) {
                                feature = iterator.next();
                                // create a Map of all available attributes as name value pairs
                                Map<String, Object> attr = feature.getAttributes();
                                Set<String> keys = attr.keySet();
                                for (String key : keys) {
                                    Object value = attr.get(key);
                                    // format observed field value as date
                                    if (value instanceof GregorianCalendar) {
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                                        value = simpleDateFormat.format(((GregorianCalendar) value).getTime());
                                        Date dataMaisUm = simpleDateFormat.parse(String.valueOf(value));
                                        Calendar cal = Calendar.getInstance();
                                        cal.setTime(dataMaisUm );
                                        cal.add(Calendar.DATE, 1);
                                        dataMaisUm = cal.getTime();
                                        value = simpleDateFormat.format(dataMaisUm);
                                    }
                                    // append name value pairs to TextView
                                    if (key.equals("de_endereco")) {
                                        calloutContent.append("Endereço: " + value + "\n");
                                    } else if (key.equals("de_descricao")){
                                        calloutContent.append("Descrição: " + value + "\n");
                                    } else if (key.equals("dt_data")){
                                        calloutContent.append("Data: " + value + "\n");
                                    }

                                }
                                counter++;
                                // center the mapview on selected feature
                                Envelope envelope1 = feature.getGeometry().getExtent();
                                mMapView.setViewpointGeometryAsync(envelope1, 200);
                                // show CallOut
                                mCallout.setLocation(clickPoint);
                                mCallout.setContent(calloutContent);
                                mCallout.show();
                            }
                        } catch (Exception e1) {
                            Log.e(getResources().getString(R.string.app_name), "Select feature failed: " + e1.getMessage());
                        }
                    });
                    return super.onSingleTapConfirmed(e);
                }
            });
        } else if(PontoDeRiscoActivity != null){
            ServiceFeatureTable serviceFeatureTableAreasDeRisco = new ServiceFeatureTable(
                    getResources().getString(R.string.areas_de_risco_url));
            mCallout = mMapView1.getCallout();
            mMapView1.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView1) {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    // remove any existing callouts
                    if (mCallout.isShowing()) {
                        mCallout.dismiss();
                    }
                    // get the point that was clicked and convert it to a point in map coordinates
                    final Point clickPoint = mMapView
                            .screenToLocation(new android.graphics.Point(Math.round(e.getX()), Math.round(e.getY())));
                    // create a selection tolerance
                    int tolerance = 5;
                    double mapTolerance = tolerance * mMapView.getUnitsPerDensityIndependentPixel();
                    // use tolerance to create an envelope to query
                    Envelope envelope = new Envelope(clickPoint.getX() - mapTolerance, clickPoint.getY() - mapTolerance,
                            clickPoint.getX() + mapTolerance, clickPoint.getY() + mapTolerance, map.getSpatialReference());
                    QueryParameters query = new QueryParameters();
                    query.setGeometry(envelope);
                    // request all available attribute fields
                    final ListenableFuture<FeatureQueryResult> future = serviceFeatureTableAreasDeRisco
                            .queryFeaturesAsync(query, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);
                    // add done loading listener to fire when the selection returns
                    future.addDoneListener(() -> {
                        try {
                            //call get on the future to get the result
                            FeatureQueryResult result = future.get();
                            // create an Iterator
                            Iterator<Feature> iterator = result.iterator();
                            // create a TextView to display field values
                            TextView calloutContent = new TextView(getApplicationContext());
                            calloutContent.setTextColor(Color.BLACK);
                            calloutContent.setSingleLine(false);
                            calloutContent.setVerticalScrollBarEnabled(true);
                            calloutContent.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
                            calloutContent.setMovementMethod(new ScrollingMovementMethod());
                            calloutContent.setLines(10);
                            // cycle through selections
                            int counter = 0;
                            Feature feature;
                            while (iterator.hasNext()) {
                                feature = iterator.next();
                                // create a Map of all available attributes as name value pairs
                                Map<String, Object> attr = feature.getAttributes();
                                Set<String> keys = attr.keySet();
                                for (String key : keys) {
                                    Object value = attr.get(key);
                                    // format observed field value as date
                                    if (value instanceof GregorianCalendar) {
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                                        value = simpleDateFormat.format(((GregorianCalendar) value).getTime());
                                        Date dataMaisUm = simpleDateFormat.parse(String.valueOf(value));
                                        Calendar cal = Calendar.getInstance();
                                        cal.setTime(dataMaisUm );
                                        cal.add(Calendar.DATE, 1);
                                        dataMaisUm = cal.getTime();
                                        value = simpleDateFormat.format(dataMaisUm);
                                    }

                                    // append name value pairs to TextView
                                    if (key.equals("de_local")) {
                                        calloutContent.append("Local: " + value + "\n");
                                    } else if (key.equals("de_tipolog")){
                                        calloutContent.append("Tipologia: " + value + "\n");
                                    } else if (key.equals("dt_data_se")){
                                        calloutContent.append("Data: " + value + "\n");
                                    } else if (key.equals("de_situaca")){
                                        calloutContent.append("Situação: " + value + "\n");
                                    } else if (key.equals("de_projeto")){
                                        calloutContent.append("Projeto: " + value + "\n");
                                    }
                                }
                                counter++;
                                // center the mapview on selected feature
                                Envelope envelope1 = feature.getGeometry().getExtent();
                                mMapView.setViewpointGeometryAsync(envelope1, 200);
                                // show CallOut
                                mCallout.setLocation(clickPoint);
                                mCallout.setContent(calloutContent);
                                mCallout.show();
                            }
                        } catch (Exception e1) {
                            Log.e(getResources().getString(R.string.app_name), "Select feature failed: " + e1.getMessage());
                        }
                    });
                    return super.onSingleTapConfirmed(e);
                }
            });

        }
    }

    private void montaPoligono(){
        queue = Volley.newRequestQueue(this);

        try {
            IDint = Integer.parseInt(IDpoligono);
        } catch (NumberFormatException e) {
            System.out.println("Numero com formato errado!");
        }

        String url = "http://192.168.25.218:3000/arearisco/filtrar/" + IDint;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        jsonArray = response.getJSONArray("AreasRisco");
                        pontoRisco = ((jsonArray.getJSONObject(0).getJSONObject("Shape").getJSONArray("points")));
                        for (int i = 0; i < pontoRisco.length(); i++) {
                            JSONObject latlong = pontoRisco.getJSONObject(i);
                            double x = latlong.getDouble("x");
                            double y = latlong.getDouble("y");
                            points.add(x, y);
                        }
                            if (points != null) {
                                marcaPoligonoNoMapa(points);
                            }
                    } catch (JSONException e) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Ponto de Risco");
                        builder.setMessage("Erro ao listar, tente novamente");
                        builder.setCancelable(false);
                        builder.setPositiveButton("OK", (dialog, which) -> {
                            Intent intent = new Intent(MapDinamicActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
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

    //TODO só funciona na primeira vez
    public void marcaPoligonoNoMapa(PointCollection points){
        mMapView1.getGraphicsOverlays().clear();
        polygon = new Polygon(points, SpatialReference.create(31985));
        mGraphicsOverlay = new GraphicsOverlay();
        mMapView1.getGraphicsOverlays().add(mGraphicsOverlay);
        SimpleFillSymbol polygonSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.TRANSPARENT,
                new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2.0f));
        Graphic polygonGraphic = new Graphic(polygon, polygonSymbol);
        mGraphicsOverlay.getGraphics().add(polygonGraphic);
        viewpoint = new Viewpoint(polygon.getExtent().getCenter(), 5000);
        map.setInitialViewpoint(viewpoint);
    }

    private void marcaPontoNoMapa(Double x, Double y) {
        mMapView1.getGraphicsOverlays().clear();
        point = new Point(x, y, SpatialReference.create(31985));
        mGraphicsOverlay = new GraphicsOverlay();
        mMapView1.getGraphicsOverlays().add(mGraphicsOverlay);
        SimpleMarkerSymbol pointSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.rgb(226, 119, 40), 10.0f);
        pointSymbol.setOutline(new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2.0f));
        Graphic pointGraphic = new Graphic(point, pointSymbol);
        mGraphicsOverlay.getGraphics().add(pointGraphic);
        viewpoint = new Viewpoint(point, 2000);
        map.setInitialViewpoint(viewpoint);
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

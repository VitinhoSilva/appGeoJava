package com.tecgeo.tecgeo_tecnova;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MapActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Callout mCallout;
    private MapView mMapView;
    private ArcGISMap map;
    private  ServiceFeatureTable serviceFeatureTablePontosDeApoio, serviceFeatureTableEquipamentosPublicos, serviceFeatureTableOcorrencias,
            serviceFeatureTableChamados, serviceFeatureTableAbrigos, serviceFeatureTablePessoas, serviceFeatureTableSolicitacaoLona, serviceFeatureTableVistoria,
            serviceFeatureTableEdificacoes, serviceFeatureTableAreasDeRisco, serviceFeatureTableBairro, serviceFeatureTableLote;
    private FeatureLayer featureLayerPontosDeApoio, featureLayerEquipamentosPublicos, featureLayerOcorrencias, featureLayerChamados, featureLayerAbrigos,
            featureLayerPessoas, featureLayerSolicitacaoLona, featureLayerVistoria, featureLayerEdificacoes, featureLayerAreasDeRisco,
            featureLayerBairro, featureLayerLote;
    private NavigationView nav;
    private MenuItem menuAreaRisco, menuAbrigo, menuLona, menuVistoria, menuTodos, menuPontoApoio, menuPessoa, menuChamado, menuOcorrencia,
    menuEdificacao, menuEquipamento, menuLote, menuBairro;
    private CheckBox checkAreaRisco, checkAbrigo, checkLona, checkVistoria, checkTodos, checkApoio, checkPessoa, checkChamado, checkOcorrencia,
    checkEdificacao, checkEquipamento, checkLote, checkBairro;
    private Toolbar toolbar;
    private  DrawerLayout drawer;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private SharedPreferences.Editor editor;
    private SharedPreferences dados;
    long agora;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        inicializaComponentes();
        ativaCamadas();
        ativaFuncaoCheckBox();
    }

    @Override
    protected void onStart() {
        super.onStart();
        validaTempoLogin();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.dispose();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

        Intent intent = new Intent(MapActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
       }

       private void inicializaComponentes(){
           // inflate MapView from layout
           mMapView = (MapView) findViewById(R.id.mapView);

           // create a map with the terrain with labels basemap
           map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, -7.24, -35.90, 12);

           toolbar = findViewById(R.id.toolbar);
           setSupportActionBar(toolbar);
           getSupportActionBar().setDisplayShowTitleEnabled(false);
           drawer = findViewById(R.id.drawer_layout);
           navigationView = findViewById(R.id.nav_view);
           toggle = new ActionBarDrawerToggle(
                   this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
           drawer.addDrawerListener(toggle);
           toggle.syncState();
           navigationView.setNavigationItemSelectedListener(this);

           nav = findViewById(R.id.nav_view);

           menuAreaRisco = nav.getMenu().findItem(R.id.arearisco);
           menuAbrigo = nav.getMenu().findItem(R.id.abrigo);
           menuLona = nav.getMenu().findItem(R.id.lona);
           menuVistoria = nav.getMenu().findItem(R.id.vistoria);
           menuPontoApoio = nav.getMenu().findItem(R.id.pontosdeapoio);
           menuPessoa = nav.getMenu().findItem(R.id.pessoa);
           menuChamado = nav.getMenu().findItem(R.id.chamado);
           menuOcorrencia = nav.getMenu().findItem(R.id.ocorrencia);
           menuEdificacao = nav.getMenu().findItem(R.id.edificacao);
           menuEquipamento = nav.getMenu().findItem(R.id.equipamento);
           menuLote = nav.getMenu().findItem(R.id.lote);
           menuBairro = nav.getMenu().findItem(R.id.bairro);
           menuTodos = nav.getMenu().findItem(R.id.todos);

           checkAreaRisco = (CheckBox) MenuItemCompat.getActionView(menuAreaRisco);
           checkAbrigo = (CheckBox) MenuItemCompat.getActionView(menuAbrigo);
           checkLona = (CheckBox) MenuItemCompat.getActionView(menuLona);
           checkVistoria = (CheckBox) MenuItemCompat.getActionView(menuVistoria);
           checkApoio = (CheckBox) MenuItemCompat.getActionView(menuPontoApoio);
           checkPessoa = (CheckBox) MenuItemCompat.getActionView(menuPessoa);
           checkChamado = (CheckBox) MenuItemCompat.getActionView(menuChamado);
           checkOcorrencia = (CheckBox) MenuItemCompat.getActionView(menuOcorrencia);
           checkEdificacao = (CheckBox) MenuItemCompat.getActionView(menuEdificacao);
           checkEquipamento = (CheckBox) MenuItemCompat.getActionView(menuEquipamento);
           checkLote = (CheckBox) MenuItemCompat.getActionView(menuLote);
           checkBairro = (CheckBox) MenuItemCompat.getActionView(menuBairro);
           checkTodos = (CheckBox) MenuItemCompat.getActionView(menuTodos);
       }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.arearisco) {
            checkAreaRisco.setChecked(true);
            checkAbrigo.setChecked(false);
            checkLona.setChecked(false);
            checkVistoria.setChecked(false);
            checkApoio.setChecked(false);
            checkPessoa.setChecked(false);
            checkChamado.setChecked(false);
            checkOcorrencia.setChecked(false);
            checkEdificacao.setChecked(false);
            checkEquipamento.setChecked(false);
            checkLote.setChecked(false);
            checkBairro.setChecked(false);
            checkTodos.setChecked(false);
            ativaCamadaAreaRisco();
        } else if (id == R.id.abrigo) {
            checkAreaRisco.setChecked(false);
            checkAbrigo.setChecked(true);
            checkLona.setChecked(false);
            checkVistoria.setChecked(false);
            checkApoio.setChecked(false);
            checkPessoa.setChecked(false);
            checkChamado.setChecked(false);
            checkOcorrencia.setChecked(false);
            checkEdificacao.setChecked(false);
            checkEquipamento.setChecked(false);
            checkLote.setChecked(false);
            checkBairro.setChecked(false);
            checkTodos.setChecked(false);
            ativaCamadaAbrigo();
        } else if (id == R.id.lona) {
            checkAreaRisco.setChecked(false);
            checkAbrigo.setChecked(false);
            checkLona.setChecked(true);
            checkVistoria.setChecked(false);
            checkApoio.setChecked(false);
            checkPessoa.setChecked(false);
            checkChamado.setChecked(false);
            checkOcorrencia.setChecked(false);
            checkEdificacao.setChecked(false);
            checkEquipamento.setChecked(false);
            checkLote.setChecked(false);
            checkBairro.setChecked(false);
            checkTodos.setChecked(false);
            ativaCamadaLona();
        } else if (id == R.id.vistoria) {
            checkAreaRisco.setChecked(false);
            checkAbrigo.setChecked(false);
            checkLona.setChecked(false);
            checkVistoria.setChecked(true);
            checkApoio.setChecked(false);
            checkPessoa.setChecked(false);
            checkChamado.setChecked(false);
            checkOcorrencia.setChecked(false);
            checkEdificacao.setChecked(false);
            checkEquipamento.setChecked(false);
            checkLote.setChecked(false);
            checkBairro.setChecked(false);
            checkTodos.setChecked(false);
            ativaCamadaVistoria();
        } else if (id == R.id.pontosdeapoio) {
            checkAreaRisco.setChecked(false);
            checkAbrigo.setChecked(false);
            checkLona.setChecked(false);
            checkVistoria.setChecked(false);
            checkApoio.setChecked(true);
            checkPessoa.setChecked(false);
            checkChamado.setChecked(false);
            checkOcorrencia.setChecked(false);
            checkEdificacao.setChecked(false);
            checkEquipamento.setChecked(false);
            checkLote.setChecked(false);
            checkBairro.setChecked(false);
            checkTodos.setChecked(false);
            ativaCamadaApoio();
        } else if (id == R.id.pessoa) {
            checkAreaRisco.setChecked(false);
            checkAbrigo.setChecked(false);
            checkLona.setChecked(false);
            checkVistoria.setChecked(false);
            checkApoio.setChecked(false);
            checkPessoa.setChecked(true);
            checkChamado.setChecked(false);
            checkOcorrencia.setChecked(false);
            checkEdificacao.setChecked(false);
            checkEquipamento.setChecked(false);
            checkLote.setChecked(false);
            checkBairro.setChecked(false);
            checkTodos.setChecked(false);
            ativaCamadaPessoa();
        } else if (id == R.id.chamado) {
            checkAreaRisco.setChecked(false);
            checkAbrigo.setChecked(false);
            checkLona.setChecked(false);
            checkVistoria.setChecked(false);
            checkApoio.setChecked(false);
            checkPessoa.setChecked(false);
            checkChamado.setChecked(true);
            checkOcorrencia.setChecked(false);
            checkEdificacao.setChecked(false);
            checkEquipamento.setChecked(false);
            checkLote.setChecked(false);
            checkBairro.setChecked(false);
            checkTodos.setChecked(false);
            ativaCamadaChamado();
        } else if (id == R.id.ocorrencia) {
            checkAreaRisco.setChecked(false);
            checkAbrigo.setChecked(false);
            checkLona.setChecked(false);
            checkVistoria.setChecked(false);
            checkApoio.setChecked(false);
            checkPessoa.setChecked(false);
            checkChamado.setChecked(false);
            checkOcorrencia.setChecked(true);
            checkEdificacao.setChecked(false);
            checkEquipamento.setChecked(false);
            checkLote.setChecked(false);
            checkBairro.setChecked(false);
            checkTodos.setChecked(false);
            ativaCamadaOcorrencia();
        } else if (id == R.id.edificacao) {
            checkAreaRisco.setChecked(false);
            checkAbrigo.setChecked(false);
            checkLona.setChecked(false);
            checkVistoria.setChecked(false);
            checkApoio.setChecked(false);
            checkPessoa.setChecked(false);
            checkChamado.setChecked(false);
            checkOcorrencia.setChecked(false);
            checkEdificacao.setChecked(true);
            checkEquipamento.setChecked(false);
            checkLote.setChecked(false);
            checkBairro.setChecked(false);
            checkTodos.setChecked(false);
            ativaCamadaEdificacao();
        } else if (id == R.id.equipamento) {
            checkAreaRisco.setChecked(false);
            checkAbrigo.setChecked(false);
            checkLona.setChecked(false);
            checkVistoria.setChecked(false);
            checkApoio.setChecked(false);
            checkPessoa.setChecked(false);
            checkChamado.setChecked(false);
            checkOcorrencia.setChecked(false);
            checkEdificacao.setChecked(false);
            checkEquipamento.setChecked(true);
            checkLote.setChecked(false);
            checkBairro.setChecked(false);
            checkTodos.setChecked(false);
            ativaCamadaEquipamento();
        }  else if (id == R.id.lote) {
            checkAreaRisco.setChecked(false);
            checkAbrigo.setChecked(false);
            checkLona.setChecked(false);
            checkVistoria.setChecked(false);
            checkApoio.setChecked(false);
            checkPessoa.setChecked(false);
            checkChamado.setChecked(false);
            checkOcorrencia.setChecked(false);
            checkEdificacao.setChecked(false);
            checkEquipamento.setChecked(false);
            checkLote.setChecked(true);
            checkBairro.setChecked(false);
            checkTodos.setChecked(false);
            ativaCamadaLote();
        } else if (id == R.id.bairro) {
            checkAreaRisco.setChecked(false);
            checkAbrigo.setChecked(false);
            checkLona.setChecked(false);
            checkVistoria.setChecked(false);
            checkApoio.setChecked(false);
            checkPessoa.setChecked(false);
            checkChamado.setChecked(false);
            checkOcorrencia.setChecked(false);
            checkEdificacao.setChecked(false);
            checkEquipamento.setChecked(false);
            checkLote.setChecked(false);
            checkBairro.setChecked(true);
            checkTodos.setChecked(false);
            ativaCamadaBairro();
        } else if (id == R.id.todos){
            checkAreaRisco.setChecked(false);
            checkAbrigo.setChecked(false);
            checkLona.setChecked(false);
            checkVistoria.setChecked(false);
            checkApoio.setChecked(false);
            checkPessoa.setChecked(false);
            checkChamado.setChecked(false);
            checkOcorrencia.setChecked(false);
            checkEdificacao.setChecked(false);
            checkEquipamento.setChecked(false);
            checkLote.setChecked(false);
            checkBairro.setChecked(false);
            checkTodos.setChecked(true);
            ativaCamadas();
        }

//        DrawerLayout drawer = findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void ativaFuncaoCheckBox(){
        checkAreaRisco.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked == true) {
                checkAbrigo.setChecked(false);
                checkLona.setChecked(false);
                checkVistoria.setChecked(false);
                checkApoio.setChecked(false);
                checkPessoa.setChecked(false);
                checkChamado.setChecked(false);
                checkOcorrencia.setChecked(false);
                checkEdificacao.setChecked(false);
                checkEquipamento.setChecked(false);
                checkLote.setChecked(false);
                checkBairro.setChecked(false);
                checkTodos.setChecked(false);

               ativaCamadaAreaRisco();
            } else {
                mMapView.getGraphicsOverlays().clear();
                if (mCallout != null) {
                    mCallout.dismiss();
                }
                map.getOperationalLayers().clear();

            }
        });

        checkAbrigo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked == true) {
                checkAreaRisco.setChecked(false);
                checkLona.setChecked(false);
                checkVistoria.setChecked(false);
                checkApoio.setChecked(false);
                checkPessoa.setChecked(false);
                checkChamado.setChecked(false);
                checkOcorrencia.setChecked(false);
                checkEdificacao.setChecked(false);
                checkEquipamento.setChecked(false);
                checkLote.setChecked(false);
                checkBairro.setChecked(false);
                checkTodos.setChecked(false);

                ativaCamadaAbrigo();
            } else {
                mMapView.getGraphicsOverlays().clear();
                if (mCallout != null) {
                    mCallout.dismiss();
                }
                map.getOperationalLayers().clear();

            }
        });

        checkLona.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked == true) {
                checkAreaRisco.setChecked(false);
                checkAbrigo.setChecked(false);
                checkVistoria.setChecked(false);
                checkApoio.setChecked(false);
                checkPessoa.setChecked(false);
                checkChamado.setChecked(false);
                checkOcorrencia.setChecked(false);
                checkEdificacao.setChecked(false);
                checkEquipamento.setChecked(false);
                checkLote.setChecked(false);
                checkBairro.setChecked(false);
                checkTodos.setChecked(false);

                ativaCamadaLona();
            } else {
                mMapView.getGraphicsOverlays().clear();
                if (mCallout != null) {
                    mCallout.dismiss();
                }
                map.getOperationalLayers().clear();

            }
        });

        checkVistoria.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked == true) {
                checkAreaRisco.setChecked(false);
                checkAbrigo.setChecked(false);
                checkLona.setChecked(false);
                checkApoio.setChecked(false);
                checkPessoa.setChecked(false);
                checkChamado.setChecked(false);
                checkOcorrencia.setChecked(false);
                checkEdificacao.setChecked(false);
                checkEquipamento.setChecked(false);
                checkLote.setChecked(false);
                checkBairro.setChecked(false);
                checkTodos.setChecked(false);

                ativaCamadaVistoria();
            } else {
                mMapView.getGraphicsOverlays().clear();
                if (mCallout != null) {
                    mCallout.dismiss();
                }
                map.getOperationalLayers().clear();

            }
        });

        checkApoio.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked == true) {
                checkAreaRisco.setChecked(false);
                checkAbrigo.setChecked(false);
                checkLona.setChecked(false);
                checkVistoria.setChecked(false);
                checkPessoa.setChecked(false);
                checkChamado.setChecked(false);
                checkOcorrencia.setChecked(false);
                checkEdificacao.setChecked(false);
                checkEquipamento.setChecked(false);
                checkLote.setChecked(false);
                checkBairro.setChecked(false);
                checkTodos.setChecked(false);

                ativaCamadaApoio();
            } else {
                mMapView.getGraphicsOverlays().clear();
                if (mCallout != null) {
                    mCallout.dismiss();
                }
                map.getOperationalLayers().clear();

            }
        });

        checkPessoa.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked == true) {
                checkAreaRisco.setChecked(false);
                checkAbrigo.setChecked(false);
                checkLona.setChecked(false);
                checkVistoria.setChecked(false);
                checkApoio.setChecked(false);
                checkChamado.setChecked(false);
                checkOcorrencia.setChecked(false);
                checkEdificacao.setChecked(false);
                checkEquipamento.setChecked(false);
                checkLote.setChecked(false);
                checkBairro.setChecked(false);
                checkTodos.setChecked(false);

                ativaCamadaPessoa();
            } else {
                mMapView.getGraphicsOverlays().clear();
                if (mCallout != null) {
                    mCallout.dismiss();
                }
                map.getOperationalLayers().clear();

            }
        });

        checkChamado.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked == true) {
                checkAreaRisco.setChecked(false);
                checkAbrigo.setChecked(false);
                checkLona.setChecked(false);
                checkVistoria.setChecked(false);
                checkApoio.setChecked(false);
                checkPessoa.setChecked(false);
                checkOcorrencia.setChecked(false);
                checkEdificacao.setChecked(false);
                checkEquipamento.setChecked(false);
                checkLote.setChecked(false);
                checkBairro.setChecked(false);
                checkTodos.setChecked(false);

                ativaCamadaChamado();
            } else {
                mMapView.getGraphicsOverlays().clear();
                if (mCallout != null) {
                    mCallout.dismiss();
                }
                map.getOperationalLayers().clear();

            }
        });

        checkOcorrencia.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked == true) {
                checkAreaRisco.setChecked(false);
                checkAbrigo.setChecked(false);
                checkLona.setChecked(false);
                checkVistoria.setChecked(false);
                checkApoio.setChecked(false);
                checkPessoa.setChecked(false);
                checkChamado.setChecked(false);
                checkEdificacao.setChecked(false);
                checkEquipamento.setChecked(false);
                checkLote.setChecked(false);
                checkBairro.setChecked(false);
                checkTodos.setChecked(false);

                ativaCamadaOcorrencia();
            } else {
                mMapView.getGraphicsOverlays().clear();
                if (mCallout != null) {
                    mCallout.dismiss();
                }
                map.getOperationalLayers().clear();

            }
        });

        checkEdificacao.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked == true) {
                checkAreaRisco.setChecked(false);
                checkAbrigo.setChecked(false);
                checkLona.setChecked(false);
                checkVistoria.setChecked(false);
                checkApoio.setChecked(false);
                checkPessoa.setChecked(false);
                checkChamado.setChecked(false);
                checkOcorrencia.setChecked(false);
                checkEquipamento.setChecked(false);
                checkLote.setChecked(false);
                checkBairro.setChecked(false);
                checkTodos.setChecked(false);

                ativaCamadaEdificacao();
            } else {
                mMapView.getGraphicsOverlays().clear();
                if (mCallout != null) {
                    mCallout.dismiss();
                }
                map.getOperationalLayers().clear();

            }
        });

        checkEquipamento.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked == true) {
                checkAreaRisco.setChecked(false);
                checkAbrigo.setChecked(false);
                checkLona.setChecked(false);
                checkVistoria.setChecked(false);
                checkApoio.setChecked(false);
                checkPessoa.setChecked(false);
                checkChamado.setChecked(false);
                checkOcorrencia.setChecked(false);
                checkEdificacao.setChecked(false);
                checkLote.setChecked(false);
                checkBairro.setChecked(false);
                checkTodos.setChecked(false);

                ativaCamadaEquipamento();
            } else {
                mMapView.getGraphicsOverlays().clear();
                if (mCallout != null) {
                    mCallout.dismiss();
                }
                map.getOperationalLayers().clear();

            }
        });

        checkLote.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked == true) {
                checkAreaRisco.setChecked(false);
                checkAbrigo.setChecked(false);
                checkLona.setChecked(false);
                checkVistoria.setChecked(false);
                checkApoio.setChecked(false);
                checkPessoa.setChecked(false);
                checkChamado.setChecked(false);
                checkOcorrencia.setChecked(false);
                checkEdificacao.setChecked(false);
                checkEquipamento.setChecked(false);
                checkBairro.setChecked(false);
                checkTodos.setChecked(false);
                ativaCamadaLote();
            } else {
                mMapView.getGraphicsOverlays().clear();
                if (mCallout != null) {
                    mCallout.dismiss();
                }
                map.getOperationalLayers().clear();

            }
        });

        checkBairro.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked == true) {
                checkAreaRisco.setChecked(false);
                checkAbrigo.setChecked(false);
                checkLona.setChecked(false);
                checkVistoria.setChecked(false);
                checkApoio.setChecked(false);
                checkPessoa.setChecked(false);
                checkChamado.setChecked(false);
                checkOcorrencia.setChecked(false);
                checkEdificacao.setChecked(false);
                checkEquipamento.setChecked(false);
                checkLote.setChecked(false);
                checkTodos.setChecked(false);

                ativaCamadaBairro();
            } else {
                mMapView.getGraphicsOverlays().clear();
                if (mCallout != null) {
                    mCallout.dismiss();
                }
                map.getOperationalLayers().clear();

            }
        });

        checkTodos.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked == true) {
                checkAreaRisco.setChecked(false);
                checkAbrigo.setChecked(false);
                checkLona.setChecked(false);
                checkVistoria.setChecked(false);
                checkApoio.setChecked(false);
                checkPessoa.setChecked(false);
                checkChamado.setChecked(false);
                checkOcorrencia.setChecked(false);
                checkEdificacao.setChecked(false);
                checkEquipamento.setChecked(false);
                checkLote.setChecked(false);
                checkBairro.setChecked(false);

                ativaCamadas();
            } else {
                mMapView.getGraphicsOverlays().clear();
                if (mCallout != null) {
                    mCallout.dismiss();
                }
                map.getOperationalLayers().clear();

            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void ativaCamadaBairro(){
        mMapView.getGraphicsOverlays().clear();
        if (mCallout != null) {
            mCallout.dismiss();
        }
        map.getOperationalLayers().clear();
        map.getOperationalLayers().add(featureLayerBairro);
        mMapView.setMap(map);

        mCallout = mMapView.getCallout();
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
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
                final ListenableFuture<FeatureQueryResult> future = serviceFeatureTableBairro
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
                        calloutContent.setLines(4);
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
                                if (key.equals("de_nome")) {
                                    calloutContent.append("Nome: " + value + "\n");
                                } else if (key.equals("de_regiao_administrativa")){
                                    calloutContent.append("Região: " + value + "\n");
                                } else if (key.equals("de_regiao_geografica")){
                                    calloutContent.append("Região geográfica: " + value + "\n");
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
        Toast.makeText(
                MapActivity.this,
                "Camada de bairros ativada!",
                Toast.LENGTH_SHORT
        ).show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void ativaCamadaLote(){
        mMapView.getGraphicsOverlays().clear();
        if (mCallout != null) {
            mCallout.dismiss();
        }
        map.getOperationalLayers().clear();
        map.getOperationalLayers().add(featureLayerLote);
        mMapView.setMap(map);

        mCallout = mMapView.getCallout();
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
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
                final ListenableFuture<FeatureQueryResult> future = serviceFeatureTableLote
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
                        calloutContent.setLines(4);
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
                                if (key.equals("de_proprietario")) {
                                    calloutContent.append("Propietário: " + value + "\n");
                                } else if (key.equals("de_tipo_ocupacao")){
                                    calloutContent.append("Tipo: " + value + "\n");
                                } else if (key.equals("de_endereco")){
                                    calloutContent.append("Endereço: " + value + "\n");
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
        Toast.makeText(
                MapActivity.this,
                "Camada de lotes ativada!",
                Toast.LENGTH_SHORT
        ).show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void ativaCamadaEquipamento(){
        mMapView.getGraphicsOverlays().clear();
        if (mCallout != null) {
            mCallout.dismiss();
        }
        map.getOperationalLayers().clear();
        map.getOperationalLayers().add(featureLayerEquipamentosPublicos);
        mMapView.setMap(map);

        mCallout = mMapView.getCallout();
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
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
                final ListenableFuture<FeatureQueryResult> future = serviceFeatureTableEquipamentosPublicos
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
                        calloutContent.setLines(4);
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
                                if (key.equals("de_nome")) {
                                    calloutContent.append("Nome: " + value + "\n");
                                } else if (key.equals("de_tipo")){
                                    calloutContent.append("Tipo: " + value + "\n");
                                } else if (key.equals("de_endereco")){
                                    calloutContent.append("Endereço: " + value + "\n");
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
        Toast.makeText(
                MapActivity.this,
                "Camada de equipamentos ativada!",
                Toast.LENGTH_SHORT
        ).show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void ativaCamadaEdificacao(){
        mMapView.getGraphicsOverlays().clear();
        if (mCallout != null) {
            mCallout.dismiss();
        }
        map.getOperationalLayers().clear();
        map.getOperationalLayers().add(featureLayerEdificacoes);
        mMapView.setMap(map);

        mCallout = mMapView.getCallout();
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
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
                final ListenableFuture<FeatureQueryResult> future = serviceFeatureTableEdificacoes
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
                        calloutContent.setLines(4);
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
                                if (key.equals("nu_edifica")) {
                                    calloutContent.append("ID: " + value + "\n");
                                } else if (key.equals("de_ocorrencia")){
                                    calloutContent.append("Ocorrência: " + value + "\n");
                                } else if (key.equals("dt_data_risco")){
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
        Toast.makeText(
                MapActivity.this,
                "Camada de edificações ativada!",
                Toast.LENGTH_SHORT
        ).show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void ativaCamadaOcorrencia(){
        mMapView.getGraphicsOverlays().clear();
        if (mCallout != null) {
            mCallout.dismiss();
        }
        map.getOperationalLayers().clear();
        map.getOperationalLayers().add(featureLayerOcorrencias);
        mMapView.setMap(map);

        mCallout = mMapView.getCallout();
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
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
                final ListenableFuture<FeatureQueryResult> future = serviceFeatureTableOcorrencias
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
                        calloutContent.setLines(4);
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
                                if (key.equals("de_enderec")) {
                                    calloutContent.append("Endereço: " + value + "\n");
                                } else if (key.equals("de_intensi")){
                                    calloutContent.append("Intensidade: " + value + "\n");
                                } else if (key.equals("dt_chamado")){
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
        Toast.makeText(
                MapActivity.this,
                "Camada de ocorrências ativada!",
                Toast.LENGTH_SHORT
        ).show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void ativaCamadaChamado(){
        mMapView.getGraphicsOverlays().clear();
        if (mCallout != null) {
            mCallout.dismiss();
        }
        map.getOperationalLayers().clear();
        map.getOperationalLayers().add(featureLayerChamados);
        mMapView.setMap(map);

        mCallout = mMapView.getCallout();
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
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
                final ListenableFuture<FeatureQueryResult> future = serviceFeatureTableChamados
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
                        calloutContent.setLines(4);
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
                                } else if (key.equals("de_status")){
                                    calloutContent.append("Status: " + value + "\n");
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
        Toast.makeText(
                MapActivity.this,
                "Camada de chamados ativada!",
                Toast.LENGTH_SHORT
        ).show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void ativaCamadaPessoa(){
        mMapView.getGraphicsOverlays().clear();
        if (mCallout != null) {
            mCallout.dismiss();
        }
        map.getOperationalLayers().clear();
        map.getOperationalLayers().add(featureLayerPessoas);
        mMapView.setMap(map);

        mCallout = mMapView.getCallout();
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
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
                final ListenableFuture<FeatureQueryResult> future = serviceFeatureTablePessoas
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
                        calloutContent.setLines(4);
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
                                if (key.equals("de_nome")) {
                                    calloutContent.append("Nome: " + value + "\n");
                                } else if (key.equals("de_tipo_residencia")){
                                    calloutContent.append("Residência: " + value + "\n");
                                } else if (key.equals("de_recorrencia")){
                                    calloutContent.append("Recorrência: " + value + "\n");
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
        Toast.makeText(
                MapActivity.this,
                "Camada de pessoas ativada!",
                Toast.LENGTH_SHORT
        ).show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void ativaCamadaApoio(){
        mMapView.getGraphicsOverlays().clear();
        if (mCallout != null) {
            mCallout.dismiss();
        }
        map.getOperationalLayers().clear();
        map.getOperationalLayers().add(featureLayerPontosDeApoio);
        mMapView.setMap(map);

        mCallout = mMapView.getCallout();
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
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
                final ListenableFuture<FeatureQueryResult> future = serviceFeatureTablePontosDeApoio
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
                        calloutContent.setLines(4);
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
                                if (key.equals("de_name")) {
                                    calloutContent.append("Nome: " + value + "\n");
                                } else if (key.equals("de_tipo")){
                                    calloutContent.append("Tipo: " + value + "\n");
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
        Toast.makeText(
                MapActivity.this,
                "Camada de pontos de apoio ativada!",
                Toast.LENGTH_SHORT
        ).show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void ativaCamadaAreaRisco(){
        mMapView.getGraphicsOverlays().clear();
        if (mCallout != null) {
            mCallout.dismiss();
        }
        map.getOperationalLayers().clear();
        map.getOperationalLayers().add(featureLayerAreasDeRisco);
        mMapView.setMap(map);

        mCallout = mMapView.getCallout();
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
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
        Toast.makeText(
                MapActivity.this,
                "Camada de área de risco ativada!",
                Toast.LENGTH_SHORT
        ).show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void ativaCamadaAbrigo(){
        mMapView.getGraphicsOverlays().clear();
        if (mCallout != null) {
            mCallout.dismiss();
        }
        map.getOperationalLayers().clear();
        map.getOperationalLayers().add(featureLayerAbrigos);
        mMapView.setMap(map);

        mCallout = mMapView.getCallout();
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
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
        Toast.makeText(
                MapActivity.this,
                "Camada de abrigo ativada!",
                Toast.LENGTH_SHORT
        ).show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void ativaCamadaLona(){
        mMapView.getGraphicsOverlays().clear();
        if (mCallout != null) {
            mCallout.dismiss();
        }
        map.getOperationalLayers().clear();
        map.getOperationalLayers().add(featureLayerSolicitacaoLona);
        mMapView.setMap(map);

        mCallout = mMapView.getCallout();
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
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
        Toast.makeText(
                MapActivity.this,
                "Camada de lona ativada!",
                Toast.LENGTH_SHORT
        ).show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void ativaCamadaVistoria(){
        mMapView.getGraphicsOverlays().clear();
        if (mCallout != null) {
            mCallout.dismiss();
        }
        map.getOperationalLayers().clear();
        map.getOperationalLayers().add(featureLayerVistoria);
        mMapView.setMap(map);

        mCallout = mMapView.getCallout();
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
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
        Toast.makeText(
                MapActivity.this,
                "Camada de vistoria ativada",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void ativaCamadas(){
        checkTodos.setChecked(true);
        mMapView.getGraphicsOverlays().clear();
        if (mCallout != null) {
            mCallout.dismiss();
        }
        map.getOperationalLayers().clear();
        // create feature layer with its service feature table
        // create the service feature table
        serviceFeatureTablePontosDeApoio = new ServiceFeatureTable(
                getResources().getString(R.string.pontos_de_apoio_url));
        serviceFeatureTableEquipamentosPublicos = new ServiceFeatureTable(
                getResources().getString(R.string.equipamentos_publicos_url));
        serviceFeatureTableOcorrencias = new ServiceFeatureTable(
                getResources().getString(R.string.ocorrencias_url));
        serviceFeatureTableChamados = new ServiceFeatureTable(
                getResources().getString(R.string.chamados_url));
        serviceFeatureTableAbrigos = new ServiceFeatureTable(
                getResources().getString(R.string.abrigos_url));
        serviceFeatureTablePessoas = new ServiceFeatureTable(
                getResources().getString(R.string.pessoas_url));
        serviceFeatureTableSolicitacaoLona = new ServiceFeatureTable(
                getResources().getString(R.string.solicitacao_de_lona_url));
        serviceFeatureTableVistoria = new ServiceFeatureTable(
                getResources().getString(R.string.vistoria_url));
        serviceFeatureTableEdificacoes = new ServiceFeatureTable(
                getResources().getString(R.string.edificacoes_url));
        serviceFeatureTableAreasDeRisco = new ServiceFeatureTable(
                getResources().getString(R.string.areas_de_risco_url));
        serviceFeatureTableBairro = new ServiceFeatureTable(
                getResources().getString(R.string.bairro_url));
        serviceFeatureTableLote = new ServiceFeatureTable(
                getResources().getString(R.string.lote_url));

        // create the feature layer using the service feature table
        featureLayerPontosDeApoio = new FeatureLayer(serviceFeatureTablePontosDeApoio);
        featureLayerEquipamentosPublicos = new FeatureLayer(serviceFeatureTableEquipamentosPublicos);
        featureLayerOcorrencias = new FeatureLayer(serviceFeatureTableOcorrencias);
        featureLayerChamados = new FeatureLayer(serviceFeatureTableChamados);
        featureLayerAbrigos = new FeatureLayer(serviceFeatureTableAbrigos);
        featureLayerPessoas = new FeatureLayer(serviceFeatureTablePessoas);
        featureLayerSolicitacaoLona = new FeatureLayer(serviceFeatureTableSolicitacaoLona);
        featureLayerVistoria = new FeatureLayer(serviceFeatureTableVistoria);
        featureLayerEdificacoes = new FeatureLayer(serviceFeatureTableEdificacoes);
        featureLayerAreasDeRisco = new FeatureLayer(serviceFeatureTableAreasDeRisco);
        featureLayerBairro = new FeatureLayer(serviceFeatureTableBairro);
        featureLayerLote = new FeatureLayer(serviceFeatureTableLote);


        // add the layer to the map
        map.getOperationalLayers().add(featureLayerPontosDeApoio);
        map.getOperationalLayers().add(featureLayerEquipamentosPublicos);
        map.getOperationalLayers().add(featureLayerOcorrencias);
        map.getOperationalLayers().add(featureLayerChamados);
        map.getOperationalLayers().add(featureLayerAbrigos);
        map.getOperationalLayers().add(featureLayerPessoas);
        map.getOperationalLayers().add(featureLayerSolicitacaoLona);
        map.getOperationalLayers().add(featureLayerVistoria);
        map.getOperationalLayers().add(featureLayerEdificacoes);
        map.getOperationalLayers().add(featureLayerAreasDeRisco);
        map.getOperationalLayers().add(featureLayerBairro);
        map.getOperationalLayers().add(featureLayerLote);

        // set the map to be displayed in the mapview
        mMapView.setMap(map);

        Toast.makeText(
                MapActivity.this,
                "Todas camadas ativadas!",
                Toast.LENGTH_SHORT
        ).show();
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



package com.example.administrator.loadshpactivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;

import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.core.geodatabase.ShapefileFeatureTable;
import com.esri.core.renderer.Renderer;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.Symbol;

import java.io.FileNotFoundException;

import com.example.administrator.loadshpactivity.tdtutil.TianDiTuTiledMapServiceLayer;
import com.example.administrator.loadshpactivity.tdtutil.TianDiTuTiledMapServiceType;


public class MainActivity extends AppCompatActivity {
    MapView mMapView;
    ArcGISTiledMapServiceLayer tileLayer;
    FeatureLayer featureLayer;

    // 天地图
    /**
     * 矢量地图
     */
    public TianDiTuTiledMapServiceLayer t_vec;
    /**
     * 矢量标注
     */
    public TianDiTuTiledMapServiceLayer t_cva;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView=(MapView)findViewById(R.id.mapView);
//        tileLayer = new ArcGISTiledMapServiceLayer(
//                "http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer");
//
//       mMapView.addLayer(tileLayer);
// 天地图 矢量
        t_vec = new TianDiTuTiledMapServiceLayer(
                TianDiTuTiledMapServiceType.VEC_C);
        mMapView.addLayer(t_vec);
        t_cva = new TianDiTuTiledMapServiceLayer(
                TianDiTuTiledMapServiceType.CVA_C);
        mMapView.addLayer(t_cva);



        String shpPath=getSDPath()+ "/download/test.shp";
        //String shpPath="/mnt/sdcard/download/bou2_4p.shp";
        Toast.makeText(this,shpPath,Toast.LENGTH_LONG).show();
        Symbol symbol=new SimpleFillSymbol(Color.BLUE);
        Renderer renderer=new SimpleRenderer(symbol);
        try {
            ShapefileFeatureTable shapefileFeatureTable=new ShapefileFeatureTable(shpPath);
            featureLayer=new FeatureLayer(shapefileFeatureTable);
            Toast.makeText(this,featureLayer.getFeatureTable().getTableName(),Toast.LENGTH_LONG).show();
            featureLayer.setRenderer(renderer);
            mMapView.addLayer(featureLayer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    public String getSDPath(){
        String sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);   //判断sd卡是否存在
        if   (sdCardExist)
        {
            sdDir = Environment.getExternalStorageDirectory().getAbsolutePath();//获取跟目录
        }
        return sdDir;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

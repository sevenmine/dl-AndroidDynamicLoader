package com.dianping.example.activityloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import dalvik.system.DexClassLoader;

public class ActivityLoader extends ListActivity {

    private List<Map<String, String>> data = new ArrayList<Map<String, String>>();
    
    private AssetManager asm;
    private Resources    res;
    private Theme        thm;
    private ClassLoader  cl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            AssetManager asset = getAssets();
            for (String s : asset.list("apks")) {
                addItem(s, "apks/" + s);
            }
        } catch (Exception e) {
        }

        SimpleAdapter adapter = new SimpleAdapter(this, data, android.R.layout.simple_list_item_1,
                                                  new String[] { "title" }, new int[] { android.R.id.text1 });
        setListAdapter(adapter);
    }

    private void addItem(String title, String path) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("title", title);
        map.put("path", path);
        data.add(map);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        
        Map<String, String> item = data.get(position);
        String title = item.get("title");
        String path = item.get("path");

        try {
            File dex = getDir("dex", Context.MODE_PRIVATE);
            dex.mkdir();
            File f = new File(dex, title);
            InputStream fis = getAssets().open(path);
            FileOutputStream fos = new FileOutputStream(f);
            byte[] buffer = new byte[0xFF];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fis.close();
            fos.close();

            File fo = getDir("outdex", Context.MODE_PRIVATE);
            fo.mkdir();
            DexClassLoader dcl = new DexClassLoader(f.getAbsolutePath(), fo.getAbsolutePath(), null,
                                                    MyApplication.ORIGINAL_LOADER.getParent());
            MyApplication.CUSTOM_LOADER = dcl;
            
//            Intent i = new Intent("com.dianping.intent.action.SAMPLE_ACTIVITY");
//            i.putExtra("path", path);
//            i.putExtra("class", "com.dianping.example.activity.SampleActivity");
            Class<?> clazz = dcl.loadClass("com.dianping.example.activity.SampleActivity");
            Intent i = new Intent(this, clazz);
            startActivity(i);

            Toast.makeText(this, title + " loaded, try launch again", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Unable to load " + title, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            MyApplication.CUSTOM_LOADER = null;
        }
    }
    
    @Override
    public AssetManager getAssets() {
        return asm == null ? super.getAssets() : asm;
    }

    @Override
    public Resources getResources() {
        return res == null ? super.getResources() : res;
    }

    @Override
    public Theme getTheme() {
        return thm == null ? super.getTheme() : thm;
    }

    @Override
    public ClassLoader getClassLoader() {
        return cl == null ? super.getClassLoader() : cl;
    }
}

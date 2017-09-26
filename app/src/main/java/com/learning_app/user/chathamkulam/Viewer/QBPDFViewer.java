package com.learning_app.user.chathamkulam.Viewer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.learning_app.user.chathamkulam.FetchDownloadManager;
import com.learning_app.user.chathamkulam.Fragments.Drawer;
import com.learning_app.user.chathamkulam.R;
import com.learning_app.user.chathamkulam.Registration.Registration;
import com.learning_app.user.chathamkulam.Sqlite.StoreEntireDetails;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by User on 3/22/2017.
 */

public class QBPDFViewer extends AppCompatActivity {

    String fileName;

    PDFView qBankPdfView;
    WebView qBankWebView;
    TextView txtRibbon;

    SearchView searchView;
    ProgressDialog qBankProgress;

    String firstWord;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewer_qbank);

        qBankPdfView = (PDFView) findViewById(R.id.qBankPdfView);
        qBankWebView = (WebView) findViewById(R.id.qBankWebView);
        txtRibbon = (TextView) findViewById(R.id.txtRibbon);
        qBankWebView.setBackgroundColor(0x00000000);

        txtRibbon.setVisibility(View.GONE);
        qBankPdfView.setVisibility(View.GONE);
        qBankWebView.setVisibility(View.GONE);

        qBankProgress = new ProgressDialog(this);
        qBankProgress.setMessage("Loading...");
        qBankProgress.setIndeterminate(false);
        qBankProgress.setCancelable(false);

        final String currentSubjectName = getIntent().getStringExtra("Key_pdf");


        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {

            public void run() {

                if (currentSubjectName != null) {

                    qBankPdfView.setVisibility(View.VISIBLE);
                    try {
                        getSupportActionBar().setTitle(currentSubjectName);

                        File mydir = getApplicationContext().getDir("Chathamkulam", Context.MODE_PRIVATE); //Creating an internal dir;
                        final File rootPath = new File(mydir, currentSubjectName);

                        if (rootPath.exists()) {

                            File listFile[] = rootPath.listFiles();
                            if (listFile != null && listFile.length > 0) {

                                for (File aListFile : listFile) {

                                    if (aListFile.isFile()) {

                                        if (aListFile.getName().endsWith(".pdf")) {

                                            fileName = aListFile.getName();
                                            firstWord = fileName.substring(0, 1);

                                            Log.d("##QBPDFViewer : ", firstWord+"   "+fileName);

                                            if (firstWord.equals("#")) {

                                                handler.post(new Runnable() {
                                                    public void run() {

                                                        File rootPathh = new File(rootPath, fileName);
                                                        qBankPdfView.fromFile(rootPathh).scrollHandle(new DefaultScrollHandle(getApplicationContext())
                                                        ).onRender(new OnRenderListener() {
                                                            @Override
                                                            public void onInitiallyRendered(int nbPages, float pageWidth, float pageHeight) {
                                                                qBankPdfView.fitToWidth();
                                                            }
                                                        }).load();
                                                    }
                                                });
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        } else {

                            Log.d("FolderInfo", "Not found");

                            final AlertDialog.Builder builder = new AlertDialog.Builder(QBPDFViewer.this);
                            builder.setTitle("Restoration Process");
                            builder.setMessage("Your file contents are missing do you want restore it, By free downloading?")
                                    .setCancelable(false)
                                    .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                                        public void onClick(DialogInterface dialog, int id) {

                                            StoreEntireDetails storeEntireDetails = new StoreEntireDetails(getApplicationContext());
                                            Cursor cursor = storeEntireDetails.getSubjectRow(currentSubjectName);
                                            Log.d("cursorInfo", String.valueOf(cursor.getCount()));

                                            if (cursor.getCount() != 0) {

                                                if (cursor.moveToFirst()) {

                                                    do {

                                                        String country = cursor.getString(1);
                                                        String university = cursor.getString(2);
                                                        String course = cursor.getString(3);
                                                        String semester = cursor.getString(4);
                                                        String subjectId = cursor.getString(5);
                                                        String subjectNumber = cursor.getString(6);
                                                        String subject = cursor.getString(7);
                                                        String subjectCost = cursor.getString(8);
                                                        String trial = cursor.getString(9);
                                                        String duration = cursor.getString(10);
                                                        String notes_count = cursor.getString(11);
                                                        String qbank_count = cursor.getString(12);
                                                        String video_count = cursor.getString(13);
                                                        String zip_url = cursor.getString(14);
                                                        String validityTill = cursor.getString(15);
                                                        String progress = cursor.getString(16);
                                                        String status = cursor.getString(17);

                                                        new Thread(new FetchDownloadManager(zip_url, country, university, course, semester, subjectId, subjectNumber, subject,
                                                                subjectCost, trial, duration, notes_count, qbank_count, video_count, getApplicationContext(), "restore")).start();

                                                        startActivity(new Intent(getApplicationContext(), Drawer.class));

                                                    } while (cursor.moveToNext());

                                                }
                                                cursor.close();
                                            }

                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                            dialog.cancel();
                                            Intent intent = new Intent(QBPDFViewer.this, Drawer.class);
                                            startActivity(intent);
                                        }
                                    });

                            handler.post(new Runnable() {
                                public void run() {

                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                final String subjectName = getIntent().getStringExtra("Key_subjectName");
                String resultUrl = getIntent().getStringExtra("Key_OnlineJsonVideo");

                Log.v("Online Values", subjectName + resultUrl);

                if (subjectName != null) {

                    try {
                        handler.post(new Runnable() {
                            public void run() {
                                getSupportActionBar().setTitle(subjectName);
                                qBankWebView.setVisibility(View.VISIBLE);
                                txtRibbon.setVisibility(View.VISIBLE);
                            }
                        });

                        JSONArray jArray = null;
                        jArray = new JSONArray(resultUrl);

                        String url = "";
                        for (int i = 0; i < jArray.length(); i++) {
                            JSONObject json_obj = jArray.getJSONObject(i);
                            url = json_obj.getString("url");
                        }

                        final String finalUrl = url;
                        handler.post(new Runnable() {
                            public void run() {

                                qBankWebView.setWebViewClient(new MyWebViewClient());
                                qBankWebView.getSettings().setJavaScriptEnabled(true);
                                qBankWebView.loadUrl("http://docs.google.com/gview?embedded=true&url=" + finalUrl);
                                qBankWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        new Thread(runnable).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.menu_submit).setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_search:

//                searchView = (SearchView) MenuItemCompat.getActionView(item);
//                SearchOnWebView searchOnWebView = new SearchOnWebView();
//                searchOnWebView.search(this,qBankWebView,searchView);

                MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {

                        qBankWebView.clearMatches();

                        return true;
                    }
                });

                break;

            case R.id.menu_share:

                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "https://play.google.com/store/apps/details?id=com.learning_app.user.chathamkulam&hl=en";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));

                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Registration.deleteCache(getApplicationContext());
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            qBankProgress.show();
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onLoadResource(WebView view, String url) {

        }

        @Override
        public void onPageFinished(WebView view, String url) {

            // TODO Auto-generated method stub
            qBankProgress.dismiss();
            super.onPageFinished(view, url);

        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            // TODO Auto-generated method stub
//            qBankProgressBar.setVisibility(View.GONE);
            super.onPageStarted(view, url, favicon);
            qBankProgress.show();

        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            //Your code to do
            qBankProgress.dismiss();
            Toast.makeText(getApplicationContext(), "Your Internet Connection May not be active Or " + error, Toast.LENGTH_LONG).show();

        }

    }
}

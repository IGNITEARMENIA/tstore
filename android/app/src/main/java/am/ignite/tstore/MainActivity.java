package am.ignite.tstore;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.GeolocationPermissions;
import android.content.pm.PackageManager;
import android.Manifest;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;
import android.widget.FrameLayout;

public class MainActivity extends Activity {
    private static final String HOME = "https://tstore.am/";
    private static final String HOST = "tstore.am";
    private static final String HOST2 = "www.tstore.am";
    private static final String HOST3 = "tstore.tstore.workers.dev";
    private static final String OLD_HOST = "ignitearmenia.github.io";
    private WebView web;
    private View offline;
    private String geoOrigin;
    private GeolocationPermissions.Callback geoCb;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor("#FBF8F3"));
        web = new WebView(this);
        root.addView(web);
        offline = buildOffline();
        offline.setVisibility(View.GONE);
        root.addView(offline);
        setContentView(root);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setSupportMultipleWindows(false);
        s.setJavaScriptCanOpenWindowsAutomatically(false);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setUserAgentString(s.getUserAgentString() + " TStoreApp/1");

        s.setGeolocationEnabled(true);
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback cb) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    cb.invoke(origin, true, false);
                } else {
                    geoOrigin = origin; geoCb = cb;
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 7);
                }
            }
        });
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                return handle(req.getUrl());
            }
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError err) {
                if (req.isForMainFrame()) offline.setVisibility(View.VISIBLE);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                if (!url.startsWith("about:")) offline.setVisibility(View.GONE);
            }
        });

        if (state != null) web.restoreState(state);
        else web.loadUrl(HOME);
    }

    private boolean handle(Uri u) {
        String scheme = u.getScheme() == null ? "" : u.getScheme();
        String host = u.getHost() == null ? "" : u.getHost();
        if ((scheme.equals("https") || scheme.equals("http")) && (host.equals(HOST) || host.equals(HOST2) || host.equals(HOST3) || host.equals(OLD_HOST))) return false;
        try {
            Intent i;
            if (scheme.equals("intent")) {
                i = Intent.parseUri(u.toString(), Intent.URI_INTENT_SCHEME);
            } else {
                i = new Intent(Intent.ACTION_VIEW, u);
            }
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (ActivityNotFoundException | java.net.URISyntaxException e) {
            if (scheme.equals("https") || scheme.equals("http")) return false;
        }
        return true;
    }

    private View buildOffline() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setBackgroundColor(Color.parseColor("#FBF8F3"));
        box.setPadding(48, 48, 48, 48);
        TextView t = new TextView(this);
        t.setText("Нет интернета\nՉկա ինտերնետ · No internet");
        t.setTextSize(18);
        t.setTextColor(Color.parseColor("#2B1F1A"));
        t.setGravity(Gravity.CENTER);
        box.addView(t);
        Button b = new Button(this);
        b.setText("↻");
        b.setOnClickListener(v -> { offline.setVisibility(View.GONE); web.reload(); });
        box.addView(b);
        return box;
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] res) {
        super.onRequestPermissionsResult(code, perms, res);
        if (code == 7 && geoCb != null) {
            boolean ok = false;
            for (int r : res) if (r == PackageManager.PERMISSION_GRANTED) ok = true;
            geoCb.invoke(geoOrigin, ok, false);
            geoCb = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }
}

package am.ignite.tstore.admin;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

/** Opens the T Store admin (Claude artifact) in a Chrome custom tab, which reuses the Chrome sign-in. */
public class AdminActivity extends Activity {
    private static final String ADMIN = "https://claude.ai/artifact/2QJxp4zEXdgyzpHp2Nb1jd";
    private static final String[] BROWSERS = {"com.android.chrome", "com.chrome.beta", "com.sec.android.app.sbrowser", "org.mozilla.firefox", "com.microsoft.emmx"};

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(ADMIN));
        Bundle extras = new Bundle();
        extras.putBinder("android.support.customtabs.extra.SESSION", null);
        i.putExtras(extras);
        i.putExtra("android.support.customtabs.extra.TOOLBAR_COLOR", 0xFF2B1F1A);
        i.putExtra("android.support.customtabs.extra.TITLE_VISIBILITY", 1);
        String pkg = browser();
        if (pkg != null) i.setPackage(pkg);
        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ADMIN)));
            } catch (ActivityNotFoundException e2) {
                Toast.makeText(this, "Установите Chrome", Toast.LENGTH_LONG).show();
            }
        }
        finish();
    }

    private String browser() {
        PackageManager pm = getPackageManager();
        for (String p : BROWSERS) {
            try { pm.getPackageInfo(p, 0); return p; } catch (PackageManager.NameNotFoundException ignored) {}
        }
        return null;
    }
}

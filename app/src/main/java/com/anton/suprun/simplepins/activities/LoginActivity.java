package com.anton.suprun.simplepins.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.anton.suprun.simplepins.R;
import com.anton.suprun.simplepins.data.Constants;
import com.anton.suprun.simplepins.tools.Tools;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "Pins_LoginAct";
    private WebView mWebView;
    private FBAuthWebClient mWebClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Tools.isUserLogged()) {
            startMapActivity();
        } else {
            setContentView(R.layout.activity_login);
            mWebView = (WebView) findViewById(R.id.webview);

            if (mWebClient == null) {
                mWebClient = new FBAuthWebClient();
            }
            mWebView.setWebViewClient(mWebClient);

            loadLoginPage();
        }
    }

    private void loadLoginPage() {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        mWebView.loadUrl(Constants.FB_AUTH_URL);
    }

    private void startMapActivity() {
        Intent mapActivityIntent = new Intent(getApplicationContext(), MapActivity.class);
        startActivity(mapActivityIntent);
    }

    class FBAuthWebClient extends WebViewClient {
        private String userId;

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (Uri.parse(url).getHost().equals(Constants.FB_BASIC_HOST)) {
                return true;
            }

            // On successful login obtain user id
            if (Uri.parse(url).getPath().contains(Constants.FB_SUCCESS_PAGE)) {
                Pattern pattern = Pattern.compile("access_token=(\\S+)&");
                Matcher matcher = pattern.matcher(Uri.parse(url).getFragment());
                String token = "";
                if (matcher.find()) {
                    token = matcher.group(1);
                }

                view.loadUrl(String.format(Constants.FB_GET_USER_ID_URL, token));
                return true;
            }
            return false;
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            // User id is obtained from response
            if (Uri.parse(url).getLastPathSegment().equals("me")) {
                getUserId(url);
            }
            return super.shouldInterceptRequest(view, url);
        }

        private void getUserId(final String url) {
            StringBuilder content = new StringBuilder();

            // Connect to the given URL, obtain content as Input Stream
            try {
                URL aURL = new URL(url);
                URLConnection conn = aURL.openConnection();
                conn.connect();
                InputStream is = conn.getInputStream();
                int symbol;
                while ((symbol = is.read()) != -1) {
                    content.append((char) symbol);
                }
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                showLoginFailedError();
            }

            // Successful response contains JSON { "id":"1243..." }
            Pattern pattern = Pattern.compile("\\\"id\\\":\\\"(\\d+)\"");
            Matcher matcher = pattern.matcher(content.toString());
            if (matcher.find()) {
                userId = matcher.group(1);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            // If user is successfully retrieved, it's possibly to start Map Activity
            if (Uri.parse(url).getLastPathSegment().equals("me")) {
                if (!TextUtils.isEmpty(userId)) {
                    Tools.setUserId(userId);
                    startMapActivity();
                } else {
                    showLoginFailedError();
                }
            }
        }
    }

    private void showLoginFailedError() {
        AlertDialog alert = new AlertDialog.Builder(this).create();
        alert.setMessage(getString(R.string.login_failed));
        alert.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.button_retry), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loadLoginPage();
            }
        });
    }
}

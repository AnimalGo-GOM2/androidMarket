package animalgo.market;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.SoundEffectConstants;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.webkit.GeolocationPermissions;

// 원스토어
import com.onestore.app.licensing.AppLicenseChecker;
import com.onestore.app.licensing.LicenseCheckerListener;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    // 원스토어
    private String TAG = MainActivity.class.getSimpleName();
    private AppLicenseChecker appLicenseChecker;
    private static final String BASE64_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCJwyT6d6cakNfaWifLo5b4lmvwxFxA58uiwAlaVrCdfkFYedqeF167+sYL3lM4wUxVSAH0D83obvOqUrKrh7r7VpuYCqu4+aLOwphk3U9IYr+6AJ1KUamhfMi25u5isFjDUyjCyZSo/leJ9TM7hJfz7cFtmIGJmR3M5dOnrpun8wIDAQAB";
    private static final String PID = "0000748908";
    // 끝

    Context mContext;
    WebView browser;
    SwipeRefreshLayout pullfresh;
    private final Handler handler = new Handler();

    boolean isShowRefreash = false;

    private static final String TYPE_IMAGE = "image/*";
    private static final int INPUT_FILE_REQUEST_CODE = 1;

    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;

    private static String fcmToken = "";

    UploadHandler mUploadHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 원스토어
        appLicenseChecker = new AppLicenseChecker(MainActivity.this, BASE64_PUBLIC_KEY, new AppLicenseListener());
        appLicenseChecker.queryLicense();
        // 끝

        mContext = this;
        browser = ((WebView)findViewById(R.id.webView));

        ImageView iv = findViewById(R.id.ex1);
        Glide.with(this)
                .load(R.drawable.gif)

                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(iv);

        View view = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (view != null) {
                // 23 버전 이상일 때 상태바 하얀 색상에 회색 아이콘 색상을 설정
                view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
            }
        }else if (Build.VERSION.SDK_INT >= 21) {
            // 21 버전 이상일 때
            getWindow().setStatusBarColor(Color.BLACK);
        }

        if(getIntent().hasExtra("link")){
            setBrowser(getIntent().getStringExtra("link"));
        }
        else{
            setBrowser("getMarketUrl");
        }

    }

    // 원스토어
    private class AppLicenseListener implements LicenseCheckerListener {
        @Override
        public void granted(String license, String signature) {
            if (isFinishing()) {
                return;
            }
            Toast.makeText(MainActivity.this,"granted", Toast.LENGTH_LONG).show();
        }

        @Override
        public void denied() {
            if (isFinishing()) {
                return;
            }
            deniedDialog();
        }

        @Override
        public void error(int errorCode, String error) {
            if (isFinishing()) {
                return;
            }

            if (2000 == errorCode && 2100 > errorCode ) {
                unknownErrorDialog();
            } else if (2100 == errorCode) {
                goSettingForNetwork();
            } else if (2101 == errorCode) {
                retryLoginDialog();
            } else if (2102 == errorCode) {
                Error2012();
            } else if (2103 == errorCode) {
                retryInstall();
            }  else if (2104 == errorCode) {
                retryALC();
            } else if (2 == errorCode) {
                goSettingForNetwork();
            } else if (3 == errorCode) {
                Error3();
            } else if (5 == errorCode) {
                unknownErrorDialog();
            } else {
                unknownErrorDialog();
            }
        }
    }

    private void retryALC() {
        if(null == appLicenseChecker)
            appLicenseChecker = new AppLicenseChecker(MainActivity.this, BASE64_PUBLIC_KEY, new AppLicenseListener());
        appLicenseChecker.queryLicense();
    }

    private void showDialog(String message,
                            String positiveText,
                            String negativeText,
                            DialogInterface.OnClickListener positiveClickListener,
                            DialogInterface.OnClickListener negativeClickListener) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(positiveText, positiveClickListener);
        alertDialog.setNegativeButton(negativeText, negativeClickListener);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    private void deniedDialog() {
        showDialog("Purchase history does not exist, Would you like to buy...",
                "ok",
                "finish",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                "https://onestore.co.kr/userpoc/apps/view?pid="+PID));
                        startActivity(marketIntent);
                    }
                },
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

    }

    private void goSettingForNetwork() {
        showDialog("Do you want to move to the network settings screen?",
                "setting",
                "finish",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                        startActivityForResult(intent, 0);
                    }
                },
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
    }

    private void retryLoginDialog() {
        showDialog("ONE store login is required, Do you want to try again?",
                "retry",
                "finish",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        retryALC();
                    }
                },
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
    }

    private void retryInstall() {
        showDialog("ONE store service installation is required, Do you want...",
                "ok",
                "finish",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://m.onestore.co.kr/mobilepoc/etc/downloadGuide.omp"));
                        startActivity(intent);
                    }
                },
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
    }

    private void unknownErrorDialog() {
        showDialog("Unknown error, Please contact One store, Do you want to...",
                "retry",
                "finish",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        retryALC();
                    }
                },
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
    }

    private void Error2012() {
        showDialog("Installing one store service...",
                "retry",
                "finish",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        retryALC();
                    }
                },
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
    }

    private void Error3() {
        showDialog("Please update your library to the latest version.",
                "retry",
                "finish",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        retryALC();
                    }
                },
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
    }
    // 끝

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {


        FirebaseApp.initializeApp(this);
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                fcmToken = instanceIdResult.getToken();
                Log.e("fcm token", fcmToken);
            }
        });
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // 원스토어
        if (null != appLicenseChecker)
            appLicenseChecker.destroy();
        // 끝
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);

        if(hasFocus == false){

        }
        else {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == Controller.FILE_SELECTED) {
            // Chose a file from the file picker.
            if (mUploadHandler != null) {
                mUploadHandler.onResult(resultCode, intent);
            }
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }


    private void setBrowser(String url){

        //웹뷰 설정
        WebSettings settings = browser.getSettings();    //세팅 불러옴
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setSupportMultipleWindows(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(true);
        settings.setUseWideViewPort(true);
        settings.setTextZoom(100);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW); // 혼합된 컨텐츠 허용
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);										 // Cookie 허용
            cookieManager.setAcceptThirdPartyCookies(browser, true);
        }

        //캐시사용 설정
        File dir = mContext.getCacheDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        settings.setAppCachePath(dir.getPath());
        settings.setAppCacheEnabled(true);

        String userAgent = settings.getUserAgentString();
        userAgent = userAgent.replaceAll("; webview", "");
        userAgent = userAgent.replaceAll("; wv", "");
        userAgent = userAgent.replaceAll("webview", "");
        userAgent = userAgent.replaceAll("wv", "");

        Log.e("userAgent", userAgent);
        settings.setUserAgentString(userAgent);

        browser.addJavascriptInterface(new AndroidBridge(), "native");

        //browser.setWebChromeClient(mWebChromeClient);
        browser.setWebChromeClient(new MyWebChromeClient());

        browser.setWebViewClient(mWebViewClient);

        browser.loadUrl(url);
    }



    private File resizeImage(String path){

        //수정된 이미지를 저장할 임시 폴더를 생성합니다.
        String tmpPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        tmpPath += "/animalgo";
        File tmpPathFile = new File(tmpPath);
        tmpPathFile.mkdirs();


        //선택한 파일의 명칭을 가져옵니다.
        String returnFileName = path.replace("file:", "") ;


        //파일명 부분을 가져온후, 임시폴더+파일명으로 저장할 전체경로를 정합니다.
        String[] splitPath = returnFileName.split("/");
        String savaPath = tmpPath+"/"+splitPath[splitPath.length-1];
        Log.e("저장할 전체경로", savaPath);

        // 4등분한 크기로 조절된 비트맵을 생성합니다.
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);


        //파일로부터 방향 가져와서 matrix 회전시킵니다.
        int orientation = 0;
        try {

            ExifInterface ei = new ExifInterface(path);
            orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            Log.e("불러온 이미지 방향", orientation+"");

            Matrix matrix = new Matrix();

            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    break;
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
                    break;
            }

            //회전된 비트맵
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            //회전된 비트맵을 파일로 저장합니다.
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

            File saveFile = new File(savaPath);
            FileOutputStream fo = new FileOutputStream(saveFile);
            fo.write(bytes.toByteArray());
            fo.close();
            return saveFile ;
        }
        catch(Exception e){
            //sendNoti("디버그용", "에러(02)-"+e.toString());
            return null;
        }

    }




    private boolean checkAppInstalled(WebView view , String url , String type){
        if(type.equals("intent")){
            return intentSchemeParser(view, url);
        } else if(type.equals("customLink")){
            return customSchemeParser(view, url);
        }
        return false;
    }

    private boolean intentSchemeParser(WebView view , String url) {
        try {
            Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
            if(getPackageManager().resolveActivity(intent , 0) == null){
                String pakagename = intent.getPackage();
                if(pakagename != null) {
                    Uri uri = Uri.parse("market://details?id="+pakagename);
                    intent = new Intent(Intent.ACTION_VIEW , uri);
                    startActivity(intent);
                    return true;
                }
            }
            Uri uri = Uri.parse(intent.getDataString());
            intent = new Intent(Intent.ACTION_VIEW , uri);
            startActivity(intent);
            return true;

        } catch (URISyntaxException e) {
        }
        return false;
    }

    private boolean customSchemeParser(WebView view , String url) {
        String packageName = null;
        if(url.startsWith("shinhan-sr-ansimclick://")) {        //신한 앱카드
            packageName = "com.shcard.smartpay";
        }else if(url.startsWith("mpocket.online.ansimclick://")) {  //삼성앱카드
            packageName = "kr.co.samsungcard.mpocket";
        } else if(url.startsWith("hdcardappcardansimclick://")) {       //현대안심결제
            packageName = "com.hyundaicard.appcard";
        } else if(url.startsWith("droidxantivirusweb:")){               //droidx 백신
            packageName = "net.nshc.droidxantivirus";
        } else if(url.startsWith("vguardstart://") || url.startsWith("vguardend://")){  //vguard백신
            packageName = "kr.co.shiftworks.vguardweb";
        } else if(url.startsWith("hanaansim")){         //하나외환앱카드
            packageName = "com.ilk.visa3d";
        } else if(url.startsWith("nhappcardansimclick://")) { //농협앱카드
            packageName = "nh.smart.mobilecard";
        } else if(url.startsWith("ahnlabv3mobileplus")){
            packageName = "com.ahnlab.v3mobileplus";
        } else if(url.startsWith("smartxpay-transfer://")){
            packageName = "kr.co.uplus.ecredit";
        } else if(url.startsWith("ispmobile://")){
            packageName = "kvp.jjy.MispAndroid320";
        } else if(url.startsWith("kb-acp://")){
            packageName = "com.kbcard.cxh.appcard";
        }
        else {
            return false;
        }

        Intent intent = null;
        //하드코딩된 패키지명으로 앱 설치여부를 판단하여 해당 앱 실행 또는 마켓 이동
        if(chkAppInstalled(view,packageName)){

            try {
                intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                Uri uri = Uri.parse(intent.getDataString());
                intent = new Intent(Intent.ACTION_VIEW , uri);
                startActivity(intent);
                return true;
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            Uri uri = Uri.parse("market://details?id="+packageName);
            intent = new Intent(Intent.ACTION_VIEW , uri);
            startActivity(intent);
            return true;
        }

        return false;
    }


    private boolean chkAppInstalled(WebView view , String packagePath){
        boolean appInstalled = false;
        try {
            getPackageManager().getPackageInfo(packagePath, PackageManager.GET_ACTIVITIES);
            appInstalled = true;
        } catch(PackageManager.NameNotFoundException e){
            appInstalled = false;
        }
        return appInstalled;
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
    }


    private Uri getResultUri(Intent data) {
        Uri result = null;
        if(data == null || TextUtils.isEmpty(data.getDataString())) {
            // If there is not data, then we may have taken a photo
            if(mCameraPhotoPath != null) {
                result = Uri.parse(mCameraPhotoPath);
            }
        } else {
            String filePath = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                filePath = data.getDataString();
            } else {
                filePath = "file:" + RealPathUtil.getRealPath(this, data.getData());
            }
            result = Uri.parse(filePath);
        }

        return result;
    }

    BackPressCloseHandler backPressCloseHandler = new BackPressCloseHandler(this);
    @Override
    public void onBackPressed() {
//        if (browser.canGoBack()) {
//            browser.goBack();
//        } else {
//            super.onBackPressed();
//        }
        backPressCloseHandler.onBackPressed();
    }

    class MyWebChromeClient extends WebChromeClient {
        public MyWebChromeClient() {

        }

        private String getTitleFromUrl(String url) {
            String title = url;
            try {
                URL urlObj = new URL(url);
                String host = urlObj.getHost();
                if (host != null && !host.isEmpty()) {
                    return urlObj.getProtocol() + "://" + host;
                }
                if (url.startsWith("file:")) {
                    String fileName = urlObj.getFile();
                    if (fileName != null && !fileName.isEmpty()) {
                        return fileName;
                    }
                }
            } catch (Exception e) {
                // ignore
            }

            return title;
        }

        @Override
        public boolean onJsAlert(android.webkit.WebView view, String url, String message, final JsResult result) {
            String newTitle = getTitleFromUrl(url);

            new AlertDialog.Builder(MainActivity.this).setTitle(newTitle).setMessage(message).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    result.confirm();
                }
            }).setCancelable(false).create().show();
            return true;
            // return super.onJsAlert(view, url, message, result);
        }

        @Override
        public boolean onJsConfirm(android.webkit.WebView view, String url, String message, final JsResult result) {

            String newTitle = getTitleFromUrl(url);

            new AlertDialog.Builder(MainActivity.this).setTitle(newTitle).setMessage(message).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    result.confirm();
                }
            }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    result.cancel();
                }
            }).setCancelable(false).create().show();
            return true;

            // return super.onJsConfirm(view, url, message, result);
        }

        // 191120 for KaKaoMap Location
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            super.onGeolocationPermissionsShowPrompt(origin, callback);
            callback.invoke(origin, true, false);
        }

        // Android 2.x
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            openFileChooser(uploadMsg, "");
        }

        // Android 3.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            openFileChooser(uploadMsg, "", "filesystem");
        }

        // Android 4.1
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            mUploadHandler = new UploadHandler(new Controller());
            mUploadHandler.openFileChooser(uploadMsg, acceptType, capture);
        }

        // Android 4.4, 4.4.1, 4.4.2
        // openFileChooser function is not called on Android 4.4, 4.4.1, 4.4.2,
        // you may use your own java script interface or other hybrid framework.

        // Android 5.0.1
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public boolean onShowFileChooser(
                android.webkit.WebView webView, ValueCallback<Uri[]> filePathCallback,
                FileChooserParams fileChooserParams) {

            String acceptTypes[] = fileChooserParams.getAcceptTypes();

            String acceptType = "";
            for (int i = 0; i < acceptTypes.length; ++ i) {
                if (acceptTypes[i] != null && acceptTypes[i].length() != 0)
                    acceptType += acceptTypes[i] + ";";
            }
            if (acceptType.length() == 0)
                acceptType = "*/*";

            final ValueCallback<Uri[]> finalFilePathCallback = filePathCallback;

            ValueCallback<Uri> vc = new ValueCallback<Uri>() {

                @Override
                public void onReceiveValue(Uri value) {

                    Uri[] result;
                    if (value != null)
                        result = new Uri[]{value};
                    else
                        result = null;

                    finalFilePathCallback.onReceiveValue(result);

                }
            };

            openFileChooser(vc, acceptType, "filesystem");


            return true;
        }
    };

    class Controller {
        final static int FILE_SELECTED = 4;

        Activity getActivity() {
            return MainActivity.this;
        }
    }

    class UploadHandler {
        /*
         * The Object used to inform the WebView of the file to upload.
         */
        private ValueCallback<Uri> mUploadMessage;
        private String mCameraFilePath;
        private boolean mHandled;
        private boolean mCaughtActivityNotFoundException;
        private Controller mController;
        public UploadHandler(Controller controller) {
            mController = controller;
        }
        String getFilePath() {
            return mCameraFilePath;
        }
        boolean handled() {
            return mHandled;
        }
        void onResult(int resultCode, Intent intent) {
            if (resultCode == Activity.RESULT_CANCELED && mCaughtActivityNotFoundException) {
                // Couldn't resolve an activity, we are going to try again so skip
                // this result.
                mCaughtActivityNotFoundException = false;
                return;
            }
            Uri result = intent == null || resultCode != Activity.RESULT_OK ? null
                    : intent.getData();
            // As we ask the camera to save the result of the user taking
            // a picture, the camera application does not return anything other
            // than RESULT_OK. So we need to check whether the file we expected
            // was written to disk in the in the case that we
            // did not get an intent returned but did get a RESULT_OK. If it was,
            // we assume that this result has came back from the camera.
            if (result == null && intent == null && resultCode == Activity.RESULT_OK) {
                File cameraFile = new File(mCameraFilePath);
                if (cameraFile.exists()) {
                    result = Uri.fromFile(cameraFile);
                    // Broadcast to the media scanner that we have a new photo
                    // so it will be added into the gallery for the user.
                    mController.getActivity().sendBroadcast(
                            new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result));
                }
            }

            if(result != null) {
                String realPath = RealPathUtil.getRealPath(mContext, result);

                if (!realPath.equals("") && !realPath.equals(null) && (realPath.endsWith(".jpg") || realPath.endsWith(".jpeg") || realPath.endsWith(".png") || realPath.endsWith(".gif") || realPath.endsWith(".bmp"))) {
                    mUploadMessage.onReceiveValue(Uri.fromFile(resizeImage(realPath)));
                } else {
                    mUploadMessage.onReceiveValue(result);
                }
            }
            else {

                mUploadMessage.onReceiveValue(result);
            }

            mHandled = true;
            mCaughtActivityNotFoundException = false;
        }

        void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            final String imageMimeType = "image/*";
            final String videoMimeType = "video/*";
            final String audioMimeType = "audio/*";
            final String mediaSourceKey = "capture";
            final String mediaSourceValueCamera = "camera";
            final String mediaSourceValueFileSystem = "filesystem";
            final String mediaSourceValueCamcorder = "camcorder";
            final String mediaSourceValueMicrophone = "microphone";
            // According to the spec, media source can be 'filesystem' or 'camera' or 'camcorder'
            // or 'microphone' and the default value should be 'filesystem'.
            String mediaSource = mediaSourceValueFileSystem;
            if (mUploadMessage != null) {
                // Already a file picker operation in progress.
                return;
            }
            mUploadMessage = uploadMsg;
            // Parse the accept type.
            String params[] = acceptType.split(";");
            String mimeType = params[0];
            if (capture.length() > 0) {
                mediaSource = capture;
            }
            if (capture.equals(mediaSourceValueFileSystem)) {
                // To maintain backwards compatibility with the previous implementation
                // of the media capture API, if the value of the 'capture' attribute is
                // "filesystem", we should examine the accept-type for a MIME type that
                // may specify a different capture value.
                for (String p : params) {
                    String[] keyValue = p.split("=");
                    if (keyValue.length == 2) {
                        // Process key=value parameters.
                        if (mediaSourceKey.equals(keyValue[0])) {
                            mediaSource = keyValue[1];
                        }
                    }
                }
            }
            //Ensure it is not still set from a previous upload.
            mCameraFilePath = null;
            if (mimeType.equals(imageMimeType)) {
                if (mediaSource.equals(mediaSourceValueCamera)) {
                    // Specified 'image/*' and requested the camera, so go ahead and launch the
                    // camera directly.
                    startActivity(createCameraIntent());
                    return;
                } else {
                    // Specified just 'image/*', capture=filesystem, or an invalid capture parameter.
                    // In all these cases we show a traditional picker filetered on accept type
                    // so launch an intent for both the Camera and image/* OPENABLE.
                    Intent chooser = createChooserIntent(createCameraIntent());
                    chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(imageMimeType));
                    startActivity(chooser);
                    return;
                }
            } else if (mimeType.equals(videoMimeType)) {
                if (mediaSource.equals(mediaSourceValueCamcorder)) {
                    // Specified 'video/*' and requested the camcorder, so go ahead and launch the
                    // camcorder directly.
                    startActivity(createCamcorderIntent());
                    return;
                } else {
                    // Specified just 'video/*', capture=filesystem or an invalid capture parameter.
                    // In all these cases we show an intent for the traditional file picker, filtered
                    // on accept type so launch an intent for both camcorder and video/* OPENABLE.
                    Intent chooser = createChooserIntent(createCamcorderIntent());
                    chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(videoMimeType));
                    startActivity(chooser);
                    return;
                }
            } else if (mimeType.equals(audioMimeType)) {
                if (mediaSource.equals(mediaSourceValueMicrophone)) {
                    // Specified 'audio/*' and requested microphone, so go ahead and launch the sound
                    // recorder.
                    startActivity(createSoundRecorderIntent());
                    return;
                } else {
                    // Specified just 'audio/*',  capture=filesystem of an invalid capture parameter.
                    // In all these cases so go ahead and launch an intent for both the sound
                    // recorder and audio/* OPENABLE.
                    Intent chooser = createChooserIntent(createSoundRecorderIntent());
                    chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(audioMimeType));
                    startActivity(chooser);
                    return;
                }
            }
            // No special handling based on the accept type was necessary, so trigger the default
            // file upload chooser.
            startActivity(createDefaultOpenableIntent());
        }
        private void startActivity(Intent intent) {
            try {
                mController.getActivity().startActivityForResult(intent, Controller.FILE_SELECTED);
            } catch (ActivityNotFoundException e) {
                // No installed app was able to handle the intent that
                // we sent, so fallback to the default file upload control.
                try {
                    mCaughtActivityNotFoundException = true;
                    mController.getActivity().startActivityForResult(createDefaultOpenableIntent(),
                            Controller.FILE_SELECTED);
                } catch (ActivityNotFoundException e2) {
                    // Nothing can return us a file, so file upload is effectively disabled.
                    Toast.makeText(mController.getActivity(), "File Upload",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
        private Intent createDefaultOpenableIntent() {
            // Create and return a chooser with the default OPENABLE
            // actions including the camera, camcorder and sound
            // recorder where available.
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            Intent chooser = createChooserIntent(createCameraIntent(), createCamcorderIntent(),
                    createSoundRecorderIntent());
            chooser.putExtra(Intent.EXTRA_INTENT, i);
            return chooser;
        }
        private Intent createChooserIntent(Intent... intents) {
            Intent chooser = new Intent(Intent.ACTION_CHOOSER);
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);
            chooser.putExtra(Intent.EXTRA_TITLE, "File Upload");
            return chooser;
        }
        private Intent createOpenableIntent(String type) {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType(type);
            return i;
        }
        private Intent createCameraIntent() {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File externalDataDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM);
            File cameraDataDir = new File(externalDataDir.getAbsolutePath() +
                    File.separator + "Webbme");
            cameraDataDir.mkdirs();
            mCameraFilePath = cameraDataDir.getAbsolutePath() + File.separator +
                    System.currentTimeMillis() + ".jpg";
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mCameraFilePath)));
            return cameraIntent;
        }
        private Intent createCamcorderIntent() {
            return new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        }
        private Intent createSoundRecorderIntent() {
            return new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        }
    }
    WebViewClient mWebViewClient = new WebViewClient() {


        public static final String INTENT_PROTOCOL_START = "intent:";
        public static final String INTENT_PROTOCOL_INTENT = "#Intent;";
        public static final String INTENT_PROTOCOL_END = ";end;";
        public static final String GOOGLE_PLAY_STORE_PREFIX = "market://details?id=";

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url=request.getUrl().toString();
            Log.e("shouldOverrideUrl", url);

            if(url.startsWith("intent")){
                return checkAppInstalled(view, url, "intent");
            }else if (url != null && url.startsWith("market://")) {
                try {
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    if (intent != null) {
                        startActivity(intent);
                    }
                    return true;
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    return false;
                }
            } else if(url.startsWith("http://") || url.startsWith("https://")) {
                view.loadUrl(url);
                return false;
            }
            else if (url.startsWith("tel:")) {
                Intent tel = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                startActivity(tel);
                return true;
            }
            else if (url.startsWith("mailto:")) {
                String body = "Enter your Question, Enquiry or Feedback below:\n\n";
                Intent mail = new Intent(Intent.ACTION_SEND);
                mail.setType("application/octet-stream");
                mail.putExtra(Intent.EXTRA_EMAIL, new String[]{"email address"});
                mail.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                mail.putExtra(Intent.EXTRA_TEXT, body);
                startActivity(mail);
                return true;
            }
            else {
                return checkAppInstalled(view, url , "customLink");
            }

        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e("shouldOverrideUrl", url);


            if(url.startsWith("intent")){
                return checkAppInstalled(view, url, "intent");
            }else if (url != null && url.startsWith("market://")) {
                try {
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    if (intent != null) {
                        startActivity(intent);
                    }
                    return true;
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    return false;
                }
            } else if(url.startsWith("http://") || url.startsWith("https://")) {
                view.loadUrl(url);
                return false;
            }
            else if (url.startsWith("tel:")) {
                Intent tel = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                startActivity(tel);
                return true;
            }
            else if (url.startsWith("mailto:")) {
                String body = "Enter your Question, Enquiry or Feedback below:\n\n";
                Intent mail = new Intent(Intent.ACTION_SEND);
                mail.setType("application/octet-stream");
                mail.putExtra(Intent.EXTRA_EMAIL, new String[]{"email address"});
                mail.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                mail.putExtra(Intent.EXTRA_TEXT, body);
                startActivity(mail);
                return true;
            }
            else {
                return checkAppInstalled(view, url , "customLink");
            }

        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon){
            super.onPageStarted(view, url, favicon);
            Log.e("onPageStarted", url);
        }


        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.e("onPageFinished", url);
            if(url.equals("getMarketUrl/")) {
                findViewById(R.id.layout_intro).setVisibility(View.GONE);
            }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            //showToast("에러발생"+error.toString(), mContext);
            //finish();
        }

    };


    private class AndroidBridge{

        @JavascriptInterface
        public void finishApp(){
            MainActivity.this.finish();
        }

        //토스트 출력 (기본, 짧은출력)
        @JavascriptInterface
        public void showToast(final String msg){
            handler.post(new Runnable(){
                public void run(){
                    Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                }
            });
        }

        //토스트 출력 (긴 출력)
        @JavascriptInterface
        public void showToastLong(final String msg){
            handler.post(new Runnable(){
                public void run(){
                    Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
                }
            });
        }

        //다른앱으로 텍스트 전달
        @JavascriptInterface
        public void sendText(final String title, final String text){
            Intent it = new Intent(Intent.ACTION_SEND);
            it.setType("text/plain");
            it.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(it, title));
        }

        //기본 버튼음 발생시키기
        @JavascriptInterface
        public void clickSound(){
            handler.post(new Runnable(){
                public void run(){
                    AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                    audioManager.playSoundEffect(SoundEffectConstants.CLICK);
                }
            });

        }

        //버전이름 조회
        @JavascriptInterface
        public String getPackageVersionName(String packageName){
            String version = null;
            try {
                PackageInfo i = mContext.getPackageManager().getPackageInfo(packageName, 0);
                version = i.versionName;
            } catch(PackageManager.NameNotFoundException e) { }

            return version;
        }

        //버전코드 조회
        @JavascriptInterface
        public String getPackageVersionCode(String packageName){
            int version = 0;
            try {
                PackageInfo i = mContext.getPackageManager().getPackageInfo(packageName, 0);
                version = i.versionCode;
            } catch(PackageManager.NameNotFoundException e) { }

            return String.valueOf(version);
        }

        //구글 플레이 스토어 어플 상세보기 페이지 링크
        @JavascriptInterface
        public void linkStore(String packageName){
            String url = "";

            PackageManager pm = getPackageManager();
            String installed = "0";
            try {
                pm.getPackageInfo("com.android.vending", PackageManager.GET_ACTIVITIES);
                url = "market://details?id="+packageName;
            } catch (PackageManager.NameNotFoundException e) {
                url = "https://market.android.com/details?id="+packageName;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);

            intent.setData(Uri.parse(url));

            mContext.startActivity(intent);
        }



        //휴대폰번호 조회
        @JavascriptInterface
        public String getLineNumber(){
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

            try {
                String number_1 = telephonyManager.getLine1Number();
                if((number_1 != null) && (number_1.indexOf("+82") > -1))
                {
                    number_1 = number_1.replace("+82", "0");
                }
                return number_1.replace("-", "");
            }
            catch (SecurityException e){
                return "";
            }
        }

        //패키지 존재확인
        @JavascriptInterface
        public String isPackageExists(String targetPackage){
            PackageManager pm = getPackageManager();
            try {
                PackageInfo info=pm.getPackageInfo(targetPackage,PackageManager.GET_META_DATA);
            } catch (PackageManager.NameNotFoundException e) {
                return "0";
            }
            return "1";
        }


        //fcm 토큰 조회
        @JavascriptInterface
        public String getMarketToken(){
            return fcmToken;
        }
    }
}

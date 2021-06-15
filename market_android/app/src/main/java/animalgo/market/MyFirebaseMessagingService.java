package animalgo.market;

/**
 * Created by sinxo on 2017-08-07.
 */

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.sql.Timestamp;
import java.util.List;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

            // TODO(developer): Handle FCM messages here.
            // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
            Log.e(TAG, "From: " + remoteMessage.getFrom());

            // Check if message contains a data payload.
            if (remoteMessage.getData().size() > 0) {
                Log.e(TAG, "FCM: " + remoteMessage.getData());
                if(remoteMessage.getData().containsKey("link") && remoteMessage.getData().get("link").startsWith("http")){
                    Log.e(TAG, "FCM: " + "링크있음");
                    sendNoti_Message(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"), "link", remoteMessage.getData().get("link"));
                }
                else{
                    Log.e(TAG, "FCM: " + "링크없음");
                    sendNoti_Message(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"), "", "");
                }
            }

        if (remoteMessage.getNotification() != null) {
            //Log.e(TAG, "Message data type: " + remoteMessage.getData().get("type"));

            //sendNoti_Message(remoteMessage.getData().get("title"), remoteMessage.getData().get("message"), remoteMessage.getData().get("work"), remoteMessage.getData().get("url"));

        }


    }

    @Override
    public void onNewToken(String refreshedToken) {
        // Enter your code
    }

    private void sendNoti_Message(String title, String message, String work, String link) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String notiId = timestamp.getTime()+"";
        notiId = notiId.substring(5);

        Log.e("노티 아이디", notiId);
        Intent intent = createIntent(work, link);
        /*
        Intent intent = new Intent(this, Splash.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        */
        PendingIntent pendingIntent = null;
        if(intent != null) {
            pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);
        }

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Integer.valueOf(notiId)/* ID of notification */, notificationBuilder.build());
    }


    /*

    private void sendNoti_Image(String title, String message, String image, String work, String link) {

        try{
            URL url = new URL("http://85slide.85pro.kr/85slide/data/push_img/"+image);
            URLConnection conn = url.openConnection();
            conn.connect();
            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
            Bitmap imgBitmap = BitmapFactory.decodeStream(bis);
            bis.close();

            Intent intent = createIntent(work, link);


            PendingIntent pendingIntent = null;
            if(intent != null) {
                pendingIntent = PendingIntent.getActivity(this, 0 , intent, PendingIntent.FLAG_ONE_SHOT);
            }

            Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "default")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

            NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle();
            style.setBigContentTitle(title);
            style.setSummaryText(message);
            style.bigPicture(imgBitmap);
            notificationBuilder.setStyle(style);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0 , notificationBuilder.build());
        }
        catch(Exception e){
            e.printStackTrace();
        }


    }



*/
    private Intent createIntent(String work, String url){

        Intent intent;



        switch(work){

            case "link":
                intent = new Intent(this, intro.class);
                intent.putExtra("link", url);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                break;
            case "browser":
                Log.e("웹열기", "intent 설정");
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                break;

            default:
                intent = new Intent(this, intro.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                break;
        }










        return intent;
    }


    private boolean isPackageExists(String targetPackage){
        PackageManager pm = getPackageManager();
        try {
            PackageInfo info=pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }


    /*
    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.logo_small : R.drawable.logo;
    }
*/
    public static boolean isForegroundActivity(Context context, Class<?> cls) {
        if(cls == null)
            return false;

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> info = activityManager.getRunningTasks(1);
        ActivityManager.RunningTaskInfo running = info.get(0);
        ComponentName componentName = running.topActivity;

        Log.e(TAG, "확인할 액티비티 : " + cls.getName());
        return cls.getName().equals(componentName.getClassName());
    }
}
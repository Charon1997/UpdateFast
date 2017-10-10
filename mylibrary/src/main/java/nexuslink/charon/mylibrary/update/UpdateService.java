package nexuslink.charon.mylibrary.update;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.File;

import nexuslink.charon.mylibrary.R;

/**
 * 项目名称：UpdateFast
 * 类描述：
 * 创建人：Charon
 * 创建时间：2017/10/11 6:52
 * 修改人：Charon
 * 修改时间：2017/10/11 6:52
 * 修改备注：
 */

public class UpdateService extends Service {
    private String apkUrl;
    private String filePath;
    private NotificationManager notificationManager;
    private Notification notification;

    private int icon = 0;


    @Override
    public void onCreate() {
        Log.e("tag", "UpdateService onCreate()");
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("tag", "UpdateService onStartCommand()");
        if(intent==null){
            notifyUser(getString(R.string.update_download_failed), 0);
            //onStartCommand()结束后停止Service
            stopSelf();
        }
        assert intent != null;
        icon = intent.getIntExtra("icon",0);
        apkUrl = intent.getStringExtra("apkUrl");
        //外部存储的路径
        filePath = intent.getStringExtra("apkFilePath");
        notifyUser(getString(R.string.update_download_start), 0);
        startDownload();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startDownload() {
        UpdateManager.getInstance().startDownloads(apkUrl, filePath, new UpdateDownloadListener() {
            @Override
            public void onStarted() {
                notifyUser(getString(R.string.update_download_start), 0);
            }

            @Override
            public void onProgressChanged(int progress, String downloadUrl) {
                notifyUser(getString(R.string.update_download_processing), progress);
            }

            @Override
            public void onPaused() {

            }

            @Override
            public void onFinished(float completeSize, String downloadUrl) {
                notifyUser(getString(R.string.update_download_finish), 100);
                stopSelf();
            }

            @Override
            public void onFailure() {
                notifyUser(getString(R.string.update_download_failed), 0);
                stopSelf();
            }
        });
    }

    /**
     * 更新notification
     * @param result
     * @param progress
     */
    private void notifyUser(String result, int progress){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        if (icon != 0) {
            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), icon)).setSmallIcon(icon);
        }
        builder.setShowWhen(false)
                .setTicker(result)
                .setContentIntent(progress>=100 ? getContentIntent() :
                        //空的intent
                        PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT));
        if(progress>0 && progress<=100){
            //false为横条，ture为圈圈
            builder.setProgress(100,progress,false);
            builder.setAutoCancel(false);
            builder.setOngoing(true);
            builder.setContentTitle(result+"..."+progress+"%");
        }else{
            builder.setContentTitle(result);
            builder.setProgress(0, 0, false);
            builder.setAutoCancel(true);
            builder.setOngoing(false);
        }

        notification = builder.build();
        notificationManager.notify(0, notification);
    }

    /**
     * 进入apk安装程序
     * @return
     */
    private PendingIntent getContentIntent() {
        Log.e("tag", "getContentIntent()");
        File apkFile = new File(filePath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://"+apkFile.getAbsolutePath()),
                "application/vnd.android.package-archive");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        startActivity(intent);
        return pendingIntent;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


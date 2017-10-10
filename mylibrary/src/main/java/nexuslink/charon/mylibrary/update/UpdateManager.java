package nexuslink.charon.mylibrary.update;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 项目名称：UpdateFast
 * 类描述：
 * 创建人：Charon
 * 创建时间：2017/10/11 6:52
 * 修改人：Charon
 * 修改时间：2017/10/11 6:52
 * 修改备注：
 */

public class UpdateManager {
    private static final String TAG = UpdateManager.class.getSimpleName();
    private static UpdateManager updateManager;
    //创建线程池
    private ThreadPoolExecutor threadPoolExecutor;
    private UpdateDownloadRequest request;

    private UpdateManager() {
        threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    static {
        updateManager = new UpdateManager();
    }

    public static UpdateManager getInstance() {
        return updateManager;
    }

    void startDownloads(String downloadUrl, String localPath, UpdateDownloadListener listener) {
        if (request != null) {
            return;
        }
        checkLocalFilePath(localPath);
        //开始下载任务
        request = new UpdateDownloadRequest(downloadUrl, localPath, listener);
        threadPoolExecutor.submit(request);
    }

    /**
     * 检查文件路径是否存在
     *
     * @param path
     */
    private void checkLocalFilePath(String path) {
        Log.e("tag", path);
        File dir = new File(path.substring(0, path.lastIndexOf("/") + 1));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(path);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 开始下载
     * @param mContext
     * @param title
     * @param message
     * @param positiveString
     * @param negativeString
     * @param apkUrl
     * @param filePath
     * @param icon
     */
    public void startDownloadApk(final Context mContext, String title, String message, String positiveString, String negativeString
            , final String apkUrl, final String filePath, final int icon) {
        //检测版本号,获取到apkUrl
        AlertDialog dialog = new AlertDialog.Builder(mContext).setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveString, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: CheckVersion");
                        Intent intent = new Intent(mContext,UpdateService.class);
                        intent.putExtra("icon", icon);
                        intent.putExtra("apkUrl",apkUrl);
                        intent.putExtra("apkFilePath",filePath);
                        mContext.startService(intent);
                    }
                }).setNegativeButton(negativeString, null).show();
    }


    /**
     * 开始下载
     * @param context
     * @param apkUrl
     * @param filePath
     * @param icon
     */
    public void startDownloadApk(Context context, String apkUrl, String filePath,int icon) {
        Intent intent = new Intent(context,UpdateService.class);
        intent.putExtra("icon", icon);
        intent.putExtra("apkUrl",apkUrl);
        intent.putExtra("apkFilePath",filePath);
        context.startService(intent);
    }

}

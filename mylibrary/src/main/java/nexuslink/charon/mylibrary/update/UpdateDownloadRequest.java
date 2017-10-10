package nexuslink.charon.mylibrary.update;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

/**
 * 项目名称：UpdateFast
 * 类描述：
 * 创建人：Charon
 * 创建时间：2017/10/11 6:51
 * 修改人：Charon
 * 修改时间：2017/10/11 6:51
 * 修改备注：
 */

public class UpdateDownloadRequest implements Runnable {
    //APPurl地址，保存在本地的路径
    private String downloadUrl, localFilePath;
    private UpdateDownloadListener downloadListener;
    //下载的状态
    private boolean isDownloading = false;
    //当前下载的长度
    private long currentLength;
    private DownloadResponseHandler downloadHandler;

    public UpdateDownloadRequest(String downloadUrl, String localFilePath, UpdateDownloadListener downloadListener) {
        this.downloadUrl = downloadUrl;
        this.localFilePath = localFilePath;
        this.downloadListener = downloadListener;
        this.isDownloading = true;
        this.downloadHandler = new DownloadResponseHandler();
    }

    /**
     * 格式化数字，精确到两位
     *
     * @param value
     * @return
     */
    private String getTwoPointFloatStr(double value) {
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(value);
    }

    /**
     * 真的建立连接
     *
     * @throws IOException
     */
    private void makeRequest() throws IOException {
        //如果当前线程未被打断
        if (!Thread.currentThread().isInterrupted()) {
            URL url = new URL(downloadUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            //保持连接
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.connect();//这里会阻塞我们当前的线程
            currentLength = connection.getContentLength();
            if (!Thread.currentThread().isInterrupted()) {
                //获取输入流，完成下载
                downloadHandler.sendResponseMessage(connection.getInputStream());
            }
        }
    }

    @Override
    public void run() {
        try {
            makeRequest();
        } catch (IOException ignored) {

        }
    }

    /**
     * 下载过程中可能出现的异常
     */
    public enum FailureCode {
        UnknownHost, Socket, SocketTimeout, connectionTimeout, IO, HttpResponse,
        Json, Interrupted

    }

    private class DownloadResponseHandler {

        protected static final int SUCCESS_MESSAGE = 0;
        protected static final int FAILURE_MESSAGE = 1;
        protected static final int START_MESSAGE = 2;
        protected static final int FINISH_MESSAGE = 3;
        protected static final int NETWORK_OFF = 4;
        private static final int PROGRESS_CHANGED = 5;

        private long completeSize = 0;
        private int progress = 0;

        private Handler handler;

        DownloadResponseHandler() {
            handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    handleSelfMessage(msg);
                }
            };
        }

        //统一通过sendMessage发送msg信息
        void sendFinishMessage() {
            sendMessage(obtainMessage(FINISH_MESSAGE, null));
        }

        private void sendProgressChangedMessage(int progress) {
            sendMessage(obtainMessage(PROGRESS_CHANGED, new Object[]{progress}));
        }

        void sendFailureMessage(FailureCode failureCode) {
            sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[]{failureCode}));

        }

        void sendMessage(Message msg) {
            if (handler != null) {
                handler.sendMessage(msg);
            } else {
                handleSelfMessage(msg);
            }
        }

        //将信息统一转成msg
        Message obtainMessage(int responseMessge, Object response) {
            Message msg = null;
            if (handler != null) {
                msg = handler.obtainMessage(responseMessge, response);
            } else {
                msg = Message.obtain();
                msg.what = responseMessge;
                msg.obj = response;
            }
            return msg;
        }

        void handleSelfMessage(Message msg) {
            Object[] response;
            switch (msg.what) {
                case FAILURE_MESSAGE:
                    response = (Object[]) msg.obj;
                    handleFailureMessage((FailureCode) response[0]);
                    break;
                case PROGRESS_CHANGED:
                    response = (Object[]) msg.obj;
                    handleProgressChangedMessage((Integer) response[0]);
                    break;
                case FINISH_MESSAGE:
                    onFinish();
                    break;
            }
        }

        /**
         * 各种接口回调
         */
        void handleProgressChangedMessage(int progress) {
            downloadListener.onProgressChanged(progress, downloadUrl);
        }

        void onFinish() {
            downloadListener.onFinished(completeSize, "");
        }

        void onFailure(FailureCode failureCode) {
            downloadListener.onFailure();
        }

        private void handleFailureMessage(FailureCode failureCode) {
            onFailure(failureCode);
        }

        /**
         * 下载中
         * @param is
         */
        void sendResponseMessage(InputStream is) {
            RandomAccessFile randomAccessFile = null;
            completeSize = 0;
            try {
                byte[] buffer = new byte[1024];
                int length = -1;//读写长度
                int limit = 0;
                randomAccessFile = new RandomAccessFile(localFilePath, "rwd");//可读可写权限

                while ((length = is.read(buffer)) != -1) {
                    if (isDownloading) {
                        //写到本地
                        randomAccessFile.write(buffer, 0, length);
                        completeSize += length;
                        if (completeSize < currentLength) {
                            //正在下载
                            Log.e("tag", "completeSize=" + completeSize);
                            Log.e("tag", "currentLength=" + currentLength);

                            progress = (int) (Float.parseFloat(getTwoPointFloatStr(1.0 * completeSize / currentLength)) * 100);
                            Log.e("tag", "下载进度：" + progress);
                            if (limit % 30 == 0 && progress <= 100) {//隔30次更新一次notification
                                sendProgressChangedMessage(progress);
                            }
                            limit++;
                        }
                    }
                }
                //下载完成
                isDownloading = false;
                sendFinishMessage();
            } catch (IOException e) {
                //下载失败
                sendFailureMessage(FailureCode.IO);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (randomAccessFile != null) {
                        randomAccessFile.close();
                    }
                } catch (IOException e) {
                    sendFailureMessage(FailureCode.IO);
                }
            }
        }
    }
}


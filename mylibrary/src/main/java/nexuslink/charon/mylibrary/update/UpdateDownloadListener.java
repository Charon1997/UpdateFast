package nexuslink.charon.mylibrary.update;

/**
 * 项目名称：UpdateFast
 * 类描述：
 * 创建人：Charon
 * 创建时间：2017/10/11 6:50
 * 修改人：Charon
 * 修改时间：2017/10/11 6:50
 * 修改备注：
 */

public interface UpdateDownloadListener {
    /**
     * 下载请求开始
     */
    public void onStarted();

    /**
     * 进度更新回调
     * @param progress
     * @param downloadUrl
     */
    public void onProgressChanged(int progress,String downloadUrl);

    public void onPaused();

    /**
     * 下载完成回调
     * @param completeSize
     * @param downloadUrl
     */
    public void onFinished(float completeSize, String downloadUrl);

    /**
     * 下载失败回调
     */
    public void onFailure();

}

package tw.app.uniload.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;

import java.util.List;

public class Download {

    private SharedPreferences downloadSettings;

    public Download(Context context, String url, DownloaderListener listener) {
        Log.d("FileDownloader", "Preparing Download...");

        downloadSettings = context.getSharedPreferences("downloadSettings", Activity.MODE_PRIVATE);

        String fileType = ".png";

        if (url.contains(".jpg")) fileType = ".jpg";
        if (url.contains(".png")) fileType = ".png";
        if (url.contains(".mp4")) fileType = ".mp4";
        if (url.contains(".webm")) fileType = ".webm";

        String fileName = RandomString.getRandomString(8);

        if (!downloadSettings.getString("prefixFileName", "").isEmpty()) {
            fileName = downloadSettings.getString("prefixFileName", "") + "_" + fileName;
        }

        if (!downloadSettings.getString("sufixFileName", "").isEmpty()) {
            fileName = fileName + "_" + downloadSettings.getString("sufixFileName", "");
        }

        FileDownloader.getImpl().create(url)
                .setPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Uniload/" + fileName + fileType)
                .setListener(new FileDownloadListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                    }

                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        int progress = ((int) 100 * soFarBytes / totalBytes);
                        Log.d("FileDownloader", "Downloading " + progress + "%...");
                        listener.OnProgress(progress);
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        Log.d("FileDownloader", "Downloaded!");
                        listener.OnSuccess();
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        Log.d("FileDownloader", "Error Downloading: " + e.getMessage());
                        listener.OnError(e.getMessage());
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {

                    }
                }).start();
    }

    public static void MultiDownload(Context context, List<String> urls, MultiDownloaderListener listener) {
        FileDownloadListener queueTarget = new FileDownloadListener() {
            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {

            }

            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                listener.OnProgress(100 * totalBytes / soFarBytes);
            }

            @Override
            protected void completed(BaseDownloadTask task) {
                listener.OnSuccess();
            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
                listener.OnError(e.getMessage());
            }

            @Override
            protected void warn(BaseDownloadTask task) {

            }
        };

        SharedPreferences downloadSettings = context.getSharedPreferences("downloadSettings", Activity.MODE_PRIVATE);

        for (String url : urls) {
            String fileType = ".png";

            if (url.contains(".jpg")) fileType = ".jpg";
            if (url.contains(".png")) fileType = ".png";
            if (url.contains(".mp4")) fileType = ".mp4";
            if (url.contains(".webm")) fileType = ".webm";

            String fileName = RandomString.getRandomString(8);

            if (!downloadSettings.getString("prefixFileName", "").isEmpty()) {
                fileName = downloadSettings.getString("prefixFileName", "") + "_" + fileName;
            }

            if (!downloadSettings.getString("sufixFileName", "").isEmpty()) {
                fileName = fileName + "_" + downloadSettings.getString("sufixFileName", "");
            }

            FileDownloader.getImpl().create(url)
                    .setPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Uniload/" + fileName + fileType)
                    .setCallbackProgressTimes(0)
                    .setListener(queueTarget)
                    .asInQueueTask()
                    .enqueue();
        }

        FileDownloader.getImpl().start(queueTarget, false);
    }

    public interface DownloaderListener {
        void OnProgress(int progress);

        void OnSuccess();

        void OnError(String reason);
    }

    public interface MultiDownloaderListener {
        void OnProgress(int progress);

        void OnSuccess();

        void OnError(String reason);
    }
}

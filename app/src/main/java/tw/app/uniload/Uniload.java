package tw.app.uniload;

import android.app.Application;

import com.liulishuo.filedownloader.FileDownloader;

public class Uniload extends Application {
    @Override
    public void onCreate() {
        FileDownloader.setup(this);
        super.onCreate();
    }
}

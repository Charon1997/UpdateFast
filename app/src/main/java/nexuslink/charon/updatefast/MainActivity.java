package nexuslink.charon.updatefast;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import nexuslink.charon.mylibrary.update.UpdateManager;

public class MainActivity extends AppCompatActivity {
    private UpdateManager manager = UpdateManager.getInstance();
    private Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button2);
        manager.init(this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.startDownloadApk(MainActivity.this,"软件更新了","杀了个程序员祭天","更新","放弃","http://app.mi.com/download/77791", Environment.getExternalStorageDirectory()+"/AppUpdate/updatetest.apk",R.mipmap.ic_launcher);
            }
        });

    }
}

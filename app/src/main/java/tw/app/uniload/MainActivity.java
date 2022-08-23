package tw.app.uniload;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.dlyt.yanndroid.oneui.dialog.AlertDialog;
import de.dlyt.yanndroid.oneui.dialog.ProgressDialog;
import de.dlyt.yanndroid.oneui.layout.ToolbarLayout;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearLayoutManager;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearSnapHelper;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.SnapHelper;
import de.dlyt.yanndroid.oneui.utils.OnSingleClickListener;
import de.dlyt.yanndroid.oneui.view.RecyclerView;
import de.dlyt.yanndroid.oneui.view.Toast;
import tw.app.uniload.adapters.ImagesAdapter;
import tw.app.uniload.downloaders.Instagram;
import tw.app.uniload.utils.Download;

public class MainActivity extends AppCompatActivity {

    private ToolbarLayout toolbarLayoutMain;
    private LinearLayout rootLinear;
    private LinearLayout welcomeLinear;
    private EditText urlEdittext;
    private MaterialButton downloadButton;
    private LinearLayout processingLinear;
    private LinearLayout finalLinear;
    private RecyclerView imagesRecyclerView;
    private MaterialButton backButton;
    private MaterialButton settingsButton;
    private MaterialTextView apiInfoText;
    private EditText prefixEdittext;
    private EditText sufixEdittext;
    private MaterialButton downloadAllButton;
    private LinearLayout finalButtonsLinear;

    private int downloadPosition = 0;

    private static final String WELCOME_LINEAR = "WELCOME_L";
    private static final String PROCESSING_LINEAR = "PROCESSING_L";
    private static final String FINAL_LINEAR = "FINAL_L";

    private ImagesAdapter adapter;

    private SharedPreferences apiInfo;
    private SharedPreferences downloadSettings;

    private List<String> imagesUrlListToAllDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        setup();
    }

    private void initView() {
        apiInfo = getSharedPreferences("apiInfo", MODE_PRIVATE);
        downloadSettings = getSharedPreferences("downloadSettings", MODE_PRIVATE);

        toolbarLayoutMain = (ToolbarLayout) findViewById(R.id.toolbar_layout_main);
        rootLinear = (LinearLayout) findViewById(R.id.root_linear);
        welcomeLinear = (LinearLayout) findViewById(R.id.welcome_linear);
        urlEdittext = (EditText) findViewById(R.id.url_edittext);
        downloadButton = (MaterialButton) findViewById(R.id.download_button);
        processingLinear = (LinearLayout) findViewById(R.id.processing_linear);
        finalLinear = (LinearLayout) findViewById(R.id.final_linear);
        imagesRecyclerView = (RecyclerView) findViewById(R.id.images_recycler_view);
        backButton = (MaterialButton) findViewById(R.id.back_button);
        settingsButton = (MaterialButton) findViewById(R.id.settings_button);
        apiInfoText = (MaterialTextView) findViewById(R.id.api_info_text);
        prefixEdittext = (EditText) findViewById(R.id.prefix_edittext);
        sufixEdittext = (EditText) findViewById(R.id.sufix_edittext);
        downloadAllButton = (MaterialButton) findViewById(R.id.download_all_button);
        finalButtonsLinear = (LinearLayout) findViewById(R.id.final_buttons_linear);
    }

    private void setup() {
        setAnimationOnLayoutChange(rootLinear);
        setAnimationOnLayoutChange(finalButtonsLinear);

        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        imagesRecyclerView.setLayoutManager(layoutManager);

        refreshApiInfo();

        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(imagesRecyclerView);

        toolbarLayoutMain.setNavigationButtonOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                showHelpDialog();
            }
        });

        downloadAllButton.setOnClickListener(v -> {
            ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Downloading " + imagesUrlListToAllDownload.size() + " photos...");
            progressDialog.setMessage(downloadPosition + "/" + imagesUrlListToAllDownload.size());
            progressDialog.setIndeterminate(true);
            progressDialog.show();

            downloadPosition = 0;

            if (imagesUrlListToAllDownload != null) {
                Download.MultiDownload(MainActivity.this, imagesUrlListToAllDownload, new Download.MultiDownloaderListener() {
                    @Override
                    public void OnProgress(int progress) {
                        progressDialog.setProgress(progress);
                    }

                    @Override
                    public void OnSuccess() {
                        progressDialog.setTitle("Downloading " + imagesUrlListToAllDownload.size() + " files...");
                        progressDialog.setMessage((downloadPosition + 1) + "/" + imagesUrlListToAllDownload.size());

                        imagesRecyclerView.getAdapter().notifyItemChanged(downloadPosition);

                        ((ImagesAdapter) imagesRecyclerView.getAdapter()).isDownloaded.set(downloadPosition, true);

                        if (downloadPosition == imagesUrlListToAllDownload.size() - 1) {
                            progressDialog.dismiss();
                            downloadAllButton.setVisibility(View.GONE);
                        }

                        downloadPosition++;
                    }

                    @Override
                    public void OnError(String reason) {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Error:\n" + reason, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        downloadButton.setEnabled(false);
        downloadButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                urlEdittext.setEnabled(false);
                urlEdittext.setEnabled(true);

                if (!apiInfo.getString("requestsRemaining", "").contentEquals("0")) {
                    String url = urlEdittext.getText().toString();
                    urlEdittext.setText("");
                    if (url.contains("instagram")) {
                        changeStep(PROCESSING_LINEAR);
                        try {
                            Instagram instagram = new Instagram(MainActivity.this, url, new Instagram.OnDataReceived() {
                                @Override
                                public void OnReceived(List<String> imagesUrlList, String postDescription) {
                                    refreshApiInfo();
                                    if (imagesUrlList.size() == 1) {
                                        downloadAllButton.setVisibility(View.GONE);
                                        imagesUrlListToAllDownload = null;
                                    } else if (imagesUrlList.size() > 1) {
                                        downloadAllButton.setVisibility(View.VISIBLE);
                                        imagesUrlListToAllDownload = imagesUrlList;
                                    }
                                    adapter = new ImagesAdapter(imagesUrlList, MainActivity.this);
                                    imagesRecyclerView.setAdapter(adapter);
                                    changeStep(FINAL_LINEAR);
                                }

                                @Override
                                public void OnError(String reason) {
                                    changeStep(WELCOME_LINEAR);
                                    Toast.makeText(MainActivity.this, reason, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (IOException e) {
                            changeStep(WELCOME_LINEAR);
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(MainActivity.this, "You reached the limit!", Toast.LENGTH_LONG).show();
                }
            }
        });

        setupUrlEditTextListener();
        setupPrefixEditTextListener();

        settingsButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        backButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                changeStep(WELCOME_LINEAR);
            }
        });
    }

    private void setupUrlEditTextListener() {
        urlEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                urlEdittext.removeTextChangedListener(this); // Remove to prevent loop

                if (!urlEdittext.getText().toString().isEmpty()) {
                    downloadButton.setEnabled(true);
                }

                if (editable.toString().contains("?utm_source=ig_web_copy_link") || editable.toString().contains("?igshid=")) {
                    String text = urlEdittext.getText().toString().substring(1, urlEdittext.getText().toString().indexOf("?"));
                    urlEdittext.setText(text);
                }

                urlEdittext.addTextChangedListener(this);
            }
        });
    }

    private void setupPrefixEditTextListener() {
        downloadSettings.edit()
                .putString("prefixFileName", "")
                .putString("sufixFileName", "")
                .apply();

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                downloadSettings.edit()
                        .putString("prefixFileName", prefixEdittext.getText().toString())
                        .putString("sufixFileName", sufixEdittext.getText().toString())
                        .apply();
            }
        };

        prefixEdittext.addTextChangedListener(watcher);
        sufixEdittext.addTextChangedListener(watcher);
    }

    private void changeStep(String STEP_NAME) {
        switch (STEP_NAME) {
            case WELCOME_LINEAR:
                welcomeLinear.setVisibility(View.VISIBLE);
                processingLinear.setVisibility(View.GONE);
                finalLinear.setVisibility(View.GONE);
                break;
            case PROCESSING_LINEAR:
                welcomeLinear.setVisibility(View.GONE);
                processingLinear.setVisibility(View.VISIBLE);
                finalLinear.setVisibility(View.GONE);
                break;
            case FINAL_LINEAR:
                welcomeLinear.setVisibility(View.GONE);
                processingLinear.setVisibility(View.GONE);
                finalLinear.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void refreshApiInfo() {
        if (!apiInfo.getString("requestsLimit", "").isEmpty()) {
            apiInfoText.setVisibility(View.VISIBLE);

            String frstLine = "Requests: " + apiInfo.getString("requestsRemaining", "") + "/" + apiInfo.getString("requestsLimit", "");
            String secLine = "Resets At: " + getDate(new Timestamp(System.currentTimeMillis()).getTime() + Long.valueOf(apiInfo.getString("requestsReset", "")));
        }
    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ROOT);
        cal.setTimeInMillis(time * 1000);
        String date = DateFormat.format("dd-MM-yyyy", cal).toString();
        return date;
    }

    private void setAnimationOnLayoutChange(LinearLayout layout) {
        layout.getLayoutTransition()
                .enableTransitionType(LayoutTransition.CHANGING);
    }

    private void showHelpDialog() {
        AlertDialog helpDialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Help")
                .setMessage("Enter the URL to the post from Instagram or video from YouTube in the field, then click \"Download\"")
                .setPositiveButton("Close", null)
                .show();
    }
}
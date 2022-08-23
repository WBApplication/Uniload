package tw.app.uniload;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

public class SettingsActivity extends AppCompatActivity {
    private MaterialAutoCompleteTextView rapidapiKeyEdittext;
    private MaterialButton saveSettingsButton;

    private SharedPreferences settings;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initView();
        setup();
    }

    private void initView() {
        rapidapiKeyEdittext = (MaterialAutoCompleteTextView) findViewById(R.id.rapidapi_key_edittext);
        saveSettingsButton = (MaterialButton) findViewById(R.id.save_settings_button);

        settings = getSharedPreferences("settings", MODE_PRIVATE);
    }

    private void setup() {
        rapidapiKeyEdittext.setText(settings.getString("apikey", getString(R.string.rapidapi_key_one)));

        String[] keys = { getString(R.string.rapidapi_key_one), getString(R.string.rapidapi_key_two) };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, keys);
        rapidapiKeyEdittext.setAdapter(adapter);

        saveSettingsButton.setOnClickListener(v -> {
            settings.edit().putString("apikey", rapidapiKeyEdittext.getText().toString()).apply();
        });
    }
}

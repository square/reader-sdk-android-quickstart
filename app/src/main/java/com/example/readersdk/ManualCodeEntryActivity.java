package com.example.readersdk;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import com.example.readersdk.util.TextWatcherAdapter;

public class ManualCodeEntryActivity extends AppCompatActivity {

  private EditText authorizationCodeEditText;
  private View authorizeButton;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.manual_code_entry_activity);

    authorizationCodeEditText = findViewById(R.id.authorization_code_edit_text);
    authorizationCodeEditText.setOnEditorActionListener((view, actionId, event) -> {
      if (actionId == EditorInfo.IME_ACTION_DONE) {
        if (!authorizationCodeEmpty()) {
          startAuthorizing();
          return true;
        }
      }
      return false;
    });
    authorizeButton = findViewById(R.id.authorize_button);
    authorizeButton.setOnClickListener(v -> startAuthorizing());

    authorizationCodeEditText.addTextChangedListener(new TextWatcherAdapter() {
      @Override public void onTextChanged(CharSequence text, int start, int before, int count) {
        updateSubmitButtonState();
      }
    });
    updateSubmitButtonState();

    findViewById(R.id.cancel_button).setOnClickListener(view -> finish());
  }

  @Override public void onBackPressed() {
    startActivity(new Intent(this, StartAuthorizeActivity.class));
    finish();
  }

  private void updateSubmitButtonState() {
    authorizeButton.setEnabled(!authorizationCodeEmpty());
  }

  private boolean authorizationCodeEmpty() {
    return authorizationCodeEditText.getText().toString().trim().isEmpty();
  }

  private void startAuthorizing() {
    String authorizationCode = authorizationCodeEditText.getText().toString().trim();
    AuthorizingActivity.start(this, authorizationCode);
    finish();
  }
}

package com.example.readersdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.squareup.sdk.reader.ReaderSdk;
import com.squareup.sdk.reader.authorization.AuthorizationManager;
import com.squareup.sdk.reader.authorization.AuthorizationState;
import com.squareup.sdk.reader.authorization.AuthorizeErrorCode;
import com.squareup.sdk.reader.authorization.Location;
import com.squareup.sdk.reader.core.CallbackReference;
import com.squareup.sdk.reader.core.Result;
import com.squareup.sdk.reader.core.ResultError;

public class AuthorizingActivity extends AppCompatActivity {

  private static final String AUTHORIZE_CODE_EXTRA = "authorizeCodeExtra";
  private static final String TAG = AuthorizingActivity.class.getSimpleName();

  public static void start(Activity originActivity, String authorizationCode) {
    Intent intent = new Intent(originActivity, AuthorizingActivity.class);
    intent.putExtra(AUTHORIZE_CODE_EXTRA, authorizationCode);
    originActivity.startActivity(intent);
  }

  private CallbackReference authorizeCallbackRef;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.authorizing_activity);

    AuthorizationManager authorizationManager = ReaderSdk.authorizationManager();
    authorizeCallbackRef = authorizationManager.addAuthorizeCallback(this::onAuthorizeResult);

    if (savedInstanceState == null) {
      authorize();
    } else {
      AuthorizationState state = authorizationManager.getAuthorizationState();
      if (!state.isAuthorizationInProgress()) {
        if (state.isAuthorized()) {
          goToCheckoutActivity();
        } else {
          goToStartAuthorizeActivity();
        }
      }
    }
  }

  private void authorize() {
    String authorizationCode = getIntent().getStringExtra(AUTHORIZE_CODE_EXTRA);
    ReaderSdk.authorizationManager().authorize(authorizationCode);
  }

  private void onAuthorizeResult(Result<Location, ResultError<AuthorizeErrorCode>> result) {
    if (result.isSuccess()) {
      goToCheckoutActivity();
    } else {
      ResultError<AuthorizeErrorCode> error = result.getError();
      switch (error.getCode()) {
        case NO_NETWORK:
          showRetryDialog(error);
          break;
        case USAGE_ERROR:
          showUsageErrorDialog(error);
          break;
      }
    }
  }

  private void showRetryDialog(ResultError<AuthorizeErrorCode> error) {
    new AlertDialog.Builder(this)
        .setTitle(getString(R.string.network_error_dialog_title))
        .setMessage(error.getMessage())
        .setPositiveButton(R.string.retry_button, (dialog, which) -> authorize())
        .setOnCancelListener(dialog -> goToStartAuthorizeActivity())
        .show();
  }

  private void showUsageErrorDialog(ResultError<AuthorizeErrorCode> error) {
    String dialogMessage = error.getMessage();
    if (BuildConfig.DEBUG) {
      dialogMessage += "\n\nDebug Message: " + error.getDebugMessage();
      Log.d(TAG,
          error.getCode() + ": " + error.getDebugCode() + ", " + error.getDebugMessage());
    }
    new AlertDialog.Builder(this)
        .setTitle(getString(R.string.error_dialog_title))
        .setMessage(dialogMessage)
        .setPositiveButton(R.string.ok_button,
            (dialog, which) -> goToStartAuthorizeActivity())
        .setOnCancelListener(dialog -> goToStartAuthorizeActivity())
        .show();
  }

  private void goToCheckoutActivity() {
    Intent intent = new Intent(this, CheckoutActivity.class);
    startActivity(intent);
    finish();
  }

  private void goToStartAuthorizeActivity() {
    Intent intent = new Intent(this, StartAuthorizeActivity.class);
    startActivity(intent);
    finish();
  }

  @Override public void onBackPressed() {
    // Blocking until it's done.
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    authorizeCallbackRef.clear();
  }
}

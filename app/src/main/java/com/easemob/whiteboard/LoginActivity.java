package com.easemob.whiteboard;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.easemob.whiteboard.runtimepermissions.PermissionsManager;
import com.easemob.whiteboard.runtimepermissions.PermissionsResultAction;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMError;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMWhiteboard;
import com.hyphenate.exceptions.HyphenateException;

import java.util.Locale;
import java.util.Random;

public class LoginActivity extends Activity {
    private EditText roomName;
    private EditText roomPwd;
    private Button joinRoom;
    private String loginUser;
    private boolean progressShow;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        requestPermissions();
        loginUser = getRandomAccount();
        initView();
    }

    private void initView() {
        pd = new ProgressDialog(LoginActivity.this);
        pd.setCanceledOnTouchOutside(false);
        pd.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                progressShow = false;
            }
        });
        pd.setMessage(getString(R.string.Is_landing));

        roomName = findViewById(R.id.room_name);
        roomPwd = findViewById(R.id.room_pwd);
        joinRoom = findViewById(R.id.join_room);

        joinRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (roomName.getText().toString().trim().isEmpty()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.The_room_name_cannot_be_empty), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (roomPwd.getText().toString().trim().isEmpty()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.The_room_password_cannot_be_empty), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (EMClient.getInstance().isLoggedInBefore()) {
                    joinRoom();
                } else {
                    createRandomAccount();
                }
            }
        });
    }

    // 随机创建账号登录
    private void createRandomAccount() {
        progressShow = true;
        pd.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().createAccount(loginUser, "123");
                    EMClient.getInstance().login(loginUser, "123", new EMCallBack() {
                        @Override
                        public void onSuccess() {
                            joinRoom();
                        }

                        @Override
                        public void onError(int i, String s) {
                            if (!progressShow) {
                                return;
                            }
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    pd.dismiss();
                                    Toast.makeText(getApplicationContext(), getString(R.string.Login_failed) + s,
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onProgress(int i, String s) {

                        }
                    });
                } catch (HyphenateException e) {
                    if (!progressShow) {
                        return;
                    }
                    pd.dismiss();
                    int errorCode = e.getErrorCode();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (errorCode == EMError.NETWORK_ERROR) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_anomalies), Toast.LENGTH_SHORT).show();
                            } else if (errorCode == EMError.USER_ALREADY_EXIST) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.User_already_exists), Toast.LENGTH_SHORT).show();
                            } else if (errorCode == EMError.USER_AUTHENTICATION_FAILED) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.registration_failed_without_permission), Toast.LENGTH_SHORT).show();
                            } else if (errorCode == EMError.USER_ILLEGAL_ARGUMENT) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.illegal_user_name), Toast.LENGTH_SHORT).show();
                            } else if (errorCode == EMError.EXCEED_SERVICE_LIMIT) {
                                Toast.makeText(LoginActivity.this, getResources().getString(R.string.register_exceed_service_limit), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registration_failed), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }).start();

    }

    // 房间存在直接加入，不存在就创建加入
    private void joinRoom() {
        if (!progressShow) {
            progressShow = true;
            pd.show();
        }
        EMClient.getInstance().conferenceManager().joinWhiteboardRoomWithId(EMClient.getInstance().getCurrentUser(), EMClient.getInstance().getAccessToken(), roomName.getText().toString(), roomPwd.getText().toString(), new EMValueCallBack<EMWhiteboard>() {
            @Override
            public void onSuccess(EMWhiteboard emWhiteboard) {
                if (!LoginActivity.this.isFinishing() && pd.isShowing()) {
                    pd.dismiss();
                }
                startActivity(new Intent(LoginActivity.this, MainActivity.class).putExtra("roomUrl", emWhiteboard.getRoomUrl()));
                finish();
            }

            @Override
            public void onError(int i, String s) {
                if (i == EMError.CALL_INVALID_ID) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            EMClient.getInstance().conferenceManager().createWhiteboardRoom(EMClient.getInstance().getCurrentUser(), EMClient.getInstance().getAccessToken(), roomName.getText().toString(), roomPwd.getText().toString(), new EMValueCallBack<EMWhiteboard>() {
                                @Override
                                public void onSuccess(EMWhiteboard emWhiteboard) {
                                    if (!LoginActivity.this.isFinishing() && pd.isShowing()) {
                                        pd.dismiss();
                                    }
                                    emWhiteboard.getRoomId();
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class).putExtra("roomUrl", emWhiteboard.getRoomUrl()));
                                    finish();
                                }

                                @Override
                                public void onError(int i, String s) {
                                    if (!LoginActivity.this.isFinishing() && pd.isShowing()) {
                                        pd.dismiss();
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), getString(R.string.Join_failed) + ":" + i + ":" + s,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                        }
                    });
                } else {
                    if (!LoginActivity.this.isFinishing() && pd.isShowing()) {
                        pd.dismiss();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), getString(R.string.Join_failed) + ":" + i + ":" + s,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
    }

    //随机创建id
    private String getRandomAccount() {
        String val = "";
        Random random = new Random();
        for (int i = 0; i < 15; i++) {
            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num"; //输出字母还是数字
            if ("char".equalsIgnoreCase(charOrNum)) {// 字符串
                int choice = random.nextInt(2) % 2 == 0 ? 65 : 97; //取得大写字母还是小写字母
                val += (char) (choice + random.nextInt(26));
            } else if ("num".equalsIgnoreCase(charOrNum)) {// 数字
                val += String.valueOf(random.nextInt(10));
            }
        }
        return val.toLowerCase(Locale.getDefault());
    }

    @TargetApi(23)
    private void requestPermissions() {
        PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(this, new PermissionsResultAction() {
            @Override
            public void onGranted() {
//				Toast.makeText(MainActivity.this, "All permissions have been granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDenied(String permission) {
                //Toast.makeText(MainActivity.this, "Permission " + permission + " has been denied", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults);
    }

}

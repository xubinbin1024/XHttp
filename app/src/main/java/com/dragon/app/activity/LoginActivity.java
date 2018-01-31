package com.dragon.app.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bean.logger.JJLogger;
import com.bean.xhttp.XHttp;
import com.bean.xhttp.callback.OnXHttpCallback;
import com.bean.xhttp.response.Response;
import com.dragon.R;
import com.dragon.abs.activity.FullscreenActivity;
import com.dragon.api.WebApi;
import com.dragon.app.bean.LoginInfo;
import com.dragon.widget.ModifyDialog;
import com.google.gson.Gson;

import static com.dragon.api.WebApi.MODIFY_URL;
import static com.dragon.constant.Code.USER_NAME;
import static com.dragon.constant.Code.USER_PASSWORD;
import static com.dragon.constant.Code.USER_PLATFORM;
import static com.dragon.manager.ManagerActivity.addActivityCST;
import static com.dragon.manager.ManagerActivity.finishAllCST;
import static com.dragon.util.UtilWidget.getView;
import static com.dragon.util.UtilWidget.setViewAlphaAnimation;
import static com.dragon.util.UtilWidget.showErrorInfo;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends FullscreenActivity implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    protected final String TAG = this.getClass().getSimpleName();
    protected EditText mAccountAct;
    protected EditText mPasswordEt;
    private VideoView mSpalshVideo;
    private TextView mItemTv;

    @Override
    protected void afterInit() {

    }

    @Override
    protected int initLayout() {
        addActivityCST(this);
        return R.layout.activity_login;
    }

    @Override
    protected void initView() {
        mSpalshVideo = getView(this, R.id.videoView);
        mAccountAct = getView(this, R.id.account);
        mPasswordEt = getView(this, R.id.password);
        mItemTv = getView(this, R.id.item);
    }

    @Override
    protected void initData() {
        initVideoView();
        String text = "登录即代表阅读并同意服务条款";
        int len = text.length();
        SpannableString spannableString = new SpannableString(text);
        mItemTv.setMovementMethod(LinkMovementMethod.getInstance());//必须设置否则无效
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(final View widget) {
                startActivity(new Intent(mActivity, ProtocolItemActivity.class));
            }

            @Override
            public void updateDrawState(final TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#0061ff"));
                ds.setUnderlineText(false);    //去除超链接的下划线
            }
        }, len - 4, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //改变选中文本的高亮颜色
//        mItemTv.setHighlightColor(Color.BLUE);
        mItemTv.setText(spannableString);
    }

    private void initVideoView() {
        //设置屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mSpalshVideo.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.mqr);
        //设置相关的监听
        mSpalshVideo.setOnPreparedListener(this);
        mSpalshVideo.setOnCompletionListener(this);
    }

    public void login(View view) {
        setViewAlphaAnimation(view);
        final String name = mAccountAct.getText().toString();
        final String password = mPasswordEt.getText().toString();

        if (checkAccountPassword(name, password)) {
            XHttp.getInstance()
                    .post(WebApi.LOGIN_URL)
                    .setParams(USER_NAME, name)
                    .setParams(USER_PLATFORM, "mobile_phone")
                    .setParams(USER_PASSWORD, password)
                    .setOnXHttpCallback(new OnXHttpCallback() {
                        @Override
                        public void onSuccess(final Response response) {
                            JJLogger.logInfo(TAG, "LoginActivity.onSuccess :" + response.toString());
                            Gson gson = new Gson();
                            LoginInfo userBean = gson.fromJson(response.toString(), LoginInfo.class);
                            switch (userBean.getCode()) {
                                case "1003":
                                    Toast.makeText(LoginActivity.this, "登录成功！", Toast.LENGTH_SHORT).show();
                                    intoApp();
                                    break;
                                default:
                                    showErrorInfo(LoginActivity.this, userBean.getMsg(), "");
                                    break;
                            }
                        }

                        @Override
                        public void onFailure(final Exception ex, final String errorCode) {
                            showErrorInfo(LoginActivity.this, ex.getMessage(), "");
                        }
                    });
        } else {
            showErrorInfo(LoginActivity.this, mErrorInfo, "");
        }
    }

    public void skip(View view) {
        intoApp();
    }

    private void intoApp() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finishAllCST();
    }

    protected boolean checkAccountPassword(final String account, final String password) {
        if (TextUtils.isEmpty(account)) {
            mErrorInfo = "账号为空";
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            mErrorInfo = "密码为空";
            return false;
        }
        return true;
    }

    @Override
    public void onCompletion(final MediaPlayer mp) {
        //开始播放
        mSpalshVideo.start();
    }

    @Override
    public void onPrepared(final MediaPlayer mp) {
        //开始播放
        mSpalshVideo.start();
    }

    public void forgetPassword(View view) {

        new ModifyDialog(this).setOnModifyDialogListener(new ModifyDialog.OnModifyDialogListener() {
            @Override
            public void onSure(final String name, final String password, final String telephone, final Dialog dialog) {
                XHttp.getInstance()
                        .post(MODIFY_URL)
                        .setParams(USER_NAME, name)
                        .setParams(USER_PASSWORD, password)
                        .setParams("telephone", telephone)
                        .setOnXHttpCallback(new OnXHttpCallback() {
                            @Override
                            public void onSuccess(final Response response) {

                                Log.i(TAG, "LoginActivity.onSuccess :" + response.toString());
                                Gson gson = new Gson();
                                LoginInfo userBean = gson.fromJson(response.toString(), LoginInfo.class);
                                switch (userBean.getCode()) {
                                    case "1012":
                                        Toast.makeText(LoginActivity.this, "登录成功！", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                        break;
                                    default:
                                        Toast.makeText(LoginActivity.this, userBean.getMsg(), Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            }

                            @Override
                            public void onFailure(final Exception ex, final String errorCode) {
                                Toast.makeText(LoginActivity.this, "请求失败" + errorCode, Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onCancel(final Dialog dialog) {
                dialog.dismiss();
            }
        }).show();

    }

    public void registerNew(View view) {
        register();
    }

    private void register() {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }
}


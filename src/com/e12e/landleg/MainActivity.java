package com.e12e.landleg;

import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import com.e12e.utils.AddressUtil;
import com.e12e.utils.HttpUtil;
import com.e12e.utils.MD5Util;

import android.app.AlertDialog.Builder;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	protected static final int VERIFYCODEEXCEPTION = 0;
	protected static final int LOGINEXCEPTION = 1;
	protected static final int LOGINSUCCESS = 2;
	protected static final int LOGINFAILED = 3;
	private static final int NASIPEXCEPTION = 4;
	private static final int LOGOUTEXCEPTION = 5;
	private static final int LOGOUTSUCCESS = 6;
	private static final int LOGOUTFAILED = 7;
	protected static final int GETVERIFYCODEFAILED = 8;
	// 更新UI所用到的变量
	Handler handler;
	Message msg;

	// 设置信息变量
	String username;
	String password;
	Boolean isUse;
	String clientip;
	String clientmac;
	String nasip;
	int time;

	EsurfingService esurfingService;
	SharedPreferences sharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// 将preferencefragment显示出来
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		SettingFragment settingFragment = new SettingFragment();
		fragmentTransaction.replace(R.id.pref, settingFragment).commit();

		// 初始化
		esurfingService = new EsurfingService();

		// 更新界面
		handler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				String message = null;
				switch (msg.what) {
				case LOGINSUCCESS:
					message = "登录成功";
					break;
				case LOGINFAILED:
					message = "登录失败：" + (String) msg.obj;
					break;
				case LOGINEXCEPTION:
					message="登录时出现异常";
					break;
				case VERIFYCODEEXCEPTION:
					message = "获取验证码时发生异常";
					break;
				case NASIPEXCEPTION:
					message = "自动获取NASIP时发生异常";
					break;
				case LOGOUTEXCEPTION:
					message = "登出时发生异常";
					break;
				case LOGOUTSUCCESS:
					message = "登出成功";
					break;
				case LOGOUTFAILED:
					message = "登出失败：" + (String) msg.obj;
					break;
				case GETVERIFYCODEFAILED:
					message = "验证码获取失败：" + (String) msg.obj;
					break;
				default:
					message = "此信息类型没有定义：" + msg.what;
					break;
				}
				Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
			};
		};

	}

	/**
	 * 点击“连接”按钮时的操作
	 * 
	 * @param v
	 *            该按钮
	 */
	public void doConnect(View v) {
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		username = sharedPreferences.getString("username", "");
		password = sharedPreferences.getString("password", "");
		isUse = sharedPreferences.getBoolean("isUse", false);
	//	Log.d("isUse-------->", isUse + "");

		// 如果启用高级设置
		if (isUse) {
			nasip = sharedPreferences.getString("nasip", "");
			clientip = sharedPreferences.getString("clientip", "");
			clientmac = sharedPreferences.getString("clientmac", "");

			// 暂时不可用
			time = Integer.parseInt(sharedPreferences.getString("time", "30"));

			// 直接利用已填写的信息来登录
			Login();
		} else {
			// 如果没有启用高级设置，则自动获取相关信息再进行登录操作
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						// 获取NASIP
						nasip = esurfingService.getNASIP();
						// Log.d("NASIP-------->", nasip);
					} catch (Exception e) {
						handler.sendEmptyMessage(NASIPEXCEPTION);
						return;
					}
					// 获取ip和mac
					clientmac = AddressUtil.getLocalMAC(MainActivity.this);
					clientip = AddressUtil.getLocalIP(MainActivity.this);

					// 回调Login
					Login();
				}
			}).start();

		}

	}

	/**
	 * 信息获取成功后回调的 登录操作
	 */
	public void Login() {
		// 再次初始化，添加数据
		esurfingService = new EsurfingService(username, password, nasip, clientip, clientmac);
		new Thread(new Runnable() {
			@Override
			public void run() {

				// 获取验证码
				String verifyCodeString = null;
				String verifyCode = null;

				try {
					verifyCodeString = esurfingService.getVerifyCodeString();
				} catch (Exception e) {
					handler.sendEmptyMessage(VERIFYCODEEXCEPTION);
					return;
				}

				JSONObject challengeJSONObject = null;
				try {
					challengeJSONObject = new JSONObject(verifyCodeString);
				} catch (JSONException e) {
					e.printStackTrace();
					return;
				}
				int rescode0 = challengeJSONObject.optInt("rescode");
				if (rescode0 == 0) {
					verifyCode = challengeJSONObject.optString("challenge");
				} else {
					msg = handler.obtainMessage();
					msg.what = GETVERIFYCODEFAILED;
					msg.obj = challengeJSONObject.optString("resinfo");
					handler.sendMessage(msg);
					return;
				}
				// 登录
				String loginString = null;
				try {
					loginString = esurfingService.doLogin(verifyCode);
				} catch (Exception e) {
					handler.sendEmptyMessage(LOGINEXCEPTION);
					return;
				}

				JSONObject loginJSONObject = null;
				try {
					loginJSONObject = new JSONObject(loginString);
				} catch (JSONException e) {
					e.printStackTrace();
					return;
				}

				int rescode = loginJSONObject.optInt("rescode");
				if (rescode == 0) {
					handler.sendEmptyMessage(LOGINSUCCESS);
					// 记录上次登录时使用的登录信息，用于登出：
					sharedPreferences.edit().putBoolean("lastIsUse", isUse).putString("lastUserame", username)
							.putString("lastPassword", password).putString("lastNasip", nasip)
							.putString("lastClientip", clientip).putString("lastClientmac", clientmac).commit();
					
					//小统计：
					Analytics();
					
				} else {
					String resinfo = loginJSONObject.optString("resinfo");
					msg = handler.obtainMessage();
					msg.what = LOGINFAILED;
					msg.obj = resinfo;
					handler.sendMessage(msg);
				}

			}
		}).start();
	}

	/**
	 * 退出登录
	 */
	public void Logout() {
		// 登出
		new Thread(new Runnable() {
			@Override
			public void run() {
				String logoutString = null;
				try {
					// 获取上次登录时的数据
					boolean lastIsUse = sharedPreferences.getBoolean("lastIsUse", false);
					String lastUsername = sharedPreferences.getString("lastUsername", "");
					String lastPassword = sharedPreferences.getString("lastPassword", "");
					String lastNasip = sharedPreferences.getString("lastNasip", "");
					String lastClientip = sharedPreferences.getString("lastClientip", "");
					String lastClientmac = sharedPreferences.getString("lastClientmac", "");
					esurfingService = new EsurfingService(lastUsername, lastPassword, lastNasip, lastClientip,
							lastClientmac);
					// 退出上次的登录
					logoutString = esurfingService.doLogout();
				} catch (Exception e) {
					handler.sendEmptyMessage(LOGOUTEXCEPTION);
					e.printStackTrace();
					return;
				}
				JSONObject logoutJSONObject = null;

				try {
					logoutJSONObject = new JSONObject(logoutString);
				} catch (JSONException e) {
					e.printStackTrace();
					return;
				}

				int rescode = logoutJSONObject.optInt("rescode");
				if (rescode == 0) {
					handler.sendEmptyMessage(LOGOUTSUCCESS);
				} else {
					msg = handler.obtainMessage();
					msg.obj = logoutJSONObject.opt("resinfo");
					msg.what = LOGOUTFAILED;
					handler.sendMessage(msg);
				}
			}
		}).start();
	}

	/**
	 * 填充菜单
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * 处理菜单事件
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.logout) {
			Logout();
		} else if (item.getItemId() == R.id.about) {
			TextView tv = new TextView(this);
			String msg = "<h1>LandLeg Android v1.0</h1>" + "LandLeg即地腿，是天翼校园第三方客户端，可以<b>替代</b>中国电信的天翼校园进行拨号上网"
					+ "，此安卓版是在<a href=\"https://github.com/Coande/LandLeg_Login_Java\">地腿Java版</a>移植而来"
					+ "，功能相当，完全开源。详细介绍请参看：<b><a href=\"http://coande.github.io\">Coande的博客</a></b><br/><br/><h4>By Coande</h4>";
			tv.setText(Html.fromHtml(msg));
			tv.setMovementMethod(LinkMovementMethod.getInstance());
			tv.setTextColor(Color.BLACK);
			tv.setTextSize(16);
			tv.setPadding(50, 50, 50, 50);
			new Builder(this).setView(tv).show();
		}
		return true;
	}
	
	
	/**
	 *顺便 做个小统计，不要介意哈~~
	 */
	public void Analytics(){
		try{
			String resString=HttpUtil.doGet("http://ip.taobao.com/service/getIpInfo.php", "ip=myip");
			JSONObject jsonObject2=new JSONObject(resString);
			int code=jsonObject2.optInt("code");
			String city = null;
			if(code==0){
				JSONObject jsonObject3=(JSONObject) jsonObject2.opt("data");
				city=(String) jsonObject3.opt("city");
				city=URLEncoder.encode(city, "utf-8");
				String param="uid="+MD5Util.MD5(AddressUtil.getLocalMAC(MainActivity.this))+"&city="+city+"&type=2";
				HttpUtil.doGet("http://s2.e12e.com:8080/Analytics/", param);
			}
		}catch(Exception e){
			//不要报错~~~
		}
	}

	// 退出提示
	boolean isExit = false;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isExit == false) {
				isExit = true; // 准备退出
				Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						isExit = false; // 取消退出
					}
				}, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

			} else {
				finish();
				System.exit(0);
			}
		}
		return true;
	}

}

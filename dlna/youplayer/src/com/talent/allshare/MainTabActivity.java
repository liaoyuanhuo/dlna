package com.talent.allshare;


import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;

import com.talent.allshare.local.LocalActivity;
import com.talent.allshare.more.MoreActivity;
import com.talent.allshare.server.ShareActivity;
import com.wireme.activity.Wifi_Setting;
import com.youplayer.player.R;
//import android.view.View.OnClickListener;
import com.youplayer.player.YouApplication;

@TargetApi(16)
@SuppressLint({ "ResourceAsColor", "NewApi", "NewApi" }) public class MainTabActivity extends TabActivity implements OnTabChangeListener ,android.view.View.OnClickListener
{
    static  private TabHost mTabHost;
    static private Context mContext;
    private boolean mInitFlag = false;
    static private TabWidget mTabWidget;
    private static String TAG = "MainTabActivity";
    private static final String TAB_D = "D";
	private static final String TAB_C = "C";
	private static final String TAB_B = "B";
	private static final String TAB_A = "A";
	static private Timer mTimer;
	static private MyTimerTask mTimerTask;
	static private Handler handler  = new Handler(){
		
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0x123:
				stopTimer();
				notFindDlnaTip();
//				cheupdate();
				break;
			case 0x124:
				stopTimer();
				initView();
				break;
			case 0x125:
				stopTimer();
				wifiErrorTip();
				break;
			default:
				break;
			}
		};
	};
	public static void StartTimer()
	{
		if(mTimer == null)
			mTimer = new Timer(true);
	     
	      if (mTimerTask != null)
	      {  	  
	    	  mTimerTask.cancel();  
	      }
	      
	      mTimerTask = new MainTabActivity.MyTimerTask();  
	      mTimer.schedule(mTimerTask, 10*1000);
	 }
	private static void stopTimer()
	{
		if (mTimerTask != null)
	      {  	  
	    	  mTimerTask.cancel();  //将原任务从队列中移除
	      }
//		if(mTimer != null)
//		{
//			mTimer.cancel();
//		}
	}
  static class MyTimerTask extends TimerTask
  {
	  @Override
	  public void run() 
	  {
		   // TODO Auto-generated method stub
		   Log.i(MainTabActivity.TAG, "run...");
		   handler.sendEmptyMessageDelayed(0x125, 1000);
	  }
     
    }
	public static String getGateWay(Context context){ 
		WifiManager wifiManager;
		DhcpInfo dhcpInfo;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE); 
        dhcpInfo = wifiManager.getDhcpInfo(); 
         
     //dhcpInfo获取的是最后一次成功的相关信息，包括网关、ip等  
     return FormatIP(dhcpInfo.gateway);      
    } 
	public static String getLocalMacAddress() {  
        WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);  
        WifiInfo info = wifi.getConnectionInfo();  
        System.out.println("info.getMacAddress()="+info.getMacAddress());
        return info.getMacAddress();  
    }  	
    @SuppressLint("NewApi") 
    public static String FormatIP(int IpAddress) {
        return Formatter.formatIpAddress(IpAddress);
        }
    @Override
	protected void onResume() {
		super.onResume();
		
		if (checkNetWork() && !mInitFlag)
		{
			Wifi_Setting.mac_crypto(getGateWay(MainTabActivity.this),getLocalMacAddress());
			StartTimer();
			mInitFlag = true;
		}
	}
    static public void WifiNotFindDlna()
    {
    	handler.sendEmptyMessageDelayed(0x123, 1000);
    }
    static public void gotoSearchActivity()
    {
    	handler.sendEmptyMessageDelayed(0x124, 1000);
    }
    private static  void notFindDlnaTip()
    {
    	new AlertDialog.Builder(mContext).setTitle(mContext.getResources().getString(R.string.no_dlna))//""
		.setNegativeButton(mContext.getResources().getString(R.string.research), new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Wifi_Setting.mac_crypto(getGateWay(mContext),getLocalMacAddress()); 
				StartTimer();
			}
		})
		.setNeutralButton(mContext.getResources().getString(R.string.cancel), new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).show();
    }
    private static void wifiErrorTip()
    {
    	new AlertDialog.Builder(mContext).setTitle(mContext.getResources().getString(R.string.no_res))
		.setNegativeButton(mContext.getResources().getString(R.string.research), new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Wifi_Setting.mac_crypto(getGateWay(mContext),getLocalMacAddress()); 
				StartTimer();
			}
		})
		.setNeutralButton(mContext.getResources().getString(R.string.cancel), new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).show();
    }
	@Override
    protected void onPause() {
    	super.onPause();
    }
    @Override
    protected void onStop() {
    	
    	super.onStop();
    	UninitData();
    }
    private void UninitData()
    {
//    	mContext = null;
    }
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_tab);
        mContext = this;
        
//        StartTimer();
    }
    EditText tv;

	static private View subTabView1;

	static private View subTabView2;

	static private View subTabView3;

	static private View subTabView4;
	private static LinearLayout linearLayout1;
	private static LinearLayout linearLayout2;
	private static LinearLayout linearLayout3;
	private static LinearLayout linearLayout4;
//	private ImageView iv_tab_a;
//
//	private TextView tv_tab_a;
//
//	private ImageView iv_tab_b;
//
//	private TextView tv_tab_b;
//
//	private ImageView iv_tab_c;
//
//	private TextView tv_tab_c;
//
//	private ImageView iv_tab_d;
//
//	private TextView tv_tab_d;
//
	static private TabWidget tabWidget;


    
    /*
     * init view
     */
	@SuppressLint({ "ResourceAsColor", "NewApi" })
	private static void initView() {

		subTabView1 = View.inflate(mContext, R.layout.main_tabhost_tab_a, null);
		linearLayout1 = (LinearLayout)subTabView1.findViewById(R.id.tab_min_bg_a);
//		iv_tab_a = (ImageView) subTabView1.findViewById(R.id.iv_tab_a);
//		tv_tab_a = (TextView) subTabView1.findViewById(R.id.tv_tab_a);
		subTabView2 = View.inflate(mContext, R.layout.main_tabhost_tab_b, null);
		linearLayout2 = (LinearLayout)subTabView2.findViewById(R.id.tab_min_bg_b);
//		iv_tab_b = (ImageView) subTabView2.findViewById(R.id.iv_tab_b);
//		tv_tab_b = (TextView) subTabView2.findViewById(R.id.tv_tab_b);
        subTabView3 = View.inflate(mContext, R.layout.main_tabhost_tab_c, null);
        linearLayout3 = (LinearLayout)subTabView3.findViewById(R.id.tab_min_bg_c);
//        iv_tab_c = (ImageView) subTabView3.findViewById(R.id.iv_tab_c);
//        tv_tab_c = (TextView) subTabView3.findViewById(R.id.tv_tab_c);
        
        subTabView4 = View.inflate(mContext, R.layout.main_tabhost_tab_d, null);
        linearLayout4 = (LinearLayout)subTabView4.findViewById(R.id.tab_min_bg_d);
//        iv_tab_d = (ImageView) subTabView4.findViewById(R.id.iv_tab_d);
//        tv_tab_d = (TextView) subTabView4.findViewById(R.id.tv_tab_d);
        
		
		mTabHost = ((TabActivity) mContext).getTabHost();
		mTabHost.setBackgroundResource(R.drawable.bg);
		
		  TabHost.TabSpec spec1  = mTabHost.newTabSpec(TAB_A).setIndicator(subTabView1).setContent(new Intent(mContext,ContentActivity.class));
		  mTabHost.addTab(spec1);
	        TabHost.TabSpec spec2  = mTabHost.newTabSpec(TAB_B).setIndicator(subTabView2).setContent(new Intent(mContext,LocalActivity.class));
	        mTabHost.addTab(spec2);
	        TabHost.TabSpec spec3  = mTabHost.newTabSpec(TAB_C).setIndicator(subTabView3).setContent(new Intent(mContext,ShareActivity.class));
	        mTabHost.addTab(spec3);
	        TabHost.TabSpec spec4  = mTabHost.newTabSpec(TAB_D).setIndicator(subTabView4).setContent(new Intent(mContext,MoreActivity.class));
	        mTabHost.addTab(spec4);
	      
	        mTabHost.setCurrentTab(0);
	        tabWidget = mTabHost.getTabWidget();
	        tabWidget.setBackgroundResource(R.drawable.tab_bg_);//将来是要设置背景图片的，现在以颜色代替
   
	        mTabHost.setOnTabChangedListener((OnTabChangeListener) mContext);
	}

	public void resetTextColor(int index){
        for (int i = 0; i < mTabWidget.getChildCount(); i++)
        {
            TextView tv = (TextView) mTabWidget.getChildAt(i).findViewById(android.R.id.title);
            if(index == i){
            	tv.setTextColor(Color.rgb(88, 183, 23));
            }else{
            	tv.setTextColor(Color.rgb(170, 175, 181));
            }
        }

	}
	
	@TargetApi(16)
	@SuppressLint("ResourceAsColor") @Override
	public void onTabChanged(String tabId) {
		//监测网络
		
		
		
		
//		checkNetWork();
		
//		TextView header_title = (TextView) findViewById(R.id.header_title);

		if(tabId.equals("1")){

			checkNetWork();
			Log.e("gzf","1111111");
			subTabView1.setBackgroundResource(R.drawable.tab_bg_bar);
			// linearLayout1.setBackgroundResource(R.drawable.tab_min_bg);
			// linearLayout2.setBackground(null);
			// linearLayout3.setBackground(null);
			// linearLayout4.setBackground(null);
		}
		if(tabId.equals("2")){
			Log.e("gzf","22222222");
			subTabView2.setBackgroundResource(R.drawable.tab_bg_bar);
			// linearLayout2.setBackgroundResource(R.drawable.tab_min_bg);
			// linearLayout1.setBackground(null);
			// linearLayout3.setBackground(null);
			// linearLayout4.setBackground(null);
		}
		if(tabId.equals("3")){
			Log.e("gzf","333333333");
			subTabView3.setBackgroundResource(R.drawable.tab_bg_bar);
			// linearLayout3.setBackgroundResource(R.drawable.tab_min_bg);
			// linearLayout2.setBackground(null);
			// linearLayout1.setBackground(null);
			// linearLayout4.setBackground(null);
			checkNetWork();
		}
		if(tabId.equals("4")){
			Log.e("gzf","4444444444444");
			subTabView4.setBackgroundResource(R.drawable.tab_bg_bar);
			// linearLayout4.setBackgroundResource(R.drawable.tab_min_bg);
			// linearLayout2.setBackground(null);
			// linearLayout3.setBackground(null);
			// linearLayout1.setBackground(null);
//			checkNetWork();
		}
		
		
	}
	private boolean checkNetWork() {
		if(isWifiConnected(MainTabActivity.this)){
			return true;
		}else{
			new AlertDialog.Builder(MainTabActivity.this).setTitle(getResources().getString(R.string.unconnectowifi))
			.setNegativeButton(getResources().getString(R.string.ok), new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Intent wifiSettingsIntent = new Intent("android.settings.WIFI_SETTINGS");   
					   startActivity(wifiSettingsIntent);   
				}
			})
			.setNeutralButton(getResources().getString(R.string.cancel), new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}).show();
			return false;
		}
	}
	public boolean isWifiConnected(Context context) { 
		if (context != null) { 
		ConnectivityManager mConnectivityManager = (ConnectivityManager) context 
		.getSystemService(Context.CONNECTIVITY_SERVICE); 
		NetworkInfo mWiFiNetworkInfo = mConnectivityManager 
		.getNetworkInfo(ConnectivityManager.TYPE_WIFI); 
		if (mWiFiNetworkInfo != null) { 
		return mWiFiNetworkInfo.isAvailable(); 
		} 
		} 
		return false; 
		} 
	@Override
	public void onBackPressed() {
//		super.onBackPressed();
		new AlertDialog.Builder(MainTabActivity.this).setTitle(mContext.getResources().getString(R.string.sure_to_exit))
		.setNegativeButton(mContext.getResources().getString(R.string.ok), new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			
				dialog.dismiss();
				MainTabActivity.this.finish();
			}
		})
		.setNeutralButton(mContext.getResources().getString(R.string.cancel), new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).show();
		
	}

	@Override
	public void onClick(View v) {
//		subTabView3.setBackgroundColor(R.color.transparent);
//		subTabView3.setBackgroundColor(R.color.transparent);
//		subTabView3.setBackgroundColor(R.color.transparent);
//		subTabView3.setBackgroundColor(R.color.transparent);
	}
}

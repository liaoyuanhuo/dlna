package com.talent.allshare;

import java.util.ArrayList;
import java.util.List;

import org.cybergarage.upnp.Device;
import org.cybergarage.util.CommonLog;
import org.cybergarage.util.LogFactory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.talent.allshare.adapter.ContentAdapter;
import com.talent.allshare.adapter.DeviceAdapter;
import com.talent.allshare.bean.PlaylistBean;
import com.talent.allshare.network.Item;
import com.talent.allshare.network.ItemFactory;
import com.talent.allshare.network.UpnpUtil;
import com.talent.allshare.player.DownLoadHelper;
import com.talent.allshare.player.DownLoadHelper.IDownLoadCallback;
import com.talent.allshare.player.FileManager;
import com.talent.allshare.player.MediaManager;
import com.talent.allshare.proxy.AllShareProxy;
import com.talent.allshare.proxy.ContentManager;
import com.talent.allshare.proxy.ControlRequestProxy;
import com.talent.allshare.proxy.ControlRequestProxy.ControlRequestCallback;
import com.talent.allshare.proxy.DeviceBrocastFactory;
import com.talent.allshare.proxy.IDeviceChangeListener;
import com.talent.allshare.softapplication.SoftApplication;
import com.talent.allshare.util.CommonUtil;
import com.youplayer.player.R;
import com.youplayer.player.YouExplorer;

@SuppressLint("ResourceAsColor")
public class ContentActivity extends Activity implements OnItemClickListener,
		IDeviceChangeListener, ControlRequestCallback, OnClickListener {

	private static final CommonLog log = LogFactory.createLog();

	private TextView mTVSelDeV;
	private ListView mContentListView;
	private ImageView mBtnBack;

	private ContentAdapter mContentAdapter;
	private AllShareProxy mAllShareProxy;
	private ContentManager mContentManager;

	private List<Item> mCurItems, mSafe;
	private DeviceBrocastFactory mBrocastFactory;

	private Handler mHandler;

	private int currentFlag = -1;// video 0 ,music 1 ,photo 2, file 3
	private ImageView videoBtn;//

	private ImageView musicBtn;

	private ImageView photoBtn;

	private ImageView loaclBtn;

	private ImageView mUpdateBtn;

	private ListView mDevListView;
	private DeviceAdapter mDevAdapter;

	private ImageView updateBtn;

	private LinearLayout allshareView;

	private LinearLayout contentView;

	private DownLoadHelper mDownLoadHelper;

	private Handler handler;

	private LinearLayout ll_progress;

      private TextView myfloatview;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		handler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case 0x90001:
					Toast.makeText(ContentActivity.this, getResources().getString(R.string.downloadok), 
							Toast.LENGTH_SHORT).show();
					break;
				case 0x90002:
					Toast.makeText(ContentActivity.this, getResources().getString(R.string.downloaderror),
							Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
				}

			};
		};

		setContentView(R.layout.content_layout);
		mDownLoadHelper = new DownLoadHelper();
		mDownLoadHelper.init();
		initView();
		initData();
		mAllShareProxy.startSearch();

	    /*
		myfloatview=new TextView(getApplicationContext());       
        myfloatview.setText("Only for demo !!!"); 
	 myfloatview.setBackgroundColor(0x7000ff00);
	 myfloatview.setTextColor(0xFFFF0000);
    
	   WindowManager wm=(WindowManager)getApplicationContext().getSystemService("window"); 

	  WindowManager.LayoutParams lp= new WindowManager.LayoutParams(
	  	150,
	  	31,
	  	WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
	  	WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
	  	PixelFormat.RGBA_8888);
	  
        lp.gravity=Gravity.LEFT|Gravity.TOP;   
        lp.x=20;  
        lp.y=50;  
        lp.width=140;  
        lp.height=25;  
        wm.addView(myfloatview, lp);   */


	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stu =
		currentFlag = -1;
		super.onResume();

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (mContentManager != null)
			mContentManager.clear();
		if (mBrocastFactory != null)
			mBrocastFactory.unRegisterListener();


		if(mAllShareProxy!=null) {
			mAllShareProxy.exitSearch();
		}

		/*if(myfloatview!=null ) {

			WindowManager wm=(WindowManager)getApplicationContext().getSystemService("window");	
			wm.removeView(myfloatview);
			myfloatview=null;
		}*/


		super.onDestroy();
	}

	private void initView() {
		ll_progress = (LinearLayout) findViewById(R.id.ll_progress);
		iv_loading_frame = (ImageView) findViewById(R.id.iv_loading_frame);
		allshareView = (LinearLayout) findViewById(R.id.allshare_ll);
		contentView = (LinearLayout) findViewById(R.id.content_ll);
		allshareView.setVisibility(View.VISIBLE);
		contentView.setVisibility(View.GONE);
		startLoadingFrameAnim(iv_loading_frame);
		ll_progress.setVisibility(View.VISIBLE);
		updateBtn = (ImageButton) findViewById(R.id.device_update);

		mDevListView = (ListView) findViewById(R.id.device_list);
		mDevListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Device device = (Device) parent.getItemAtPosition(position);
				mAllShareProxy.setSelectedDevice(device);
				allshareView.setVisibility(View.GONE);
				contentView.setVisibility(View.VISIBLE);
				ll_progress.setVisibility(View.GONE);
				initContent();
			}
		});
		TextView emptyView = new TextView(this);
		emptyView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		emptyView.setTextSize(23);
		emptyView.setVisibility(View.GONE);
		// ViewGroup v;
		((ViewGroup) mDevListView.getParent()).addView(emptyView);
		mDevListView.setEmptyView(emptyView);
		mTVSelDeV = (TextView) findViewById(R.id.tv_selDev);
		mContentListView = (ListView) findViewById(R.id.content_list);
		mContentListView.setOnItemClickListener(this);
		mContentListView
				.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
							View arg1, int position, long arg3) {
						Item item = (Item) parent.getItemAtPosition(position);
//						showTheDialog(position, item, "");
//						if (UpnpUtil.isVideoItem(item)) {
//							showTheDialog(position, item, "music");
//						}
						downloadbyURL(item.getRes());
						return true;
					}

				});
		mBtnBack = (ImageView) findViewById(R.id.btn_back);
		mBtnBack.setOnClickListener(this);
		mUpdateBtn = (ImageView) findViewById(R.id.btn_update);
		mUpdateBtn.setOnClickListener(this);

		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage("Loading...");

		videoBtn = (ImageView) findViewById(R.id.video_btn);
		musicBtn = (ImageView) findViewById(R.id.music_btn);
		photoBtn = (ImageView) findViewById(R.id.photo_btn);
		loaclBtn = (ImageView) findViewById(R.id.loacl_btn);

		videoBtn.setOnClickListener(this);
		musicBtn.setOnClickListener(this);
		photoBtn.setOnClickListener(this);
		loaclBtn.setOnClickListener(this);

		updateBtn.setOnClickListener(this);
	}

	/**
	 * 显示下载对话框
	 * 
	 * @param arg2
	 * @param item
	 */
	Dialog dialog = null;
	int switchFlag = -1;

	private void showTheDialog(int arg2, final Item item, String string) {
		if ("music".equals(string)) {

		} else {

		}
		new AlertDialog.Builder(this)
				.setTitle(getResources().getString(R.string.qingxuanze))
				.setIcon(android.R.drawable.ic_dialog_info)
				.setSingleChoiceItems(new String[] { "下载", "添加 到播放列表" }, 0,
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									int which) {
								switchFlag = which;
							}
						})
				.setNegativeButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							Toast.makeText(ContentActivity.this, "开始下载",
									Toast.LENGTH_SHORT).show();
							Log.e("gzf", "开始下载");
							String requestUrl = item.getRes();
							Log.e("gzf", "" + requestUrl);
							mDownLoadHelper.syncDownLoadFile(requestUrl,
									FileManager.mkSaveFilePath(requestUrl),
									new IDownLoadCallback() {

										@Override
										public void downLoadResult(
												boolean isSuccess,
												String savePath) {
											//
											Log.e("gzf", "" + isSuccess);
											download(isSuccess);
										}
									});
							break;
						case 1:
							List<PlaylistBean> list = SoftApplication
									.getInstance().getMusicPlaylists();
							list.add(new PlaylistBean("1", "2", "3", item));
							SoftApplication.getInstance().setMusicPlaylists(
									list);

							break;

						default:
							break;
						}
					}
				}).show();

		// AlertDialog.Builder builder = new Builder(ContentActivity.this);
		// builder.setMessage("是否下载该项"+item.getAlbum());
		//
		// builder.setTitle("提示");
		//
		// builder.setPositiveButton("确认", new
		// android.content.DialogInterface.OnClickListener() {
		//
		// @Override
		// public void onClick(final DialogInterface dialog, int which) {
		// Toast.makeText(ContentActivity.this, "开始下载",
		// Toast.LENGTH_SHORT).show();
		// Log.e("gzf", "开始下载");
		// String requestUrl = item.getRes();
		// Log.e("gzf", ""+requestUrl);
		// mDownLoadHelper.syncDownLoadFile(requestUrl,
		// FileManager.mkSaveFilePath(requestUrl), new IDownLoadCallback() {
		//
		// @Override
		// public void downLoadResult(boolean isSuccess, String savePath) {
		// //
		// Log.e("gzf", ""+isSuccess);
		// download(isSuccess );
		// dialog.dismiss();
		// }
		// });
		// }
		// });
		// builder.setNegativeButton("取消", new
		// android.content.DialogInterface.OnClickListener() {
		//
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// dialog.dismiss();
		// }
		// });
		//
		// builder.create().show();

		// View view = View.inflate(ContentActivity.this,
		// R.layout.download_dialog, null);
		// TextView tv_download = (TextView)view.findViewById(R.id.tv_download);
		// tv_download.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View arg0) {
		//
		// }
		// });
		// dialog = new Dialog(this,R.style.dialog);
		// dialog.setContentView(R.layout.download_dialog);
		// dialog.set
		// dialog.show();

		// dialog = new
		// AlertDialog.Builder(this).setView(view)ContentActivity.show();
	}

	public void download(boolean bo) {

		if (bo) {
			// Toast.makeText(ContentActivity.this,"下载成功",
			// Toast.LENGTH_SHORT).show();
			System.err.println(getResources().getString(R.string.downloadok));
			handler.sendEmptyMessage(0x90001);

		} else {
			// Toast.makeText(ContentActivity.this,"下载失败",
			// Toast.LENGTH_SHORT).show();
			handler.sendEmptyMessage(0x90002);
			System.err.println(getResources().getString(R.string.downloaderror));
		}
	}

	private void initData() {

		mDevAdapter = new DeviceAdapter(this, new ArrayList<Device>());
		mDevListView.setAdapter(mDevAdapter);
		mBrocastFactory = new DeviceBrocastFactory(this);
		mBrocastFactory.registerListener(this);
		mAllShareProxy = AllShareProxy.getInstance(this);
		//

	}

	private void initContent() {

		mContentManager = ContentManager.getInstance();

		mCurItems = new ArrayList<Item>();
		mContentAdapter = new ContentAdapter(this, mCurItems);
		mContentListView.setAdapter(mContentAdapter);

		updateSelDev();// 设置选中名字

		mHandler = new Handler();

		currentFlag = -1;// init currentflag video flag;
		// TODO setVidebuttton fouce
		mHandler.postDelayed(new RequestDirectoryRunnable(), 100);
	}

	private void requestDirectory() {
		Device selDevice = mAllShareProxy.getSelectedDevice();
		if (selDevice == null) {
			CommonUtil.showToask(this, getResources().getString(R.string.nodevices));
			// finish();

			return;
		}

		ControlRequestProxy.syncGetDirectory(this, this);
		showProgress(true);
	}

	class RequestDirectoryRunnable implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			requestDirectory();
		}

	}

	private void setContentlist(List<Item> list, int type) {
		mSafe = new ArrayList<Item>();
		mSafe = list;
		mCurItems = list;
		if (list == null) {
			mContentAdapter.clear();
		} else {
			List<Item> newlist = new ArrayList<Item>();
			for (int i = 0; i < list.size(); i++) {
				Item item = list.get(i);
				switch (type) {
				case 0:
					if (UpnpUtil.isVideoItem(item) || UpnpUtil.isNULLItem(item)) {
						newlist.add(item);
					}
					break;
				case 1:
					if (UpnpUtil.isAudioItem(item) || UpnpUtil.isNULLItem(item)) {
						newlist.add(item);
					}
					break;
				case 2:
					if (UpnpUtil.isPictureItem(item)
							|| UpnpUtil.isNULLItem(item)) {
						newlist.add(item);
					}
					break;

				case 3:
					if (UpnpUtil.isFileItem(item) || UpnpUtil.isNULLItem(item)) {
						newlist.add(item);
					}
					break;

				default:
					newlist.add(item);
					break;
				}
			}

			mContentAdapter.refreshData(newlist);
		}
	}

	private ProgressDialog mProgressDialog;

	private void showProgress(boolean bShow) {
		mProgressDialog.dismiss();
		if (bShow) {
			mProgressDialog.show();
		}

	}

	private void goMusicPlayerActivity(int index, Item item) {
//		Intent intent = new Intent();
//		intent.setClass(this, MusicPlayerActivity.class);
//		intent.putExtra(MusicPlayerActivity.PLAY_INDEX, index);
//		ItemFactory.putItemToIntent(item, intent);
//		ContentActivity.this.startActivity(intent);
		
		
		
		Intent intent = new Intent(this, YouExplorer.class);
		intent.setAction("android.intent.action.VIEW");
		intent.setData(Uri.parse(item.getRes()));
		
		ContentActivity.this.startActivity(intent);
//		finish();
	

	}

	private void goVideoPlayerActivity(int position, Item item) {

		// MediaManager.getInstance().setVideoList(mCurItems);
		//
		// Intent intent = new Intent();
		// intent.setClass(this, VideoPlayerActivity.class);
		// intent.putExtra(VideoPlayerActivity.PLAY_INDEX, position);
		//
		// ItemFactory.putItemToIntent(item, intent);
		// ContentActivity.this.startActivity(intent);
		Intent intent = new Intent(this, YouExplorer.class);
		intent.setAction("android.intent.action.VIEW");
		intent.setData(Uri.parse(item.getRes()));
		ContentActivity.this.startActivity(intent);
	}

	private void goPicturePlayerActivity(int position, Item item) {

				int position1=-1;
                   List<Item> newlist = new ArrayList<Item>();
			for (int i = 0; i < mCurItems.size(); i++) {
				Item item1 = mCurItems.get(i);
			
					if (UpnpUtil.isPictureItem(item1)) {
						newlist.add(item1);
						if(i<=position) {
							position1++;
						}
					}
				}
		//MediaManager.getInstance().setPictureList(mCurItems);
		MediaManager.getInstance().setPictureList(newlist);
		
		Intent intent = new Intent();
		intent.setClass(this, PicturePlayerActivity.class);
		intent.putExtra(PicturePlayerActivity.PLAY_INDEX, position1);
		ItemFactory.putItemToIntent(item, intent);
		ContentActivity.this.startActivity(intent);
	}

	private void goFilePlayerActivity(int position, Item item) {
		int position1=-1;
		List<Item> newlist = new ArrayList<Item>();
			for (int i = 0; i < mCurItems.size(); i++) {
				Item item1 = mCurItems.get(i);
			
					if (UpnpUtil.isFileItem(item1)) {
						newlist.add(item1);
						if(i<=position) {
							position1++;
						}
					}
				}
		//MediaManager.getInstance().setPictureList(mCurItems);
		MediaManager.getInstance().setPictureList(newlist);
		

		Intent intent = new Intent();
		intent.setClass(this, FileReaderActivity.class);
		intent.putExtra(FileReaderActivity.PLAY_INDEX, position1);
		ItemFactory.putItemToIntent(item, intent);
		ContentActivity.this.startActivity(intent);
		// to be done
	}

	/**
	 * 开始"..."的帧动画
	 * 
	 * @param imageView
	 */
	private ImageView iv_loading_frame;
	private AnimationDrawable loadingFrameAnimDrawable;

	public void startLoadingFrameAnim(ImageView imageView) {
		imageView.setBackgroundResource(R.anim.frame_animation);
		loadingFrameAnimDrawable = (AnimationDrawable) imageView
				.getBackground();
		imageView.getViewTreeObserver().addOnPreDrawListener(
				new OnPreDrawListener() {
					@Override
					public boolean onPreDraw() {
						loadingFrameAnimDrawable.start();
						return true;
					}
				});
	}

	/**
	 * 停止载入中的帧动画"..."
	 */
	public void stopLoadingFrameAnim() {
		if (loadingFrameAnimDrawable != null) {
			loadingFrameAnimDrawable.stop();
		}
	}
	boolean exit = true;
	private void back(boolean b) {
		
		if(mContentManager == null) 
		{
			new AlertDialog.Builder(ContentActivity.this)
			.setTitle(getResources().getString(R.string.sure_to_exit))
			.setNegativeButton(
					getResources().getString(R.string.ok),
					new android.content.DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							dialog.dismiss();
							ContentActivity.this.finish();
						}
					})
			.setNeutralButton(
					getResources().getString(R.string.cancel),
					new android.content.DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							dialog.dismiss();
						}
					}).show();
			return;
		}
		System.out.println("back---1");
		List<Item> listpre = mContentManager.peekListItem();
		mContentManager.popListItem();
		List<Item> listnxt = mContentManager.peekListItem();
		if(listpre ==null){
			if(exit){
				startLoadingFrameAnim(iv_loading_frame);
				if (b)
					return;
					
				// mBtnBack.setVisibility(View.INVISIBLE);
					new AlertDialog.Builder(ContentActivity.this)
							.setTitle(getResources().getString(R.string.sure_to_exit))
							.setNegativeButton(
									getResources().getString(R.string.ok),
									new android.content.DialogInterface.OnClickListener() {
	
										@Override
										public void onClick(DialogInterface dialog,
												int which) {
											dialog.dismiss();
											ContentActivity.this.finish();
										}
									})
							.setNeutralButton(
									getResources().getString(R.string.cancel),
									new android.content.DialogInterface.OnClickListener() {
	
										@Override
										public void onClick(DialogInterface dialog,
												int which) {
											dialog.dismiss();
										}
									}).show();
				
			}else{
				exit =true;
			}
		}else{
			if(listnxt == null){
				contentView.setVisibility(View.GONE);
				allshareView.setVisibility(View.VISIBLE);
				exit = true;
			}else{
				setContentlist(listnxt, currentFlag);
				exit = false;
			}
		}
		
		
//		if (mContentManager != null) {
//
//			List<Item> list1 = mContentManager.peekListItem();
//			mContentManager.popListItem();
//			List<Item> list = mContentManager.peekListItem();
//			if(list1 == null){
//				startLoadingFrameAnim(iv_loading_frame);
//				if (b)
//					return;
//				
//				if(exit){
//					
//				// mBtnBack.setVisibility(View.INVISIBLE);
//					new AlertDialog.Builder(ContentActivity.this)
//							.setTitle(getResources().getString(R.string.sure_to_exit))
//							.setNegativeButton(
//									getResources().getString(R.string.ok),
//									new android.content.DialogInterface.OnClickListener() {
//	
//										@Override
//										public void onClick(DialogInterface dialog,
//												int which) {
//											dialog.dismiss();
//											ContentActivity.this.finish();
//										}
//									})
//							.setNeutralButton(
//									getResources().getString(R.string.cancel),
//									new android.content.DialogInterface.OnClickListener() {
//	
//										@Override
//										public void onClick(DialogInterface dialog,
//												int which) {
//											dialog.dismiss();
//										}
//									}).show();
//				}
//				contentView.setVisibility(View.GONE);
//				allshareView.setVisibility(View.VISIBLE);
//				exit = true;
//			}else{
//				if(list == null){
//					exit = true;
//				}else{
//					setContentlist(list, currentFlag);
//					exit = false;
//				}
//			}
			
//			if (list == null) {
//				if(exit){
//
//					startLoadingFrameAnim(iv_loading_frame);
//					if (b)
//						return;
//					// mBtnBack.setVisibility(View.INVISIBLE);
//					new AlertDialog.Builder(ContentActivity.this)
//							.setTitle(getResources().getString(R.string.sure_to_exit))
//							.setNegativeButton(
//									getResources().getString(R.string.ok),
//									new android.content.DialogInterface.OnClickListener() {
//
//										@Override
//										public void onClick(DialogInterface dialog,
//												int which) {
//											dialog.dismiss();
//											ContentActivity.this.finish();
//										}
//									})
//							.setNeutralButton(
//									getResources().getString(R.string.cancel),
//									new android.content.DialogInterface.OnClickListener() {
//
//										@Override
//										public void onClick(DialogInterface dialog,
//												int which) {
//											dialog.dismiss();
//										}
//									}).show();
//					
//				}else{
//					contentView.setVisibility(View.GONE);
//					allshareView.setVisibility(View.VISIBLE);
//					exit = true;
//				}
//				// super.onBackPressed();
//							} else {
//								exit= false;
//				// mBtnBack.setVisibility(View.VISIBLE);
//				setContentlist(list, currentFlag);
//			}
//		} else {
//			exit = false;
//			new AlertDialog.Builder(ContentActivity.this)
//					.setTitle(getResources().getString(R.string.sure_to_exit))
//					.setNegativeButton(
//							getResources().getString(R.string.ok),
//							new android.content.DialogInterface.OnClickListener() {
//
//								@Override
//								public void onClick(DialogInterface dialog,
//										int which) {
//									dialog.dismiss();
//									ContentActivity.this.finish();
//								}
//							})
//					.setNeutralButton(
//							getResources().getString(R.string.cancel),
//							new android.content.DialogInterface.OnClickListener() {
//
//								@Override
//								public void onClick(DialogInterface dialog,
//										int which) {
//									dialog.dismiss();
//								}
//							}).show();
//		}
	}

	@Override
	public void onBackPressed() {
		System.out.println("back");
		back(false);

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Item item = (Item) parent.getItemAtPosition(position);
		log.e("item = \n" + item.getShowString());

		if (UpnpUtil.isAudioItem(item)) {
			goMusicPlayerActivity(position, item);
		} else if (UpnpUtil.isVideoItem(item)) {
			goVideoPlayerActivity(position, item);
		} else if (UpnpUtil.isPictureItem(item)) {
			goPicturePlayerActivity(position, item);
		} else if (UpnpUtil.isFileItem(item)) {
			goFilePlayerActivity(position, item);
		} else {
			ControlRequestProxy.syncGetItems(ContentActivity.this,
					item.getStringid(), ContentActivity.this);
			showProgress(true);
		}

	}

	private void updateDeviceList() {
		List<Device> list = mAllShareProxy.getDeviceList();
		mDevAdapter.refreshData(list);
	}

	@Override
	public void onDeviceChange(boolean isSelDeviceChange) {
		// TODO Auto-generated method stub
		contentView.setVisibility(View.GONE);
		allshareView.setVisibility(View.VISIBLE);
		startLoadingFrameAnim(iv_loading_frame);
		ll_progress.setVisibility(View.GONE);
		updateDeviceList();

		if (isSelDeviceChange) {
			//CommonUtil.showToask(this, "当前设备已卸载...");
			ll_progress.setVisibility(View.VISIBLE);
			// finish();
		}
	}

	@Override
	public void onGetItems(final List<Item> list) {
		ll_progress.setVisibility(View.GONE);
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				showProgress(false);
				if (list == null) {
					CommonUtil.showToask(ContentActivity.this, getResources().getString(R.string.not_found_path));
					return;
				}
				mContentManager.pushListItem(list);
				setContentlist(list, currentFlag);

			}
		});
	}

	private void updateSelDev() {
		setSelDevUI(mAllShareProxy.getSelectedDevice());
	}

	private void setSelDevUI(Device device) {
		if (device == null) {
			mTVSelDeV.setText("no select device");
		} else {
			mTVSelDeV.setText(device.getFriendlyName());
		}

	}


	class DeviceUpdateRunnable implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(mAllShareProxy!=null) {
				//mAllShareProxy.resetSearch();
				mAllShareProxy.startSearch();
			}
		}

	}
	public void setNOselect(int i,boolean dou){
		ImageView iv01 = (ImageView)findViewById(R.id.iv_01);
		ImageView iv02 = (ImageView)findViewById(R.id.iv_02);
		ImageView iv03 = (ImageView)findViewById(R.id.iv_03);
		ImageView iv04 = (ImageView)findViewById(R.id.iv_04);
		iv01.setVisibility(View.INVISIBLE);
		iv02.setVisibility(View.INVISIBLE);
		iv03.setVisibility(View.INVISIBLE);
		iv04.setVisibility(View.INVISIBLE);
		switch (i) {
		case 1:
			if(!dou){
				iv01.setVisibility(View.VISIBLE);
			}
			break;
		case 2:
			if(!dou){
				iv02.setVisibility(View.VISIBLE);
			}
			break;
		case 3:
			if(!dou){
				iv03.setVisibility(View.VISIBLE);
			}
			break;
		case 4:
			if(!dou){
				iv04.setVisibility(View.VISIBLE);
			}
			break;

		default:
			break;
		}
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.video_btn:
			if (currentFlag == 0) {
				currentFlag = -1;
				setNOselect(1, true);
			} else {
				selectBtn(0);
				setNOselect(1, false);
			}
			// videoBtn.setBackgroundColor(R.color.yellow);
			List<Item> list = mContentManager.peekListItem();
			setContentlist(list, currentFlag);
			break;
		case R.id.music_btn:
			if (currentFlag == 1) {
				currentFlag = -1;
				setNOselect(2, true);
			} else {
				selectBtn(1);
				setNOselect(2, false);
			}
			list = mContentManager.peekListItem();
			setContentlist(list, currentFlag);
			break;
		case R.id.photo_btn:
			if (currentFlag == 2) {
				currentFlag = -1;
				setNOselect(3, true);
			} else {
				selectBtn(2);
				setNOselect(3, false);
			}
			list = mContentManager.peekListItem();
			setContentlist(list, currentFlag);
			break;
		case R.id.loacl_btn:
			if (currentFlag == 3) {
				currentFlag = -1;
				setNOselect(4, true);
			} else {
				selectBtn(3);
				setNOselect(4, false);
			}
			list = mContentManager.peekListItem();
			setContentlist(list, currentFlag);
			break;
		case R.id.btn_back:
			back(true);
			break;
		case R.id.device_update:
			// TODO刷新界面
			if(mDevAdapter!=null) {
				List<Device> deviceList =  new ArrayList<Device>();
				mDevAdapter.refreshData(deviceList);
			}
			
			if(mAllShareProxy!=null) {
				mAllShareProxy.resetSearch();
	
			}
			if(handler!=null) {
				handler.postDelayed(new DeviceUpdateRunnable(), 100);
			}
			
			break;

		}
		videoBtn.setImageResource(R.drawable.tab_video_s);
		musicBtn.setImageResource(R.drawable.tab_music_s);
		photoBtn.setImageResource(R.drawable.tab_pic_s);
		loaclBtn.setImageResource(R.drawable.tab_file_s);
		switch (currentFlag) {
		case -1:

			break;
		case 0:
			videoBtn.setImageResource(R.drawable.tab_video);
			break;
		case 1:
			musicBtn.setImageResource(R.drawable.tab_music);
			break;
		case 2:
			photoBtn.setImageResource(R.drawable.tab_pic);
			break;
		case 3:
			loaclBtn.setImageResource(R.drawable.tab_file);

			break;
		default:
			break;
		}
	}

	private void selectBtn(int i) {
		currentFlag = i;
		switch (i) {
		case 0:
			break;
		case 1:
			break;
		case 2:
			break;
		default:
			break;
		}
	}

	protected void downloadbyURL(String requestUrl) {

	DownloadProcess download = new DownloadProcess(this);
			String decodeUrl = download.decodeUri(requestUrl);
			String fileName = decodeUrl
					.substring(decodeUrl.lastIndexOf("/") + 1);
			download.startDownload(fileName, requestUrl);
	}
	/*
	@SuppressLint("NewApi") protected void downloadbyURL(String requestUrl) {
		DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
		  
		 Uri uri = Uri.parse(requestUrl);
		 Request request = new Request(uri);
		 //设置允许使用的网络类型，这里是移动网络和wifi都可以 
		 request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE|DownloadManager.Request.NETWORK_WIFI); 
		 //禁止发出通知，既后台下载，如果要使用这一句必须声明一个权限：android.permission.DOWNLOAD_WITHOUT_NOTIFICATION 
		 //request.setShowRunningNotification(false); 
		 //不显示下载界面 
		 request.setVisibleInDownloadsUi(false);
		       
		//request.setDestinationInExternalFilesDir(this, null, "tar.apk");
		long id = downloadManager.enqueue(request);		
	}*/
}

package com.demo.recyclerviewuploadpicture;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Canvas;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;


import butterknife.ButterKnife;

public class PictureActivity extends AppCompatActivity implements PictureAdapter.SaveEditListener {


    private Button btn_pic_choose,btn_pic_upload;
    private int screenwidth;
    private MyDialog myDialog;
    RecyclerView mRecyView;
    private PictureAdapter myAdapter;
    private File mTmpFile;
    private long exitTime = 0;
    private ArrayList<String> mDatas = new ArrayList<>();
    //设置最大数量
    private int mMaxNum = 100;
    private boolean remove;
    private int lastVisibleItem ;
    private ItemTouchHelper itemTouchHelper;
    LinearLayoutManager linearLayoutManager;
    private MyActionSheetDialog myActionSheetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //透明导航栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        ButterKnife.bind(this);
        initView();
        setListener();
    }

    private void initView() {
        btn_pic_choose = (Button) findViewById(R.id.btn_pic_choose);
        btn_pic_upload = (Button) findViewById(R.id.btn_pic_upload);
        mRecyView = (RecyclerView)findViewById(R.id.recyclerView_pic);

        linearLayoutManager = new LinearLayoutManager(PictureActivity.this);
        myAdapter = new PictureAdapter(PictureActivity.this,mDatas,mMaxNum);
        mRecyView.setLayoutManager(linearLayoutManager);
        mRecyView.setAdapter(myAdapter);

        myAdapter.setOnItemClickListener(new PictureAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {

            }
            @Override
            public void onItemLongClick(View v, final int position) {
                //Dialog 是否删除
                myDialog = new MyDialog(PictureActivity.this);
                myDialog.setTitle("提示");
                myDialog.setMessage("确定要删除此项目吗？");
                myDialog.setYesOnclickListener("删除", new MyDialog.onYesOnclickListener() {
                    @Override
                    public void onYesClick() {
                        myAdapter.removeItem(position);
                        myDialog.dismiss();
                    }
                });
                myDialog.setNoOnclickListener("取消", new MyDialog.onNoOnclickListener() {
                    @Override
                    public void onNoClick(){
                        myDialog.dismiss();
                    }
                });
                myDialog.show();

            }
        });


        btn_pic_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //选择照片
                Toast.makeText(PictureActivity.this,"请拍照或从相册中选取要上传的图片",Toast.LENGTH_SHORT).show();
                myActionSheetDialog = new MyActionSheetDialog(PictureActivity.this);
                myActionSheetDialog.addMenuItem("拍照");
                myActionSheetDialog.addMenuItem("相册获取");
                myActionSheetDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        myActionSheetDialog.hide();
                    }
                });
                myActionSheetDialog.setMenuListener(new MyActionSheetDialog.MenuListener() {
                    @Override
                    public void onItemSelected(int position, String item) {
                        if(position == 0){//拍照
                            Toast.makeText(PictureActivity.this,"拍照去喽",Toast.LENGTH_SHORT).show();
                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                                // 设置系统相机拍照后的输出路径
                                // 创建临时文件
                                mTmpFile = PicUtils.createFile(getApplicationContext());
                                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTmpFile));
                                startActivityForResult(cameraIntent, 102);
                            } else {
                                Toast.makeText(getApplicationContext(), "呜呜...没有找到相机呀", Toast.LENGTH_SHORT).show();
                            }
                        }else if(position == 1){//相册
                            Toast.makeText(PictureActivity.this,"浏览相册去喽",Toast.LENGTH_SHORT).show();
                            Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
                            intent1.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                            startActivityForResult(intent1, 103);
                        }
                    }
                    @Override
                    public void onCancel() {
                        Toast.makeText(PictureActivity.this, "onCancel", Toast.LENGTH_SHORT).show();
                    }
                });
                myActionSheetDialog.show();
            }
        });
        btn_pic_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //这里应该写个progressbar 临时用Toast代替了
                Toast toast = Toast.makeText(getApplicationContext(), "正在上传"+mDatas, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);//屏幕中心
                toast.show();
                /**
                 * 开启新线程（重要）new Thread  这里没写
                 * 1.取得各个图片路径 和内容
                 * 2.取得URL 进行上传
                 * 3.根据返回结果进行下一步操作
                 */
                for(int i = 0;i<mDatas.size();i++){
                    Log.i("图片"+i+"路径是",""+mDatas.get(i));
                    String ii = i+"";//这里直接写int类型也可以，当然也改一下存放数据那里
                    SharedPreferences sharedPreferences = getSharedPreferences("map", Context.MODE_PRIVATE);
                    String ss = sharedPreferences.getString(ii, "");
                    Log.i("图片"+i+"描述是",ss);

//                  这里因为没有写具体地址，所以注释了，
//                    String requestURL = "服务器地址";
//                    String picPath = mDatas.get(i);
//                    File file = new File(picPath);
//                    Log.i("upload", "file exists:" + file.exists());
//                    if (file.exists()) {
//                        Map<String, String> params = new HashMap<>();
//                        params.put(ii, ss);
//                        //...如果有其他参数添加到这里
//                        String request = UploadUtil.uploadFile(file, requestURL, params, "image");
//                        Log.i("upload", request);
//                    }

                }



            }
        });
    }
    private void setListener() {
        WindowManager wm = (WindowManager)PictureActivity.this
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        screenwidth =outMetrics.widthPixels;

        itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int dragFlags=0,swipeFlags=0;
                if(recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager){
                    dragFlags=ItemTouchHelper.UP|ItemTouchHelper.DOWN|ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT;
                }else if(recyclerView.getLayoutManager() instanceof LinearLayoutManager){
                    dragFlags=ItemTouchHelper.UP|ItemTouchHelper.DOWN;
                    //设置侧滑方向为从左到右和从右到左都可以
                    swipeFlags = ItemTouchHelper.LEFT;
                }
                return makeMovementFlags(dragFlags,swipeFlags);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                myAdapter.removeItem(viewHolder.getAdapterPosition());
            }
            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                viewHolder.itemView.scrollTo(-(int)dX,-(int)dY);//根据item的滑动偏移修改HorizontalScrollView的滚动
                if(Math.abs(dX)>screenwidth/5&&!remove&&isCurrentlyActive){
                    //用户收滑动item超过屏幕5分之1，标记为要删除
                    myAdapter.removeItem(viewHolder.getAdapterPosition());
                    remove=true;
                }else if(Math.abs(dX)<screenwidth/5&&remove&&!isCurrentlyActive){
                    //用户收滑动item没有超过屏幕5分之1，标记为不删除
                    remove=false;
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });
        mRecyView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //获取加载的最后一个可见视图在适配器的位置
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case 102:
                if (resultCode == Activity.RESULT_OK) {
                    mDatas.add(mTmpFile.getAbsolutePath());
                }
                break;
            case 103:
                // 外界的程序访问ContentProvider所提供数据 可以通过ContentResolver接口
                try {
                    ContentResolver resolver = getContentResolver();
                    Cursor query = resolver.query(data.getData(), null, null, null, null);

                    String str = null;
                    while (query.moveToNext()) {
                        str =query.getString(query.getColumnIndex(MediaStore.Images.Media.DATA));
                    }
                    query.close();

                    mDatas.add(str);
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
        }
        myAdapter.notifyDataSetChanged();
    }

    @Override
    public void SaveEdit(int position, String string) {
//        String p = position+"";
//        map.put(p,string);
//        Toast.makeText(PictureActivity.this,"position是："+position+"String是:"+string,Toast.LENGTH_SHORT).show();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            if((System.currentTimeMillis()-exitTime) > 2000){
//                Toast toast;
//                toast = Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT);
//                toast.setGravity(Gravity.CENTER, 0, 0);
//                toast.show();
                Toast  toast = Toast.makeText(getApplicationContext(),
                        "再按一次退出程序", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                RelativeLayout toastView = (RelativeLayout) toast.getView();
                ImageView imageCodeProject = new ImageView(getApplicationContext());
                imageCodeProject.setImageResource(R.mipmap.icon_pic);
                toastView.addView(imageCodeProject, 0);
                toast.show();
                exitTime = System.currentTimeMillis();
            } else {
                SharedPreferences mySharedPreferences= getSharedPreferences("map", Activity.MODE_PRIVATE);
                mySharedPreferences.getAll().clear();
                SharedPreferences.Editor editor = mySharedPreferences.edit();
                editor.clear().apply();
                PictureActivity.this.finish();
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

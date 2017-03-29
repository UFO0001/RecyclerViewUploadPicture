package com.demo.recyclerviewuploadpicture;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

/**
 * Created by UFO on 17/3/24.
 * 底部弹出选择菜单
 */

public class MyActionSheetDialog extends Dialog {

    private MyActionSheetDialog myActionSheetDialog;
    //动画
    private View mRootView;
    private Animation mShowAnim;//显示
    private Animation mDismissAnim;//消失
    private boolean isDismissing;//正在消失
    private MenuListener mMenuListener;
    private Button mCancel;
    private ListView mMenuItems;
    private ArrayAdapter<String> mAdapter;

    public MyActionSheetDialog(Context context){
        super(context, R.style.ActionSheetDialog);
        getWindow().setGravity(Gravity.BOTTOM);
        initView(context);
    }
    private void initView(Context context) {
        View rootView = View.inflate(context,R.layout.myactionsheetdialog,null);
        mCancel = (Button) rootView.findViewById(R.id.menu_cancel);
        mMenuItems = (ListView) rootView.findViewById(R.id.menu_items);
        mAdapter = new ArrayAdapter<String>(context,R.layout.menu_item){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                setBackground(position,view);
                return  view;
            }
            private void setBackground(int position,View view){
                int count = getCount();
                if(count == 1){
                    view.setBackgroundResource(R.drawable.menu_item_single);
                }else if(position == 0){
                    view.setBackgroundResource(R.drawable.menu_item_top);
                }else if(position == count - 1){
                    view.setBackgroundResource(R.drawable.menu_item_bottom);
                }else{
                    view.setBackgroundResource(R.drawable.menu_item_middle);
                }
            }
        };
        mMenuItems.setAdapter(mAdapter);
        this.setContentView(rootView);
        initAnim(context);
        mCancel.setOnClickListener(new View.OnClickListener() {//取消按钮事件
            @Override
            public void onClick(View v) {
                cancel();
                //myActionSheetDialog.dismiss();
            }
        });
        mMenuItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {//菜单事件
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mMenuListener != null){
                    mMenuListener.onItemSelected(position,mAdapter.getItem(position));
                    dismiss();
                }
            }
        });
        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if(mMenuListener != null){
                    mMenuListener.onCancel();
                }
            }
        });

    }
    public MyActionSheetDialog addMenuItem(String  items){
        mAdapter.add(items);
        return this;
    }
    public void toggle(){
        if(isShowing()){
            dismiss();
        }else{
            show();
        }
    }
    @Override
    public void show() {
        mAdapter.notifyDataSetChanged();
        super.show();
//        mRootView.startAnimation(mShowAnim); 动画出错 还在修改 故注释了先
    }

    @Override
    public void dismiss() {
        if(isDismissing) {
            return;
        }
        isDismissing = true;
        //mRootView.startAnimation(mDismissAnim);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//拦截Menu键 处理按下Menu时菜单消失
        if(keyCode == KeyEvent.KEYCODE_MENU){
            dismiss();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    private void initAnim(Context context){

        mShowAnim = AnimationUtils.loadAnimation(context,R.anim.translate_up);
        mDismissAnim = AnimationUtils.loadAnimation(context,R.anim.translate_down);
        mDismissAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //mRootView.startAnimation(mShowAnim);
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                dismissMe();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    private void dismissMe() {
        super.dismiss();
        isDismissing = false;
    }
    public interface MenuListener{
        void onItemSelected(int position, String item);
        void onCancel();
    }
    public MenuListener getMenuListener() {
        return mMenuListener;
    }

    public void setMenuListener(MenuListener menuListener) {
        mMenuListener = menuListener;
    }
}


















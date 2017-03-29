package com.demo.recyclerviewuploadpicture;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by UFO on 17/3/27.
 */

public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.MyViewHolder>{

    private Context mContext;
    private List<String> mDatas;
    private int            mMaxNum;
    private LayoutInflater mInflater;
    private String p,str;
    private boolean mIsDelete = false;
    OnItemClickListener mOnItemClickListener;
    public interface SaveEditListener{

        void SaveEdit(int position, String string);
    }

    public PictureAdapter(Context context, List<String> datas, int maxNum) {
        mContext = context;
        mDatas   = datas;
        mMaxNum  = maxNum;
        mInflater = LayoutInflater.from(context);
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.item_picture_recyview, parent, false);
        return new MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        //holder.setIsRecyclable(false);
        if (mDatas.size() < mMaxNum) {
                    String filePath = mDatas.get(position);
                    holder.mIvDisPlayItemPhoto.setVisibility(View.VISIBLE);
                    holder.my_miaoshu.setVisibility(View.VISIBLE);
                    holder.my_miaoshu.addTextChangedListener(new TextSwitcher(holder));
                    holder.my_miaoshu.setTag(position);

                    SharedPreferences sharedPreferences = mContext.getSharedPreferences("map", Context.MODE_PRIVATE);
                    String ss = sharedPreferences.getString(position+"", "");
                    holder.my_miaoshu.setText(ss);
                    Picasso.with(mContext).load(new File(filePath)).centerCrop().resize(PicUtils.dip2px(mContext,120), PicUtils.dip2px(mContext,120))
                            .error(R.mipmap.pictures_no).into(holder.mIvDisPlayItemPhoto);
        } else {
            Toast.makeText(mContext,"已经超过最大上传数量!",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
            return mDatas.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.img_recy)
        ImageView mIvDisPlayItemPhoto;
        @Bind(R.id.ed_recy)
        EditText my_miaoshu;
        @Bind(R.id.ll_recy)
        View      mRootView;


        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            initListener(itemView);
        }


        private void initListener(View itemView) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemLongClick(v, getAdapterPosition());
                    }
                    return false;
                }
            });
        }
    }


    public interface OnItemClickListener {
        void onItemClick(View v, int position);
        void onItemLongClick(View v, int position);
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void removeItem(final int position) {
        mDatas.remove(position);
        notifyItemRemoved(position);
    }


    public void setIsDelete(boolean isDelete) {
        mIsDelete = isDelete;
    }

    class TextSwitcher implements TextWatcher{
        private MyViewHolder myViewHolder;
        public TextSwitcher(MyViewHolder myViewHolder){
            this.myViewHolder = myViewHolder;
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            //用户输入完毕后 处理输入数据 回调给主界面处理
            SaveEditListener listener = (SaveEditListener) mContext;
            Log.i("图片输入监听","滴滴滴内容"+s);
            Log.i("图片输入监听","滴滴滴数量"+myViewHolder.my_miaoshu.getTag().toString());

            if (s != null){
                listener.SaveEdit(Integer.parseInt(myViewHolder.my_miaoshu.getTag().toString()),s.toString());
                p   = myViewHolder.my_miaoshu.getTag().toString();
                str = s.toString();
                SharedPreferences mySharedPreferences= mContext.getSharedPreferences("map", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = mySharedPreferences.edit();
                editor.putString(p, str);
                editor.commit();

            }
        }
    }



}

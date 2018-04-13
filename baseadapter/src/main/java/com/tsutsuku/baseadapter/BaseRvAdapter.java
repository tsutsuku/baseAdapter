package com.tsutsuku.baseadapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author tsutsuku
 * @Create 2017/1/11
 * @Description BaseRvAdapter
 */

public abstract class BaseRvAdapter<T> extends BaseAdapter<BaseRvAdapter.RvAdapterItem> implements IAdapter<T> {

    protected List<T> mDataList;

    private int currentPos;

    private Object mType;

    private ItemTypeUtil mUtil;

    private int pageIndex = 1;
    private int total = 0;
    private boolean loading;

    private Gson gson = new Gson();

    public BaseRvAdapter(@Nullable List<T> data) {
        if (data == null) {
            data = new ArrayList<>();
        }

        mDataList = data;
        mUtil = new ItemTypeUtil();
    }

    @Override
    public RvAdapterItem onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RvAdapterItem(parent.getContext(), parent, createItem(mType));
    }

    @Override
    public void onBindViewHolder(RvAdapterItem holder, int position) {
        onBindViewHolder(holder, position, null);
    }

    @Override
    public void onBindViewHolder(RvAdapterItem holder, int position, List<Object> payloads) {
        Object curItem = getConvertedData(mDataList.get(position), mType);
        holder.item.setCurItem(curItem);
        holder.item.setCurPosition(position);
        if (payloads != null && !payloads.isEmpty()) {
            holder.item.handleData(curItem, position, payloads);
        } else {
            holder.item.handleData(curItem, position);
        }

    }

    static class RvAdapterItem extends RecyclerView.ViewHolder {

        protected AdapterItem item;

        public RvAdapterItem(Context context, ViewGroup parent, AdapterItem item) {
            super(LayoutInflater.from(context).inflate(item.getLayoutResId(), parent, false));
            this.item = item;
            this.item.bindViews(itemView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        this.currentPos = position;
        mType = getItemType(mDataList.get(position));
        return mUtil.getIntType(mType);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public void setData(@NonNull List<T> data) {
        mDataList = data;
        notifyDataSetChanged();
    }

    @Override
    public List<T> getData() {
        return mDataList;
    }

    @Override
    public Object getItemType(T t) {
        return -1;
    }

    @NonNull
    @Override
    public Object getConvertedData(T data, Object type) {
        return data;
    }

    @Override
    public int getCurrentPosition() {
        return currentPos;
    }

    public String addPageIndex() {
        pageIndex += 1;
        return String.valueOf(pageIndex);
    }

    public String clearPageIndex() {
        pageIndex = 1;
        return String.valueOf(pageIndex);
    }

    public void addData(String jsonSrc, Type type) {
        mDataList.addAll((List<T>) gson.fromJson(jsonSrc, type));
    }

    public void addData(List<T> list){
        mDataList.addAll(list);
        notifyDataSetChanged();
    }

    /**
     * 清空list并设置list总量
     *
     * @param total
     */
    public void setTotal(int total) {
        mDataList.clear();
        this.total = total;
    }

    public boolean isLimit() {
        return loading || mDataList.size() >= total || (pageIndex - 1) * 10 > total;
    }

    public void finishLoading() {
        loading = false;
    }

    public void setupScroll(final RecyclerView rvBase, final CallBack callBack) {
        final BaseRvAdapter adapter = this;
        rvBase.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int lastVisibleItem;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && lastVisibleItem + 1 == rvBase.getAdapter().getItemCount()
                        && !adapter.isLimit()) {
                    //加载更多
                    loading = true;
                    callBack.loadData();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = callBack.findLastVisibleItemPosition();
            }
        });
    }

    /**
     * 动态型
     * @param rvBase
     * @param callBack
     */
    public void setupLastScroll(final RecyclerView rvBase, final CallBack callBack) {
        final BaseRvAdapter adapter = this;
        rvBase.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int lastVisibleItem;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && rvBase.getAdapter() != null
                        && lastVisibleItem + 1 == rvBase.getAdapter().getItemCount()
                        && !loading) {
                    //加载更多
                    loading = true;
                    callBack.loadData();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = callBack.findLastVisibleItemPosition();
            }
        });
    }

    public interface CallBack {
        int findLastVisibleItemPosition();

        void loadData();
    }
}

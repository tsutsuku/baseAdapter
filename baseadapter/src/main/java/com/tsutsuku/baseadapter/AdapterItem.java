package com.tsutsuku.baseadapter;

import android.support.annotation.LayoutRes;
import android.view.View;

import java.util.List;

/**
 * @Author tsutsuku
 * @Create 2017/1/11
 * @Description 基础AdapterItem
 */

public interface AdapterItem<T> {

    @LayoutRes
    int getLayoutResId();

    void bindViews(final View root);

    void handleData(T item, int position);

    void handleData(T item, int position, List<Object> payloads);

    T getCurItem();

    void setCurItem(T item);

    void setCurPosition(int position);
}

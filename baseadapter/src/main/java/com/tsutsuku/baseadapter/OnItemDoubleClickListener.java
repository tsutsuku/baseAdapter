package com.tsutsuku.baseadapter;

/**
 * @Author tsutsuku
 * @Create 2017/2/11
 * @Description
 */

public interface OnItemDoubleClickListener<T> {
    void onItemOneClick(T item);

    void onItemTwoClick(T item);
}

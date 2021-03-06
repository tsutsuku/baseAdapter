package com.tsutsuku.baseadapter;

public class DataBindingJudgement {

    public static final boolean SUPPORT_DATABINDING;

    static {
        boolean hasDependency;
        try {
            Class.forName("android.databinding.ObservableList");
            hasDependency = true;
        } catch (ClassNotFoundException e) {
            hasDependency = false;
        }

        SUPPORT_DATABINDING = hasDependency;
    }
}

package com.tsutsuku.baseadapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;

/**
 * @Author tsutsuku
 * @Create 2017/1/23
 * @Description RcvAdapterWrapper base on Jack Tony's RcvAdapterWrapper
 */

public class RcvAdapterWrapper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_HEADER = 99930;

    public static final int TYPE_FOOTER = 99940;

    public static final int TYPE_EMPTY = 99932;

    private RecyclerView.LayoutManager layoutManager;

    private RecyclerView.Adapter mWrapped;

    private boolean hasShownEmptyView = false;

    private RecyclerView emptyViewParent;

    private View headerView = null;

    private View footerView = null;

    private View emptyView = null;

    public RcvAdapterWrapper(@Nullable RecyclerView.Adapter adapter, @Nullable RecyclerView.LayoutManager layoutManager) {
        mWrapped = adapter;
        mWrapped.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                notifyItemRangeChanged(positionStart + getHeaderCount(), itemCount);
            }

            @Override
            public void onChanged() {
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                notifyItemRangeInserted(positionStart + getHeaderCount(), itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                notifyItemRangeRemoved(positionStart + getHeaderCount(), itemCount);
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount);
            }
        });

        this.layoutManager = layoutManager;

        if (this.layoutManager instanceof GridLayoutManager) {
            setSpanSizeLookup(this, (GridLayoutManager) this.layoutManager);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_HEADER:
                return new SimpleViewHolder(headerView);
            case TYPE_FOOTER:
                return new SimpleViewHolder(footerView);
            case TYPE_EMPTY:
                return new SimpleViewHolder(emptyView);
            default:
                return mWrapped.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        if (type != TYPE_HEADER && type != TYPE_FOOTER && type != TYPE_EMPTY) {
            mWrapped.onBindViewHolder(holder, position - getHeaderCount());
        }

        if (type == TYPE_EMPTY && emptyViewParent != null) {
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            int headHeight = headerView != null ? headerView.getHeight() : 0;
            params.height = emptyViewParent.getHeight() - headHeight;
        }
    }

    @Override
    public int getItemCount() {
        int count = mWrapped.getItemCount();

        int offset = 0;

        if (headerView != null) {
            offset++;
        }
        if (footerView != null) {
            offset++;
        }
        if (emptyView != null) {
            if (count == 0) {
                offset++;
                hasShownEmptyView = true;
            } else {
                hasShownEmptyView = false;
            }
        }

        return count + offset;
    }

    @Override
    public int getItemViewType(int position) {
        if (headerView != null && position == 0) {
            return TYPE_HEADER;
        } else if (footerView != null && position == getItemCount() - 1) {
            return TYPE_FOOTER;
        } else if (emptyView != null && mWrapped.getItemCount() == 0 && position == getHeaderCount()) {
            return TYPE_EMPTY;
        } else {
            return mWrapped.getItemViewType(position - getHeaderCount());
        }
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager){
        if (this.layoutManager == layoutManager){
            return;
        }

        this.layoutManager = layoutManager;
        if (this.layoutManager instanceof GridLayoutManager){
            setSpanSizeLookup(this, (GridLayoutManager)this.layoutManager);
        }
        setFullSpan(headerView, layoutManager);
        setFullSpan(footerView, layoutManager);
        setFullSpan(emptyView , layoutManager);

    }

    //////////////////////////////////////////////////
    // Set/Remove HeaderView, FooterView or EmptyView
    //////////////////////////////////////////////////
    public void setHeaderView(@Nullable View headerView) {
        if (this.headerView == headerView || headerView == null) {
            return;
        }

        this.headerView = headerView;
        setFullSpan(headerView, layoutManager);
    }

    public void setFooterView(@Nullable View footerView) {
        if (this.footerView == footerView || footerView == null) {
            return;
        }
        this.footerView = footerView;
        setFullSpan(footerView, layoutManager);
    }

    public void setEmptyView(@Nullable View emptyView, @Nullable RecyclerView emptyViewParent) {
        if (this.emptyView == emptyView || emptyView == null) {
            return;
        }
        this.emptyView = emptyView;
        this.emptyViewParent = emptyViewParent;
        setFullSpan(emptyView, layoutManager);
    }

    public RecyclerView.Adapter getWrappedAdapter() {
        return mWrapped;
    }

    public void removeHeaderView() {
        if (headerView == null) {
            return;
        }
        headerView = null;
        notifyDataSetChanged();
    }

    public void removeFooterView() {
        if (footerView == null) {
            return;
        }
        footerView = null;
        int footerPos = getItemCount() - 1;
        notifyItemRemoved(footerPos);
    }

    public int getHeaderCount() {
        return headerView != null ? 1 : 0;
    }

    public int getFooterCount() {
        return footerView != null ? 1 : 0;
    }

    private void setSpanSizeLookup(final RecyclerView.Adapter adapter, final GridLayoutManager layoutManager) {
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int type = adapter.getItemViewType(position);
                if (type == TYPE_HEADER || type == TYPE_FOOTER || type == TYPE_EMPTY) {
                    return layoutManager.getSpanCount();
                } else {
                    return 1;
                }

            }
        });
    }

    private void setFullSpan(@Nullable View view, RecyclerView.LayoutManager layoutManager) {
        if (view != null) {
            final int itemHeight = view.getLayoutParams() != null ?
                    view.getLayoutParams().height : RecyclerView.LayoutParams.WRAP_CONTENT;
            if (layoutManager instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager.LayoutParams layoutParams =
                        new StaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight);
                layoutParams.setFullSpan(true);
                view.setLayoutParams(layoutParams);
            } else if (layoutManager instanceof LinearLayoutManager
                    || layoutManager instanceof GridLayoutManager) {
                view.setLayoutParams(new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, itemHeight));
            }
            notifyDataSetChanged();
        }
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (!(holder instanceof SimpleViewHolder)) {
            mWrapped.onViewAttachedToWindow(holder);
        }
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (!(holder instanceof SimpleViewHolder)) {
            mWrapped.onViewDetachedFromWindow(holder);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mWrapped.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mWrapped.onDetachedFromRecyclerView(recyclerView);
    }

    private static class SimpleViewHolder extends RecyclerView.ViewHolder {
        SimpleViewHolder(View itemView) {
            super(itemView);
        }
    }
}

package com.apanda.base.Widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ListAdapter
import android.widget.ListView

/**
 * Created by kai.xiong on 2016-03-08-0008.
 */
class NestedListView(context: Context, attrs: AttributeSet) : ListView(context, attrs), View.OnTouchListener, AbsListView.OnScrollListener {

    private val listViewTouchAction: Int

    init {
        listViewTouchAction = -1
        setOnScrollListener(this)
        setOnTouchListener(this)
    }

    override fun onScroll(view: AbsListView, firstVisibleItem: Int,
                          visibleItemCount: Int, totalItemCount: Int) {
        if (adapter != null && adapter.count > MAXIMUM_LIST_ITEMS_VIEWABLE) {
            if (listViewTouchAction == MotionEvent.ACTION_MOVE) {
                scrollBy(0, -1)
            }
        }
    }

    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var newHeight = 0
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
        if (heightMode != View.MeasureSpec.EXACTLY) {
            val listAdapter = adapter
            if (listAdapter != null && !listAdapter.isEmpty) {
                var listPosition = 0
                listPosition = 0
                while (listPosition < listAdapter.count && listPosition < MAXIMUM_LIST_ITEMS_VIEWABLE) {
                    val listItem = listAdapter.getView(listPosition, null, this)
                    //now it will not throw a NPE if listItem is a ViewGroup instance
                    if (listItem is ViewGroup) {
                        listItem.setLayoutParams(AbsListView.LayoutParams(
                                AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT))
                    }
                    listItem.measure(widthMeasureSpec, heightMeasureSpec)
                    newHeight += listItem.measuredHeight
                    listPosition++
                }
                newHeight += dividerHeight * listPosition
            }
            if (heightMode == View.MeasureSpec.AT_MOST && newHeight > heightSize) {
                if (newHeight > heightSize) {
                    newHeight = heightSize
                }
            }
        } else {
            newHeight = measuredHeight
        }
        setMeasuredDimension(measuredWidth, newHeight)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (adapter != null && adapter.count > MAXIMUM_LIST_ITEMS_VIEWABLE) {
            if (listViewTouchAction == MotionEvent.ACTION_MOVE) {
                scrollBy(0, 1)
            }
        }
        return false
    }

    companion object {
        private val MAXIMUM_LIST_ITEMS_VIEWABLE = 99
    }
}
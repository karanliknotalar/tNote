package com.aturan.tnote;

import android.os.Parcelable;

import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewState {

    private final RecyclerView recyclerView;
    private Parcelable parcelable;

    public RecyclerViewState(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public void save() {
        parcelable = recyclerView.getLayoutManager().onSaveInstanceState();
    }

    public void restore() {
        recyclerView.getLayoutManager().onRestoreInstanceState(parcelable);
    }
}

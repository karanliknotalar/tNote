package com.aturan.tnote;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class adapterNotlar extends RecyclerView.Adapter<adapterNotlar.holder> {

    private final Context context;
    private ArrayList<dataNotlar> dataNotlars;
    private final interfaceAdapter interfaceAdapter;
    private Boolean selectMode;


    public adapterNotlar(Context context, ArrayList<dataNotlar> dataNotlars, interfaceAdapter interfaceAdapter) {
        this.context = context;
        this.dataNotlars = dataNotlars;
        this.interfaceAdapter = interfaceAdapter;

    }

    public Boolean getSelectMode() {
        return selectMode;
    }

    public void setSelectMode(Boolean selectMode) {
        this.selectMode = selectMode;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDataNotlars(ArrayList<dataNotlar> dataNotlars) {
        this.dataNotlars = dataNotlars;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new holder(LayoutInflater.from(context).inflate(R.layout.item_note, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull holder holder, int position) {

        holder.bind(dataNotlars.get(position));

    }

    @Override
    public int getItemCount() {
        return dataNotlars.size();
    }

    public class holder extends RecyclerView.ViewHolder {

        private final TextView txtBaslik, txtNot, txtTarih;
        private final CardView cardView;
        private final ImageView checkImg, imgLock;
        private int clickCount = 0;

        public holder(@NonNull View v) {
            super(v);
            txtBaslik = v.findViewById(R.id.item_note_txtBaslik);
            txtNot = v.findViewById(R.id.item_note_txtNot);
            cardView = v.findViewById(R.id.item_note_cardView);
            checkImg = v.findViewById(R.id.item_note_checkBox);
            txtTarih = v.findViewById(R.id.item_note_txtTarih);
            imgLock = v.findViewById(R.id.item_note_imgLock);

            checkImg.setVisibility(getSelectMode() ? View.VISIBLE : View.GONE);

        }

        @SuppressLint({"ResourceAsColor", "ClickableViewAccessibility", "SetTextI18n"})
        void bind(final dataNotlar dataNotlar) {

            gorunumAyarla(dataNotlar.getChecked());

            CryptUtil crypt = new CryptUtil(context).initKey();

            if (!crypt.decrypt(dataNotlar.getNot()).equals(crypt.ERROR)) {

                String baslik = crypt.decrypt(dataNotlar.getBaslik());
                if (baslik.length() > 17) txtBaslik.setText(baslik.substring(0, 17) + "...");
                else txtBaslik.setText(baslik);

                String not = crypt.decrypt(dataNotlar.getNot());
                if (not.length() > 30) txtNot.setText(not.substring(0, 60) + "...");
                else txtNot.setText(not);

                imgLock.setVisibility(View.GONE);

            } else {
                imgLock.setVisibility(View.VISIBLE);
                txtBaslik.setText("Şifreli");
                txtNot.setText("");
                txtBaslik.setTextColor(context.getColor(R.color.sifreli_veri_item_color));
            }

            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat formatter = new SimpleDateFormat("d MMMM yyyy");
            if (dataNotlar.getZaman() != null)
                txtTarih.setText(formatter.format(new Date(dataNotlar.getZaman().getSeconds() * 1000L)));

            if (!crypt.decrypt(dataNotlar.getBaslik()).equals(crypt.ERROR))
                if (Objects.equals(crypt.decrypt(dataNotlar.getBaslik()), ""))
                    txtBaslik.setVisibility(View.GONE);
                else
                    txtBaslik.setVisibility(View.VISIBLE);

            cardView.setOnClickListener(view -> {

                dataNotlar.setChecked(!dataNotlar.getChecked());
                gorunumAyarla(dataNotlar.getChecked());
                interfaceAdapter.click(dataNotlars.get(getAdapterPosition()));

                cardView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.item_action_down));
                cardView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.item_action_up));

                //animasyon denemeleri
                if (!getSelectMode()) {


                }

            });


            cardView.setOnTouchListener((view, motionEvent) -> {

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    clickCount++;
                    if (clickCount == 1) {
                        cardView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.item_action_down));

                    }


                } else if (clickCount > 0) {
                    cardView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.item_action_up));
                    clickCount = 0;
                }

                return false;
            });

            cardView.setOnLongClickListener(view -> {

                dataNotlar.setChecked(!dataNotlar.getChecked());
                gorunumAyarla(dataNotlar.getChecked());
                setSelectMode(true);
                interfaceAdapter.longClick(dataNotlars.get(getAdapterPosition()));
                return true;

            });

        }

        @SuppressLint("UseCompatLoadingForDrawables")
        private void gorunumAyarla(Boolean b) {


            int pad = (int) context.getResources().getDimension(R.dimen.checkpad);
            if (b) {
                checkImg.setPadding(pad, pad, pad, pad);
                checkImg.setImageDrawable(context.getDrawable(R.drawable.custom_check_true));
                checkImg.setImageResource(R.drawable.ic_baseline_check_24);

            } else {
                checkImg.setPadding(0, 0, 0, 0);
                checkImg.setImageDrawable(context.getDrawable(R.drawable.custom_check_false));
            }

            if (getSelectMode())
                cardView.setCardBackgroundColor(b ? context.getResources().getColor(R.color.item_bg_color_selected) :
                        context.getResources().getColor(R.color.item_bg_color));

        }


    }

    public ArrayList<dataNotlar> getSelected() {
        ArrayList<dataNotlar> selected = new ArrayList<>();
        for (int i = 0; i < dataNotlars.size(); i++) {
            if (dataNotlars.get(i).getChecked()) {
                selected.add(dataNotlars.get(i));
            }

        }
        return selected;
    }

}

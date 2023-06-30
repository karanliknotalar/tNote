package com.aturan.tnote;

import com.google.firebase.Timestamp;

public class dataNotlar {

    private String baslik;
    private String not;
    private String not_id;
    private Timestamp zaman;
    private Boolean isChecked = false;

    public Boolean getChecked() {
        return isChecked;
    }

    public void setChecked(Boolean checked) {
        isChecked = checked;
    }

    public dataNotlar() {

    }

    public dataNotlar(String baslik, String not, String not_id) {
        this.baslik = baslik;
        this.not = not;
        this.not_id = not_id;
    }

    public String getBaslik() {
        return baslik;
    }

    public String getNot() {
        return not;
    }

    public String getNot_id() {
        return not_id;
    }

    public Timestamp getZaman() {
        return zaman;
    }
}

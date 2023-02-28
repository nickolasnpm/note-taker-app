package com.example.keepmynote;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;

public class Note {

    private String titled;
    private String texted;
    private boolean completed;
    private Timestamp created;
    private String userId;

    public Note() {
    }

    public Note(String titled, String texted, boolean completed, Timestamp created, String userId) {
        this.titled = titled;
        this.texted = texted;
        this.completed = completed;
        this.created = created;
        this.userId = userId;
    }

    public String getTitled() {
        return titled;
    }

    public void setTitled(String titled) {
        this.titled = titled;
    }

    public String getTexted() {
        return texted;
    }

    public void setTexted(String texted) {
        this.texted = texted;
    }

    public boolean getCompleted() {
        return completed;
    }

    public Timestamp getCreated() {
        return created;
    }

    @NonNull
    @Override
    public String toString() {
        return "Note{" +
                "titled='" + titled + '\'' +
                ", texted='" + texted + '\'' +
                ", completed=" + completed +
                ", created=" + created +
                ", userId='" + userId + '\'' +
                '}';
    }
}

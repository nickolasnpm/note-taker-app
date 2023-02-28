package com.example.keepmynote;

import static android.content.ContentValues.TAG;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class NoteRecyclerAdapter extends FirestoreRecyclerAdapter <Note, NoteRecyclerAdapter.NoteViewHolder> {

    NoteListener noteListener;

    public NoteRecyclerAdapter(@NonNull FirestoreRecyclerOptions<Note> options, NoteListener noteListener) {
        super(options);
        this.noteListener = noteListener;
    }

    @Override
    protected void onBindViewHolder(@NonNull NoteViewHolder holder, int position, @NonNull Note note) {

        holder.titleTextView.setText(note.getTitled());
        holder.noteTextView.setText(note.getTexted());
        holder.checkBox.setChecked(note.getCompleted());
        CharSequence dateCharSequence = DateFormat.format("EEEE, MMM, d, yyyy, h:mm", note.getCreated().toDate());
        holder.dateTextView.setText(dateCharSequence);
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.note_row, parent, false);
        return new NoteViewHolder(view);
    }
    
    class NoteViewHolder extends RecyclerView.ViewHolder{

        TextView titleTextView, noteTextView, dateTextView;
        CheckBox checkBox;

        @SuppressWarnings("deprecation")
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            titleTextView = itemView.findViewById(R.id.titleTextView);
            noteTextView = itemView.findViewById(R.id.noteTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            checkBox = itemView.findViewById(R.id.checkBox);

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                DocumentSnapshot snapshot = getSnapshots().getSnapshot(getAdapterPosition());
                Note note = getItem(getAdapterPosition());
                if(note.getCompleted() != isChecked){
                    noteListener.handleCheckChanged(isChecked, snapshot);
                }
            });

            itemView.setOnClickListener(v -> {

                DocumentSnapshot snapshot = getSnapshots().getSnapshot(getAdapterPosition());
                noteListener.handleEditNote(snapshot);
            });
        }

        @SuppressWarnings("deprecation")
        public void deleteItem() {

            Log.d(TAG, "deleteItem: "+getAdapterPosition());
            Log.d(TAG, "deleteItem: "+getSnapshots().getSnapshot(getAdapterPosition()));
            noteListener.handleDeleteItem(getSnapshots().getSnapshot(getAdapterPosition()));
        }
    }

    interface NoteListener {
        void handleCheckChanged(boolean isChecked, DocumentSnapshot snapshot);
        void handleEditNote(DocumentSnapshot snapshot);
        void handleDeleteItem(DocumentSnapshot snapshot);
    }
}




package com.example.keepmynote;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;
import java.util.Objects;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener, NoteRecyclerAdapter.NoteListener {

    private static final String TAG = "MainActivity";
    RecyclerView recyclerView;
    FloatingActionButton fab;
    NoteRecyclerAdapter noteRecyclerAdapter;
    View alertDialogLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        recyclerView=findViewById(R.id.recyclerView);
        //recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        fab=findViewById(R.id.floatingbutton);
        fab.setOnClickListener(v -> showAlertDialog());

    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private void showAlertDialog(){

        alertDialogLayout = getLayoutInflater().inflate(R.layout.alert_dialog_layout, null);
        EditText editTextTitle = alertDialogLayout.findViewById(R.id.editTextTitle);
        EditText editTextContent = alertDialogLayout.findViewById(R.id.editTextContent);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder
                .setIcon(R.drawable.ic_baseline_assignment_24)
                .setTitle("Add Note")
                .setView(alertDialogLayout)
                .setPositiveButton("Add", (dialog, i) -> addNote(editTextTitle.getText().toString(), editTextContent.getText().toString()))
                .setNegativeButton("Cancel", null)
                .show();

    }

    @SuppressWarnings("ConstantConditions")
    private void addNote (String title, String text){

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Note note = new Note(title, text, false, new Timestamp(new Date()), userId);

        if(!(title.isEmpty() && text.isEmpty() )){

            FirebaseFirestore.getInstance()
                    .collection("notes")
                    .add(note)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "onSuccess: Successfully added the note");
                        Toast.makeText(this, "Successfully added the note", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());

        }

    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected (MenuItem menuItem){
        int id = menuItem.getItemId();

        switch (id){

            case R.id.action_profile:
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                return true;

            case R.id.action_logout:
                Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
                AuthUI.getInstance().signOut(this);
                finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(this);

    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(this);

        if (noteRecyclerAdapter !=null){
            noteRecyclerAdapter.stopListening();
        }

    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

        if(firebaseAuth.getCurrentUser() ==null){
            Intent intent=new Intent(getApplicationContext(), LoginRegisterActivity.class);
            startActivity(intent);
            finish();
        }
        initRecyclerView(firebaseAuth.getCurrentUser());
    }

    private void initRecyclerView (FirebaseUser user){

        Query query = FirebaseFirestore.getInstance()
                .collection("notes")
                .whereEqualTo("userId", user.getUid())
                .orderBy("completed", Query.Direction.ASCENDING)
                .orderBy("created", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class)
                .build();

        noteRecyclerAdapter = new NoteRecyclerAdapter(options, this);
        recyclerView.setAdapter(noteRecyclerAdapter);

        noteRecyclerAdapter.startListening();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Toast.makeText(MainActivity.this, "Item Deleted", Toast.LENGTH_SHORT).show();

                NoteRecyclerAdapter.NoteViewHolder noteViewHolder = (NoteRecyclerAdapter.NoteViewHolder) viewHolder;
                noteViewHolder.deleteItem();
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.red))
                    .addActionIcon(R.drawable.ic_baseline_delete_24)
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        }
    };

    @Override
    public void handleCheckChanged(boolean isChecked, DocumentSnapshot snapshot) {
        Log.d(TAG, "handleCheckChanged: "+isChecked);

        snapshot.getReference()
                .update("completed", isChecked)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess: "))
                .addOnFailureListener(e -> Log.d(TAG, "onFailure: " +e.getLocalizedMessage()));

    }

    @SuppressLint("InflateParams")
    @SuppressWarnings("StringOperationCanBeSimplified")
    @Override
    public void handleEditNote(DocumentSnapshot snapshot) {

        Note note = snapshot.toObject(Note.class);
        alertDialogLayout = getLayoutInflater().inflate(R.layout.alert_dialog_layout, null);

        EditText editTextTitle = alertDialogLayout.findViewById(R.id.editTextTitle);
        EditText editTextContent = alertDialogLayout.findViewById(R.id.editTextContent);

        assert note !=null;
        editTextTitle.setText(note.getTitled().toString());
        editTextTitle.setSelection(note.getTitled().length());

        editTextContent.setText(note.getTexted().toString());
        editTextContent.setSelection(note.getTexted().length());

        new AlertDialog.Builder(this)
                .setTitle("Edit Note")
                .setIcon(R.drawable.ic_baseline_assignment_24)
                .setView(alertDialogLayout)
                .setPositiveButton("Done", (dialog, i) -> {

                    String newTitle = editTextTitle.getText().toString();
                    note.setTitled(newTitle);

                    String newText = editTextContent.getText().toString();
                    note.setTexted(newText);

                    if ((!(newText.isEmpty())) && (newTitle.isEmpty())) {

                        snapshot.getReference()
                                .set(note)
                                .addOnSuccessListener(unused -> {
                                        Log.d(TAG, "onSuccess: update note success");
                                    Toast.makeText(this, "Successfully edited the note", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Log.e(TAG, "onFailure: ", e.getCause()));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void handleDeleteItem(DocumentSnapshot snapshot) {

        DocumentReference documentReference = snapshot.getReference();
        Note note = snapshot.toObject(Note.class);

        documentReference
                .delete()
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "onSuccess: Item Deleted");

                    Snackbar.make(recyclerView, "Item Deleted", Snackbar.LENGTH_LONG)
                            .setAction("Undo", y ->
                                    documentReference.set(Objects.requireNonNull(note)))
                            .show();

                });
    }

}

// Tutorial Resources = https://www.youtube.com/watch?v=hVJe51Z67Bo&list=PLdHg5T0SNpN2NimxW3piNqEVBWtXcraz-&index=1&ab_channel=yoursTRULY
package com.example.configapp; // <-- change to your own package name

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ContactActivity extends AppCompatActivity {

    private static final String PREFS = "contacts_prefs";
    private static final String KEY = "contacts";

    private EditText editName, editPhone;
    private ListView listContacts;
    private TextView textEmpty;

    private final List<String[]> contacts = new ArrayList<>(); // each item: {name, phone}
    private ArrayAdapter<String> adapter;
    private final List<String> displayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        editName = findViewById(R.id.editName);
        editPhone = findViewById(R.id.editPhone);
        Button buttonAdd = findViewById(R.id.buttonAddContact);
        listContacts = findViewById(R.id.listContacts);
        textEmpty = findViewById(R.id.textEmpty);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        listContacts.setAdapter(adapter);

        loadContacts();
        refreshList();

        buttonAdd.setOnClickListener(v -> addContact());

        // Tap a contact -> open the dialer with the number ready to call
        listContacts.setOnItemClickListener((parent, view, position, id) -> {
            Intent dial = new Intent(Intent.ACTION_DIAL);
            dial.setData(Uri.parse("tel:" + contacts.get(position)[1]));
            startActivity(dial);
        });

        // Long-press a contact -> delete it
        listContacts.setOnItemLongClickListener((parent, view, position, id) -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete contact")
                    .setMessage("Delete " + contacts.get(position)[0] + "?")
                    .setPositiveButton("Delete", (d, w) -> {
                        contacts.remove(position);
                        saveContacts();
                        refreshList();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });
    }

    private void addContact() {
        String name = editName.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Enter both name and phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        contacts.add(new String[]{name, phone});
        saveContacts();
        refreshList();

        editName.setText("");
        editPhone.setText("");
        Toast.makeText(this, "Contact saved", Toast.LENGTH_SHORT).show();
    }

    private void refreshList() {
        displayList.clear();
        for (String[] c : contacts) {
            displayList.add(c[0] + "\n" + c[1]);
        }
        adapter.notifyDataSetChanged();
        textEmpty.setVisibility(contacts.isEmpty() ? TextView.VISIBLE : TextView.GONE);
    }

    // ---------- Storage (SharedPreferences as JSON) ----------
    private void saveContacts() {
        JSONArray array = new JSONArray();
        try {
            for (String[] c : contacts) {
                JSONObject obj = new JSONObject();
                obj.put("name", c[0]);
                obj.put("phone", c[1]);
                array.put(obj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY, array.toString()).apply();
    }

    private void loadContacts() {
        contacts.clear();
        String json = getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "[]");
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                contacts.add(new String[]{obj.getString("name"), obj.getString("phone")});
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
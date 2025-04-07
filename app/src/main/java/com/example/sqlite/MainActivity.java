package com.example.sqlite;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sqlite.classes.Etudiant;
import com.example.sqlite.service.EtudiantService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private EditText nom;
    private EditText prenom;
    private EditText dateNaissance;
    private Button add;
    private Button datePickerButton;
    private Button selectPhotoButton;
    private ImageView imageViewPhoto;

    private Uri selectedImageUri = null;

    private EditText id;
    private Button rechercher, delete, list;
    private TextView res;

    private Calendar calendar;

    // Register image picker result
    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imageViewPhoto.setImageURI(uri);
                }
            });

    void clear() {
        nom.setText("");
        prenom.setText("");
        dateNaissance.setText("");
        selectedImageUri = null;
        imageViewPhoto.setImageResource(android.R.drawable.ic_menu_gallery);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendar = Calendar.getInstance();
        final EtudiantService es = new EtudiantService(this);

        nom = findViewById(R.id.nom);
        prenom = findViewById(R.id.prenom);
        dateNaissance = findViewById(R.id.dateNaissance);
        imageViewPhoto = findViewById(R.id.imageViewPhoto);

        // Setup photo picker button
        selectPhotoButton = findViewById(R.id.selectPhotoButton);
        selectPhotoButton.setOnClickListener(v -> mGetContent.launch("image/*"));

        datePickerButton = findViewById(R.id.datePickerButton);
        datePickerButton.setOnClickListener(v -> showDatePickerDialog());

        add = findViewById(R.id.bn);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nomText = nom.getText().toString().trim();
                String prenomText = prenom.getText().toString().trim();
                String dateNaissanceText = dateNaissance.getText().toString().trim();
                String photoUriText = selectedImageUri != null ? selectedImageUri.toString() : "";

                if (!nomText.isEmpty() && !prenomText.isEmpty() && !dateNaissanceText.isEmpty()) {
                    Etudiant etudiant = new Etudiant(nomText, prenomText, dateNaissanceText, photoUriText);
                    es.create(etudiant);
                    clear();
                    Toast.makeText(MainActivity.this, "Étudiant ajouté", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Veuillez remplir tous les champs obligatoires", Toast.LENGTH_SHORT).show();
                }
            }
        });

        id = findViewById(R.id.id);
        rechercher = findViewById(R.id.load);
        delete = findViewById(R.id.delete);
        list = findViewById(R.id.list);
        res = findViewById(R.id.res);

        rechercher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!id.getText().toString().isEmpty()) {
                    Etudiant e = es.findById(Integer.parseInt(id.getText().toString()));

                    if (e != null) {
                        res.setText(e.getNom()+" "+e.getPrenom()+" - "+e.getDateNaissance());

                        // Show image if available
                        if (e.getPhotoUri() != null && !e.getPhotoUri().isEmpty()) {
                            try {
                                Uri photoUri = Uri.parse(e.getPhotoUri());
                                imageViewPhoto.setImageURI(photoUri);
                                selectedImageUri = photoUri;
                            } catch (Exception ex) {
                                // Handle parsing error
                                imageViewPhoto.setImageResource(android.R.drawable.ic_menu_gallery);
                            }
                        } else {
                            imageViewPhoto.setImageResource(android.R.drawable.ic_menu_gallery);
                        }
                    } else {
                        res.setText("Étudiant n'existe pas");
                        imageViewPhoto.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Veuillez entrer un ID", Toast.LENGTH_SHORT).show();
                }
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!id.getText().toString().isEmpty()) {
                    Etudiant e = es.findById(Integer.parseInt(id.getText().toString()));
                    //es.delete(e);
                    res.setText("L'étudiant est supprimé");
                    imageViewPhoto.setImageResource(android.R.drawable.ic_menu_gallery);
                } else {
                    Toast.makeText(MainActivity.this, "Veuillez entrer un ID", Toast.LENGTH_SHORT).show();
                }
            }
        });

        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), etudiantList.class);
                startActivity(intent);
            }
        });
    }

    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
                dateNaissance.setText(sdf.format(calendar.getTime()));
            }
        };

        new DatePickerDialog(this, dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }
}
package com.example.sqlite;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sqlite.adapter.EtudiantAdapter;
import com.example.sqlite.classes.Etudiant;
import com.example.sqlite.service.EtudiantService;

import java.util.List;

public class etudiantList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EtudiantAdapter adapter;
    private List<Etudiant> etudiants;

    private Button btnRouter, btnSearchById;
    private EditText id;
    private TextView res;
    private ImageView imageViewPhoto;

    private Uri selectedImageUri = null;
    private EtudiantService es;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_etudiant_list);

        // Initialiser
        recyclerView = findViewById(R.id.recyclerView);
        btnRouter = findViewById(R.id.btnRouter);
        btnSearchById = findViewById(R.id.btnSearchById);
        id = findViewById(R.id.id); // Champ texte pour l'ID
        res = findViewById(R.id.res); // TextView résultat
        imageViewPhoto = findViewById(R.id.imageViewPhoto); // ImageView pour la photo

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        // Service
        es = new EtudiantService(this);
        etudiants = es.findAll();

        adapter = new EtudiantAdapter(this, etudiants);
        recyclerView.setAdapter(adapter);

        // Bouton retour vers MainActivity
        btnRouter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(etudiantList.this, MainActivity.class);
                startActivity(intent);
                finish(); // optionnel
            }
        });

        // Recherche par ID
        btnSearchById.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!id.getText().toString().isEmpty()) {
                    Etudiant e = es.findById(Integer.parseInt(id.getText().toString()));

                    if (e != null) {
                        res.setText(e.getNom() + " " + e.getPrenom() + " - " + e.getDateNaissance());

                        if (e.getPhotoUri() != null && !e.getPhotoUri().isEmpty()) {
                            try {
                                Uri photoUri = Uri.parse(e.getPhotoUri());
                                imageViewPhoto.setImageURI(photoUri);
                                selectedImageUri = photoUri;
                            } catch (Exception ex) {
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
                    Toast.makeText(etudiantList.this, "Veuillez entrer un ID", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

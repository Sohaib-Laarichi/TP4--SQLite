package com.example.sqlite.classes;

public class Etudiant {
    private int id;
    private String nom;
    private String prenom;
    private String dateNaissance;
    private String photoUri; // Nouveau champ pour l'URI de la photo

    // Default constructor
    public Etudiant() {}

    // Constructor with all fields
    public Etudiant(String nom, String prenom, String dateNaissance, String photoUri) {
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.photoUri = photoUri;
    }

    // Getter and setter for photoUri
    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    // Existing getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(String dateNaissance) {
        this.dateNaissance = dateNaissance;
    }
}
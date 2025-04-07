package com.example.sqlite.adapter;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sqlite.R;
import com.example.sqlite.classes.Etudiant;
import com.example.sqlite.service.EtudiantService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EtudiantAdapter extends RecyclerView.Adapter<EtudiantAdapter.EtudiantViewHolder> {
    private Context context;
    private List<Etudiant> etudiants;
    private EtudiantService etudiantService;
    private Calendar calendar;
    private Uri tempSelectedImageUri = null;
    private ImageView tempImageView = null;

    // Declare the ActivityResultLauncher for image picking
    private ActivityResultLauncher<String> imagePickerLauncher;

    public EtudiantAdapter(Context context, List<Etudiant> etudiants) {
        this.context = context;
        this.etudiants = etudiants;
        this.etudiantService = new EtudiantService(context);
        this.calendar = Calendar.getInstance();

        // Register the image picker launcher if context is an AppCompatActivity
        if (context instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) context;
            imagePickerLauncher = activity.registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null && tempImageView != null) {
                            tempSelectedImageUri = uri;
                            tempImageView.setImageURI(uri);
                        }
                    }
            );
        }
    }

    @NonNull
    @Override
    public EtudiantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.etudiant, parent, false);
        return new EtudiantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EtudiantViewHolder holder, int position) {
        Etudiant e = etudiants.get(position);
        holder.etudiantId.setText(String.valueOf(e.getId()));
        holder.etudiantNom.setText(e.getNom());
        holder.etudiantPrenom.setText(e.getPrenom());
        holder.etudiantDateNaissance.setText(e.getDateNaissance());

        // Set image if available
        if (e.getPhotoUri() != null && !e.getPhotoUri().isEmpty()) {
            try {
                Uri photoUri = Uri.parse(e.getPhotoUri());
                holder.imagePhoto.setImageURI(photoUri);
            } catch (Exception ex) {
                // If there's an error loading the image, show a placeholder
                holder.imagePhoto.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            holder.imagePhoto.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Set click listener for the entire item view
        holder.itemView.setOnClickListener(v -> showOptionsDialog(e, position));
    }

    private void showOptionsDialog(Etudiant etudiant, int position) {
        String[] options = {"Modifier", "Supprimer"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Options");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Modifier
                    showModifyDialog(etudiant, position);
                    break;
                case 1: // Supprimer
                    showDeleteConfirmationDialog(etudiant, position);
                    break;
            }
        });
        builder.show();
    }

    private void showModifyDialog(Etudiant etudiant, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Modifier Étudiant");

        // Inflate custom layout for dialog
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_modify_etudiant, null);

        // Find views in dialog layout
        final EditText nomInput = dialogView.findViewById(R.id.editNom);
        final EditText prenomInput = dialogView.findViewById(R.id.editPrenom);
        final EditText dateNaissanceInput = dialogView.findViewById(R.id.editDateNaissance);
        final ImageView photoImageView = dialogView.findViewById(R.id.dialogImageView);
        final Button selectPhotoButton = dialogView.findViewById(R.id.dialogSelectPhotoButton);

        // Set current values
        nomInput.setText(etudiant.getNom());
        prenomInput.setText(etudiant.getPrenom());
        dateNaissanceInput.setText(etudiant.getDateNaissance());
        dateNaissanceInput.setFocusable(false);

        // Set the existing photo if available
        tempSelectedImageUri = null;
        if (etudiant.getPhotoUri() != null && !etudiant.getPhotoUri().isEmpty()) {
            try {
                Uri photoUri = Uri.parse(etudiant.getPhotoUri());
                photoImageView.setImageURI(photoUri);
                tempSelectedImageUri = photoUri;
            } catch (Exception ex) {
                photoImageView.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            photoImageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Set up date picker
        dateNaissanceInput.setOnClickListener(v -> showDatePickerDialog(dateNaissanceInput));

        // Set up photo picker
        tempImageView = photoImageView;
        selectPhotoButton.setOnClickListener(v -> {
            if (imagePickerLauncher != null) {
                imagePickerLauncher.launch("image/*");
            } else {
                Toast.makeText(context, "Sélection d'image non disponible dans cette vue", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setView(dialogView);

        builder.setPositiveButton("Enregistrer", (dialog, which) -> {
            String newNom = nomInput.getText().toString().trim();
            String newPrenom = prenomInput.getText().toString().trim();
            String newDateNaissance = dateNaissanceInput.getText().toString().trim();
            String newPhotoUri = tempSelectedImageUri != null ? tempSelectedImageUri.toString() : etudiant.getPhotoUri();

            if (!newNom.isEmpty() && !newPrenom.isEmpty() && !newDateNaissance.isEmpty()) {
                // Update student in database
                etudiant.setNom(newNom);
                etudiant.setPrenom(newPrenom);
                etudiant.setDateNaissance(newDateNaissance);
                etudiant.setPhotoUri(newPhotoUri);
                etudiantService.update(etudiant);

                // Update in the list
                etudiants.set(position, etudiant);
                notifyItemChanged(position);

                Toast.makeText(context, "Étudiant modifié", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Veuillez remplir tous les champs obligatoires", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    private void showDatePickerDialog(EditText dateInput) {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
                dateInput.setText(sdf.format(calendar.getTime()));
            }
        };

        // If there's an existing date, parse it
        String existingDate = dateInput.getText().toString();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
            calendar.setTime(sdf.parse(existingDate));
        } catch (Exception e) {
            // If parsing fails, use current date
            calendar = Calendar.getInstance();
        }

        new DatePickerDialog(context, dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showDeleteConfirmationDialog(Etudiant etudiant, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Confirmer la suppression")
                .setMessage("Voulez-vous vraiment supprimer cet étudiant ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    // Delete from database
                    // etudiantService.delete(etudiant);

                    // Remove from list
                    etudiants.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, etudiants.size());

                    Toast.makeText(context, "Étudiant supprimé", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Non", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return etudiants.size();
    }

    public static class EtudiantViewHolder extends RecyclerView.ViewHolder {
        TextView etudiantId, etudiantNom, etudiantPrenom, etudiantDateNaissance;
        ImageView imagePhoto;

        public EtudiantViewHolder(@NonNull View itemView) {
            super(itemView);
            etudiantId = itemView.findViewById(R.id.textId);
            etudiantNom = itemView.findViewById(R.id.textNom);
            etudiantPrenom = itemView.findViewById(R.id.textprenom);
            etudiantDateNaissance = itemView.findViewById(R.id.textDateNaissance);
            imagePhoto = itemView.findViewById(R.id.imagePhoto);
        }
    }
}
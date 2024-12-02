package cs477.gmu.jahmed9_project2;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivityAddItem extends AppCompatActivity {

    private DatabaseOpenHelper dbHelper;
    private EditText itemName, itemDescription, itemCost, itemStock;
    private Button addItemButton, cancelButton, deleteButton;
    private boolean updateItem = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_add_item);

        dbHelper = new DatabaseOpenHelper(this);

        itemName = findViewById(R.id.item_name);
        TextView itemNameDisplay = findViewById(R.id.item_name_display);
        itemDescription = findViewById(R.id.item_description);
        itemCost = findViewById(R.id.item_cost);
        itemStock = findViewById(R.id.item_stock);
        addItemButton = findViewById(R.id.add_item_button);
        cancelButton = findViewById(R.id.cancel_button);
        deleteButton = findViewById(R.id.delete_button);

        // Check if in edit mode, user wants to update item
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            updateItem = true;
            String name = extras.getString("itemName");
            String description = extras.getString("itemDescription");
            double price = extras.getDouble("itemPrice");
            int stock = extras.getInt("itemStock");

            //when user wants to update, keep item name (unchanged)
            itemName.setVisibility(View.GONE);
            itemNameDisplay.setVisibility(View.VISIBLE);
            itemNameDisplay.setText(name);

            // set hints to previous set data, clear editText
            itemDescription.setText("");
            itemDescription.setHint(description);
            itemCost.setText(String.valueOf(""));
            itemCost.setHint(String.valueOf(price));
            itemStock.setText(String.valueOf(""));
            itemStock.setHint(String.valueOf(stock));

            addItemButton.setText("Update"); // Change button text to "Update"
            deleteButton.setVisibility(View.VISIBLE); // Show delete button in edit mode
        } else {
            deleteButton.setVisibility(View.GONE); // Hide delete button, delete button not to be shown when adding a new item.
        }

        // Add Item
        addItemButton.setOnClickListener(v -> {
            // Check if the user wanted to update the item
            if (updateItem) {
                // Get current values in case the user does not want to change all item fields
                String currentDescription = itemDescription.getHint() != null ? itemDescription.getHint().toString().trim() : "";
                String currentPriceStr = itemCost.getHint() != null ? itemCost.getHint().toString().trim() : "0";
                String currentStockStr = itemStock.getHint() != null ? itemStock.getHint().toString().trim() : "0";

                // Get updated values from EditText
                String updatedDescription = itemDescription.getText().toString().trim();
                String updatedPriceStr = itemCost.getText().toString().trim();
                String updatedStockStr = itemStock.getText().toString().trim();

                // If the input is empty, retain previous values
                if (updatedDescription.isEmpty()) {
                    updatedDescription = currentDescription;
                }
                if (updatedPriceStr.isEmpty()) {
                    updatedPriceStr = currentPriceStr;
                }
                if (updatedStockStr.isEmpty()) {
                    updatedStockStr = currentStockStr;
                }

                // Validate and parse updated values
                double updatedPrice;
                int updatedStock;
                try {
                    updatedPrice = Double.parseDouble(updatedPriceStr);
                    updatedStock = Integer.parseInt(updatedStockStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid number format for price or stock.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update the item in the database
                // Replace itemName with itemNameDisplay for fetching the item name to update
                int rowsUpdated = dbHelper.update(itemNameDisplay.getText().toString().trim(), updatedDescription, updatedPrice, updatedStock);

                if (rowsUpdated > 0) {
                    Toast.makeText(this, "Item updated successfully.", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to the manage inventory activity
                } else {
                    Toast.makeText(this, "Failed to update item.", Toast.LENGTH_SHORT).show();
                }
            } else {
                // User is adding a new item
                // Gather item information
                String newName = itemName.getText().toString().trim();
                String newDescription = itemDescription.getText().toString().trim();
                String newPriceStr = itemCost.getText().toString().trim();
                String newStockStr = itemStock.getText().toString().trim();

                // Check if all fields are filled in
                if (newName.isEmpty() || newDescription.isEmpty() || newPriceStr.isEmpty() || newStockStr.isEmpty()) {
                    Toast.makeText(this, "Please fill in all fields to add the item.", Toast.LENGTH_SHORT).show();
                    return;
                }

                double newPrice;
                int newStock;
                try {
                    newPrice = Double.parseDouble(newPriceStr);
                    newStock = Integer.parseInt(newStockStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid number format for price or stock.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Insert the new item into the database
                long result = dbHelper.insert(newName, newDescription, newPrice, newStock);
                if (result != -1) {
                    Toast.makeText(this, "Item added successfully.", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to the manage inventory activity after adding
                } else {
                    Toast.makeText(this, "Failed to add item. Item may already exist.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // cancel new addition or cancel update
        cancelButton.setOnClickListener(v -> {
            finish();
        });

        // Delete button logic
        deleteButton.setOnClickListener(v -> showDeleteConfirmation());
    }


    private void showDeleteConfirmation() {
        String name = itemName.getText().toString().trim();
        // alert user before actual deletion
        new AlertDialog.Builder(this)
                .setTitle("Delete Confirmation")
                .setMessage("Do you want to delete " + name + "?")
                .setPositiveButton("DELETE", (dialog, which) -> {
                    dbHelper.delete(name);
                    Toast.makeText(this, name + " deleted successfully.", Toast.LENGTH_SHORT).show();
                    finish(); // back to manage inventory after deleted item
                })
                .setNegativeButton("CANCEL", (dialog, which) -> {
                    Log.d("MainActivityAddItem", "Delete canceled");
                    dialog.dismiss();
                })
                .show();
    }
}

package cs477.gmu.jahmed9_project2;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

public class MainActivityManageInventory extends AppCompatActivity {

    private DatabaseOpenHelper dbHelper;
    private SimpleCursorAdapter myAdapter;
    private ListView listView;
    private Cursor c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_manage_inventory);

        dbHelper = new DatabaseOpenHelper(this);
        listView = findViewById(R.id.inventory_list);
        Button addItemButton = findViewById(R.id.add_item_button);

        //when add new item button is clicked, start add item activity.
        addItemButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivityManageInventory.this, MainActivityAddItem.class);
            startActivity(intent);
        });

        // display list of items in inventory
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (c != null && c.moveToPosition(position)) {
                int nameIndex = c.getColumnIndexOrThrow("item");
                int descriptionIndex = c.getColumnIndexOrThrow("description");

                String name = c.getString(nameIndex);
                String description = c.getString(descriptionIndex);
                // display the details of the item with snackbar
                Snackbar.make(view, name + ": " + description, Snackbar.LENGTH_INDEFINITE)
                        .setAction("Action", null).show();
            }
        });

        // long click to update the list item
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            if (c != null && c.moveToPosition(position)) {
                String itemName = c.getString(c.getColumnIndexOrThrow("item"));
                String itemDescription = c.getString(c.getColumnIndexOrThrow("description"));
                double itemPrice = c.getDouble(c.getColumnIndexOrThrow("price"));
                int itemStock = c.getInt(c.getColumnIndexOrThrow("stock"));

                // For editing, open main activity add item
                Intent intent = new Intent(MainActivityManageInventory.this, MainActivityAddItem.class);
                intent.putExtra("itemName", itemName);
                intent.putExtra("itemDescription", itemDescription);
                intent.putExtra("itemPrice", itemPrice);
                intent.putExtra("itemStock", itemStock);
                startActivity(intent);

            }
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        new LoadDB().execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (c != null) {
            c.close();
        }
    }

    // if user wants to go back to main page, (switch to handle order)
    public void goBack(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }


    //use AsyncTask to load db
    private final class LoadDB extends AsyncTask<Void, Void, Cursor> {
        @Override
        protected Cursor doInBackground(Void... voids) {
            return dbHelper.readAll();
        }

        @Override
        protected void onPostExecute(Cursor data) {
            if (data != null) {
                myAdapter = new SimpleCursorAdapter(
                        MainActivityManageInventory.this,
                        R.layout.item_inventory,
                        data,
                        new String[]{"item", "price", "stock"},
                        new int[]{R.id.item_name, R.id.item_cost, R.id.item_quantity},
                        0
                );
                myAdapter.setViewBinder((view, cursor, columnIndex) -> {
                    if (view.getId() == R.id.item_cost) {
                        double price = cursor.getDouble(columnIndex);
                        ((TextView) view).setText(String.format("Cost per item: $%.2f", price));
                        return true;
                    } else if (view.getId() == R.id.item_quantity) {
                        int quantity = cursor.getInt(columnIndex);
                        ((TextView) view).setText(String.format("%d in stock", quantity));
                        return true;
                    }
                    return false;
                });

                c = data;
                listView.setAdapter(myAdapter);
            }
        }
    }
}

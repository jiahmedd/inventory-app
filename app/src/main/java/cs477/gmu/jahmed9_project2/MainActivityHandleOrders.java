package cs477.gmu.jahmed9_project2;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivityHandleOrders extends AppCompatActivity {

    private DatabaseOpenHelper dbHelper;
    private Spinner itemSpinner;
    private EditText stockInput;
    private TextView currentOrder;
    private Button addOrder, removeOrder, finishOrder;

    private Map<String, Integer> orders = new HashMap<>();
    private double totalCost = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_handle_orders);

        dbHelper = new DatabaseOpenHelper(this);

        itemSpinner = findViewById(R.id.item_spinner);
        stockInput = findViewById(R.id.quantity_input);
        currentOrder = findViewById(R.id.order_summary);
        addOrder = findViewById(R.id.add_to_order_button);
        removeOrder = findViewById(R.id.remove_from_order_button);
        finishOrder = findViewById(R.id.finish_order_button);

        putItemsInSpinner();

        addOrder.setOnClickListener(v -> handleAddToOrder());
        removeOrder.setOnClickListener(v -> handleRemoveFromOrder());
        finishOrder.setOnClickListener(v -> handleFinishOrder());
    }

    private void putItemsInSpinner() {
        Cursor c = dbHelper.readAll();
        ArrayList<String> items = new ArrayList<>();
        if (c != null) {
            while (c.moveToNext()) {
                String itemName = c.getString(c.getColumnIndexOrThrow("item"));
                items.add(itemName);
            }
            c.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        itemSpinner.setAdapter(adapter);
    }

    private void handleAddToOrder() {
        String selectedItem = itemSpinner.getSelectedItem().toString();
        String quantityString = stockInput.getText().toString().trim();

        if (quantityString.isEmpty()) {
            Toast.makeText(this, "Please enter a quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid quantity entered", Toast.LENGTH_SHORT).show();
            return;
        }
    // find item
        Cursor c = dbHelper.readAll();
        if (c != null) {
            while (c.moveToNext()) {
                try {
                    String itemName = c.getString(c.getColumnIndexOrThrow("item"));
                    int stock = c.getInt(c.getColumnIndexOrThrow("stock"));
                    double price = c.getDouble(c.getColumnIndexOrThrow("price"));

                    if (itemName.equals(selectedItem)) {
                        if (quantity > stock) {
                            Toast.makeText(this, "Only " + stock + " available. Adding all available stock.", Toast.LENGTH_SHORT).show();
                            quantity = stock;
                        }

                        if (quantity > 0) {
                            orders.put(itemName, orders.getOrDefault(itemName, 0) + quantity);
                            totalCost += price * quantity;
                            updateOrderSummary();
                            Toast.makeText(this, "Item added to order!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Quantity must be greater than zero!", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }

                } catch (IllegalArgumentException e) {
                    Toast.makeText(this, "Error! Column not found.", Toast.LENGTH_SHORT).show();
                }
            }
            c.close();
            stockInput.setText("");
        } else {
            Toast.makeText(this, "Error reading from database", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleRemoveFromOrder() {
        String selectedItem = itemSpinner.getSelectedItem().toString();
        String quantityString = stockInput.getText().toString().trim();

        if (quantityString.isEmpty()) {
            Toast.makeText(this, "Please enter a quantity to remove", Toast.LENGTH_SHORT).show();
            return;
        }
        int quantityToRemove;
        try {
            quantityToRemove = Integer.parseInt(quantityString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid quantity entered", Toast.LENGTH_SHORT).show();
            return;
        }

        if (orders.containsKey(selectedItem)) {
            int currentQuantity = orders.get(selectedItem);
            if (quantityToRemove > currentQuantity) { // check if remove quantity is more than quantity added to order
                Toast.makeText(this, "Only " + currentQuantity + " available in the order. Removing all available stock.", Toast.LENGTH_SHORT).show();
                orders.remove(selectedItem);
                totalCost -= getItemPrice(selectedItem) * currentQuantity;
            } else {
               // subtract entered quantity from in order quantity
                int newQuantity = currentQuantity - quantityToRemove;
                if (newQuantity > 0) {
                    orders.put(selectedItem, newQuantity);
                    totalCost -= getItemPrice(selectedItem) * quantityToRemove;
                    Toast.makeText(this, "Removed " + quantityToRemove + " of " + selectedItem + " from the order.", Toast.LENGTH_SHORT).show();
                } else {
                    orders.remove(selectedItem);
                    totalCost -= getItemPrice(selectedItem) * currentQuantity;
                    Toast.makeText(this, "Removed all of " + selectedItem + " from the order.", Toast.LENGTH_SHORT).show();
                }
            }

            updateOrderSummary();
            stockInput.setText("");
        } else {
            Toast.makeText(this, "Item not found in order.", Toast.LENGTH_SHORT).show();
        }
    }

    private double getItemPrice(String itemName) {
        Cursor c = dbHelper.readAll();
        double price = 0.0;

        if (c != null) {
            while (c.moveToNext()) {
                // find item and get price
                if (c.getString(c.getColumnIndexOrThrow("item")).equals(itemName)) {
                    price = c.getDouble(c.getColumnIndexOrThrow("price"));
                    break;
                }
            }
            c.close();
        }
        return price;
    }

    private void updateOrderSummary() {
        StringBuilder summary = new StringBuilder("Current Order:\n");
        totalCost = 0.0;

        Cursor c = dbHelper.readAll();
        for (Map.Entry<String, Integer> entry : orders.entrySet()) {
            String itemName = entry.getKey();
            int quantity = entry.getValue();

            if (quantity > 0 && c != null) {
                while (c.moveToNext()) {
                    if (c.getString(c.getColumnIndexOrThrow("item")).equals(itemName)) {
                        double price = c.getDouble(c.getColumnIndexOrThrow("price"));
                        double itemTotalCost = price * quantity;
                        totalCost += itemTotalCost;

                        summary.append(itemName)
                                .append(" (")
                                .append(quantity)
                                .append(") $")
                                .append(String.format("%.2f", itemTotalCost))
                                .append("\n");
                        break;
                    }
                }
                c.moveToFirst();
            }
        }

        if (c != null) {
            c.close();
        }

        summary.append("TOTAL COST: $").append(String.format("%.2f", totalCost));
        currentOrder.setText(summary.toString());
    }

    private void handleFinishOrder() {
        Cursor c = dbHelper.readAll();
        if (c != null) {
            while (c.moveToNext()) {
                try {
                    String itemName = c.getString(c.getColumnIndexOrThrow("item"));
                    if (orders.containsKey(itemName)) {
                        int stock = c.getInt(c.getColumnIndexOrThrow("stock"));
                        int quantityOrdered = orders.get(itemName);
                        int newStock = stock - quantityOrdered;
                        //update database with new stock
                        dbHelper.update(
                                itemName,
                                c.getString(c.getColumnIndexOrThrow("description")),
                                c.getDouble(c.getColumnIndexOrThrow("price")),
                                newStock
                        );
                    }
                } catch (IllegalArgumentException e) {
                    Toast.makeText(this, "Error! Column not found.", Toast.LENGTH_SHORT).show();
                }
            }
            c.close();
        }

        orders.clear();
        totalCost = 0.0;
        updateOrderSummary();
        Toast.makeText(this, "Order completed and inventory updated.", Toast.LENGTH_SHORT).show();
        finish();
    }

}

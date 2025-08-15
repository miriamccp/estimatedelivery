

import java.io.*;
import java.util.*;

public class DeliveryEstimateComparison {
    
    static class OrderData {
        Map<String, String> fields = new HashMap<>();
        
        public String get(String key) {
            return fields.getOrDefault(key, "");
        }
        
        public void put(String key, String value) {
            fields.put(key, value == null ? "" : value.trim());
        }
    }
    
    static class ComparisonResult {
        String orderKey;
        String field;
        String beforeValue;
        String afterValue;
        String changeType;
        
        ComparisonResult(String orderKey, String field, String beforeValue, String afterValue, String changeType) {
            this.orderKey = orderKey;
            this.field = field;
            this.beforeValue = beforeValue;
            this.afterValue = afterValue;
            this.changeType = changeType;
        }
        
        @Override
        public String toString() {
            return String.format("%-15s | %-20s | %-25s | %-25s | %s", 
                orderKey, field, beforeValue, afterValue, changeType);
        }
    }
    
    public static void main(String[] args) {
//        if (args.length != 2) {
//            System.out.println("Usage: java DeliveryEstimateComparison <before.csv> <after.csv>");
//            System.out.println("Example: java DeliveryEstimateComparison delivery_estimates_before.csv delivery_estimates_after.csv");
//            return;
//        }
        
        String beforeFile = "delivery_estimates_before.csv";
        String afterFile = "delivery_estimates_after.csv";
        
        try {
            Map<String, OrderData> beforeData = loadCSV(beforeFile);
            Map<String, OrderData> afterData = loadCSV(afterFile);
            
            System.out.println("=== DELIVERY ESTIMATE COMPARISON REPORT ===");
            System.out.println("Before file: " + beforeFile + " (" + beforeData.size() + " orders)");
            System.out.println("After file: " + afterFile + " (" + afterData.size() + " orders)");
            System.out.println();
            
            compareData(beforeData, afterData);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    static Map<String, OrderData> loadCSV(String filename) throws IOException {
        Map<String, OrderData> data = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = reader.readLine();
            if (line == null) throw new IOException("Empty file: " + filename);
            
            String[] headers = line.split(",");
            
            // Clean headers
            for (int i = 0; i < headers.length; i++) {
                headers[i] = headers[i].trim().replace("\"", "");
            }
            
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                String[] values = parseCSVLine(line);
                
                if (values.length < headers.length) {
                    System.err.println("Warning: Line " + lineNum + " has fewer values than headers. Skipping.");
                    continue;
                }
                
                OrderData order = new OrderData();
                for (int i = 0; i < headers.length && i < values.length; i++) {
                    order.put(headers[i], values[i]);
                }
                
                // Create unique key: PO + Item + Part ID
                String key = order.get("PO") + "|" + order.get("Item") + "|" + order.get("Part ID");
                data.put(key, order);
            }
        }
        
        return data;
    }
    
    static String[] parseCSVLine(String line) {
        List<String> values = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        values.add(current.toString().trim());
        
        return values.toArray(new String[0]);
    }
    
    static void compareData(Map<String, OrderData> beforeData, Map<String, OrderData> afterData) {
        List<ComparisonResult> differences = new ArrayList<>();
        List<String> summaryStats = new ArrayList<>();
        
        // Key fields to compare
        String[] keyFields = {
            "Order Number", "PO", "Item", "Part ID", "Required Qty", "Request Date", "PO Create Date",
            "Estimated Delivery Dates", "Pulling Dates", "Status", 
            "Stock Used", "Flight Qty Used", "Available Qty", "Fulfilled Qty", "Remaining Qty"
        };
        
        int totalOrders = beforeData.size();
        int changedOrders = 0;
        int unchangedOrders = 0;
        
        Map<String, Integer> changesByStatus = new HashMap<>();
        Map<String, Integer> changesByField = new HashMap<>();
        
        for (String key : beforeData.keySet()) {
            OrderData before = beforeData.get(key);
            OrderData after = afterData.get(key);
            
            if (after == null) {
                differences.add(new ComparisonResult(key, "MISSING", "EXISTS", "NOT FOUND", "ERROR"));
                continue;
            }
            
            boolean orderChanged = false;
            String status = before.get("Status");
            
            for (String field : keyFields) {
                String beforeValue = before.get(field);
                String afterValue = after.get(field);
                
                if (!beforeValue.equals(afterValue)) {
                    orderChanged = true;
                    
                    String changeType = determineChangeType(field, beforeValue, afterValue, status);
                    differences.add(new ComparisonResult(key, field, beforeValue, afterValue, changeType));
                    
                    changesByField.merge(field, 1, Integer::sum);
                }
            }
            
            if (orderChanged) {
                changedOrders++;
                changesByStatus.merge(status, 1, Integer::sum);
            } else {
                unchangedOrders++;
            }
        }
        
        // Print summary statistics
        System.out.println("=== SUMMARY STATISTICS ===");
        System.out.println("Total Orders: " + totalOrders);
        System.out.println("Changed Orders: " + changedOrders);
        System.out.println("Unchanged Orders: " + unchangedOrders);
        System.out.println();
        
        System.out.println("=== CHANGES BY STATUS ===");
        for (Map.Entry<String, Integer> entry : changesByStatus.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue() + " orders changed");
        }
        System.out.println();
        
        System.out.println("=== CHANGES BY FIELD ===");
        for (Map.Entry<String, Integer> entry : changesByField.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue() + " changes");
        }
        System.out.println();
        
        // Print detailed differences
        if (differences.isEmpty()) {
            System.out.println("✅ NO DIFFERENCES FOUND - Files are identical!");
        } else {
            System.out.println("=== DETAILED DIFFERENCES ===");
            System.out.println("Total differences: " + differences.size());
            System.out.println();
            System.out.printf("%-15s | %-20s | %-25s | %-25s | %s%n", 
                "ORDER KEY", "FIELD", "BEFORE", "AFTER", "CHANGE TYPE");
            System.out.println("-".repeat(120));
            
            // Group by change type
            Map<String, List<ComparisonResult>> groupedChanges = new HashMap<>();
            for (ComparisonResult diff : differences) {
                groupedChanges.computeIfAbsent(diff.changeType, k -> new ArrayList<>()).add(diff);
            }
            
            for (String changeType : Arrays.asList("EXPECTED-STOCK", "EXPECTED-FLIGHT", "UNEXPECTED", "ERROR")) {
                List<ComparisonResult> changes = groupedChanges.get(changeType);
                if (changes != null && !changes.isEmpty()) {
                    System.out.println("\n" + changeType + " CHANGES (" + changes.size() + "):");
                    for (ComparisonResult change : changes) {
                        System.out.println(change);
                    }
                }
            }
        }
        
        // Validation summary
        System.out.println("\n=== VALIDATION SUMMARY ===");
        validateChanges(changesByStatus, changesByField);
    }
    
    static String determineChangeType(String field, String beforeValue, String afterValue, String status) {
    	// Expected changes for any orders using stock
        if ("Estimated Delivery Dates".equals(field)) {
            // Any status that contains "Stock" or has "(stock)" in delivery dates
            if (status.contains("Stock") || 
                beforeValue.contains("(stock)") || 
                afterValue.contains("(stock)")) {
                return "EXPECTED-STOCK";
            }
        }
        
        // Expected changes for flight-based orders
        if (field.equals("Pulling Dates") || field.equals("Estimated Delivery Dates")) {
            if (status.contains("Flight") || status.contains("Awaiting")) {
                return "EXPECTED-FLIGHT";
            }
        }
        
        // Fields that should never change
        if (Arrays.asList("Order Number", "PO", "Item", "Part ID", "Required Qty", 
                         "Request Date", "PO Create Date").contains(field)) {
            return "ERROR";
        }
        
        return "UNEXPECTED";
    }
    
    static void validateChanges(Map<String, Integer> changesByStatus, Map<String, Integer> changesByField) {
        System.out.println("Expected changes:");
        System.out.println("✓ 'In Stock' orders should have delivery date changes");
        System.out.println("✓ Flight-based orders should have pulling/delivery date changes");
        
        System.out.println("\nUnexpected changes:");
        System.out.println("✗ 'No Inventory Available' orders should NOT change");
        System.out.println("✗ 'Invalid Date' orders should NOT change");
        System.out.println("✗ Quantity fields should NOT change (unless there's a bug)");
        
        // Check for concerning changes
        if (changesByStatus.containsKey("No Inventory Available")) {
            System.out.println("⚠️  WARNING: 'No Inventory Available' orders changed!");
        }
        
        if (changesByStatus.containsKey("Invalid Date")) {
            System.out.println("⚠️  WARNING: 'Invalid Date' orders changed!");
        }
        
        if (changesByField.containsKey("Required Qty") || changesByField.containsKey("PO") || 
            changesByField.containsKey("Item") || changesByField.containsKey("Part ID")) {
            System.out.println("⚠️  WARNING: Input data fields changed - possible data corruption!");
        }
    }
}
# Delivery Date Estimator - Requirements Specification

**Version:** 2.8 (Partial Fulfillment Edition with Multiple Date Tracking)  
**Date:** August 2025  
**Platform:** Modern Web Browsers (Chrome, Firefox, Safari, Edge)  
**Libraries:** SheetJS (XLSX.js) for Excel file processing  
**Deployment:** Single HTML file - no installation required  
**Calendar:** Taiwan ROC Year 114 (2025) Official Business Calendar  
**Key Update:** Partial fulfillment with multiple delivery dates and enhanced sorting logic

## 1. SYSTEM OVERVIEW

A browser-based JavaScript application that processes customer orders using **Partial Fulfillment allocation logic** to estimate delivery dates based on stock availability and flight arrival schedules. The system reads three Excel files client-side and produces delivery estimates with detailed allocation tracking, **multiple delivery date support**, comprehensive unparseable data handling, **warehouse pulling rules** integrated with **Taiwan's official 2025 business calendar**, and **zero buffer days default** for same-day in-stock delivery.

**Key Advantages:**
- **Zero Installation**: Runs directly in web browser
- **Partial Fulfillment Logic**: Fulfills orders to maximum extent possible using available inventory
- **Multiple Date Tracking**: Shows individual pulling and delivery dates for each flight used
- **Enhanced FIFO Sorting**: Primary sort by request date, secondary sort by PO create date
- **Future Flight Utilization**: Uses any available future flights regardless of timing
- **Client-Side Processing**: Files never leave user's computer
- **Cross-Platform**: Works on Windows, Mac, Linux, tablets, phones
- **No Dependencies**: Single HTML file contains everything needed
- **Modern UI**: Responsive design with real-time progress tracking and multi-date display
- **Enhanced Debugging**: Detailed console logging for allocation decisions

## 2. INPUT FILES

### 2.1 Customer Orders (`order_*.xlsx`)
- **Purpose:** Contains customer order requests from multiple customer files
- **File Pattern:** `order_CUSTOMER.xlsx` (e.g., `order_HZ4600.xlsx`)
- **Upload Method:** Multi-file selection via browser file picker
- **Key Columns:**
  - `part no` - Part identifier (may have leading words/prefixes)
  - `request qty` - Quantity requested by customer
  - `request date` - Date customer wants parts (YYYYMMDD format, e.g., "20250802")
  - `po create date` - Purchase order creation date (used for secondary sorting)
  - `po` - Purchase order number
  - `item` - Item description
- **Notes:** 
  - Date parsing handles YYYYMMDD strings, Excel Date objects, and Excel serial numbers
  - **Unparseable dates** (e.g., "cancel", "pending", "N/A") are preserved and orders are processed with invalid date status
  - Part numbers may contain prefixes that need restricted fuzzy matching
  - Customer code extracted from filename (e.g., "HZ4600" from "order_HZ4600.xlsx")
  - System automatically processes all uploaded order files

### 2.2 Part Stock (`part_stock.xlsx`)
- **Purpose:** Current inventory levels
- **Upload Method:** Single file selection
- **Key Columns:**
  - `part no` - Part identifier
  - `qty` - Available stock quantity
- **Notes:** 
  - **Real stock tracking** - system validates actual availability at time of processing
  - **Inventory consumption** - stock levels decrease as orders are allocated in FIFO sequence
  - Use restrictive fuzzy matching to match with customer order part numbers

### 2.3 Flight Information (`onflight.xlsx`)
- **Purpose:** Scheduled part arrivals with quantities and dates at airport
- **Upload Method:** Single file selection
- **Key Columns:**
  - `part no` - Part identifier
  - `date` - **Flight arrival date at airport** (various formats)
  - `qty` - Quantity arriving on that flight
- **Notes:** 
  - Multiple flights per part number allowed
  - Each row represents specific quantity arriving on specific date
  - **Fixed date format display**: Shows as `2000×2025-08-15` instead of `2000×45806`
  - **Unparseable dates** (e.g., "NA", "TBD") are preserved and flights are excluded from scheduling
  - Must sort by arrival date for proper allocation (unparseable dates sorted to end)

## 3. CORE BUSINESS LOGIC

### 3.1 **🆕 Partial Fulfillment Allocation System**
- **Primary Rule:** Orders processed in chronological order by `request date` (earliest first), with `po create date` as secondary sort
- **Secondary Sort:** When request dates are identical, sort by PO create date (earliest first)
- **Tertiary Sort:** Order number as final tiebreaker
- **Maximum Fulfillment:** Orders are fulfilled to the maximum extent possible using available inventory
- **Future Flight Utilization:** System accepts ANY future flights regardless of timing constraints
- **Real Inventory Tracking:** Actual stock and flight quantities are consumed as orders are allocated
- **No Skipping:** Orders are never skipped - always fulfilled partially if any inventory exists
- **Multiple Delivery Support:** Orders can have multiple delivery dates when fulfilled across multiple flights

### 3.2 **🆕 Enhanced Sorting Logic**
```javascript
// Multi-level sorting implementation
1. Primary Sort: Request Date (ascending) - earliest orders first
2. Secondary Sort: PO Create Date (ascending) - earlier PO creation first  
3. Tertiary Sort: Order Number (ascending) - consistent tiebreaker
```

**Example Sorting Behavior:**
- Order A: Request Date = 2025-08-01, PO Date = 2025-07-15, Order# = 100
- Order B: Request Date = 2025-08-01, PO Date = 2025-07-10, Order# = 101  
- **Processing Order**: B → A (same request date, but B has earlier PO date)

### 3.3 Flight Date Interpretation and Acceptance Criteria
- **Flight Date Definition:** The date in flight file represents **flight departure date**, not arrival date
- **Flight Arrival Calculation:** Flights typically arrive same day or next day after departure
- **Customs Clearance:** Requires 1 business day (Monday-Saturday) after flight arrival
- **All Future Flights Accepted:** No buffer day restrictions - any flight departure in the future can be used
- **Early Flights:** Always accept flights departing before order date
- **Late Flights:** Accept all flights departing after order date (unlimited days late)

### 3.4 Restrictive Part Number Matching
- **Purpose:** Handle part numbers with "-A" postfix variations only
- **Rules:** 
  1. **Exact match:** `part_12345678` = `part_12345678` (case insensitive)
  2. **Postfix match:** `part_12345678` ↔ `part_12345678-A` (bidirectional)
  3. **No other fuzzy matching** - eliminates false positives

### 3.5 **🆕 Zero Buffer Days Default**
**In-Stock Order Delivery:**
- **Default Buffer:** 0 days (same-day delivery)
- **Working Day Check:** If order date falls on weekend/holiday, delivery moves to next working day
- **Configurable:** Users can increase buffer days if needed

### 3.6 **Enhanced Taiwan Working Calendar Integration**
The system uses Taiwan's official ROC Year 114 (2025) business calendar for all date calculations.

**Official Taiwan 2025 Holidays (ROC Year 114):**
1. **1月1日 (2025-01-01)** - New Year's Day (元旦)
2. **1月27日 (2025-01-27)** - Chinese New Year's Eve (農曆除夕)
3. **1月28日 (2025-01-28)** - Chinese New Year Day 1 (春節初一)
4. **1月29日 (2025-01-29)** - Chinese New Year Day 2 (春節初二)
5. **1月30日 (2025-01-30)** - Chinese New Year Day 3 (春節初三)
6. **1月31日 (2025-01-31)** - Chinese New Year Day 4 (春節初四)
7. **2月28日 (2025-02-28)** - Peace Memorial Day (和平紀念日)
8. **4月3日 (2025-04-03)** - Tomb Sweeping Day Adjusted (清明節調整)
9. **4月4日 (2025-04-04)** - Children's Day / Tomb Sweeping Day (兒童節/清明節)
10. **5月1日 (2025-05-01)** - Labor Day (勞動節)
11. **5月30日 (2025-05-30)** - Dragon Boat Festival (端午節)
12. **9月29日 (2025-09-29)** - Mid-Autumn Festival Adjusted (中秋節調整)
13. **10月6日 (2025-10-06)** - Mid-Autumn Festival (中秋節)
14. **10月10日 (2025-10-10)** - National Day (國慶日)
15. **10月24日 (2025-10-24)** - Additional Holiday (補假)
16. **12月25日 (2025-12-25)** - Christmas Day (聖誕節)

### 3.7 **Enhanced Warehouse Pulling Rules with Flight Processing Sequence**

Flight departure dates represent when flights leave their origin. The complete processing sequence from flight departure to warehouse pulling involves multiple stages with specific business rules.

**Flight Processing Sequence:**
1. **Flight Departure:** Date specified in flight file (input data)
2. **Flight Arrival:** Departure + 0-1 days (typically next day for international flights)
3. **Customs Clearance:** Arrival + 1 business day (Monday-Saturday, excludes Taiwan holidays)
4. **Pulling Eligibility:** Clearance completion + configurable lead days (default: 1 day)
5. **Pulling Date:** Next available Monday or Wednesday after eligibility

**Company Pulling Rules (Original Chinese):**
- 拉回(pulling in): 固定周一和周三，再隔兩天才能出貨，即周三和周五
- 當月最後一天不能拉回
- 班機在周末六日,周一也不可以拉回, 只能週三

**Enhanced Pulling Constraints:**
1. **Only Monday and Wednesday** are allowed for warehouse pulling operations
2. **Cannot pull on the last day of the month** (operational constraint)
3. **Must be a Taiwan working day** (excludes weekends and Taiwan holidays)
4. **Weekend flight restriction:** If flight departs Saturday/Sunday, cannot pull Monday, must wait until Wednesday
5. **Customs clearance timing:** Minimum 1 business day (Monday-Saturday) after flight arrival
6. **Lead time:** Configurable Taiwan working days between customs clearance completion and pulling eligibility (default: 1 day)

**Enhanced Pulling Date Calculation Process:**
1. **Flight Departure Date:** Input from flight file
2. **Calculate Arrival Date:** Departure + 1 day (international flight assumption)
3. **Calculate Customs Clearance Date:** 
   - Arrival + 1 business day
   - Skip Sundays (customs closed)
   - Skip Taiwan holidays (customs closed)
4. **Add Lead Time:** Clearance + configurable working days (default: 1 day)
5. **Find Valid Pulling Day:** Next Monday or Wednesday from candidate date
6. **Apply Weekend Flight Rule:** If departure was Sat/Sun, skip Monday options
7. **Working Day Check:** Ensure selected date is a Taiwan working day (not holiday)
8. **Month End Check:** If last day of month, move to next valid pulling day
9. **Final Pulling Date:** Confirmed warehouse pulling date

**Customs Clearance Rules:**
- **Operating Days:** Monday through Saturday
- **Closed:** Sundays and Taiwan holidays
- **Processing Time:** 1 business day minimum
- **Integration:** Automatically calculated in pulling date sequence

**Example Calculation:**
- Flight departs Friday → Arrives Saturday → Customs Monday → Lead time Tuesday → Pull Wednesday
- Flight departs Saturday → Arrives Sunday → Customs Monday → Lead time Tuesday → Pull Wednesday (Monday skipped due to weekend departure)
- Flight departs Sunday → Arrives Monday → Customs Tuesday → Lead time Wednesday → Pull Wednesday

### 3.8 **🆕 Multiple Delivery Date Calculation**
The delivery calculation now supports multiple delivery dates for orders fulfilled across multiple flights:

**In-Stock Orders:**
- **Formula:** `Delivery Date = Order Date + Buffer Days (default: 0) + Working Day Adjustment (BACKWARD)`
- **Working Day Rule:** If order date falls on weekend/holiday, delivery moves to **previous** working day
- **Zero Buffer Default:** Same-day delivery for working day requests, previous working day for weekend/holiday requests
- **Format:** `"500×2025-08-01 (stock)"`

**Flight-Based Orders:**
- **Formula:** `Delivery Date = Pulling Date + Delivery Lead Days (Taiwan Working Days)`
- **Multiple Delivery Dates:** One date per flight used
- **Format:** `"300×2025-08-15 (flight), 700×2025-08-25 (flight)"`

**Mixed Orders (Stock + Flights):**
- **Multiple Delivery Dates:** Separate dates for stock and each flight
- **Format:** `"200×2025-08-01 (stock), 500×2025-08-15 (flight), 300×2025-08-25 (flight)"`

**Pulling Dates Display:**
- **Stock Only:** `"N/A"` (no pulling required)
- **Single Flight:** `"500×2025-08-12"`
- **Multiple Flights:** `"300×2025-08-12, 700×2025-08-22"`

## 4. USER INTERFACE SPECIFICATIONS

### 4.1 **File Upload Interface**
- **Visual Design:** Card-based upload areas with drag-and-drop styling
- **File Validation:** Real-time feedback on file selection
- **Progress Indicators:** File size display and upload confirmation
- **Multiple Files:** Support for multiple order files in single selection
- **File Type Validation:** Accepts .xlsx and .xls files only

### 4.2 **Configuration Panel**
- **Interactive Controls:** Number inputs with validation and reasonable ranges
- **Real-time Updates:** Changes apply immediately to next calculation
- **Configurable Parameters:**
  - **Buffer Days (In-Stock Orders): 0-30 days, default 0**
  - Flight Buffer Days (Max Late): 0-30 days, default 0 (UNUSED - all future flights accepted)
  - Pulling Lead Days: 1-10 days, default 2
  - Delivery Lead Days: 1-10 days, default 2

### 4.3 **Processing Interface**
- **Progress Bar:** Real-time visual progress during file processing
- **Enhanced Processing Log:** Live console-style log with timestamps and detailed allocation decisions
- **Status Updates:** Clear indication of current processing step
- **FIFO Indicators:** Shows order processing sequence and inventory consumption
- **Error Display:** User-friendly error messages with auto-dismiss

### 4.4 **🆕 Enhanced Results Display with Column Reordering**
- **Priority Column Sequence (Left to Right):**
  1. **Order #** - Order number
  2. **Cust Code** - Customer code  
  3. **PO** - Purchase order
  4. **Item** - Item description
  5. **Part ID** - Part identifier
  6. **Req Qty** - Required quantity
  7. **Request Date** - Order request date
  8. **Delivery Dates** - Multiple delivery dates with quantities
  9. **Pulling Dates** - Multiple pulling dates with quantities
- **Additional Columns:** Fulfilled, Remaining, PO Create Date, Lead Days, Status, Stock Used, Flight Qty, Flight Details, Warehouse Notes
- **Interactive Features:** Sortable columns, hover expansion for multi-date fields, status color coding
- **Multiple Date Display:** Expandable cells showing full date details on hover

## 5. OUTPUT REQUIREMENTS

### 5.1 **🆕 Enhanced Browser Results Table**
**Priority Displayed Columns (Left to Right):**
- Order #, Cust Code, PO, Item, Part ID, Req Qty, Request Date
- **Delivery Dates**, **Pulling Dates**, Fulfilled, Remaining
- PO Create Date, Lead Days, Status, Stock Used, Flight Qty, Flight Details, Warehouse Notes

**🆕 Multiple Date Display Format:**
- **Single Stock Delivery:** `"500×2025-08-01 (stock)"`
- **Single Flight Delivery:** `"500×2025-08-15 (flight)"`
- **Multiple Deliveries:** `"200×2025-08-01 (stock), 300×2025-08-15 (flight), 500×2025-08-25 (flight)"`
- **Pulling Dates:** `"300×2025-08-12, 500×2025-08-22"` or `"N/A"` for stock-only

**Interactive Features:**
- **Hover Expansion:** Date columns expand to show complete details
- **Status Color Coding:** Visual indicators for order fulfillment status
- **Responsive Design:** Adapts to screen sizes with horizontal scroll

### 5.2 **🆕 Excel Download File (`delivery_estimates_PARTIAL_FULFILLMENT_TIMESTAMP.xlsx`)**
**Complete Columns (Reordered):**
- Order Number, Cust Code, PO, Item, Part ID, Required Qty, Request Date
- **Estimated Delivery Dates**, **Pulling Dates**, Available Qty, Fulfilled Qty, Remaining Qty
- PO Create Date, Lead Time (Days), Status, Notes, Stock Before, Stock After
- Stock Used, Flight Qty Used, Flight Details, Warehouse Notes

**File Features:**
- **Auto-sizing Columns:** Optimized column widths for readability
- **Timestamp Filename:** Includes "PARTIAL_FULFILLMENT" identifier
- **Complete Data:** All processing details with proper multi-date formatting
- **Enhanced Column Order:** Priority information first, technical details after

### 5.3 **🆕 Enhanced Status Values**

The system provides detailed status classifications based on fulfillment method and completeness, enabling accurate tracking of partial fulfillment scenarios.

**Primary Status Categories:**

-   **"In Stock"** - Fully fulfilled from existing stock (single delivery date)
-   **"Partial Stock + Flight"** - Mixed fulfillment using stock and flights (multiple delivery dates)
-   **"Awaiting Flight"** - Fully fulfilled from flight arrivals (multiple delivery dates possible)
-   **"Partial Stock Only"** - Partially fulfilled using only available stock
-   **"Partial Flight Only"** - Partially fulfilled using only available flights
-   **"No Inventory Available"** - No current stock or future flights available
-   **"Invalid Date"** - Orders with unparseable request dates

**Status Classification Logic:**

-   **Full Fulfillment (100%):**

    -   Stock only → "In Stock"
    -   Flight only → "Awaiting Flight"
    -   Stock + Flight → "Partial Stock + Flight"
-   **Partial Fulfillment (1-99%):**

    -   Stock only used → "Partial Stock Only"
    -   Flight only used → "Partial Flight Only"
    -   Stock + Flight used → "Partial Stock + Flight"
-   **No Fulfillment (0%):**

    -   No inventory sources → "No Inventory Available"
-   **Processing Issues:**

    -   Invalid request date → "Invalid Date"

**Stock Delivery Date Behavior by Status:**

-   Any order using stock (regardless of status) follows backward adjustment rule
-   Weekend/holiday requests deliver on previous working day
-   Stock delivery dates contain "(stock)" identifier in delivery date field
-   Multiple delivery scenarios properly tracked with individual date components

**Flight Delivery Date Behavior by Status:**

-   Any order using flights follows departure-based pulling calculation
-   Flight delivery dates contain "(flight)" identifier in delivery date field
-   Multiple flight dates tracked individually with separate pulling/delivery dates
-   Weekend departure restrictions properly applied to pulling dates

**Status Persistence Rules:**

-   Status reflects final fulfillment state after allocation
-   Partial fulfillment statuses indicate maximum possible fulfillment achieved
-   No orders skipped due to insufficient inventory (core partial fulfillment principle)
-   Status accurately represents business reality for customer communication

### 5.4 **🆕 Enhanced Summary Statistics**
**Real-time Dashboard:**
- **Total Orders**, **Fully Fulfilled**, **Partially Fulfilled**, **No Inventory**
- **In Stock**, **Using Future Flights** (count of orders with flight-based fulfillment)
- **Customer breakdown** by customer code
- **Realistic allocation breakdown** based on actual inventory availability and consumption

### 5.5 **🆕 Enhanced Warehouse Notes Format**
**Multiple Delivery Scenarios:**
- **Stock Only:** `"300 from stock → 2025-08-01"`
- **Flight Only:** `"500 from flight 2025-08-15 (+10d) → 2025-08-22"`
- **Mixed Fulfillment:** `"200 from stock → 2025-08-01; 300 from flight 2025-08-15 (+10d) → 2025-08-22; 500 from flight 2025-08-25 (+20d) → 2025-09-02"`
- **No Fulfillment:** `"No inventory available - no current stock or future flights"`

### 5.6 **🆕 Enhanced Processing Log**
**Real-time Console with Detailed Tracking:**
- Enhanced sorting notification: `"Processing 278 orders in FIFO sequence (request date → PO create date)"`
- **Multi-level sorting decisions:** Shows primary and secondary sort results
- **Partial fulfillment tracking:** Real-time inventory consumption and allocation decisions
- **Multiple date calculations:** Individual pulling and delivery date calculations for each flight
- **Stock tracking accuracy:** Current stock levels before and after each order
- Error messages and warnings with specific allocation details

## 6. TECHNICAL SPECIFICATIONS

### 6.1 **Browser Compatibility**
- **Supported Browsers:** Chrome 90+, Firefox 88+, Safari 14+, Edge 90+
- **JavaScript Features:** ES6+, FileReader API, Blob API
- **CSS Features:** Grid, Flexbox, CSS Variables, Hover Effects
- **No Plugins Required:** Pure HTML/CSS/JavaScript

### 6.2 **🆕 Enhanced Multi-Level Sorting Implementation**
```javascript
// Enhanced sorting with secondary PO create date sort
const sortedOrders = [...orders].sort((a, b) => {
    // Handle invalid dates
    if (!a.hasValidDate() && b.hasValidDate()) return 1;
    if (a.hasValidDate() && !b.hasValidDate()) return -1;
    if (!a.hasValidDate() && !b.hasValidDate()) return a.orderNumber - b.orderNumber;
    
    // Primary sort: request date ascending
    const requestDateDiff = a.requestDate.getTime() - b.requestDate.getTime();
    if (requestDateDiff !== 0) return requestDateDiff;
    
    // Secondary sort: PO create date ascending
    if (a.poCreateDate && b.poCreateDate) {
        return a.poCreateDate.getTime() - b.poCreateDate.getTime();
    } else if (a.poCreateDate && !b.poCreateDate) {
        return -1; // Valid PO date comes before invalid
    } else if (!a.poCreateDate && b.poCreateDate) {
        return 1; // Invalid PO date comes after valid
    }
    
    // Tertiary sort: order number
    return a.orderNumber - b.orderNumber;
});
```

### 6.3 **🆕 Partial Fulfillment Allocation Logic**
```javascript
// Partial fulfillment with multiple date tracking
function tryPartialAllocation(order, availableStock, availableFlights) {
    const pullingDates = [];
    const deliveryDates = [];
    
    // Use available stock first with delivery date calculation
    if (stockOnHand > 0) {
        stockUsed = Math.min(stockOnHand, order.requiredQuantity);
        
        // Calculate stock delivery date
        let stockDeliveryDate = new Date(order.requestDate);
        // Apply buffer and working day logic
        deliveryDates.push({
            quantity: stockUsed,
            date: stockDeliveryDate,
            type: 'stock'
        });
    }
    
    // Use ALL available future flights
    for (const flight of flights) {
        if (remainingNeeded <= 0) break;
        if (!flight.arrivalDate) continue; // Skip unparseable dates
        
        // Accept ANY future flight (no buffer restrictions)
        const quantityToUse = Math.min(flight.quantity, remainingNeeded);
        if (quantityToUse > 0) {
            const flightPullingDate = calculatePullingDate(flight.arrivalDate);
            const flightDeliveryDate = calculateDeliveryFromPulling(flightPullingDate);
            
            pullingDates.push({
                quantity: quantityToUse,
                date: flightPullingDate,
                flightDate: flight.arrivalDate
            });
            
            deliveryDates.push({
                quantity: quantityToUse,
                date: flightDeliveryDate,
                type: 'flight',
                flightDate: flight.arrivalDate
            });
        }
    }
}
```

### 6.4 **🆕 Multiple Date Display Implementation**
```javascript
// Format multiple delivery dates
multipleDeliveryDates = sortedDeliveryDates.map(item => {
    const dateStr = formatDate(item.date);
    if (item.type === 'stock') {
        return `${item.quantity}×${dateStr} (stock)`;
    } else {
        return `${item.quantity}×${dateStr} (flight)`;
    }
}).join(', ');

// Format multiple pulling dates (flights only)
if (sortedPullingDates.length > 0) {
    multiplePullingDates = sortedPullingDates.map(item => {
        return `${item.quantity}×${formatDate(item.date)}`;
    }).join(', ');
} else {
    multiplePullingDates = null; // Stock-only orders
}
```

## 7. ALLOCATION ALGORITHM

### 7.1 **🆕 Enhanced Processing Flow (Partial Fulfillment with Multiple Dates)**
1. **File Upload & Validation:** User selects files, system validates and displays info
2. **Configuration Update:** Read current UI settings before processing
3. **Data Loading:** Parse Excel files with enhanced error handling and debugging
4. **🆕 Enhanced Order Sorting:** Multi-level sort (request date → PO create date → order number)
5. **Inventory Initialization:** Create working copies for consumption tracking
6. **🆕 Partial Fulfillment Allocation:** Process each order with maximum fulfillment logic
7. **🆕 Multiple Date Calculation:** Calculate individual pulling and delivery dates for each flight
8. **Inventory Consumption:** Each order reduces available inventory for subsequent orders
9. **🆕 Enhanced Results Generation:** Format multiple dates and reorder columns
10. **UI Update:** Display results with new column order and multi-date support
11. **Download Preparation:** Format data for Excel export with enhanced column structure

### 7.2 **🆕 Partial Fulfillment Allocation Logic**
1. **Enhanced Part Matching:** Use restrictive fuzzy matching (exact + "-A" postfix only)
2. **Stock First Allocation:** Use available stock with immediate delivery date calculation
3. **Future Flight Addition:** Accept ALL future flights regardless of timing
4. **Individual Date Calculation:** Calculate pulling and delivery dates for each flight used
5. **Inventory Consumption:** Deduct consumed quantities for next orders
6. **Multiple Date Formatting:** Generate display strings for multiple delivery scenarios
7. **Status Classification:** Enhanced status based on fulfillment method and completeness

### 7.3 **🆕 Enhanced Real-time Processing Feedback**
- **Enhanced Progress Bar:** Visual indication with multi-phase processing
- **Detailed Live Log:** Console-style updates with FIFO sorting decisions and multiple date calculations
- **Inventory Consumption Tracking:** Real-time stock and flight level updates
- **Multiple Date Generation:** Shows individual date calculations for complex orders
- **Performance Optimization:** Client-side processing with detailed logging

## 8. ERROR HANDLING & VALIDATION

### 8.1 **File Validation**
- **File Type Check:** Ensure .xlsx/.xls extensions
- **File Size Display:** Show file sizes to user
- **Empty File Detection:** Warn if files appear empty
- **Column Detection:** Flexible header matching with fallbacks

### 8.2 **🆕 Enhanced Data Validation**
- **Date Parsing:** Graceful handling of multiple date formats
- **Multiple Date Validation:** Ensure all calculated dates are valid Taiwan working days
- **Stock Consumption Tracking:** Verify inventory levels remain non-negative
- **Flight Date Processing:** Validate all flight dates before allocation
- **Missing Data Handling:** Use appropriate fallback values

### 8.3 **🆕 Enhanced Processing Validation**
- **Sorting Verification:** Log multi-level sorting decisions for verification
- **Allocation Math:** Verify partial fulfillment calculations
- **Multiple Date Logic:** Validate pulling and delivery date calculations
- **Inventory Consistency:** Monitor stock levels throughout FIFO processing
- **Status Classification:** Ensure accurate status assignment based on fulfillment method

## 9. DEPLOYMENT & USAGE

### 9.1 **Deployment Model**
- **Single File:** Complete application in one HTML file (delivery_estimator_v2.8_PARTIAL_FULFILLMENT.html)
- **CDN Dependencies:** SheetJS library loaded from CloudFlare CDN
- **No Server Required:** Runs entirely client-side
- **Distribution:** Email, web hosting, or file sharing
- **Version Control:** Self-contained versioning within filename

### 9.2 **🆕 Enhanced User Instructions**
1. **Open File:** Double-click HTML file or open in browser
2. **Upload Files:** Select all required Excel files (orders, stock, flights)
3. **Configure Settings:** Adjust parameters if needed (buffer days default = 0)
4. **Process:** Click "Calculate Delivery Dates" with Partial Fulfillment logic
5. **Monitor Progress:** Watch enhanced processing log with sorting and allocation decisions
6. **Review Results:** Check realistic summary statistics with new column order and multiple dates
7. **Download:** Save Excel results file with PARTIAL_FULFILLMENT timestamp and enhanced structure

### 9.3 **System Requirements**
- **Browser:** Any modern browser (see compatibility list)
- **Memory:** Sufficient RAM for file processing (typically 100MB+ available)
- **Storage:** Temporary space for file downloads
- **Network:** Internet connection for initial CDN library loading only
- **File System:** Local file access for Excel download

## 10. VALIDATION CHECKLIST

### 10.1 Core Functionality Validation

When verifying browser-based results, check:

-   [ ] **File Upload:** All three file types upload successfully with visual confirmation
-   [ ] **🆕 Enhanced Sorting:** Orders sorted by request date, then PO create date, then order number
-   [ ] **Configuration:** UI settings properly affect calculations (zero buffer default)
-   [ ] **Unparseable Dates:** Orders with invalid dates processed with "Invalid Date" status
-   [ ] **🆕 Partial Fulfillment:** All orders processed, none skipped due to insufficient inventory
-   [ ] **🆕 Future Flight Usage:** System accepts and uses any future flights regardless of timing
-   [ ] **🆕 Flight Date Interpretation:** System treats flight dates as departure dates, not arrival dates
-   [ ] **🆕 Flight Processing Sequence:** Proper sequence from departure → arrival → customs → pulling
-   [ ] **🆕 Weekend Flight Restriction:** Saturday/Sunday departures cannot pull Monday, only Wednesday
-   [ ] **🆕 Stock Delivery Direction:** Weekend/holiday requests deliver on previous working day (not next)
-   [ ] **🆕 Multiple Date Display:** Orders with multiple flights show multiple pulling and delivery dates
-   [ ] **🆕 Column Ordering:** Priority columns appear first (Order#, Cust Code, PO, Item, Part ID, etc.)
-   [ ] **Stock Consumption Tracking:** Stock Before/After columns show correct progression through FIFO sequence
-   [ ] **Taiwan Calendar Integration:** All date calculations use Taiwan working calendar
-   [ ] **Enhanced Pulling Logic:** Pulling dates respect Mon/Wed rule and Taiwan working days
-   [ ] **Zero Buffer Default:** In-stock orders deliver same day or previous working day for weekends/holidays
-   [ ] **Customs Clearance Integration:** Flight processing includes proper customs clearance timing (1 business day)
-   [ ] **Part Matching:** Only exact match or "-A" postfix variations allowed
-   [ ] **🆕 Enhanced Results Display:** Summary statistics show realistic partial fulfillment breakdown
-   [ ] **🆕 Excel Download:** Downloaded file contains reordered columns with multiple date format
-   [ ] **🆕 Enhanced Processing Log:** Detailed logging of sorting decisions, allocation process, and multiple date calculations
-   [ ] **Error Handling:** Appropriate handling of invalid data, variable scope issues resolved
-   [ ] **🆕 Multi-Date Hover:** Date columns expand on hover to show complete details

### 10.2 Expected Date Behaviors

-   [ ] **Stock order on Friday → Deliver Friday** (working day)
-   [ ] **Stock order on Saturday → Deliver Friday** (previous working day)
-   [ ] **Stock order on Sunday → Deliver Friday** (previous working day)
-   [ ] **Stock order on holiday → Deliver previous working day**
-   [ ] **Flight departs Friday → Can pull Monday** (if working day)
-   [ ] **Flight departs Saturday → Cannot pull Monday, must wait until Wednesday**
-   [ ] **Flight departs Sunday → Cannot pull Monday, must wait until Wednesday**

### 10.3 Expected Changes After Code Updates

-   [ ] **Stock Delivery Date Changes:** Any status containing "Stock" or delivery dates containing "(stock)"
-   [ ] **Flight Delivery Date Changes:** Orders with Flight Qty Used > 0 or delivery dates containing "(flight)"
-   [ ] **No Changes for "No Inventory Available":** These orders should remain identical
-   [ ] **No Changes for "Invalid Date":** These orders should remain identical
-   [ ] **No Changes to Quantity Fields:** All qty fields should remain identical (unless there's a bug)
-   [ ] **No Changes to Input Data:** PO, Item, Part ID, Request Date, PO Create Date should remain identical

## 11. KEY IMPROVEMENTS IN v2.8

### 11.1 **🆕 Partial Fulfillment Logic**
- **No More Skipped Orders:** Every order is processed and fulfilled to maximum extent possible
- **Future Flight Utilization:** System accepts ANY future flights to maximize fulfillment
- **Maximum Inventory Usage:** Uses all available stock and flights optimally
- **Enhanced Customer Service:** Enables partial shipments and better delivery communication

### 11.2 **🆕 Multiple Delivery Date Support**
- **Individual Flight Tracking:** Each flight used generates its own pulling and delivery dates
- **Mixed Fulfillment Display:** Clear visualization of stock + multiple flight combinations
- **Enhanced Planning:** Enables coordination of multiple delivery phases
- **Logistics Optimization:** Detailed schedule for managing complex fulfillment scenarios

### 11.3 **🆕 Enhanced FIFO Sorting**
- **Multi-Level Sort:** Request date → PO create date → order number
- **Fair Processing:** Orders with same request date sorted by PO creation time
- **Predictable Priority:** Consistent, logical order processing sequence
- **Business Alignment:** Reflects real business prioritization practices

### 11.4 **🆕 Improved User Interface**
- **Priority Column Order:** Most important information displayed first
- **Multiple Date Visualization:** Hover expansion for complex date information
- **Enhanced Statistics:** Realistic breakdown of partial fulfillment results
- **Better Usability:** Logical information flow and user-friendly design

### 11.5 **🆕 Technical Enhancements**
- **Variable Scope Fixes:** Resolved JavaScript errors for multiple date processing
- **Enhanced Debugging:** Improved logging for stock and flight data loading
- **Accurate Stock Tracking:** Fixed "Stock Before/After" columns to show real consumption
- **Performance Optimization:** Efficient multi-date calculation and display

### 11.6 **Business Benefits**
- ✅ **Maximized Revenue:** Fulfill every possible order to maximum extent
- ✅ **Improved Customer Satisfaction:** Clear communication of delivery schedules
- ✅ **Inventory Optimization:** Use all available stock and future flights
- ✅ **Operational Efficiency:** Detailed planning for warehouse operations
- ✅ **Realistic Expectations:** Accurate delivery date communication
- ✅ **Partial Shipments:** Enable delivery of available quantities without waiting
- ✅ **Accurate Flight Processing:** Proper sequence from departure → arrival → customs → pulling
- ✅ **Realistic Weekend Handling:** Stock deliveries move to previous working day for weekend requests
- ✅ **Enhanced Flight Restrictions:** Weekend departures properly restrict Monday pulling
- ✅ **Improved Date Accuracy:** All date calculations respect Taiwan business calendar and customs processing

## 12. BROWSER-SPECIFIC FEATURES

### 12.1 **File Processing**
- **Multi-file Upload:** Select all order files simultaneously
- **Enhanced Validation:** Real-time feedback with improved error handling
- **Progress Tracking:** Visual progress with detailed multi-phase processing
- **Memory Management:** Efficient handling of large Excel files with complex date calculations

### 12.2 **Interactive Elements**
- **🆕 Multiple Date Display:** Hover expansion for viewing complete delivery schedules
- **Enhanced Responsive Design:** Optimal display across all device sizes
- **Improved Color Coding:** Enhanced visual indicators for partial fulfillment status
- **Advanced Tooltips:** Detailed information for complex multi-date scenarios

### 12.3 **🆕 Enhanced Results Display**
- **Realistic Summary Statistics:** Based on actual partial fulfillment results
- **Multiple Date Formatting:** Professional display of complex delivery schedules
- **Priority Column Order:** Essential information immediately visible
- **Interactive Debugging:** Accessible processing log for troubleshooting

### 12.4 **Data Export**
- **Enhanced Downloads:** Browser's native download with improved filename structure
- **Multiple Date Export:** Complete preservation of complex delivery schedules
- **Column Reordering:** Excel export matches browser display priorities
- **Professional Formatting:** Business-ready output for customer communication

##13. RESULT COMPARISON AND VALIDATION
-------------------------------------

### 13.1 Expected Changes After Code Updates

When comparing before/after results, the following changes are expected and indicate correct system behavior:

**Stock Delivery Date Changes (EXPECTED):**

-   Orders with any status containing "Stock" (In Stock, Partial Stock Only, Partial Stock + Flight)
-   Orders with delivery dates containing "(stock)" identifier
-   Weekend/holiday requests: dates move backward to previous working day
-   Working day requests: typically no change (unless buffer days configured)
-   Examples: Saturday request 2025-12-13 → Friday delivery 2025-12-12

**Flight Delivery Date Changes (EXPECTED):**

-   Orders with Flight Qty Used > 0 or delivery dates containing "(flight)"
-   All flight-based dates recalculated using departure-based logic with customs clearance
-   Weekend flight departures: pulling dates avoid Monday, use Wednesday
-   Flight processing sequence: departure → arrival → customs → pulling → delivery
-   Examples: Saturday departure → Wednesday pulling (Monday skipped)

**No Changes Expected (UNCHANGED):**

-   Orders with status "No Inventory Available" (no inventory = no date calculations)
-   Orders with status "Invalid Date" (unparseable dates remain invalid)
-   All quantity fields (Required Qty, Stock Used, Flight Qty Used, Available Qty, etc.)
-   Input data fields (PO, Item, Part ID, Request Date, PO Create Date)
-   Order processing sequence and allocation amounts

### 13.2 Comparison Tool Classification Logic

For automated comparison tools, use this enhanced logic to properly classify changes:

```
static String determineChangeType(String field, String beforeValue, String afterValue, String status) {
    // Expected changes for any orders using stock
    if ("Estimated Delivery Dates".equals(field)) {
        // Any status that contains "Stock" or has "(stock)" in delivery dates
        if (status.contains("Stock") ||
            beforeValue.contains("(stock)") ||
            afterValue.contains("(stock)")) {
            return "EXPECTED-STOCK";
        }

        // Flight-based changes
        if (status.contains("Flight") || status.contains("Awaiting") ||
            beforeValue.contains("(flight)") || afterValue.contains("(flight)")) {
            return "EXPECTED-FLIGHT";
        }
    }

    // Pulling date changes (always flight-related)
    if ("Pulling Dates".equals(field)) {
        return "EXPECTED-FLIGHT";
    }

    // Fields that should never change (data corruption indicators)
    if (Arrays.asList("Order Number", "PO", "Item", "Part ID", "Required Qty",
                     "Request Date", "PO Create Date").contains(field)) {
        return "ERROR";
    }

    return "UNEXPECTED";
}

```

### 13.3 Validation Summary Guidelines

**Successful Validation Indicators:**

-   Total orders processed remains identical (no data loss)
-   "No Inventory Available" and "Invalid Date" orders unchanged
-   Stock delivery date changes follow backward adjustment pattern
-   Flight delivery date changes reflect new departure-based logic
-   All quantity allocations remain mathematically consistent
-   Input data fields remain completely unchanged

**Warning Indicators:**

-   Changes to "No Inventory Available" status orders
-   Changes to "Invalid Date" status orders
-   Modifications to quantity fields without corresponding status changes
-   Alterations to input data fields (PO, Item, Part ID, dates)

**Error Indicators:**

-   Total order count changes
-   Mathematical inconsistencies in allocations
-   Stock/flight quantity consumption errors
-   Corrupted input data fields

### 13.4 Business Logic Verification

**Date Calculation Verification:**

-   Stock deliveries on weekends move to Friday (previous working day)
-   Flight departures on weekends cannot pull Monday (use Wednesday)
-   All Taiwan holidays properly excluded from working day calculations
-   Customs clearance properly integrated (1 business day, Monday-Saturday)

**Partial Fulfillment Verification:**

-   No orders completely skipped due to insufficient inventory
-   Maximum possible fulfillment achieved using all available sources
-   Multiple delivery dates properly calculated and displayed
-   Stock consumption tracking accurate throughout FIFO sequence

This section provides comprehensive guidance for validating the enhanced partial fulfillment system and ensuring all changes align with business requirements.
---

**Current Version:** This Partial Fulfillment Edition v2.8 represents a major advancement in order processing capabilities. The system now fulfills orders to the maximum extent possible using all available inventory sources, provides detailed multiple delivery date tracking, and implements enhanced multi-level sorting for fair order prioritization. 

**Key Innovation:** The implementation of partial fulfillment logic with multiple delivery date support enables businesses to maximize order fulfillment and provide accurate delivery schedules for complex orders spanning multiple

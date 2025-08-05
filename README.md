# Delivery Date Estimator - Requirements Specification

**Version:** 2.6.1 (Real FIFO Edition with Professional Excel Export)  
**Date:** August 2025  
**Platform:** Modern Web Browsers (Chrome, Firefox, Safari, Edge)  
**Libraries:** SheetJS (XLSX.js) Community Edition for Excel file processing  
**Deployment:** Single HTML file - no installation required  
**Calendar:** Taiwan ROC Year 114 (2025) Official Business Calendar  
**Key Updates:** Professional Excel export with auto-sized columns, zero flight buffer default maintained, PO Create Date column for enhanced order tracking

## 1. SYSTEM OVERVIEW

A browser-based JavaScript application that processes customer orders using **REAL FIFO allocation logic** to estimate delivery dates based on stock availability and flight arrival schedules. The system reads three Excel files client-side and produces delivery estimates with detailed allocation tracking, comprehensive unparseable data handling, **warehouse pulling rules** integrated with **Taiwan's official 2025 business calendar**, **zero buffer days default** for same-day in-stock delivery, **zero flight buffer default** for accepting flights arriving on the same day as order date, and **professional Excel export with auto-sized columns**.

**Key Advantages:**
- **Zero Installation**: Runs directly in web browser
- **Real FIFO Logic**: Actual inventory allocation with consumption tracking
- **Zero Buffer Defaults**: Same-day delivery for in-stock orders and same-day flight acceptance
- **Enhanced Order Tracking**: PO Create Date column for complete order lifecycle visibility
- **Professional Excel Export**: Auto-sized columns for optimal readability across all spreadsheet applications
- **Fixed Flight Dates**: Proper YYYY-MM-DD format instead of Excel serial numbers
- **Client-Side Processing**: Files never leave user's computer
- **Cross-Platform**: Works on Windows, Mac, Linux, tablets, phones
- **No Dependencies**: Single HTML file contains everything needed
- **Modern UI**: Responsive design with real-time progress tracking
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
  - `po create date` - **NEW:** Purchase order creation date for order tracking
  - `po` - Purchase order number
  - `item` - Item description
- **Notes:** 
  - Date parsing handles YYYYMMDD strings, Excel Date objects, and Excel serial numbers
  - **Unparseable dates** (e.g., "cancel", "pending", "N/A") are preserved and orders are skipped
  - Part numbers may contain prefixes that need restricted fuzzy matching
  - Customer code extracted from filename (e.g., "HZ4600" from "order_HZ4600.xlsx")
  - System automatically processes all uploaded order files
  - **PO Create Date tracking** enables complete order lifecycle analysis

### 2.2 Part Stock (`part_stock.xlsx`)
- **Purpose:** Current inventory levels
- **Upload Method:** Single file selection
- **Key Columns:**
  - `part no` - Part identifier
  - `qty` - Available stock quantity
- **Notes:** 
  - **Real stock checking** - system validates actual availability
  - **Inventory consumption** - stock levels decrease as orders are allocated
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

### 3.1 **🆕 REAL FIFO Allocation System**
- **Primary Rule:** Orders processed in chronological order by `request date` (earliest first)
- **Actual Inventory Tracking:** Real stock and flight quantities are consumed as orders are allocated
- **Inventory Consumption:** Each fulfilled order reduces available stock/flight quantities for subsequent orders
- **Realistic Allocation:** Orders can only be fulfilled if sufficient inventory actually exists
- **Status Accuracy:** Proper classification based on actual availability
- **Skip Logic:** Orders that cannot be fulfilled are marked as "Skipped" and inventory remains unchanged

### 3.2 **🆕 Flight Acceptance Criteria (Zero Buffer Default)**
- **Early Flights:** Always accept flights arriving before order date (no limit)
- **Same-Day Flights:** **DEFAULT:** Accept flights arriving on the same day as order date (flight buffer = 0)
- **Late Flights:** Accept flights arriving after order date only if ≤ configurable days late (default: 0 days)
- **Unparseable Dates:** Flights with unparseable dates are excluded from allocation
- **Buffer Configuration:** `flightBufferDays` (user configurable via UI, default: 0)
- **Example:** Order date 2025-06-24, Buffer = 0 days (DEFAULT)
  - Flight 2025-06-12: ✅ Accept (12 days early)
  - Flight 2025-06-24: ✅ Accept (same day - DEFAULT)
  - Flight 2025-06-25: ❌ Reject (1 day late, exceeds 0-day buffer)
  - Flight "NA": ❌ Reject (unparseable date)

### 3.3 Restrictive Part Number Matching
- **Purpose:** Handle part numbers with "-A" postfix variations only
- **Rules:** 
  1. **Exact match:** `part_12345678` = `part_12345678` (case insensitive)
  2. **Postfix match:** `part_12345678` ↔ `part_12345678-A` (bidirectional)
  3. **No other fuzzy matching** - eliminates false positives
- **Example:** Order "part_12345678" matches stock "part_12345678-A"
- **Note:** Previous substring/partial matching removed to prevent mismatches

### 3.4 Enhanced Unparseable Date Handling
- **Order Dates:** Preserve original string (e.g., "cancel", "pending") and skip order
- **Flight Dates:** Preserve original string (e.g., "NA", "TBD") and exclude from allocation
- **PO Create Dates:** **NEW:** Preserve unparseable PO create dates for tracking
- **No Auto-Conversion:** Never convert unparseable dates to current date
- **Clear Logging:** Shows exactly what was unparseable in processing log
- **Display Format:** Flight dates show proper YYYY-MM-DD format in results

### 3.5 **🆕 Zero Buffer Days Default**
**In-Stock Order Delivery:**
- **Default Buffer:** 0 days (same-day delivery)
- **Working Day Check:** If order date falls on weekend/holiday, delivery moves to next working day
- **Configurable:** Users can increase buffer days if needed
- **Examples:**
  - Order on Monday → Delivery on Monday (same day)
  - Order on Saturday → Delivery on Monday (next working day)
  - Order on Taiwan holiday → Delivery on next working day

### 3.6 **Enhanced Taiwan Working Calendar Integration**
The system uses Taiwan's official ROC Year 114 (2025) business calendar for all date calculations.

**Taiwan Working Days:**
- **Standard Working Days:** Monday through Friday
- **Weekends:** Saturday and Sunday are non-working days
- **Holiday System:** Taiwan national holidays are excluded from working day calculations
- **Timezone Handling:** All date calculations use local time to prevent date shifting

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

**Working Day Calculation Impact:**
- All warehouse pulling date calculations respect Taiwan holidays
- Flight arrival lead times account for holiday periods
- Delivery date calculations skip holidays and weekends
- Chinese New Year period (1/27-1/31) requires extended lead times

### 3.7 **Enhanced Warehouse Pulling Rules**
Flight arrival dates represent when parts arrive at the airport. Parts must then be pulled into the warehouse before delivery can occur.

**Pulling Constraints:**
1. **Only Monday and Wednesday** are allowed for warehouse pulling operations
2. **Cannot pull on the last day of the month** (operational constraint)
3. **Must be a Taiwan working day** (excludes weekends and Taiwan holidays)
4. **Lead time:** Minimum configurable Taiwan working days between flight arrival and pulling eligibility (default: 2 days)

**Enhanced Pulling Date Calculation Process:**
1. **Start Date:** Flight arrival date at airport
2. **Add Lead Time:** + configurable Taiwan working days (excludes weekends and holidays)
3. **Find Valid Day:** Next Monday or Wednesday from candidate date
4. **Working Day Check:** Ensure selected date is a Taiwan working day (not holiday)
5. **Month End Check:** If last day of month, move to next valid pulling day
6. **Final Pulling Date:** Confirmed warehouse pulling date

### 3.8 **Enhanced Delivery Date Calculation**
The delivery calculation depends on order fulfillment method and uses Taiwan working calendar:

**In-Stock Orders:**
- **Formula:** `Delivery Date = Order Date + Buffer Days (default: 0) + Weekend/Holiday Adjustment`
- **Buffer Days:** Default 0 (configurable via UI)
- **Weekend/Holiday Handling:** Skip Saturday/Sunday and Taiwan holidays if delivery falls on non-working day
- **No warehouse pulling required** (direct from existing stock)
- **Same-day delivery** when buffer = 0 and order date is working day

**Flight-Based Orders:**
- **Formula:** `Delivery Date = Pulling Date + Delivery Lead Days (Taiwan Working Days)`
- **Taiwan Working Days:** Excludes weekends and Taiwan holidays
- **No additional weekend skipping needed** (already handled in Taiwan working day calculation)
- **Pulling Date:** Calculated using enhanced warehouse pulling rules with Taiwan calendar

**Hybrid Orders (Partial Stock + Flight):**
- **Uses flight-based calculation** (delivery driven by last required flight)
- **Formula:** Same as flight-based orders
- **Rationale:** Cannot deliver until all parts (including flights) are available

## 4. USER INTERFACE SPECIFICATIONS

### 4.1 **File Upload Interface**
- **Visual Design:** Card-based upload areas with drag-and-drop styling
- **File Validation:** Real-time feedback on file selection
- **Progress Indicators:** File size display and upload confirmation
- **Multiple Files:** Support for multiple order files in single selection
- **File Type Validation:** Accepts .xlsx and .xls files only

### 4.2 **🆕 Configuration Panel**
- **Interactive Controls:** Number inputs with validation and reasonable ranges
- **Real-time Updates:** Changes apply immediately to next calculation
- **Configurable Parameters:**
  - **Buffer Days (In-Stock Orders): 0-30 days, default 0**
  - **Flight Buffer Days (Max Late): 0-30 days, default 0**
  - Pulling Lead Days: 1-10 days, default 2
  - Delivery Lead Days: 1-10 days, default 2

### 4.3 **Processing Interface**
- **Progress Bar:** Real-time visual progress during file processing
- **Enhanced Processing Log:** Live console-style log with timestamps and detailed allocation decisions
- **Status Updates:** Clear indication of current processing step
- **FIFO Indicators:** Shows order processing sequence and inventory consumption
- **Error Display:** User-friendly error messages with auto-dismiss

### 4.4 **🆕 Enhanced Results Display**
- **Summary Statistics Dashboard:** Key metrics in visual cards including realistic allocation breakdown
- **Interactive Table:** Sortable results with hover effects and proper flight date formatting
- **Status Color Coding:** Visual distinction between order statuses
- **Flight Date Column:** Shows formatted dates like `"2000×2025-08-15, 1000×2025-08-20"`
- **PO Create Date Column:** **NEW:** Displays PO creation dates for order tracking
- **Responsive Design:** Adapts to different screen sizes
- **Tooltip Support:** Full text on hover for truncated cells

## 5. OUTPUT REQUIREMENTS

### 5.1 **🆕 Enhanced Browser Results Table**
**Displayed Columns:**
- Order #, Customer, Part ID, PO, Item, Req Qty, Alloc Qty
- **Request Date, PO Create Date**, **Pulling Date**, Delivery Date, Lead Days, Status
- Stock Before, Stock Used, Flight Qty, **Flight Dates Used**, **Warehouse Notes**

**Interactive Features:**
- **Sortable Columns:** Click headers to sort data
- **Status Color Coding:** Visual indicators for order status
- **Fixed Flight Dates:** Proper YYYY-MM-DD format display
- **Hover Details:** Full warehouse notes and flight dates on hover
- **PO Create Date Display:** **NEW:** Shows purchase order creation dates
- **Responsive Layout:** Horizontal scroll on smaller screens

### 5.2 **🆕 Professional Excel Download File (`delivery_estimates_REAL_FIFO_TIMESTAMP.xlsx`)**
**Complete Columns:**
- Order Number, Cust Code, PO, Item, Part ID, Required Qty, Available Qty
- **Request Date, PO Create Date**, **Pulling Date**, Estimated Delivery, Lead Time (Days), Status, Notes
- Stock Before, Stock After, Stock Used, Flight Qty Used
- **Flight Dates Used**, **Flight Dates Not Used**, **Warehouse Notes**

**File Features:**
- **Auto-sizing Columns:** Intelligent column width calculation based on content length
- **Professional Layout:** Clean, readable format suitable for business reporting
- **Cross-Platform Compatibility:** Works with Excel 2016+, Microsoft 365, LibreOffice, and Google Sheets
- **Timestamp Filename:** Includes "REAL_FIFO" indicator and prevents overwriting
- **Complete Data:** All processing details included with proper date formatting
- **Fixed Flight Dates:** Shows `"2000×2025-08-15"` instead of `"2000×45806"`
- **PO Create Date Export:** **NEW:** Includes PO creation dates in Excel output
- **Optimized File Size:** Efficient Excel generation without unnecessary formatting overhead

### 5.3 **🆕 Enhanced Status Values**
- **"In Stock"** - Fulfilled entirely from existing stock (no warehouse pulling, zero buffer default)
- **"Partial Stock + Flight"** - Combination of stock and flight arrivals (uses warehouse pulling)
- **"Awaiting Flight"** - Fulfilled entirely from flight arrivals (uses warehouse pulling)
- **"Skipped"** - Insufficient inventory based on real availability check

### 5.4 **Summary Statistics**
**Real-time Dashboard:**
- Total Orders, Fulfilled, Skipped
- In Stock, Need Flights, Warehouse Pulling
- Customer breakdown by customer code
- **Realistic allocation breakdown** based on actual inventory availability

### 5.5 **🆕 Enhanced Warehouse Notes Format**
**Purpose:** Show complete warehouse workflow for flight-based orders with proper date formatting

**In-Stock Orders:**
- `"Direct from stock, same-day delivery"` (when buffer = 0)
- `"Direct from stock, X buffer day(s) applied"` (when buffer > 0)

**Flight-Based Orders:**
- `"Flight arrival: 2025-08-06 → Pulling: 2025-08-12 → Delivery: 2025-08-14"`

**Skipped/Invalid Orders:**
- `"Invalid order date"` or `"Order skipped due to insufficient inventory"`

### 5.6 **🆕 Enhanced Processing Log**
**Real-time Console with Detailed FIFO Tracking:**
- Timestamped entries for all processing steps
- File loading progress and statistics
- **FIFO allocation decisions:** Shows why orders were fulfilled/skipped
- **Inventory consumption tracking:** Real-time stock/flight level updates
- **Part matching results:** Shows fuzzy matching decisions
- Error messages and warnings with specific details
- Final summary statistics with realistic breakdown

## 6. TECHNICAL SPECIFICATIONS

### 6.1 **Browser Compatibility**
- **Supported Browsers:** Chrome 90+, Firefox 88+, Safari 14+, Edge 90+
- **JavaScript Features:** ES6+, FileReader API, Blob API
- **CSS Features:** Grid, Flexbox, CSS Variables, Hover Effects
- **No Plugins Required:** Pure HTML/CSS/JavaScript

### 6.2 **🆕 Enhanced File Processing Technology**
```javascript
// Excel file reading using SheetJS Community Edition with enhanced error handling
const workbook = XLSX.read(data, { type: 'array' });
const sheet = workbook.Sheets[workbook.SheetNames[0]];
const data = XLSX.utils.sheet_to_json(sheet, { header: 1, defval: '' });

// Professional Excel export with auto-sizing
function downloadResults() {
    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.json_to_sheet(excelData);
    
    // Auto-size columns based on content
    const cols = [];
    const headers = Object.keys(excelData[0]);
    headers.forEach((header, i) => {
        let maxLen = header.length;
        excelData.forEach(row => {
            const cellValue = String(row[header] || '');
            maxLen = Math.max(maxLen, cellValue.length);
        });
        cols.push({ width: Math.min(Math.max(maxLen + 2, 10), 50) });
    });
    ws['!cols'] = cols;
    
    XLSX.utils.book_append_sheet(wb, ws, 'Delivery Estimates');
    XLSX.writeFile(wb, filename);
}

// Enhanced date parsing with proper formatting and PO Create Date support
function parseDate(dateValue) {
    // Handle Excel serial dates (numbers) with timezone fix
    if (typeof dateValue === 'number') {
        const excelEpoch = new Date(1899, 11, 30);
        return new Date(excelEpoch.getTime() + dateValue * 24 * 60 * 60 * 1000);
    }
    
    // Handle YYYYMMDD format
    if (/^\d{8}$/.test(dateStr)) {
        const year = parseInt(dateStr.substring(0, 4));
        const month = parseInt(dateStr.substring(4, 6)) - 1;
        const day = parseInt(dateStr.substring(6, 8));
        return new Date(year, month, day);
    }
    
    // Handle unparseable values with preservation (including PO create dates)
    if (dateStr.toLowerCase().match(/^(na|n\/a|null|cancel|pending|tbd)$/)) {
        return null; // Preserved as unparseable
    }
}

// Fixed timezone-safe date formatting for display
function formatDateLocal(date) {
    if (!date) return 'N/A';
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}
```

### 6.3 **🆕 Real FIFO Allocation Implementation**
```javascript
// Real inventory allocation with consumption tracking
function tryAllocateInventory(order, availableStock, availableFlights) {
    const stockPartId = findBestMatch(order.partId, availableStock);
    const flightPartId = findBestMatch(order.partId, availableFlights);
    
    const stockOnHand = stockPartId ? (availableStock[stockPartId] || 0) : 0;
    const flights = flightPartId ? (availableFlights[flightPartId] || []) : [];
    
    console.log(`🔍 Allocating ${order.partId}: stock=${stockOnHand}, flights=${flights.length}`);
    
    // Check if stock alone is sufficient
    if (stockOnHand >= order.requiredQuantity) {
        return {
            canFulfill: true,
            status: 'In Stock',
            // ... detailed allocation tracking
        };
    }
    
    // Combine stock + flights with detailed tracking
    // ... complex FIFO flight allocation logic with 0-day buffer default
}

// Apply allocation to consume inventory
function applyAllocation(allocation, remainingStock, remainingFlights) {
    // Consume stock
    for (const [partId, used] of Object.entries(allocation.stockUsed)) {
        remainingStock[partId] -= used;
        console.log(`📦 Stock consumed: ${partId} -${used}`);
    }
    
    // Consume flight quantities
    for (const [partId, usedFlights] of Object.entries(allocation.flightUsed)) {
        // ... detailed flight consumption tracking
    }
}
```

### 6.4 **Enhanced Taiwan Working Calendar Implementation**
```javascript
// Taiwan working day checker with zero buffer support
function isTaiwanWorkingDay(date) {
    const dayOfWeek = date.getDay(); // 0 = Sunday, 6 = Saturday
    const dateString = formatDateLocal(date); // Timezone-safe formatting
    
    // Weekend check
    if (dayOfWeek === 0 || dayOfWeek === 6) {
        return false;
    }
    
    // Holiday check - Taiwan 2025 holidays
    if (TAIWAN_2025_HOLIDAYS.includes(dateString)) {
        return false;
    }
    
    return true;
}

// Add Taiwan working days with holiday awareness
function addWorkingDays(startDate, workingDays) {
    let current = new Date(startDate);
    let daysAdded = 0;
    
    while (daysAdded < workingDays) {
        current.setDate(current.getDate() + 1);
        if (isTaiwanWorkingDay(current)) {
            daysAdded++;
        }
    }
    
    return current;
}
```

### 6.5 **🆕 Enhanced Configuration Management**
```javascript
// Configuration object with zero flight buffer default
let config = {
    bufferDays: 0,              // Default: same-day delivery for in-stock
    flightBufferDays: 0,        // DEFAULT: same-day flight acceptance
    pullingLeadDays: 2,         // From UI input #pulling-lead-days
    deliveryLeadDays: 2,        // From UI input #delivery-lead-days
    skipWeekends: true          // Fixed to true
};

// Real-time config updates before processing
function updateConfigFromUI() {
    config.bufferDays = parseInt(document.getElementById('buffer-days').value) || 0;
    config.flightBufferDays = parseInt(document.getElementById('flight-buffer-days').value) || 0;
    config.pullingLeadDays = parseInt(document.getElementById('pulling-lead-days').value) || 2;
    config.deliveryLeadDays = parseInt(document.getElementById('delivery-lead-days').value) || 2;
    
    console.log('📊 Config updated:', config);
}
```

## 7. ALLOCATION ALGORITHM

### 7.1 **🆕 Enhanced Processing Flow (Real FIFO Implementation)**
1. **File Upload & Validation:** User selects files, system validates file types and displays info
2. **Configuration Update:** Read current UI settings before processing (including 0-day flight buffer)
3. **Data Loading:** Parse Excel files using SheetJS library with progress tracking (including PO create dates)
4. **Order Sorting:** Sort by request date (ascending) - invalid dates processed last
5. **Inventory Initialization:** Create working copies of stock and flight data for consumption tracking
6. **REAL FIFO Allocation:** Process each order chronologically with actual inventory checking (0-day flight buffer)
7. **Inventory Consumption:** Each fulfilled order reduces available stock/flights for subsequent orders
8. **Results Generation:** Calculate delivery dates with warehouse pulling rules using Taiwan calendar
9. **UI Update:** Display results table and summary statistics with realistic breakdown (including PO create dates)
10. **Download Preparation:** Format data for professional Excel export with auto-sized columns (including PO create dates)

### 7.2 **🆕 Real Inventory Allocation Logic**
1. **Part Matching:** Use restrictive fuzzy matching (exact + "-A" postfix only)
2. **Stock Checking:** Verify actual stock availability from current inventory levels
3. **Stock First:** Use available stock up to required quantity
4. **Flight Addition:** Add flights meeting acceptance criteria (parseable dates + 0-day buffer timing)
5. **Validation:** Verify sufficient combined inventory exists
6. **Allocation:** Deduct consumed quantities from working inventory for next orders
7. **Status Classification:** Accurate status based on actual fulfillment method
8. **Tracking:** Record detailed allocation information for transparency (including PO create dates)

### 7.3 **Enhanced Real-time Processing Feedback**
- **Progress Bar:** Visual indication of processing percentage
- **Live Log:** Console-style updates with timestamps and FIFO decisions
- **Inventory Tracking:** Shows stock/flight consumption in real-time
- **Error Handling:** User-friendly error messages with specific details
- **Performance:** Client-side processing with detailed allocation logging

## 8. ERROR HANDLING & VALIDATION

### 8.1 **File Validation**
- **File Type Check:** Ensure .xlsx/.xls extensions
- **File Size Warning:** Display file sizes to user
- **Empty File Detection:** Warn if files appear empty
- **Column Detection:** Flexible header matching with fallbacks (including PO create date)

### 8.2 **🆕 Enhanced Data Validation**
- **Date Parsing:** Support for multiple date formats with unparseable date preservation
- **PO Create Date Analysis:** Enables comparison between PO creation and request dates for lead time analysis
- **Export Integration:** PO Create Date included in all output formats for comprehensive reporting
- **Graceful handling of multiple date formats with timezone safety (including PO create dates)**
- **Numeric Validation:** Convert and validate quantity fields
- **Stock Availability:** Real-time checking of actual inventory levels
- **Flight Date Formatting:** Ensure proper YYYY-MM-DD display format
- **Missing Data:** Use fallback values where appropriate

### 8.3 **User Error Feedback**
```javascript
// Enhanced error display with FIFO context
function showError(message) {
    const errorDiv = document.createElement('div');
    errorDiv.className = 'error';
    errorDiv.innerHTML = `<strong>Error:</strong> ${message}`;
    
    document.querySelector('.content').appendChild(errorDiv);
    setTimeout(() => errorDiv.remove(), 10000); // Auto-dismiss after 10 seconds
}
```

### 8.4 **🆕 Enhanced Processing Validation**
- **Allocation Math:** Verify stock + flights = total allocated
- **FIFO Verification:** Log processing sequence for verification with inventory consumption
- **Inventory Tracking:** Monitor stock levels throughout processing with detailed logging
- **Date Logic:** Validate warehouse pulling date calculations with Taiwan calendar
- **Flight Date Display:** Ensure all flight dates show in YYYY-MM-DD format
- **PO Create Date Tracking:** **NEW:** Validate and preserve PO creation date information

## 9. DEPLOYMENT & USAGE

### 9.1 **Deployment Model**
- **Single File:** Complete application in one HTML file (delivery_estimator_v2.6.1_REAL_FIFO.html)
- **CDN Dependencies:** SheetJS Community Edition loaded from reliable CDN
- **No Server Required:** Runs entirely client-side
- **Distribution:** Email, web hosting, or file sharing
- **Version Control:** Self-contained versioning within filename

### 9.2 **🆕 Enhanced User Instructions**
1. **Open File:** Double-click HTML file or open in browser
2. **Upload Files:** Select all required Excel files (orders, stock, flights)
3. **Configure Settings:** Adjust parameters if needed (buffer days default = 0, flight buffer default = 0)
4. **Process:** Click "Calculate Delivery Dates" button with REAL FIFO logic
5. **Monitor Progress:** Watch detailed processing log with allocation decisions
6. **Review Results:** Check realistic summary statistics and order details with proper flight dates and PO create dates
7. **Download:** Save professional Excel results file with auto-sized columns and REAL_FIFO timestamp

### 9.3 **System Requirements**
- **Browser:** Any modern browser (see compatibility list)
- **Memory:** Sufficient RAM for file processing (typically 100MB+ available)
- **Storage:** Temporary space for file downloads
- **Network:** Internet connection for initial CDN library loading only
- **File System:** Local file access for Excel download

## 10. VALIDATION CHECKLIST

When verifying browser-based results, check:
- [ ] **File Upload:** All three file types upload successfully with visual confirmation
- [ ] **Configuration:** UI settings properly affect calculations (zero buffer and flight buffer defaults)
- [ ] **Date Processing:** Orders sorted chronologically by request date
- [ ] **PO Create Date:** **NEW:** PO creation dates are properly parsed and displayed
- [ ] **Unparseable Dates:** Orders with invalid dates ("cancel", "NA") are skipped with preserved original strings
- [ ] **Real Stock Tracking:** Stock levels decrease correctly with each fulfilled order
- [ ] **Flight Date Format:** All flight dates display as YYYY-MM-DD instead of Excel serial numbers
- [ ] **Flight Acceptance:** Early flights always accepted, same-day flights accepted (0-day buffer), late flights rejected if > configured days
- [ ] **REAL FIFO Allocation:** Earlier orders consume inventory first, affecting later orders
- [ ] **Status Accuracy:** Mix of statuses (In Stock, Partial Stock + Flight, Awaiting Flight, Skipped) based on actual availability
- [ ] **Taiwan Calendar Integration:** All date calculations use Taiwan working calendar
- [ ] **Taiwan Holiday Exclusion:** Working day calculations skip all Taiwan holidays
- [ ] **Enhanced Pulling Logic:** Pulling dates respect both Mon/Wed rule and Taiwan working days
- [ ] **Holiday Period Handling:** Extended lead times during Chinese New Year and other holiday periods
- [ ] **Zero Buffer Defaults:** In-stock orders deliver same day or next working day (when buffer = 0), flights accepted same day (when flight buffer = 0)
- [ ] **Part Matching:** Only exact match or "-A" postfix variations allowed
- [ ] **Results Display:** Summary statistics and table display correctly with realistic breakdown (including PO create dates)
- [ ] **Excel Downloa

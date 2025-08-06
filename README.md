# 📦 Delivery Date Estimator v2.7 - Partial Fulfillment Edition
# 📦 交貨日期估算器 v2.7 - 部分履約版本

*[中文版本](#中文版本) | [English Version](#english-version)*

---

## English Version

### 🚀 Overview

A browser-based JavaScript application that processes customer orders using **Partial Fulfillment allocation logic** to estimate delivery dates based on stock availability and flight arrival schedules. The system maximizes order fulfillment by utilizing all available inventory sources and provides detailed multiple delivery date tracking.

### ✨ Key Features

- **🔄 Partial Fulfillment Logic**: Fulfills orders to maximum extent possible - no orders skipped
- **📅 Multiple Delivery Dates**: Individual tracking for each flight used in order fulfillment  
- **🎯 Enhanced FIFO Sorting**: Primary sort by request date, secondary by PO create date
- **✈️ Future Flight Utilization**: Uses any available future flights regardless of timing
- **📊 Real-time Stock Tracking**: Accurate inventory consumption through FIFO processing
- **🏢 Taiwan Business Calendar**: Integrated with ROC Year 114 (2025) official holidays
- **💻 Zero Installation**: Runs directly in web browser - no software installation required
- **🔒 Client-side Processing**: Files never leave your computer - enhanced security
- **📱 Cross-platform**: Works on Windows, Mac, Linux, tablets, and phones

### 🎯 Perfect For

- **Supply Chain Managers**: Optimize inventory allocation and delivery planning
- **Customer Service Teams**: Provide accurate delivery dates for partial shipments
- **Warehouse Operations**: Coordinate pulling schedules across multiple flights
- **Procurement Teams**: Track PO fulfillment across complex inventory sources

### 📋 Input Files Required

| File Type | Pattern | Description |
|-----------|---------|-------------|
| **Customer Orders** | `order_*.xlsx` | Customer order requests with quantities and dates |
| **Part Stock** | `part_stock.xlsx` | Current inventory levels for all parts |
| **Flight Information** | `onflight.xlsx` | Scheduled part arrivals with dates and quantities |

### 📊 Output Features

- **Priority Column Order**: Essential information displayed first
- **Multiple Date Display**: `"300×2025-08-01 (stock), 500×2025-08-15 (flight)"`
- **Hover Expansion**: Full details on hover for complex orders
- **Real-time Statistics**: Accurate fulfillment breakdown
- **Professional Export**: Business-ready Excel files

### 💡 What's New in v2.7

- ✅ **No More Skipped Orders**: Every order fulfilled to maximum extent possible
- ✅ **Multiple Delivery Dates**: Individual tracking for each flight used
- ✅ **Enhanced Sorting**: Request date → PO create date → order number
- ✅ **Future Flight Usage**: Accept all future flights without time restrictions
- ✅ **Improved UI**: Priority information first, better column organization
- ✅ **Accurate Stock Tracking**: Fixed stock before/after progression

### 📖 Documentation

- **[English Specification](spec/en.md)**: Complete technical documentation
- **[中文規格書](spec/zh.md)**: 完整技術文件

### 🛠️ Technical Details

- **Technology**: Pure HTML/CSS/JavaScript with SheetJS for Excel processing
- **Browser Support**: Chrome 90+, Firefox 88+, Safari 14+, Edge 90+
- **File Formats**: .xlsx and .xls support
- **Processing**: Client-side only - no server required
- **Memory Usage**: Optimized for large datasets

### 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.


---

## 中文版本

### 🚀 系統概述

基於瀏覽器的JavaScript應用程式，使用**部分履約分配邏輯**處理客戶訂單，根據庫存可用性和航班抵達時程估算交貨日期。系統通過利用所有可用庫存來源最大化訂單履約，並提供詳細的多重交貨日期追蹤。

### ✨ 主要功能

- **🔄 部分履約邏輯**: 盡可能最大程度履行訂單 - 無訂單跳過
- **📅 多重交貨日期**: 訂單履約中每個航班的個別追蹤  
- **🎯 加強型FIFO排序**: 以需求日期為主要排序，PO建立日期為次要排序
- **✈️ 未來航班利用**: 使用任何可用的未來航班，不受時間限制
- **📊 即時庫存追蹤**: 通過FIFO處理準確的庫存消耗
- **🏢 台灣營業日曆**: 整合中華民國114年 (2025年) 官方假日
- **💻 零安裝**: 直接在網頁瀏覽器中運行 - 無需安裝軟體
- **🔒 客戶端處理**: 檔案絕不離開您的電腦 - 加強安全性
- **📱 跨平台**: 可在Windows、Mac、Linux、平板電腦和手機上運行

### 🎯 適用對象

- **供應鏈管理者**: 優化庫存分配和交貨規劃
- **客戶服務團隊**: 為部分出貨提供準確的交貨日期
- **倉庫營運**: 協調跨多個航班的提貨時程表
- **採購團隊**: 追蹤跨複雜庫存來源的PO履約

### 📋 所需輸入檔案

| 檔案類型 | 檔案格式 | 說明 |
|----------|----------|------|
| **客戶訂單** | `order_*.xlsx` | 包含數量和日期的客戶訂單需求 |
| **零件庫存** | `part_stock.xlsx` | 所有零件的目前庫存水準 |
| **航班資訊** | `onflight.xlsx` | 預定零件抵達日期和數量 |

### 📊 輸出功能

- **優先欄位順序**: 重要資訊首先顯示
- **多重日期顯示**: `"300×2025-08-01 (現貨), 500×2025-08-15 (航班)"`
- **懸停展開**: 複雜訂單的完整詳情懸停顯示
- **即時統計**: 準確的履約細分
- **專業匯出**: 業務就緒的Excel檔案

### 💡 v2.7 新功能

- ✅ **不再跳過訂單**: 每個訂單都盡可能最大程度履行
- ✅ **多重交貨日期**: 每個使用航班的個別追蹤
- ✅ **加強型排序**: 需求日期 → PO建立日期 → 訂單號碼
- ✅ **未來航班使用**: 接受所有未來航班，無時間限制
- ✅ **改善的UI**: 優先資訊在前，更好的欄位組織
- ✅ **準確庫存追蹤**: 修正庫存前/後進展

### 📖 文件

- **[English Specification](spec/en.md)**: Complete technical documentation
- **[中文規格書](spec/zh.md)**: 完整技術文件

### 🛠️ 技術細節

- **技術**: 純HTML/CSS/JavaScript，使用SheetJS進行Excel處理
- **瀏覽器支援**: Chrome 90+, Firefox 88+, Safari 14+, Edge 90+
- **檔案格式**: 支援.xlsx和.xls
- **處理**: 僅客戶端 - 無需伺服器
- **記憶體使用**: 為大型資料集優化

### 📄 授權

此專案採用MIT授權 - 詳見[LICENSE](LICENSE)檔案。

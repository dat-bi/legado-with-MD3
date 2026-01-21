# Ghi chú thay đổi (log)

## 2026-01-17

- Khôi phục module dịch hoàn chỉnh: `TranslateConfig`, `TranslateCache`, `TranslateManager`.
- Thêm provider mới **SANGTACVIET** (API: `http://14.225.254.182/index.php?ngmar=trans&langhint=chinese`).
- Tích hợp dịch vào luồng đọc chương: dịch tiêu đề + nội dung trong `ReadBook.contentLoadFinish`.
- Khôi phục cấu hình dịch trong `AppConfig` + `PreferKey`.
- Thêm mục Cài đặt dịch (Bật dịch + Nhà cung cấp) trong `pref_config_other.xml`.
- Bổ sung `strings.xml` và `arrays.xml` cho UI dịch.
- Tạo script test API: `scripts/test_translate_api.js` (dichngay/dichnhanh/sangtacviet).

## Ghi chú vận hành

- Dịch chỉ áp dụng khi chuỗi có ký tự Hán.
- Provider mặc định: **DICHNGAY**.
- Bật dịch trong app: **Cài đặt → Cài đặt khác → Bật dịch**.

## Lưu ý

- Có cảnh báo lint PowerShell: `Call-Translate` là verb không chuẩn (không ảnh hưởng chạy).

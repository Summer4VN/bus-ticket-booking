package jX;

public class Session {
    // --- BIẾN TOÀN CỤC (GLOBAL VARIABLES) ---
    // 'public static': Biến này sống trong vùng nhớ Static (Heap), tồn tại suốt vòng đời ứng dụng.
    // Bất kỳ file nào thay đổi giá trị biến này, các file khác đều thấy giá trị mới đó.
    
    // Lưu username người dùng sau khi đăng nhập thành công.
    // Ví dụ: Khi đăng nhập xong, gán currentUsername = "sinhvienUIT".
    // Sang trang đặt vé, ta lấy giá trị này ra để biết vé đó là của "sinhvienUIT".
    public static String currentUsername = ""; 
    
    // Lưu vai trò: "ADMIN" hoặc "CUSTOMER".
    // Dùng để phân quyền: Nếu là ADMIN thì hiện nút "Quản lý", nếu là CUSTOMER thì ẩn đi.
    public static String currentRole = "";     
}

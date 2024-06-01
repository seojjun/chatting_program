module com.seojjun.chatting_program {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.seojjun.chatting_program to javafx.fxml;
    exports com.seojjun.chatting_program;
}
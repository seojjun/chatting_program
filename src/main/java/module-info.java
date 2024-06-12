module com.seojjun.chatting_program {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;


    exports com.seojjun.chatting_program.Client;
    opens com.seojjun.chatting_program.Client to javafx.fxml;
    exports com.seojjun.chatting_program.Server;
    opens com.seojjun.chatting_program.Server to javafx.fxml;
}
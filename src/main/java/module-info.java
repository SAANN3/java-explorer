module com.explorer {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.explorer to javafx.fxml;
    exports com.explorer;
    opens com.explorer.components.LeftColumn to javafx.fxml;
    opens com.explorer.components.MainView to javafx.fxml;
    opens com.explorer.components.MainView.File to javafx.fxml;
    opens com.explorer.components.TopBar to javafx.fxml;
}
package com.explorer.components.TopBar;

import com.explorer.components.MainView.MainView;
import com.explorer.components.Utils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class TopBar extends HBox {
    private static MainView mv ;
    @FXML private  Label currentFolderName;
    private static Label currentFolderNameStatic;
    public TopBar() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("topBar.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(TopBar.this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        getStyleClass().add("borderStyle");
        currentFolderNameStatic = currentFolderName;
    }
    @FXML private void backButton(){
        mv.goUndo();
    }
    @FXML private void forwardButton(){
        mv.goRedo();
    }
    @FXML private void upButton(){
        mv.goUpDirectory();
    }
    public static void setMV(MainView givenMV){
        mv = givenMV;
    }
    public static void setCurrentFolderName(String path){
        currentFolderNameStatic.setText(path);
    }

}
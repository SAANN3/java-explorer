package com.explorer.components.MainView.File;

import com.explorer.components.MainView.MainView;
import com.explorer.components.Utils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;


import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class FileType extends HBox {
    private  File file;
    private final boolean permissionWrite;
    private final boolean permissionRead;
    private final MainView parent;
    @FXML private Label fileName;
    @FXML private Label fileSize;
    public FileType(File file,MainView parent){
        this.parent = parent;
        this.file = file;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("file.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(FileType.this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        getStyleClass().add("borderStyle");
        fileName.setText(file.getName());
        if(file.isDirectory()){
            fileSize.setText("Directory");
        }
        else {
            fileSize.setText(Utils.BytesToHumanReadable(file.length()));
        }
        permissionRead = file.canRead();
        permissionWrite = file.canWrite();
        setupOnMouseClicked();
        fileName.setMouseTransparent(true); //hope this fixed a strange bug
    }
    private void setupOnMouseClicked(){
        FileType thisObject = this;
        setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                parent.controlSelectedFile(thisObject);
                if(mouseEvent.getButton()== MouseButton.PRIMARY){
                    if(mouseEvent.getClickCount()==2){
                        open();
                    }
                }
                if(mouseEvent.getButton()==MouseButton.SECONDARY){
                    final ContextMenu contextMenu = new ContextMenu();
                    List<MenuItem> menuItems = getContextMenuOptions();
                    for(int i = 0;i<menuItems.size();i++){
                        contextMenu.getItems().add(menuItems.get(i));
                    }
                    fileName.setContextMenu(contextMenu);
                    contextMenu.show(fileName, mouseEvent.getScreenX(),mouseEvent.getScreenY());
                    mouseEvent.consume();
                }
            }
        });
    }
    private List<MenuItem> getContextMenuOptions(){
        List<MenuItem> menuItems = new ArrayList<>();
        MenuItem itemOpen = new MenuItem("open");
        menuItems.add(itemOpen);
        if(!permissionRead){
            itemOpen.setDisable(true);
        }
        itemOpen.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                open();
            }
        });


        MenuItem itemRename = new MenuItem("rename");
        menuItems.add(itemRename);
        if(!permissionWrite || !permissionRead){
            itemRename.setDisable(true);
        }
        itemRename.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                rename();
            }
        });

        MenuItem itemCut = new MenuItem("cut");
        menuItems.add(itemCut);
        if(!permissionWrite || !permissionRead){
            itemCut.setDisable(true);
        }
        itemCut.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                cut();
            }
        });

        MenuItem itemCopy = new MenuItem("copy");
        menuItems.add(itemCopy);
        if(!permissionRead){
            itemCopy.setDisable(true);
        }
        itemCopy.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                copy();
            }
        });

        MenuItem itemDelete = new MenuItem("delete");
        menuItems.add(itemDelete);
        if(!permissionWrite || !permissionRead){
            itemDelete.setDisable(true);
        }
        itemDelete.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                delete();
            }
        });

        MenuItem itemPaste = new MenuItem("Paste");
        menuItems.add(itemPaste);
        if(parent.isCopiedFileEmpty()){
            itemPaste.setDisable(true);
        }
        itemPaste.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                paste();
            }
        });

        MenuItem itemLocation = new MenuItem("copy location path");
        menuItems.add(itemLocation);
        itemLocation.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                copyPath();
            }
        });

        Menu itemNew = new Menu("new(here)");
        menuItems.add(itemNew);
        if(!parent.canOpenedFolderWrite()){
            itemNew.setDisable(true);
        }

        MenuItem itemNewFile = new MenuItem("file");
        itemNew.getItems().add(itemNewFile);
        itemNewFile.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {parent.newFile(0);}
        });
        MenuItem itemNewFolder = new MenuItem("folder");
        itemNew.getItems().add(itemNewFolder);
        itemNewFolder.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {parent.newFile(1);}
        });

        MenuItem itemProperties = new MenuItem("properties");
        menuItems.add(itemProperties);
        itemProperties.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                properties();
            }
        });

        return menuItems;
    }
    private void open(){
        if(!permissionRead){
            return;
        }
        if(file.isDirectory()){
            parent.listFiles(file.getAbsolutePath());
        }

    }
    public void onFocused(){
        setStyle("-fx-background-color: lightgrey;");
        fileName.setWrapText(true);

    }
    public void onUnfocused(){
        getChildren().set(0,fileName);
        setStyle("-fx-background-color:WHITESMOKE");
        fileName.setWrapText(false);
    }
    private void rename(){
        if(!file.exists()){
            return;
        }
        FileType thisObject = this;
        TextArea textArea = new TextArea();
        textArea.setText(fileName.getText());
        textArea.setWrapText(true);
        textArea.setPrefWidth(Integer.MAX_VALUE);
        textArea.setMaxHeight(fileName.getHeight());
        textArea.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode().equals(KeyCode.ENTER)) {
                    Path source = Paths.get(file.getAbsolutePath());
                    try {
                        String newFileName = textArea.getText().replaceAll("[\n\r]", "");
                        if(!newFileName.isEmpty()) {
                            if(Files.exists(source.resolveSibling(newFileName))){
                                boolean input = parent.simpleQuestionWin("File already exists,replace?");
                                if(input){
                                    try {
                                        Files.move(source, source.resolveSibling(newFileName), StandardCopyOption.REPLACE_EXISTING);
                                        parent.listFiles();
                                    }
                                    catch (IOException ex) {throw new RuntimeException(ex);}
                                }
                            }
                            else {
                                Files.move(source, source.resolveSibling(newFileName));
                                file = new File(source.resolveSibling(newFileName).toString());
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    fileName.setText(file.getName());
                    thisObject.getChildren().set(0,fileName);
                }
                if (ke.getCode().equals(KeyCode.ESCAPE)){
                    thisObject.getChildren().set(0,fileName);
                }
            }
        });
        getChildren().set(0,textArea);

    }
    private void delete()  {
        if(!file.exists()){
            return;
        }
        boolean input = parent.simpleQuestionWin("Are you sure?");
        if(input){
            boolean deleted = false;
            if(file.isDirectory()){
                try(Stream<Path> items = Files.walk(Paths.get(file.getAbsolutePath()))) {
                    items.sorted(Comparator.reverseOrder()).forEach(file ->{
                        try {
                            Files.delete(file);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                deleted = true;
            }
            else{
                deleted = file.delete();
            }
            if(deleted){
                parent.getChildren().remove(this);
            }
        }
    }
    private void copyPath(){
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(file.getAbsolutePath());
        clipboard.setContent(content);
    }
    private void cut(){
        parent.prepareToCutOrPaste(file,true);
    }
    private void copy(){
        parent.prepareToCutOrPaste(file,false);
    }
    private void paste(){
        parent.paste();
    }
    private void properties(){parent.fileProperties(file);}

}

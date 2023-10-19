package com.explorer.components.MainView;

import com.explorer.components.MainView.File.FileComparators;
import com.explorer.components.MainView.File.FileType;
import com.explorer.components.TopBar.TopBar;
import com.explorer.components.Utils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainView extends VBox {
    private List<String> folderHistroy = new ArrayList<>();
    private List<String> redoFolderHistory = new ArrayList<>();
    private boolean listFiledRedoClear = true;
    private FileType focusedFile;
    private File openedFolder;
    private File copiedFile;
    private final ContextMenu contextMenu;
    private boolean deleteAfterCopy = false;
    public MainView(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("mainView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(MainView.this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        TopBar.setMV(this);
        listFiles(System.getProperty("user.home")+"/Downloads");
        contextMenu = new ContextMenu();
        Platform.runLater(new Runnable() {
            @Override public void run() {
                setUpMouseClick();
            }
        });
    }
    private void setUpMouseClick(){
        ScrollPane parent = (ScrollPane) getScene().lookup("#scPane");
        parent.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.getButton()==MouseButton.SECONDARY ){
                    List<MenuItem> menuItems = getContextMenuOptions();
                    contextMenu.getItems().clear();
                    for(int i = 0;i<menuItems.size();i++){
                        contextMenu.getItems().add(menuItems.get(i));
                    }
                    parent.setContextMenu(contextMenu);
                    contextMenu.show(parent, mouseEvent.getScreenX(),mouseEvent.getScreenY());
                    parent.setContextMenu(null);
                }
            }
        });
    }
    private List<MenuItem> getContextMenuOptions(){
        List<MenuItem> menuItems = new ArrayList<>();

        Menu itemNew = new Menu("new");
        menuItems.add(itemNew);
        if(!canOpenedFolderWrite()){
            itemNew.setDisable(true);
        }

        MenuItem itemNewFile = new MenuItem("file");
        itemNew.getItems().add(itemNewFile);
        itemNewFile.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {newFile(0);}
        });
        MenuItem itemNewFolder = new MenuItem("folder");
        itemNew.getItems().add(itemNewFolder);
        itemNewFolder.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {newFile(1);}
        });

        MenuItem itemProperties = new MenuItem("properties");
        menuItems.add(itemProperties);
        itemProperties.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                fileProperties();
            }
        });

        return menuItems;
    }
    public void listFiles(String path){
        openedFolder = new File(path);
        TopBar.setCurrentFolderName(openedFolder.getAbsolutePath());
        historySetter(openedFolder.getAbsolutePath());
        File[] files = openedFolder.listFiles();
        if(files==null){
            return;
        }
        files = sort(files,1);
        getChildren().clear();
        if(files == null){
            return;
        }
        for(int i = 0;i<files.length;i++){
            this.getChildren().add(new FileType(files[i],this));
        }
        //analog of overload
        if(listFiledRedoClear){
            redoFolderHistory.clear();
        }
        listFiledRedoClear=true;
    }
    public void listFiles(){
        listFiles(openedFolder.getAbsolutePath());
    }
    public void goUpDirectory(){
        if(openedFolder.getParent()==null){
            return;
        }
        this.listFiles(openedFolder.getParent());
    }
    public void goUndo(){
        if(folderHistroy==null || folderHistroy.size()<2){
            return;
        }
        listFiledRedoClear = false;
        listFiles(folderHistroy.get(folderHistroy.size()-2));
        folderHistroy.remove(folderHistroy.size()-1);
        redoFolderHistory.add(folderHistroy.get(folderHistroy.size()-1));
        folderHistroy.remove(folderHistroy.size()-1);
    }
    public void goRedo(){
        if(redoFolderHistory==null || redoFolderHistory.isEmpty()){
            return;
        }
        listFiledRedoClear = false;
        listFiles(redoFolderHistory.get(redoFolderHistory.size()-1));
        redoFolderHistory.remove(redoFolderHistory.size()-1);
    }
    private void historySetter(String path){
        if(folderHistroy==null){
            return;
        }
        if(folderHistroy.size()>20){
            folderHistroy.remove(0);
        }
        folderHistroy.add(path);

    }
    private File[] sort(File[] files,int type){
        final int bySize = 1;
        final int byName = 2;
        final int byModified = 3;
        File[] arrayReturn = new File[0];
        File[] files1 = new File[0];
        for(int i = 0;i<files.length;i++){
            if(files[i].isDirectory()){
                arrayReturn = Arrays.copyOf(arrayReturn,arrayReturn.length+1);
                arrayReturn[arrayReturn.length-1] = files[i];
            }
            else{
                files1 = Arrays.copyOf(files1,files1.length+1);
                files1[files1.length-1] = files[i];
            }
        }
        if(type==bySize){
            Arrays.sort(files1, FileComparators.BySize);
        }

        for(int i =0;i<files1.length;i++){
            arrayReturn = Arrays.copyOf(arrayReturn,arrayReturn.length+1);
            arrayReturn[arrayReturn.length-1] = files1[i];
        }
        if(type==byModified){
            Arrays.sort(arrayReturn, FileComparators.ByLastModified);
        }
        if(type==byName){
            Arrays.sort(arrayReturn, FileComparators.ByName);
        }
        return arrayReturn;

    }
    public void controlSelectedFile(FileType fileType){
        if(focusedFile!=fileType){
            if(focusedFile!=null) {
                focusedFile.onUnfocused();
            }
            fileType.onFocused();
            focusedFile = fileType;
        }
    }
    public void prepareToCutOrPaste(File file,boolean toCut){
        deleteAfterCopy = toCut;
        copiedFile = file;
    }
    public void paste(){
        Path source = Paths.get(copiedFile.getAbsolutePath());
        Path dest = Paths.get(openedFolder.getAbsolutePath());
        boolean result = false;
        if(Files.exists(dest.resolve(copiedFile.getName()))){
            result = simpleQuestionWin("File already exists,replace?");
            if(!result){
                return;
            }
        }
        if(deleteAfterCopy){
            try {
                Files.move(source, dest.resolve(copiedFile.getName()),StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
            throw new RuntimeException(e);
            }
        }
        else{
            try {
                if(copiedFile.isDirectory()){
                    Path dir = dest.resolve(copiedFile.getName());
                    Files.walk(source).forEach(file -> {
                        try {
                            if(!dest.equals(dir.resolve(source.relativize(file)))){
                                Files.copy(file,dir.resolve(source.relativize(file)),StandardCopyOption.REPLACE_EXISTING);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
                else{
                    Files.copy(source,dest.resolve(copiedFile.getName()),StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        listFiles(dest.toString());
    }
    public boolean isCopiedFileEmpty(){
        if(copiedFile==null){
            return true;
        }
        if(copiedFile.exists()){
            return false;
        }
        else{
            return true;
        }
    }
    public boolean simpleQuestionWin(String text){
        AtomicBoolean value = new AtomicBoolean(false);
        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);
        Stage stage = createStage(150,400,vbox);
        Label label = new Label(text);
        HBox buttonsLayout = new HBox();
        buttonsLayout.setAlignment(Pos.CENTER_RIGHT);
        Button buttonCancel = new Button("No");
        buttonCancel.setOnAction(e->{
            stage.close();
            value.set(false);
        });
        Button buttonConfirm = new Button("Yes");
        buttonConfirm.setOnAction(e->{
            stage.close();
            value.set(true);
        });
        buttonsLayout.getChildren().addAll(buttonCancel,buttonConfirm);
        vbox.getChildren().addAll(label,buttonsLayout);
        stage.showAndWait();
        return value.get();
    }
    public void newFile(int type){
        VBox comp = new VBox(6);
        comp.setPadding(new Insets(10));
        Label label = new Label();
        if(type==0){
            label.setText("create new file");
        }
        if(type==1){
            label.setText("create new folder");
        }
        HBox hbox = new HBox();
        HBox emptySpace = new HBox();
        HBox.setHgrow(emptySpace,Priority.ALWAYS);
        Button buttonCancel = new Button("cancel");
        Button buttonConfirm = new Button("ok");
        hbox.getChildren().addAll(emptySpace,buttonCancel,buttonConfirm);
        TextField textField = new TextField("new name");
        comp.getChildren().addAll(label,textField,hbox);
        Stage popup = createStage(100,400,comp);
        buttonCancel.setOnAction(e->{
            popup.close();
        });
        buttonConfirm.setOnAction(e->{
            String name = textField.getText();
            Path newPath = Paths.get(openedFolder.getAbsolutePath()).resolve(name);
            File file = new File(newPath.toString());
            if(file.exists()){
                popup.setMaxHeight(150);
                popup.setHeight(150);
                Label warning = new Label(file.getAbsolutePath() + " already exists");
                comp.getChildren().add(warning);
                return;
            }
            if(type==0){
                try {
                    file.createNewFile();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            if(type==1){
                file.mkdir();
            }
            popup.close();
            listFiles(openedFolder.getAbsolutePath());
        });
        popup.showAndWait();
    }
    public Stage createStage(int maxHeight,int maxWidth,VBox content){
        Stage newStage = new Stage();
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.initStyle(StageStyle.UTILITY);
        newStage.setResizable(false);
        Scene newScene = new Scene(content,100,100);
        newStage.setScene(newScene);
        newStage.setMaxHeight(maxHeight);
        newStage.setMaxWidth(maxWidth);
        return newStage;
    }
    public void fileProperties(File file)  {
        BasicFileAttributes attributes;
        DateFormat formater = new SimpleDateFormat("dd MMM yyyy HH:mm:ss:SS Z");
        try {
            attributes = Files.readAttributes(Path.of(file.getAbsolutePath()), BasicFileAttributes.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int space = 20;
        VBox comp = new VBox(space);
        comp.setPadding(new Insets(5,10,5,10));
        comp.setAlignment(Pos.TOP_CENTER);
        Label label = new Label("Properties");
        Label name = new Label(file.getName());
        Label size = new Label(Utils.BytesToHumanReadable(file.length()));

        VBox vPath = new VBox(5);
        Label vPathLabel = new Label("Location");
        Label vPatchReal = new Label(file.getAbsolutePath());
        vPath.getChildren().addAll(vPathLabel,vPatchReal);

        VBox vModified = new VBox(5);
        Label vModifiedLabel = new Label("Modified");
        Label vModifiedTime = new Label(formater.format(new Date(file.lastModified())));
        vModified.getChildren().addAll(vModifiedLabel,vModifiedTime);

        VBox vAcessed = new VBox(5);
        Label vAcessedLabel = new Label("Acessed");
        Label vAcessedTime = new Label(formater.format(attributes.lastAccessTime().toMillis()));
        vAcessed.getChildren().addAll(vAcessedLabel,vAcessedTime);

        VBox vCreated = new VBox(5);
        Label vCreatedLabel = new Label("Created");
        Label vCreatedTime = new Label(formater.format(attributes.creationTime().toMillis()));
        vCreated.getChildren().addAll(vCreatedLabel,vCreatedTime);

        HBox vPermissions = new HBox(5);
        Label vPermissionLabel = new Label("Permission");
        String permissions;
        if(file.canWrite()&file.canRead()){permissions = "Read and write";}
        else if(file.canRead()){permissions = "Read";}
        else{permissions = "None";}
        Label vPermissionReal = new Label(permissions);
        HBox emptySpace = new HBox();
        HBox.setHgrow(emptySpace,Priority.ALWAYS);
        vPermissions.getChildren().addAll(vPermissionLabel,emptySpace,vPermissionReal);

        comp.getChildren().addAll(label,name,size,vPath,vModified,vAcessed,vCreated,vPermissions);
        Stage popup = createStage(600,400,comp);
        popup.showAndWait();
    }
    public void fileProperties(){
        fileProperties(openedFolder);
    }

    public boolean canOpenedFolderWrite(){
        return openedFolder.canWrite();
    }

}

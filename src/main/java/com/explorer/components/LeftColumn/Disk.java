package com.explorer.components.LeftColumn;

import com.explorer.components.TopBar.TopBar;
import com.explorer.components.Utils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.*;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.security.auth.login.AccountException;
import javax.swing.filechooser.FileSystemView;

public class Disk extends VBox {
    private File diskFile;
    private String type;
    private String label;
    private String path;
    private String mountPoint;
    @FXML private  Label disk_name;
    @FXML private Label disk_path;
    @FXML private  Label disk_space;

    public Disk(String type,String label,String path,String mountPoint){
        this.type = type;
        this.label = label;
        this.path= path;
        this.mountPoint = mountPoint;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("disk.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(Disk.this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        getStyleClass().add("borderStyle");
        disk_name.setText(label);
        disk_path.setText(mountPoint);
        File disk = new File(mountPoint);
        if(!mountPoint.equals("Not mounted")) {
            String freeSpace = Utils.BytesToHumanReadable(disk.getFreeSpace());
            String maxSpace = Utils.BytesToHumanReadable(disk.getTotalSpace());
            disk_space.setText(freeSpace + "|" + maxSpace);
            disk_space.setMinWidth(Region.USE_PREF_SIZE);
            diskFile = new File(mountPoint);
        }
        setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.getButton()== MouseButton.SECONDARY){
                    final ContextMenu contextMenu = new ContextMenu();
                    List<MenuItem> itemList = getContextMenuOptions();
                    for(int i =0;i<itemList.size();i++){
                        contextMenu.getItems().add(itemList.get(i));
                    }
                    disk_name.setContextMenu(contextMenu);
                    contextMenu.show(disk_name,mouseEvent.getScreenX(),mouseEvent.getScreenY());
                }
            }
        });
    }
    private List<MenuItem> getContextMenuOptions() {
        List<MenuItem> menuItems = new ArrayList<>();

        MenuItem itemMount = new MenuItem("mount");
        if(!mountPoint.equals("Not mounted")){
            itemMount.setDisable(true);
        }
        itemMount.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {mount();}
        });
        menuItems.add(itemMount);
        MenuItem itemUnmount = new MenuItem("unmount");
        if(mountPoint.equals("Not mounted")){
            itemUnmount.setDisable(true);
        }
        itemUnmount.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {unmount();}
        });
        menuItems.add(itemUnmount);
        MenuItem itemProperties= new MenuItem("Properties");
        if(diskFile==null){
            itemProperties.setDisable(true);
        }
        itemProperties.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                TopBar.getMV().fileProperties(diskFile);
            }
        });
        menuItems.add(itemProperties);

        return menuItems;
    }
    private void mount() {
        //Use polkit to run root command

        //hardcoded path,maybe there is another way
        //also didn't find a way to unmount & remove folder on exit after sigkill
        Path enterPoint = Paths.get("/media/");
        if(!Files.exists(enterPoint)){
            System.out.println("/media dir doesn't exists");
            return;
        }
        if(!Files.exists(enterPoint.resolve(label))) {
            try {
                Files.createDirectory(enterPoint.resolve(label));
            } catch (AccessDeniedException e){
                String[] command = {"pkexec","mkdir",enterPoint.resolve(label).toString()};
                Utils.RunCommand(command);
            }catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        String[] command = {"pkexec","mount",path,enterPoint.resolve(label).toString()};
        Utils.RunCommand(command);
    }
    private void unmount(){
        String[] command = {"pkexec","umount",path};
        Utils.RunCommand(command);
    }
}

package com.explorer.components.LeftColumn;

import com.explorer.components.Utils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.*;
import java.util.List;
import javax.swing.filechooser.FileSystemView;

public class Disk extends VBox {
    String name;
    String path;
    int current_space;
    @FXML private  Text disk_name;
    @FXML private  Text disk_path;
    @FXML private  Text disk_space;

    public Disk(File disk){
        String OS = System.getProperty("os.name");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("disk.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(Disk.this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        String name = null;
        if(OS.startsWith("Linux")){
            List<String> output = Utils.RunCommand(new String[]{"lsblk", "-e7","-a","-n","-i","-r","-o","LABEL,MOUNTPOINT"});
            for(int i =0;i<output.size();i++){
                String line = output.get(i);
                int space_index = line.indexOf(" ");
                String mount_point = line.substring(space_index+1);
                String label = line.substring(0,space_index);
                if(disk.getAbsolutePath().equals(mount_point)){
                    name = label;
                    break;
                }
            }
        }
        getStyleClass().add("borderStyle");
        if(OS.startsWith("Windows")){
            name = FileSystemView.getFileSystemView().getSystemDisplayName(disk);
        }
        disk_name.setText(name);
        disk_path.setText(disk.getAbsolutePath());
        String freeSpace = Utils.BytesToHumanReadable(disk.getFreeSpace());
        String maxSpace = Utils.BytesToHumanReadable(disk.getTotalSpace());
        disk_space.setText(freeSpace + "|" + maxSpace);
    }
}

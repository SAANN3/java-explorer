package com.explorer.components.LeftColumn;

import com.explorer.components.Utils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

import java.io.*;
import java.util.Arrays;
import java.lang.*;
import java.util.List;

public class LeftColumn extends VBox {
    @FXML private VBox disk_placement;
    public LeftColumn(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("leftColumn.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(LeftColumn.this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        getStyleClass().add("borderStyle");
        showDisks();

    }
    public void showDisks(){
        disk_placement.getChildren().clear();
        Disk[] disk_array = ListDisks();
        for(int i = 0;i<disk_array.length;i++){
            disk_placement.getChildren().add(disk_array[i]);
        }
    }
    private Disk[] ListDisks()  {
        Disk[] disks = new Disk[]{};
        List<String> output = Utils.RunCommand(new String[]{"lsblk", "-e7","-a","-n","-i","-r","-p","-o","TYPE,LABEL,NAME,MOUNTPOINT"});
        for(int i =0;i<output.size();i++){
            String[] line = output.get(i).split(" ");
            String type = line[0];
            if(type.equals("disk")){
                continue;
            }
            String label = line[1];
            if(label.isEmpty()){
                continue;
            }
            String nameDisk = line[2];
            String mountPoint = "Not mounted";
            if(line.length==4){
                mountPoint= line[3];
            }
            if(mountPoint.equals("[SWAP]")){
                continue;
            }
            disks = Arrays.copyOf(disks,disks.length+1);
            disks[disks.length-1] = new Disk(type,label,nameDisk,mountPoint);
        }
        if(disks.length == 0){
            System.out.print("Couldn't find any partition");
            Platform.exit();
        }
        return disks;
    }



}

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
        File[] disk_array = ListDisks();
        System.out.println(Arrays.toString(disk_array));
        for(int i = 0;i<disk_array.length;i++){
            disk_placement.getChildren().add(new Disk(disk_array[i]));
        }

    }
    private File[] ListDisks()  {
        File[] disks = new File[]{};
        String OS = System.getProperty("os.name");
        if(OS.startsWith("Linux")){
            List<String> output = Utils.RunCommand(new String[]{"lsblk", "-e7","-a","-n","-i","-r","-o","NAME,MOUNTPOINT"});
            for(int i =0;i<output.size();i++){
                String line = output.get(i);
                int space_index = line.indexOf(" ");
                String mount_point = line.substring(space_index+1);
                if(mount_point.equals("[SWAP]")){
                    continue;
                }
                disks = Arrays.copyOf(disks,disks.length+1);
                disks[disks.length-1] = new File(mount_point);
            }

        }
        if(OS.startsWith("Windows")){
            disks = File.listRoots();
        }
        if(disks.length == 0){
            System.out.print("Couldn't find any partition");
            //Platform.exit();
        }
        return disks;
    }



}

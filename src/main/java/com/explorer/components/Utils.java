package com.explorer.components;

import com.explorer.components.MainView.MainView;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import java.io.*;
import java.util.*;

public class Utils {
    public static String BytesToHumanReadable(long bytes){
        double kilobyte = 1024;
        double megabyte = kilobyte * 1024;
        double gigabyte = megabyte * 1024;
        double terabyte = gigabyte * 1024;
        if ((bytes >= 0) && (bytes < kilobyte)) {
            return bytes + " B";
        } else if ((bytes >= kilobyte) && (bytes < megabyte)) {
            return ((double)((int)((bytes / kilobyte)  *100.0)))/100.0 + " KB";
        } else if ((bytes >= megabyte) && (bytes < gigabyte)) {
            return ((double)((int)((bytes / megabyte) *100.0)))/100.0 + " MB";
        } else if ((bytes >= gigabyte) && (bytes < terabyte)) {
            return ((double)((int)((bytes / gigabyte) *100.0)))/100.0 + " GB";
        } else if (bytes >= terabyte) {
            return ((double)((int)((bytes / terabyte)*100.0)))/100.0 + " TB";
        } else {

            return bytes + " Bytes";
        }
    }
    public static List<String> RunCommand(String[] command){
        List<String> output = new ArrayList<String>();
        ProcessBuilder pb = new ProcessBuilder(command);
        try{
            Process p = pb.start();
            InputStream is = p.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = br.readLine()) != null) {
                int space_index = line.indexOf(" ");
                if(line.length() > space_index+1){
                    output.add(line);
                }
            }
        }
        catch (IOException e){
            throw new RuntimeException(e);
        }
        return output;
    }


}

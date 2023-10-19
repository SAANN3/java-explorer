package com.explorer.components.MainView.File;

import com.explorer.components.Utils;

import java.io.File;
import java.util.Comparator;

public enum FileComparators implements Comparator<File> {
    ByLastModified {
        public int compare(File f1, File f2) {
            return Long.compare(f1.lastModified(), f2.lastModified());
        }
    },
    ByName{
        public int compare(File f1,File f2){
            return f1.getName().compareTo(f2.getName());
        }
    },
    BySize{
        public int compare(File f1,File f2){
            return Long.compare(f1.length(),f2.length());
        }
    }

}

package com.example.artadmission.UI;

import java.io.Closeable;
import java.io.IOException;

public class MyUtils {


    public static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

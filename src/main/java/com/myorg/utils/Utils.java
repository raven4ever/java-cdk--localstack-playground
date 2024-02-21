package com.myorg.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Utils {

    public static String getFileContentAsString(String fileName) {
        String content = "";

        try {
            content = Files.readString(
                    Paths.get(Utils.class.getClassLoader().getResource(fileName).toURI()));
        } catch (IOException ex) {
            System.out.println("resource not found!");
        } catch (URISyntaxException ex2) {
            System.out.println("URI syntax exception!");
        }

        return content;
    }
}

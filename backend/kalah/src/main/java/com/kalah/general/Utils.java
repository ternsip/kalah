package com.kalah.general;

import lombok.SneakyThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Utils {

    @SneakyThrows
    public static InputStream loadResourceAsStream(File file) {
        InputStream in = Utils.class.getClassLoader().getResourceAsStream(getPath(file));
        if (in == null) {
            throw new FileNotFoundException("Can't find file: " + getPath(file));
        }
        return in;
    }

    @SneakyThrows
    public static String loadResourceAsString(File file) {
        return new String(loadResourceAsStream(file).readAllBytes(), StandardCharsets.UTF_8);
    }

    @SneakyThrows
    public static String getPath(File file) {
        return file.getPath().replace("\\", "/");
    }

}

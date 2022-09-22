/*
 *   RouterToDNS - ConfigFileReader.java
 *
 *   Copyright (c) 2022-2022, Slinky Software
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as
 *   published by the Free Software Foundation, either version 3 of the
 *   License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   A copy of the GNU Affero General Public License is located in the 
 *   AGPL-3.0.md supplied with the source code.
 *
 */
package com.slinkytoybox.routertodns.routerconfig;

import lombok.*;
import java.io.*;

/**
 *
 * @author James Cowan <mrjamescowan@gmail.com>
 */
@Data
public class ConfigFileReader {

    private ConfigFileReader() {
        
    }
    
    public static String readConfigFile(String asaConfigFileName) throws IOException {
        
        StringBuilder loadingConfigFile = new StringBuilder();
        File file = new File(asaConfigFileName);    //creates a new file instance
        FileReader fr = new FileReader(file);   //reads the file
        try (BufferedReader br = new BufferedReader(fr)) {
        String line;
        while ((line = br.readLine()) != null) {
            loadingConfigFile.append(line);      //appends line to string buffer
            loadingConfigFile.append("\n");     //line feed
        }  //creates a buffering character input stream
        }

        return loadingConfigFile.toString();
    }
}

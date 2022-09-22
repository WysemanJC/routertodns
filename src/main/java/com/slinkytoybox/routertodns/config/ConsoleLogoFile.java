/*
 *   RouterToDNS - ConsoleLogoFile.java
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
package com.slinkytoybox.routertodns.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

/**
 *
 * @author James Cowan <mrjamescowan@gmail.com>
 */
@Slf4j
public class ConsoleLogoFile {
    
    public String getLogo(String logoFileName) throws IOException { /* Constructor */
        final String logPrefix = "getLogo() -";
        log.trace("{} Entering Method", logPrefix);
        
        log.trace("{} Making new Resource", logPrefix);
        Resource resource = new ClassPathResource(logoFileName);
        log.trace("{} Doing Input Stream", logPrefix);
        InputStream inputStream = resource.getInputStream();
        try {
            byte[] bdata = FileCopyUtils.copyToByteArray(inputStream);
            String data = new String(bdata, StandardCharsets.UTF_8);
            log.trace("{} Got File Data", logPrefix);
            return data;
        } catch (IOException e) {
            log.trace("{} IOException", e);
            return null;
        }    
    }
}

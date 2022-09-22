/*
 *   RouterToDNS - ApplicationProperties.java
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

/**
 *
 * @author James Cowan <mrjamescowan@gmail.com>
 */

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public final class ApplicationProperties {

    private final String loggingLevel;

    private final String dnsSubDomain;
    private final String dnsDomain;
    private final String aRecordDNSServer;

    private final Integer zoneConfidenceLevel;

    @Autowired
    public ApplicationProperties(
            @Value("${application.logging.level}") String loggingLevel,
            @Value("${dns.subdomain}") String dnsSubDomain,
            @Value("${dns.domain}") String dnsDomain,
            @Value("${dns.arecordserver}") String aRecordDNSServer,
            @Value("${dns.zone.confidence.level}") Integer zoneConfidenceLevel
    ) {
        this.loggingLevel = loggingLevel;

        this.dnsSubDomain = dnsSubDomain;        
        this.dnsDomain = dnsDomain;    
        this.aRecordDNSServer = aRecordDNSServer;

        this. zoneConfidenceLevel = zoneConfidenceLevel;
    }
    
    

}
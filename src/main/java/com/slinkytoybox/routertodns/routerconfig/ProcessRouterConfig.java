/*
 *   RouterToDNS - ProcessRouterConfig.java
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

import static java.lang.System.exit;
import static java.util.Objects.isNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author James Cowan <mrjamescowan@gmail.com>
 */

@Slf4j
@Component
public class ProcessRouterConfig {

    @Autowired
    public ProcessRouterConfig( @Value("${config-file}") String configFilename,
                                @Value("${site-name}") String siteName,
                                @Value("${dns.subdomain}") String dnsSubDomain,
                                @Value("${dns.domain}") String dnsDomain,
                                @Value("${dns.arecordserver}") String aRecordDNSServer,
                                @Value("${dns.zone.confidence.level}") Integer dnsConfidenceLevel ) {
        RouterDeviceConfig routerConfig = null;


        if (configFilename.isBlank()) {
            log.error("Please specify config filename (--config-filename=<filename>");
            exit(0);
        } else {
            if (!siteName.isBlank()) {
                routerConfig = new RouterDeviceConfig(configFilename, siteName, dnsSubDomain, dnsDomain, dnsConfidenceLevel);

            } else {
                routerConfig = new RouterDeviceConfig(configFilename, "site", dnsSubDomain, dnsDomain, dnsConfidenceLevel);
            }
        }
        if (!isNull(routerConfig)) {
            routerConfig.LogDeviceStats();
            routerConfig.OutputNewARecords(aRecordDNSServer);
            routerConfig.OutputConfidentPTRRecords();
            routerConfig.OutputUncertainPTRRecords();

            routerConfig.OutputExistingARecords();
            routerConfig.OutputExistingPTRRecords();
        } else {
            exit(0);
        }
    }
}

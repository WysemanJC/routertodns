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

import com.slinkytoybox.routertodns.config.ApplicationProperties;
import static java.lang.System.exit;
import static java.util.Objects.isNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 *
 * @author James Cowan <mrjamescowan@gmail.com>
 */
@Slf4j
@Component
public class ProcessRouterConfig {

    @Autowired
    ApplicationProperties appProp;

    @EventListener({ApplicationReadyEvent.class})
    public void ProcessRouterConfigPost() {
        RouterDeviceConfig routerConfig = null;

        if (appProp.getConfigFilename().isEmpty()) {
            log.error("Please specify config filename (--config-filename=<filename>");
            exit(0);
        } else {
            routerConfig = new RouterDeviceConfig(appProp.getConfigFilename(), appProp.getSiteName(), appProp.getDnsSubDomain(), appProp.getDnsDomain(), appProp.getZoneConfidenceLevel());
        }
        if (!isNull(routerConfig)) {
            routerConfig.LogDeviceStats();
            routerConfig.OutputNewARecords(appProp.getARecordDNSServer());
            routerConfig.OutputConfidentPTRRecords();
            routerConfig.OutputUncertainPTRRecords();

            routerConfig.OutputExistingARecords();
            routerConfig.OutputExistingPTRRecords();
        } else {
            exit(0);
        }
    }
}

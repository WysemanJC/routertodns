/*
 *   RouterToDNS - RouterDeviceConfig.java
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

import com.slinkytoybox.routertodns.datamodel.collections.DeviceInterfaceCollection;
import com.slinkytoybox.routertodns.datamodel.items.DeviceInterfaceItem;
import com.slinkytoybox.routertodns.datamodel.items.DeviceItem;
import java.io.IOException;
import java.util.Iterator;
import static java.util.Objects.isNull;
import java.util.StringJoiner;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author James Cowan <mrjamescowan@gmail.com>
 */

@Slf4j
@Data
public class RouterDeviceConfig {

    @Setter(AccessLevel.NONE)
    private final DeviceItem networkDevice = new DeviceItem();
    @Setter(AccessLevel.NONE)
    private final DeviceInterfaceCollection allDefinedInterfaces = new DeviceInterfaceCollection();

    /* p_ variables are used to identify when we're processing a particular object type */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    boolean processingInterface = false;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    boolean processingBanner = false;

    /* Current variables are used to store the currently building object before adding to the list of objects */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    DeviceInterfaceItem currentInterface = new DeviceInterfaceItem();

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    String currentAccessListName;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    String deviceConfigData;

    public RouterDeviceConfig(String filename, String siteName, String dnsSubDomain, String dnsDomain, Integer dnsConfidenceLevel) {
        /* Constructor */

        final String logPrefix = "RouterDeviceConfig() - ";

        log.trace("{} Entering method", logPrefix);

        String deviceConfigData = LoadDeviceConfig(filename);

        if (!isNull(deviceConfigData)) {
            parseDeviceConfig(deviceConfigData);
            log.warn("Done Processing Configuration File", logPrefix);
        }

        PopulateDNSData(siteName, dnsSubDomain, dnsDomain, dnsConfidenceLevel);

        log.trace("{} Leaving method", logPrefix);
    }

    private String LoadDeviceConfig(String filename) {
        /* Constructor */
        final String logPrefix = "LoadDeviceConfig() - ";
        log.trace("{} Entering method", logPrefix);

        String activeConfigFile = null;

        log.info("{}Loading Config File {}", logPrefix, filename);
        try {
            activeConfigFile = ConfigFileReader.readConfigFile(filename);

        } catch (IOException ex) {
            log.error("{}Config File {} not found.", logPrefix, filename);
            return null;
        }
        log.trace("{} Leaving method", logPrefix);
        return activeConfigFile;

    }

    public int getInterfacesSize() {
        return allDefinedInterfaces.getSize();
    }

    private void parseDeviceConfig(String deviceConfigData) {

        String logPrefix = "ParseDeviceConfig() - ";
        String logPrefixAddition;
        log.trace("{}Entering method", logPrefix);

        String[] configLines;
        configLines = deviceConfigData.split("\n");
        StringJoiner buildDescription;

        int i;
        int l;
        String desc = null;
        int errorCount = 0;

        log.info("{}Processing {} Configuration Lines", logPrefix, configLines.length);

        for (i = 0; i < configLines.length; i++) {

            String x = configLines[i].trim();
            String[] configLine = x.split(" ");

            int thisLine = i + 1;
            try {
                logPrefixAddition = padRight("[" + thisLine + "]", 8) + padLeft("[" + configLine[0] + "]", 17);
                logPrefix = "ParseDeviceConfig() - " + logPrefixAddition + " ";
                switch (configLine[0]) {
                    /* 
                    Standard Device Info
                     */
                    case "hostname":
                        networkDevice.setHostName(configLine[1]);
                        log.trace("{}{}", logPrefix, networkDevice.getHostName());
                        break;

                    /*
                    Main Configuration Sections
                     */
                    case "interface":
                        writeDeviceConfig(logPrefixAddition);
                        if (!"preconfigure".equals(configLine[1])) {
                            processingInterface = true;
                            currentInterface.setHardwarePort(configLine[1]);
                            currentInterface.setinterfaceName(currentInterface.getHardwarePort());

                            log.trace("{}{}", logPrefix, currentInterface.getHardwarePort());
                        }
                        break;

                    case "vrf":
                        if (processingInterface) {
                            currentInterface.setVrf(configLine[1]);
                            log.trace("{}{}", logPrefix, currentInterface.getVrf());
                        }

                        break;
                    case "vrrp":
                        if (processingInterface) {
                            if (configLine[2].equals("ip")) {
                                currentInterface.setVrrpAddress(configLine[3]);
                                currentInterface.setVrrpNumber(configLine[1]);
                                log.trace("{}{}", logPrefix, currentInterface.getVrrpAddress());
                            }
                        }

                        break;
                    /* Detail entries under Interface Sections */
                    case "ip":
                        if (processingInterface) {
                            if (configLine[1].equals("address")) {
                                currentInterface.setIpAddress(configLine[2]);
                                currentInterface.setNetMask(configLine[3]);
                                log.trace("{}{}/{}", logPrefix, currentInterface.getIpAddress(), currentInterface.getNetMask());
                            }
                            if (configLine[1].equals("ipv4")) {
                                currentInterface.setIpAddress(configLine[3]);
                                currentInterface.setNetMask(configLine[4]);
                                log.trace("{}{}/{}", logPrefix, currentInterface.getIpAddress(), currentInterface.getNetMask());
                            }
                            if (configLine[1].equals("vrf")) {
                                currentInterface.setVrf(configLine[3]);
                            }
                        }
                        break;
                    case "ipv4":
                        if (processingInterface) {
                            if (configLine[1].equals("address")) {
                                currentInterface.setIpAddress(configLine[2]);
                                currentInterface.setNetMask(configLine[3]);
                                log.trace("{}{}/{}", logPrefix, currentInterface.getIpAddress(), currentInterface.getNetMask());
                            }
                            if (configLine[1].equals("vrf")) {
                                currentInterface.setVrf(configLine[3]);
                            }
                        }
                        break;
                    case "encapsulation":
                        if (processingInterface) {
                            currentInterface.setVlan(configLine[2]);
                            log.trace("{}{}", logPrefix, currentInterface.getVlan());
                        } else {
                            log.error("{} Unknown vlan Configuration ({})", logPrefix, configLine);
                        }
                        break;
                    case "shutdown":
                        if (processingInterface) {
                            currentInterface.setShutDown(true);
                            log.trace("{}{}", logPrefix, currentInterface.getShutDown());
                        } else {
                            log.error("{} Unknown shutdown Configuration ({})", logPrefix, configLine);
                        }
                        break;
                    case "management-only":
                        if (processingInterface) {
                            currentInterface.setManagementPort(true);
                            log.trace("{}{}", logPrefix, currentInterface.getManagementPort());
                        } else {
                            log.error("{} Unknown management-only Configuration ({})", logPrefix, configLine);
                        }
                        break;

                    case "description":
                        /* Decode Multi Line Description */
                        buildDescription = new StringJoiner(" ");
                        for (l = 1; l < configLine.length; l++) {
                            buildDescription.add(configLine[l]);
                        }
                        desc = buildDescription.toString();
                        if (!desc.isEmpty()) {
                            if (processingInterface) {
                                currentInterface.setDescription(desc);
                                log.trace("{}{}", logPrefix, currentInterface.getDescription());
                            }
                        }
                        break;

                    case "route":
                        // Not using now but should capture
                        log.trace("{} Unknown route Configuration ({})", logPrefix, configLine);
                        break;

                    case "certificate":
                    case "banner":
                        processingBanner = true;
                        break;

                    case "same-security-traffic":
                    case "ipv6":
                    case "64598":
                    case "0.0.0.0/0":
                    case "end":
                    case "advertisement-interval":
                    case "snmp":
                    case "length":
                    case "ASA":
                    case "terminal":
                    case "enable":
                    case "xlate":
                    case "dns":
                    case "passwd":
                    case "mtu":
                    case "asdm":
                    case "ssh":
                    case "username":
                    case "policy-map":
                    case "class-map":
                    case "snmp-server":
                    case "http":
                    case "aaa":
                    case "timeout":
                    case "threat-detection":
                    case "crypto":
                    case "no":
                    case "logging":
                    case "absolute":
                    case "time-range":
                    case "names":
                    case "pager":
                    case "match":
                    case "parameters":
                    case "message-length":
                    case "inspect":
                    case "flow-export":
                    case "monitor-interface":
                    case "nat":
                    case "class":
                    case "user-statistics":
                    case "set":
                    case "aaa-server":
                    case "telnet":
                    case "service-policy":
                    case "access-list":
                    case "dynamic-access-policy-record":
                    case ":":
                    case " ":
                    case "icmp":
                    case "arp":
                    case "key":
                    case "precision":
                    case "reactivation-mode":
                    case "user-identity":
                    case "boot":
                    case "ftp":
                    case "clock":
                    case "name-server":
                    case "console":
                    case "management-access":
                    case "dhcpd":
                    case "ntp":
                    case "ssl":
                    case "webvpn":
                    case "hsts":
                    case "max-age":
                    case "include-sub-domains":
                    case "anyconnect-essentials":
                    case "cache":
                    case "disable":
                    case "error-recovery":
                    case "prompt":
                    case "log":
                    case "dhcprelay":
                    case "prefix-list":
                    case "route-map":
                    case "router":
                    case "bgp":
                    case "timers":
                    case "address-family":
                    case "neighbor":
                    case "redistribute":
                    case "synchronization":
                    case "exit-address-family":
                    case "permit":
                    case "policy-list":
                    case "max-failed-attempts":
                    case "bfd-template":
                    case "login":
                    case "interval":
                    case "lldp":
                    case "dampening":
                    case "load-interval":
                    case "carrier-delay":
                    case "ha-mode":
                    case "access-class":
                    case "call-home":
                    case "contact-email-addr":
                    case "profile":
                    case "active":
                    case "version":
                    case "service":
                    case "platform":
                    case "boot-start-marker":
                    case "boot-end-marker":
                    case "rd":
                    case "route-target":
                    case "import":
                    case "server":
                    case "secure-server":
                    case "server-private":
                    case "subscriber":
                    case "multilink":
                    case "enrollment":
                    case "serial-number":
                    case "subject-name":
                    case "revocation-check":
                    case "rsakeypair":
                    case "quit":
                    case "next-hop-self":
                    case "license":
                    case "spanning-tree":
                    case "diagnostic":
                    case "redundancy":
                    case "mode":
                    case "police":
                    case "track":
                    case "object":
                    case "lacp":
                    case "threshold":
                    case "privilege":
                    case "cabundle":
                    case "conform-action":
                    case "exceed-action":
                    case "hold-queue":
                    case "bfd":
                    case "negotiation":
                    case "channel-group":
                    case "template":
                    case "exit-peer-session":
                    case "remote-as":
                    case "fall-over":
                    case "password":
                    case "icmp-echo":
                    case "control-plane":
                    case "line":
                    case "stopbits":
                    case "session-timeout":
                    case "transport":
                    case "wsma":
                    case "transport-map":
                    case "violate-action":
                    case "maximum-paths":
                    case "send-community":
                    case "exit-peer-policy":
                    case "soft-reconfiguration":
                    case "Building":
                    case "Current":
                    case "frequency":
                    case "udp-jitter":
                    case "tos":
                    case "history":
                    case "verify-data":
                    case "priority":
                    case "tag":
                    case "network":
                    case "aggregate-address":
                    case "default-information":
                    case "area":
                    case "auto-cost":
                    case "router-id":
                    case "random-detect":
                    case "fair-queue":
                    case "bandwidth":
                    case "queue-limit":
                    case "flow":
                    case "destination":
                    case "source":
                    case "exec-timeout":
                    case "alias":
                    case "rmon":
                    case "passive-interface":
                    case "capability":
                    case "remark":
                    case "shape":
                    case "file":
                    case "memory":
                    case "hidekeys":
                    case "notify":
                    case "speed":
                    case "scheduler":
                    case "archive":
                    case "record":
                    case "exporter":
                    case "path-echo":
                    case "process":
                    case "10":
                    case "20":
                    case "30":
                    case "40":
                    case "50":
                    case "60":
                    case "70":
                    case "80":
                    case "90":
                    case "100":
                    case "110":
                    case "120":
                    case "130":
                    case "140":
                    case "150":
                    case "160":
                    case "170":
                    case "180":
                    case "190":
                    case "200":
                    case "210":
                    case "220":
                    case "230":
                    case "240":
                    case "250":
                    case "260":
                    case "270":
                    case "280":
                    case "290":
                    case "300":
                    case "310":
                    case "320":
                    case "330":
                    case "335":
                    case "340":
                    case "350":
                    case "360":
                    case "370":
                    case "380":
                    case "390":
                    case "400":
                    case "410":
                    case "420":
                    case "430":
                    case "440":
                    case "450":
                    case "460":
                    case "470":
                    case "480":
                    case "490":
                    case "500":
                    /* ISR Options */
                    case "bundle":
                    case "cdp":
                    case "macsec":
                    case "route-policy":
                    case "pass":
                    case "end-policy":
                    case "set-overload-bit":
                    case "is-type":
                    case "net":
                    case "nsr":
                    case "distribute":
                    case "nsf":
                    case "lsp-gen-interval":
                    case "lsp-refresh-interval":
                    case "max-lsp-lifetime":
                    case "metric-style":
                    case "fast-reroute":
                    case "microloop":
                    case "mpls":
                    case "spf-interval":
                    case "attached-bit":
                    case "segment-routing":
                    case "spf":
                    case "circuit-type":
                    case "point-to-point":
                    case "hello-password":
                    case "passive":
                    case "update":
                    case "neighbor-group":
                    case "update-source":
                    case "use":
                    case "prefix-sid":
                    case "ibgp":
                    case "additional-paths":
                    case "label":
                    case "send-community-ebgp":
                    case "maximum-prefix":
                    case "candidate-paths":
                    case "autoroute":
                    case "preference":
                    case "dynamic":
                    case "anycast-sid-inclusion":
                    case "constraints":
                    case "affinity":
                    case "exclude-any":
                    case "metric":
                    case "traffic-eng":
                    case "name":
                    case "policy":
                    case "color":
                    case "include":
                    case "type":
                    case "affinity-map":
                    case "macsec-policy":
                    case "conf-offset":
                    case "security-policy":
                    case "window-size":
                    case "cipher-suite":
                    case "include-icv-indicator":
                    case "policy-exception":
                    case "crl":
                    case "export":
                    case "maximum":
                    case "tcp":
                    case "accounting":
                    case "authorization":
                    case "vty-pool":
                    case "host":
                    case "authentication-key":
                    case "authenticate":
                    case "trusted-key":
                    case "update-calendar":
                    case "multipath":
                    case "echo":
                    case "accept-lifetime":
                    case "key-string":
                    case "send-lifetime":
                    case "cryptographic-algorithm":
                    case "lifetime":
                    case "contact":
                    case "http-proxy":
                    case "reporting":
                    case "management-plane":
                    case "out-of-band":
                    case "allow":
                    case "address":
                    case "end-class-map":
                    case "end-policy-map":
                    case "tunnel":
                    case "event":
                    case "action":
                    case "hw-module":
                    case "taskgroup":
                    case "inherit":
                    case "task":
                    case "usergroup":
                    case "tftp":
                    case "route-reflector-client":
                    case "!allow":
                    case "!!":
                    case "=~=~=~=~=~=~=~=~=~=~=~=":
                        break;
                    case "!":
                        if (processingInterface) {
                            writeDeviceConfig("");
                        }
                        if (processingBanner) {
                            processingBanner = false;
                        }
                        break;
                    default:
                        if (!processingBanner) {
                            if (!configLine[0].matches("Cryptochecksum(.*)") && !configLine[0].isEmpty()) {
                                log.error("{} Unknown Configuration ({})", logPrefix, configLine);
                            }
                        }
                        break;
                }
            } catch (Exception ex) {
                errorCount++;
                log.error("Exception Encountered {}", configLine, ex);
            }
        }
        if (processingInterface) {
            writeDeviceConfig("");
        }
        log.error("{} Errors Encountered {}", logPrefix, errorCount);
        log.trace("{} Leaving method", logPrefix);
    }

    private String LineStartingAt(String currentConfigLine, int startPosition) {

        int l;
        String[] configLine = currentConfigLine.split(" ");
        String returnString = "";

        for (l = 0; l < (configLine.length); l++) {
            if (l >= startPosition) {
                returnString = returnString + configLine[l] + " ";
            }
        }
        return returnString.trim();
    }

    public static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }

    public static String padLeft(String s, int n) {
        return String.format("%" + n + "s", s);
    }

    private void writeDeviceConfig(String logPrefixAddition) {
        /* Method to stop processing one object and write it to the appropriate list */
        final String logPrefix = "writeDeviceConfig() - " + logPrefixAddition;
        log.trace("{}Entering method", logPrefix);

        log.trace("{}----------------------------------------", logPrefix);
        if (processingInterface) {
            allDefinedInterfaces.addDeviceInterface(currentInterface);
            processingInterface = false;
            currentInterface = new DeviceInterfaceItem();
        }
    }

    public void LogDeviceStats() {
        String logPrefix = "LogDeviceStats() - ";
        log.trace("{}Entering method", logPrefix);

        log.info("{}Statistics for Device : {}", logPrefix, getNetworkDevice().getHostName());
        log.info("{}Interfaces            : {}", logPrefix, getInterfacesSize(), logPrefix);
        log.trace("{} Leaving method", logPrefix);
    }

    public void PopulateDNSData(String siteName, String subDomainName, String domainName, Integer dnsConfidenceLevel) {
        String logPrefix = "PopulateDNSData() - ";
        log.trace("{}Entering method", logPrefix);

        log.info("{} Fetching DNS Data... Please Wait...", logPrefix);

        Iterator<DeviceInterfaceItem> iter = getAllDefinedInterfaces().getDeviceInterfaces().listIterator();

        while (iter.hasNext()) {
            DeviceInterfaceItem interfaceItem = iter.next();
            if (!interfaceItem.getShutDown()) {
                interfaceItem.processInterface(getNetworkDevice().getHostName().toLowerCase(), siteName, subDomainName, domainName, dnsConfidenceLevel);
            }
        }
        log.trace("{} Leaving method", logPrefix);
    }

    public void OutputNewARecords(String aRecordDNSServer) {
        String logPrefix = "OutoutNewARecords() - ";
        log.trace("{}Entering method", logPrefix);

        Iterator<DeviceInterfaceItem> iter = getAllDefinedInterfaces().getDeviceInterfaces().listIterator();

        log.info("{}******************************************************", logPrefix);
        log.info("{}** The following A Records are good to create now   **", logPrefix);
        log.info("{}******************************************************", logPrefix);
        /**
         * ************************************************************************************
         *
         * Output "New" A Records
         *
         *************************************************************************************
         */
        while (iter.hasNext()) {
            DeviceInterfaceItem interfaceItem = iter.next();
            /* DNS Stuff here for output */
            if (interfaceItem.getDnsARecord() != null
                    && !interfaceItem.getShutDown()
                    && !isNull(interfaceItem.getIpAddress())
                    && !interfaceItem.getDnsARecordExists()) {
                System.out.println("Add-DnsServerResourceRecordA -ComputerName \"" + aRecordDNSServer + "\" -Name \"" + interfaceItem.getDnsARecordNamePart() + "\" -ZoneName \"" + interfaceItem.getDnsARecordZonePart() + "\" -AllowUpdateAny -IPv4Address \"" + interfaceItem.getIpAddress() + "\"");
            }
        }
        log.trace("{} Leaving method", logPrefix);
    }

    public void OutputExistingARecords() {
        String logPrefix = "OutputExistingARecords() - ";
        log.trace("{}Entering method", logPrefix);

        Iterator<DeviceInterfaceItem> iter = getAllDefinedInterfaces().getDeviceInterfaces().listIterator();

        /**
         * ************************************************************************************
         *
         * Output "Existing" A Records
         *
         *************************************************************************************
         */
        log.info("{}******************************************************", logPrefix);
        log.info("{}**     The following A Records Already Exist        **", logPrefix);
        log.info("{}******************************************************", logPrefix);
        while (iter.hasNext()) {
            DeviceInterfaceItem interfaceItem = iter.next();
            if (interfaceItem.getDnsARecord() != null
                    && !interfaceItem.getShutDown()
                    && !isNull(interfaceItem.getIpAddress())
                    && interfaceItem.getDnsARecordExists()) {
                log.info("{} {},{}", logPrefix, interfaceItem.getDnsARecord(), interfaceItem.getIpAddress());
            }
        }
        log.trace("{} Leaving method", logPrefix);
    }

    public void OutputConfidentPTRRecords() {
        String logPrefix = "OutputConfidentPTRRecords() - ";
        log.trace("{}Entering method", logPrefix);
        Iterator<DeviceInterfaceItem> iter = getAllDefinedInterfaces().getDeviceInterfaces().listIterator();

        /**
         * ************************************************************************************
         *
         * Output "Confident" PTR Records
         *
         *************************************************************************************
         */
        log.info("{}******************************************************", logPrefix);
        log.info("{}** The following PTR Records are good to create now **", logPrefix);
        log.info("{}**                (High Confidence)                 **", logPrefix);
        log.info("{}******************************************************", logPrefix);
        while (iter.hasNext()) {
            DeviceInterfaceItem interfaceItem = iter.next();
            /* DNS Stuff here for output */
            if (!isNull(interfaceItem.getDnsARecord())
                    && !interfaceItem.getShutDown()
                    && !isNull(interfaceItem.getIpAddress())
                    && interfaceItem.getDnsZoneConfident()
                    && isNull(interfaceItem.getCurrentDNSPTRRecord())) {
                System.out.println("Add-DnsServerResourceRecordPtr -ComputerName \"" + interfaceItem.getDnsSOAServer() + "\" -Name \"" + interfaceItem.getReverseName() + "\" -ZoneName \"" + interfaceItem.getDnsPTRZone() + "\" -AllowUpdateAny -PtrDomainName \"" + interfaceItem.getDnsARecord() + "\"");
            }
        }
        log.trace("{} Leaving method", logPrefix);
    }

    public void OutputUncertainPTRRecords() {
        String logPrefix = "OutputUncertainPTRRecords() - ";
        log.trace("{}Entering method", logPrefix);
        Iterator<DeviceInterfaceItem> iter = getAllDefinedInterfaces().getDeviceInterfaces().listIterator();

        /**
         * ************************************************************************************
         *
         * Output "Uncertain" PTR Records
         *
         *************************************************************************************
         */
        log.info("{}******************************************************", logPrefix);
        log.info("{}**     The following PTR Records are uncertain!     **", logPrefix);
        log.info("{}**    (Low Confidence, check reverse delegation)    **", logPrefix);
        log.info("{}******************************************************", logPrefix);
        while (iter.hasNext()) {
            DeviceInterfaceItem interfaceItem = iter.next();
            /* DNS Stuff here for output */
            if (!isNull(interfaceItem.getDnsARecord())
                    && !interfaceItem.getShutDown()
                    && !isNull(interfaceItem.getIpAddress())
                    && !interfaceItem.getDnsZoneConfident()) {
                log.info("{}Zone:{}, IP:{}, Name:{}, SOA:{}", logPrefix, interfaceItem.getDnsPTRZone(), interfaceItem.getIpAddress(), interfaceItem.getDnsARecord(), interfaceItem.getDnsSOAServer());
            }
        }
        log.trace("{}Leaving method", logPrefix);
    }

    public void OutputExistingPTRRecords() {
        String logPrefix = "OutputExistingPTRRecords() - ";
        log.trace("{}Entering method", logPrefix);
        Iterator<DeviceInterfaceItem> iter = getAllDefinedInterfaces().getDeviceInterfaces().listIterator();

        /**
         * ************************************************************************************
         *
         * Output Existing PTR Records
         *
         *************************************************************************************
         */
        log.info("{}******************************************************", logPrefix);
        log.info("{}**      The following PTR records already exist     **", logPrefix);
        log.info("{}******************************************************", logPrefix);
        while (iter.hasNext()) {
            DeviceInterfaceItem interfaceItem = iter.next();
            /* DNS Stuff here for output */
            if (!isNull(interfaceItem.getDnsARecord())
                    && !interfaceItem.getShutDown()
                    && !isNull(interfaceItem.getIpAddress())
                    && interfaceItem.getDnsZoneConfident()
                    && !isNull(interfaceItem.getCurrentDNSPTRRecord())) {
                log.info("{}Zone:{}, IP:{}, Name:{}, SOA:{},  CurrentPTR:{}", logPrefix, interfaceItem.getDnsPTRZone(), interfaceItem.getIpAddress(), interfaceItem.getDnsARecord(), interfaceItem.getDnsSOAServer(), interfaceItem.getCurrentDNSPTRRecord());
            }
        }
        log.trace("{} Leaving method", logPrefix);
    }

}

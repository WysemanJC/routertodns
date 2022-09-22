/*
 *   RouterToDNS - DeviceInterfaceItem.java
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
package com.slinkytoybox.routertodns.datamodel.items;

import static java.util.Objects.isNull;
import lombok.Data;

/**
 *
 * @author James Cowan <mrjamescowan@gmail.com>
 */
@Data
public class DeviceInterfaceItem {

    private String hardwarePort;
    private Boolean managementPort = false;
    private Boolean shutDown = false;
    private String vlan;
    private String interfaceName;
    private String ipAddress;
    private String vrrpNumber;
    private String vrrpAddress;
    private String netMask;
    private String description;
    private String vrf;

    /* Public here?? Dodgy */
    private DeviceDNSInfoItem dnsInfo;

    @java.lang.SuppressWarnings(value = "all")
    public DeviceInterfaceItem() {
        this.dnsInfo = new DeviceDNSInfoItem();
    }

    public void setinterfaceName(String ifName) {

        String ifType = "";
        String ifTemp = "";
        String ifNumber;
        String ifPreDot = "";
        String ifPostDot = "";

        if (ifName.contains("Port-channel")) {
            ifType = "po";
            ifTemp = ifName.substring(12);
        } else if (ifName.contains("Loopback")) {
            ifType = "lo";
            ifTemp = ifName.substring(8);
        } else if (ifName.contains("FastEthernet")) {
            ifType = "fe";
            ifTemp = ifName.substring(12);
        } else if (ifName.contains(
                "TenGigabitEthernet")) {
            ifType = "te";
            ifTemp = ifName.substring(18);
        } else if (ifName.contains(
                "FortyGigabitEthernet")) {
            ifType = "fo";
            ifTemp = ifName.substring(20);
        } else if (ifName.contains(
                "HundredGigabitEthernet")) {
            ifType = "hu";
            ifTemp = ifName.substring(25);
        } else if (ifName.contains(
                "GigabitEthernet")) {
            ifType = "gi";
            ifTemp = ifName.substring(15);
        } else if (ifName.contains(
                "MgmtEth")) {
            ifType = "mg";
            ifTemp = ifName.substring(7);
        } else if (ifName.contains(
                "TenGigE")) {
            ifType = "te";
            ifTemp = ifName.substring(7);
        } else if (ifName.contains(
                "HundredGigE")) {
            ifType = "hu";
            ifTemp = ifName.substring(11);
        } else if (ifName.contains(
                "Bundle-Ether")) {
            ifType = "bu";
            ifTemp = ifName.substring(12);
        }

        if (ifTemp.contains(
                ".")) {
            ifPreDot = ifTemp.substring(0, ifTemp.indexOf("."));
            ifPostDot = ifTemp.substring(ifTemp.indexOf(".") + 1);

            if (!ifPreDot.isEmpty()) {
                interfaceName = ifType.concat(ifPreDot);
            } else {
                interfaceName = ifType;
            }
            interfaceName = interfaceName.concat("-").concat(ifPostDot);
        } else {
            interfaceName = ifType.concat("-".concat(ifTemp));
        }

        interfaceName = interfaceName.replace("/", "-");

    }

    public void processInterface(String hostName, String siteName, String subDomainName, String domainName, Integer dnsConfidenceLevel) {

        String vrfName = "";
        String ifName = "";
        String outputHostName = "";

        if (!shutDown) {

            if (!isNull(vrf)) {
                vrfName = vrf.toLowerCase();
                vrfName = vrfName.replace("_", "-");
                vrfName = vrfName.concat("-");
            }

            if (!isNull(interfaceName)) {
                ifName = interfaceName;
            }

            outputHostName = vrfName.concat(ifName).concat(".".concat(hostName).concat(".".concat(siteName.concat(".".concat(subDomainName).concat(".").concat(domainName)))));

            if (!outputHostName.isEmpty()) {
                dnsInfo.setDnsARecord(outputHostName);
                dnsInfo.setDnsARecordNamePart(vrfName.concat(ifName).concat(".").concat(hostName).concat(".").concat(siteName).concat(".").concat(subDomainName));
                dnsInfo.setDnsARecordZonePart(domainName);
                if (!isNull(ipAddress) && ipAddress != "") {
                    dnsInfo.PopulateDNSData(ipAddress, dnsConfidenceLevel);
                }
            }
        }
    }

    public String getDnsARecord() {
        return dnsInfo.getDnsARecord();
    }

    public String getDnsARecordNamePart() {
        return dnsInfo.getDnsARecordNamePart();
    }

    public String getDnsARecordZonePart() {
        return dnsInfo.getDnsARecordZonePart();
    }

    public String getDnsSOAServer() {
        return dnsInfo.getDnsSOAServer();
    }

    public String getDnsPTRZone() {
        return dnsInfo.getDnsPTRZone();
    }

    public String getCurrentDNSPTRRecord() {
        return dnsInfo.getCurrentDNSPTRRecord();
    }

    public Boolean getDnsARecordExists() {
        return dnsInfo.getDnsARecordExists();
    }

    public Boolean getDnsZoneConfident() {
        return dnsInfo.getDnsZoneConfident();
    }

    public String getReverseName() {
        return dnsInfo.getReverseName();
    }

}

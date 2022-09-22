/*
 *   RouterToDNS - DeviceDNSInfoItem.java
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.xbill.DNS.Address;
import static org.xbill.DNS.Address.IPv4;
import static org.xbill.DNS.Address.toByteArray;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.SOARecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;
import org.xbill.DNS.lookup.LookupSession;

/**
 *
 * @author James Cowan <mrjamescowan@gmail.com>
 */

@Slf4j
@Data
public class DeviceDNSInfoItem {

    /* DNS Information for Interface*/
    private String dnsARecord;
    
    private String dnsARecordNamePart;
    
    private String dnsARecordZonePart;

    @Setter(AccessLevel.NONE)
    private String dnsPTRZone;

    @Setter(AccessLevel.NONE)
    private Boolean dnsZoneConfident;

    @Setter(AccessLevel.NONE)
    private String dnsSOAServer;

    @Setter(AccessLevel.NONE)
    private Boolean dnsARecordExists;

    @Setter(AccessLevel.NONE)
    private String currentDNSPTRRecord;

    @Setter(AccessLevel.NONE)
    private String reverseName;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Boolean foundSOA = false;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String lookupaddress = "";

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private int octetPosition;

    public void PopulateDNSData(String ipaddress, Integer dnsConfidenceLevel) {
        populateSOARecord(ipaddress, dnsConfidenceLevel);
        populateCurrentPTR(ipaddress);
        checkCurrentARecord(dnsARecord);
    }

    private void populateSOARecord(String ipaddress, Integer dnsConfidenceLevel) {
        String logPrefix = "populateSOARecord() - ";
        octetPosition = 4;

        // log.info("{} Got Address {}", logPrefix, ipaddress);
        while (!foundSOA && octetPosition > 0) {
            octetPosition = octetPosition - 1;

            lookupaddress = getLookupName(ipaddress, octetPosition);
            // log.info("{} Got Lookup Address {}", logPrefix, lookupaddress);

            LookupSession s = LookupSession.defaultBuilder().build();
            Name soaLookup = null;
            try {
                soaLookup = Name.fromString(lookupaddress);
            } catch (TextParseException ex) {
                log.error("{} Got Error {}", logPrefix, ex);
            }

            if (soaLookup != null) {
                try {
                    s.lookupAsync(soaLookup, Type.SOA)
                            .whenComplete((answers, ex) -> {
                                if (ex == null) {
                                    if (answers.getRecords().isEmpty()) {
                                        // log.info("{} {} has no SOA", logPrefix, lookupaddress);
                                    } else {
                                        for (Record rec : answers.getRecords()) {
                                            SOARecord soa = ((SOARecord) rec);
                                            log.debug("{} SOA Host for address {} = {} (Zone: {})", logPrefix, ipaddress, soa.getHost(), lookupaddress);
                                            dnsPTRZone = lookupaddress;
                                            dnsSOAServer = soa.getHost().toString().toLowerCase().substring(0, soa.getHost().toString().length() - 1);
                                            if (octetPosition > dnsConfidenceLevel-2) {
                                                dnsZoneConfident = true;
                                            } else {
                                                dnsZoneConfident = false;
                                            }
                                            reverseName = getReverseName(ipaddress, octetPosition);
                                            foundSOA = true;
                                        }
                                    }
                                } else {
                                    // ex.printStackTrace();
                                }
                            })
                            .exceptionally((ex) -> {
                                if (ex.getCause().getClass().isAssignableFrom(org.xbill.DNS.lookup.NoSuchDomainException.class)) {
                                    log.debug("{} Record Not Found ({})", logPrefix, lookupaddress);

                                } else {
                                    log.error("{} Got Exception {}", logPrefix, ex);
                                }
                                return null;
                            })
                            .toCompletableFuture()
                            .get();
                } catch (InterruptedException | ExecutionException ex) {
                    log.error("{} Got Exception {}", logPrefix, ex);
                }
            }
        }
    }

    private void populateCurrentPTR(String ipaddress) {
        String logPrefix = "populateCurrentPTR() - ";

        byte[] lookupaddress = toByteArray(ipaddress, IPv4);
        Name ptrLookup = null;

        try {
            ptrLookup = ReverseMap.fromAddress(InetAddress.getByAddress(lookupaddress));
        } catch (UnknownHostException ex) {
            log.error("{} {} Unknown Host", logPrefix, lookupaddress);
            return;
        }

        Record[] records = new Lookup(ptrLookup, Type.PTR).run();

        if (records != null) {
            log.trace("{} PTR for address {} = {} ", logPrefix, ipaddress, records[0].rdataToString());
            currentDNSPTRRecord = records[0].rdataToString().toString().toLowerCase();
        } else {
            log.trace("{} Null PTR lookup for {}", logPrefix, ipaddress);
        }
    }

    private void checkCurrentARecord(String aRecord) {
        String logPrefix = "checkCurrentARecord() - ";
        String currentIP = null;
        try {
            InetAddress addr = Address.getByName(aRecord);
            currentIP = addr.getHostAddress();
        } catch (UnknownHostException ex) {
            log.trace("{} No Address for {}", logPrefix, aRecord);
            dnsARecordExists = false;
            return;
        }
        dnsARecordExists = true;
        log.trace("{} Found Existing A record ({}) for {}", logPrefix, currentIP, aRecord);
    }

    private static String getLookupName(String ipaddress, int pos) {
        final String logPrefix = "getLookupName() - ";
        String ReverseSuffix = "in-addr.arpa";

        // log.info("{} ipaddress = {} ", logPrefix, ipaddress);
        String[] octets = ipaddress.split("\\.");
        String ReverseString = "";

        if (octets.length > 0) {
            int x = pos;

            while (x >= 0) {
                ReverseString = ReverseString.concat(octets[x]).concat(".");
                // log.info("{} ReverseName = {} ", logPrefix, ReverseName);

                x = x - 1;
            }
            ReverseString = ReverseString.concat(ReverseSuffix);
        } else {
            log.info("{} No Octets....", logPrefix);
        }

        return ReverseString;

    }

    private static String getReverseName(String ipaddress, int pos) {
        final String logPrefix = "getReverseName() - ";

        // log.info("{} ipaddress = {} ", logPrefix, ipaddress);
        String[] octets = ipaddress.split("\\.");
        String ReverseName = "";

        switch (pos) {
            case 0:
                ReverseName = octets[3].concat(".".concat(octets[2].concat(".").concat(octets[1])));
                break;
            case 1:
                ReverseName = octets[3].concat(".").concat(octets[2]);
                break;
            case 2:
                ReverseName = octets[3];
                break;
            default:
                log.info("{} So Confused....", logPrefix);
                return null;
        }

        return ReverseName;

    }

}

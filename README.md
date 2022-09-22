# routertodns
Generate Powershell DNS commands based on Cisco router interface configurations


This application takes input of a configruation file from a Cisco Router and outputs Powershell commands 
to create forward and reverse DNS records for each interface with an IP address.

It supports both IOS and IOS XR confguration files.

Config File: (application.properties)

dns.subdomain = network
dns.domain = domain.local
dns.arecordserver = corp_ad_server.domain.local
dns.zone.confidence.level=2

Usage:
java -jar RouterToDNS-0.0.1-SNAPSHOT.jar --config-file=<config filename> --site-name=<site>

Output for DNS hostname is in the format:

<vrf>-<interface>-<subinterface>.<configfile.hostname><cmdline.site>.<properties.dns.subdomain>.<properties.dns.domain>

e.g.
main-network-bu200-323.rtioseur01pr.EUROPE.network.domain.local

PTR records are created on the Authorative DNS server, so if you have some reverse zones delegated
to another AD domain, this will output the correct powershell commands based on SOA records.

domain confidence level in the application.properties file makes the application flag records as uncertain
if a reverse zone at the specified number of IP octets does not exist.
e.g. 
an interface with an address of 192.168.1.1 (1.1.168.192.in-addr.arpa)
would be marked as uncertain if the confidence level is 2 and the zone 192.in-addr.arpa existed but the zone 168.192.in-addr.arpa did not.
a confidence level of 3 would require the zone 1.168.192.in-addr.arpa
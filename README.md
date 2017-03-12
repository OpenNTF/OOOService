# OOOService
WebService to query the OOO status of a Domino user. Implemented as OSGi Plugin

Deploy the plugin into a Domino server (just copy into `DominoAppDir/osgi/eclipse/plugins`
and the new service will become available on http restart.

There are 2 endpoints available:

 - `/oooservice/q/`
 - `/oooservice/stats`
 
 The first allows to query the status of a user by providing Notes name or eMail address: `/oooservice/q/someuser@ibm.com`
 There is an optional parameter `?force=true` which will disable using a cached result. Output is JSON
 
 The second endpoint shows call statistics about the service using JarMon - results there are HTML
 
 The plugin also contributes to Statistics collected on the Domino server. They can be retrieved using `show stats OOO`

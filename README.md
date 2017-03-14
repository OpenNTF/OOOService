# OOOService
WebService to query the OOO status of a Domino user. Implemented as OSGi Plugin

Deploy the plugin into a Domino server (just copy into `DominoAppDir/osgi/eclipse/plugins`) and the new service will become available on http restart.

There are 2 endpoints available:

 - `/oooservice/q/`
 - `/oooservice/stats`
 
 The first allows to query the status of a user by providing Notes name or eMail address: `/oooservice/q/someuser@ibm.com`
 
 There are optional parameters for the first endpoint:

 - `?force=true` which will disable using a cached result.
 - `debug=true` will show some more fields in the output
  
 Output is JSON, roughly looks like this:

 ```
{
"user": "jane.doe@projectcastle.io",
"enabled": true,
"out": "2017-03-09T10:00:00.000-0600",
"in": "2017-03-15T11:00:00.000-0500",
"subject": "I am out of the office from Fri 03/10/2017 until Thu 03/16/2017.",
"body": "I'm gone fishing!",
"lastUpdate": "2017-03-13T02:22:44.413-0500"
}
 ```
 
If there is any error condition, the resulting JSON will have an `error` property.

 The second endpoint shows call statistics about the service using JarMon - results there are HTML
 
 The plugin also contributes to Statistics collected on the Domino server. They can be retrieved using `show stats OOO`

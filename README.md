# Java-LAN-Chat-Server
A chat server over the LAN using TCP.

This is a TCP chat server on the LAN. Users can either connect using telnet and specify the ip and port, (port is 4444) or they can use the Client.

Right now, the Client uses a hardcoded ip range, but that will be the first thing to change. What will be is that the Client will calculate the ip range of the LAN using the netmask and the current IP. The Client will then send a UDP broadcast to all the IPs, and the Server will respond.
Once the Client gets the response, it knows it has found the server, and will use it.

Once connected, the user can specify a username, and then chat with other users on the server.

The RedundantServer class is deprecated. It used to be for added flexibility. If the main Server wasn't detected, it would function as an independent server. However, when the main server comes online, it would forward all messages to the main server, connecting all of it's clients with that of the main server.
It is currently deprecated as it doesn't function with the current version of the server.

LANTest is just a playground where I test networking.

Client is where the actual networking of the client is done, and the ClientGUI is responsible for the GUI.
The ClientGUI gets run, and it will instantiate a client and connect using it.

Deliver was an experiment which hasn't been implemented yet. It will be used to automatically deliver updates to the client over the LAN.s
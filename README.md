# Routing-Simulation
Implement distance vector routing protocol by using the split horizon with poison reverse. 
First initialize the distance table by putting the initial cost from this node to its neighbor into the table and send the distance to its neighbor. After the node received the distance from its neighbor, it updates its distance to other nodes and find the minimum cost to each of them. Then it sends the minimum cost to its neighbor. If the minimum distance from one node to another node needs to pass through a node, then when this node sent its minimum cost to the node that it needs to pass through, it will set the distance to the destination into infinity, which is called poison reverse. The link handler will update the cost table like the rupdate function and then find the minimum cost to other others and send to its neighbor.

Compilation instructions:
javac Project3.java
java Project3

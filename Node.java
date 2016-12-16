import java.io.*;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is the class that students need to implement. The code skeleton is provided.
 * Students need to implement rtinit(), rtupdate() and linkhandler().
 * printdt() is provided to pretty print a table of the current costs for reaching
 * other nodes in the network.
 */ 
public class Node { 
    
    public static final int INFINITY = 9999;
    
    int[] lkcost;		/*The link cost between this node and other nodes*/
    int[][] costs;  	/*Define distance table*/
    int nodename;       /*Name of this node*/
    int[] mincosts;     /*The min cost of each node*/
    
    /* Class constructor */
    public Node() { }
    
    /* students to write the following two routines, and maybe some others */
    // Initialize the cost table of each node
    void rtinit(int nodename, int[] initial_lkcost) { 
    	int costNum = initial_lkcost.length;
    	
    	//Set up the cost and cost table
    	this.lkcost = new int[costNum];
    	this.costs = new int[costNum][costNum];
    	lkcost = initial_lkcost;
    	this.nodename = nodename;
       	this.mincosts = new int[costs.length];
       	
       	//set the initial cost as the min cost in the begining
       	for (int h = 0; h < lkcost.length; h++)
       	{
       		mincosts[h] = lkcost[h];
       	}
    	
    	
    	//Put the initial cost to the cost table and set other elements into INFINITY
    	for (int i = 0; i<costNum; i++)
    	{
    		for (int j = 0; j < costNum; j++)
    		{
    			if ( i == j)
    			{
    				costs[i][j] = initial_lkcost[j];
    			}
    			else
    			{
    				costs[i][j] = INFINITY;
    			}
    		}
    	}

    	
    	//Send the minimum cost to its neighbors
    	for (int k = 0; k<costNum; k++)
    	{
    		if (lkcost[k] != 0 && lkcost[k] != INFINITY)
    		{
    			Packet packet = new Packet(nodename, k, lkcost);
    			NetworkSimulator.tolayer2(packet);
    		}
    	}
    }    
    
    // Update the cost table of each node when it receive routing packet
    void rtupdate(Packet rcvdpkt) {
    	
    	//Extract the information from the packet
    	int[] rcvCost = rcvdpkt.mincost;
    	int source = rcvdpkt.sourceid;
    	int dest = rcvdpkt.destid;
    	boolean flag = false;
    	ArrayList<Integer> min_index = new ArrayList<Integer> ();
    	
    	//Add the received cost array into the cost table
    	for (int k = 0; k < costs.length; k++)
    	{
    		costs[k][source] = rcvCost[k] + costs[source][source];
    	}
    	
    	
    	// Update the cost table
    	ArrayList<Integer> costs_list = new ArrayList<Integer>();
    	for (int i = 0; i<costs.length; i++)
    	{
    		ArrayList<Integer> tmp = new ArrayList<Integer>();
    		//Find the minimum cost and add it into the array list
    		for (int j = 0; j < costs.length; j++)
    		{
    			tmp.add(costs[i][j]);
    		}
    		int min = Collections.min(tmp);
    		int minIndex = tmp.indexOf(min);
    		min_index.add(minIndex);
			costs_list.add(min);
    	}
    	
    	//check if the min cost of this node got updated
    	for (int n = 0; n < lkcost.length; n++)
    	{
    		if (costs_list.get(n) != mincosts[n])
    		{
    			mincosts[n] = costs_list.get(n);
    			flag = true;
    			System.out.println("Min cost is changed!");
    		}
    	}
    	
    	//Send the packets to its neighbor with poison reverse
    	if (flag == true)
    	{	
    		ArrayList<Integer> sent= new ArrayList<Integer> ();
    		ArrayList<Integer> need_change = new ArrayList<Integer> ();
    		ArrayList<int[]> change_packet = new ArrayList<int[]> ();
    		int[] tmp = new int[mincosts.length];
    		System.arraycopy(mincosts, 0, tmp, 0, mincosts.length);
    		
    		//Handle the case that need poison reverse
    		for (int b = 0; b<mincosts.length; b++)
    		{
    			if (min_index.get(b) != b)
    			{
    				tmp[b] = INFINITY;
    				if (!need_change.contains(min_index.get(b)))
    				{
    					need_change.add(min_index.get(b));
    					change_packet.add(tmp);
    				}
    				else if(need_change.contains(min_index.get(b)))
    				{
    					int index = need_change.indexOf(min_index.get(b));
    					change_packet.set(index, tmp);
    				}
    				
    			}
    		}
    		
    		for (int c = 0; c<need_change.size(); c++)
    		{
    			Packet packet = new Packet(nodename, need_change.get(c), change_packet.get(c));
    			if (lkcost[min_index.get(need_change.get(c))] != 0 && lkcost[min_index.get(need_change.get(c))] != INFINITY)
    			{
    				System.out.println("Packet sent to node " + need_change.get(c));
    				System.out.println("Packet is " + Arrays.toString(change_packet.get(c)));
    				NetworkSimulator.tolayer2(packet);
            		sent.add(min_index.get(need_change.get(c)));
    			}
    		}
    		
        	//If the packet doesn't need poison reverse, send the minimum cost to its neighbors
            for (int m = 0; m<costs.length; m++)
            {
            	if (lkcost[m] != 0 && lkcost[m] != INFINITY)
            	{
            		if (sent.contains(m))
            		{
            			
            		}
            		else
            		{
            			System.out.println("Packet sent to " + m);
            			System.out.println("Packet is " + Arrays.toString(mincosts));
            			Packet packet = new Packet(dest, m, mincosts);
                		NetworkSimulator.tolayer2(packet);
            		}
            	}
            }
    	}
    	System.out.println("Here's the distance table");
    	printdt();
    }
    
    
    /* called when cost from the node to linkid changes from current value to newcost*/
    void linkhandler(int linkid, int newcost) { 
    	System.out.println("The cost at node " + nodename +" to node " + linkid + " is changed to " + newcost);
    	lkcost[linkid] = newcost;
    	ArrayList<Integer> min_index = new ArrayList<Integer> ();
    	
    	for (int k = 0; k < costs.length; k++)
    	{
    		costs[k][linkid] = newcost + (costs[k][linkid]-costs[linkid][linkid]);
    	}
    	
    	// Update the cost table
    	ArrayList<Integer> costs_list = new ArrayList<Integer>();
    	for (int i = 0; i<costs.length; i++)
    	{
    		ArrayList<Integer> tmp = new ArrayList<Integer>();
    		//Find the minimum cost and add it into the array list
    		for (int j = 0; j < costs.length; j++)
    		{
    			tmp.add(costs[i][j]);
    		}
    		int min = Collections.min(tmp);
    		int minIndex = tmp.indexOf(min);
    		min_index.add(minIndex);
			costs_list.add(min);
    	}
    	
    	//Update the mincost table
    	for (int n = 0; n < lkcost.length; n++)
    	{
    		if (costs_list.get(n) != mincosts[n])
    		{
    			mincosts[n] = costs_list.get(n);
    		}
    	}
    	
    	//Handle the poison reverse case
    	ArrayList<Integer> sent= new ArrayList<Integer> ();
		ArrayList<Integer> need_change = new ArrayList<Integer> ();
		ArrayList<int[]> change_packet = new ArrayList<int[]> ();
		int[] tmp = new int[mincosts.length];
		System.arraycopy(mincosts, 0, tmp, 0, mincosts.length);
		
		for (int b = 0; b<mincosts.length; b++)
		{
			if (min_index.get(b) != b)
			{
				tmp[b] = INFINITY;
				if (!need_change.contains(min_index.get(b)))
				{
					need_change.add(min_index.get(b));
					change_packet.add(tmp);
				}
				else if(need_change.contains(min_index.get(b)))
				{
					int index = need_change.indexOf(min_index.get(b));
					change_packet.set(index, tmp);
				}
				
			}
		}
		
		for (int c = 0; c<need_change.size(); c++)
		{
			Packet packet = new Packet(nodename, need_change.get(c), change_packet.get(c));
			if (lkcost[min_index.get(need_change.get(c))] != 0 && lkcost[min_index.get(need_change.get(c))] != INFINITY)
			{
				System.out.println("Packet sent to node " + need_change.get(c));
				System.out.println("Packet is " + Arrays.toString(change_packet.get(c)));
				NetworkSimulator.tolayer2(packet);
        		sent.add(min_index.get(need_change.get(c)));
			}
		}
		
		//If the packet doesn't need poison reverse, send it to its neighbor
		for (int m = 0; m<costs.length; m++)
        {
        	if (lkcost[m] != 0 && lkcost[m] != INFINITY)
        	{
        		if (sent.contains(m))
        		{
        			
        		}
        		else
        		{
        			System.out.println("Packet sent to node " + m);
        			System.out.println("Packet is " + Arrays.toString(mincosts));
        			Packet packet = new Packet(nodename, m, mincosts);
            		NetworkSimulator.tolayer2(packet);
        		}
        	}
        }   
		System.out.println("Here's the distance table");
    	printdt();
    }    


    /* Prints the current costs to reaching other nodes in the network */
    void printdt() {
        switch(nodename) {
	
	case 0:
	    System.out.printf("                via     \n");
	    System.out.printf("   D0 |    1     2 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     1|  %3d   %3d \n",costs[1][1], costs[1][2]);
	    System.out.printf("dest 2|  %3d   %3d \n",costs[2][1], costs[2][2]);
	    System.out.printf("     3|  %3d   %3d \n",costs[3][1], costs[3][2]);
	    break;
	case 1:
	    System.out.printf("                via     \n");
	    System.out.printf("   D1 |    0     2    3 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d   %3d\n",costs[0][0], costs[0][2],costs[0][3]);
	    System.out.printf("dest 2|  %3d   %3d   %3d\n",costs[2][0], costs[2][2],costs[2][3]);
	    System.out.printf("     3|  %3d   %3d   %3d\n",costs[3][0], costs[3][2],costs[3][3]);
	    break;    
	case 2:
	    System.out.printf("                via     \n");
	    System.out.printf("   D2 |    0     1    3 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d   %3d\n",costs[0][0], costs[0][1],costs[0][3]);
	    System.out.printf("dest 1|  %3d   %3d   %3d\n",costs[1][0], costs[1][1],costs[1][3]);
	    System.out.printf("     3|  %3d   %3d   %3d\n",costs[3][0], costs[3][1],costs[3][3]);
	    break;
	case 3:
	    System.out.printf("                via     \n");
	    System.out.printf("   D3 |    1     2 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d\n",costs[0][1],costs[0][2]);
	    System.out.printf("dest 1|  %3d   %3d\n",costs[1][1],costs[1][2]);
	    System.out.printf("     2|  %3d   %3d\n",costs[2][1],costs[2][2]);
	    break;
        }
    }
    
}

package org.ctlv.proxmox.tester;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.RestClient;
import org.ctlv.proxmox.api.data.LXC;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.javafx.collections.MappingChange.Map;

public class Main {

	public static void main(String[] args) throws LoginException, JSONException, IOException, InterruptedException {

		ProxmoxAPI api = new ProxmoxAPI();
		JSONObject response = new JSONObject();
		JSONArray responseArray = new JSONArray();
		
		api.login();
		
		// Collect informations about a Proxmox server		
		// Memory Usage
		//System.out.println("Memory Usage : "+api.getNode("srv-px3").getMemory_used()/(1074000000.00)+" GiB");
		
		// List CTs per server
		/*for (int i=1; i<=10; i++) {
			String srv ="srv-px"+i;
			System.out.println("CTs under "+srv);
			List<LXC> cts = api.getCTs(srv);
			
			for (LXC lxc : cts) {
				System.out.println("\t" + lxc.getName());
			}
		}*/
		
		// Delete our CTs in a given server
		/*String srv = "srv-px3";
		List<LXC> cts = api.getCTs(srv);
		for (LXC lxc : cts) {
			if (Integer.parseInt(lxc.getVmid())/100 == 45) {	
				if (lxc.getStatus().equals("running")) {
					api.stopCT(srv, lxc.getVmid());
					TimeUnit.SECONDS.sleep(3);
				}
				api.deleteCT(srv, lxc.getVmid());
				System.out.println(lxc.getVmid() + " deleted ! \n");
			}
		}*/

		//***************************************************************************************
		// Create randomly CTs on srv-px3 and srv-px4 and manage them : stop and migration
		
		String basename = "ct-tpgei-virt-bD5-ct";
		
		int ctId = 4500;
		int repartition = 1;
		int periode = 10;

		String srv1 = "srv-px3";
		String srv2 = "srv-px4";
		String oldest_1 = " ";
		String oldest_2 = " ";
		
		long memory_srv1 = (api.getNode(srv1).getMemory_used()/api.getNode(srv1).getMemory_total())*100;
		long memory_srv2 = (api.getNode(srv2).getMemory_used()/api.getNode(srv2).getMemory_total())*100;
		
		List<LXC> cts1 = new ArrayList<LXC>(); 
		cts1 = api.getCTs(srv1);
		List<LXC> cts2 = new ArrayList<LXC>();
		cts2 = api.getCTs(srv2);
		
		// Delete our CTs on srv1
		for (LXC lxc : cts1) {
			if (Integer.parseInt(lxc.getVmid())/100 == 45) {	
				if (lxc.getStatus().equals("running")) {
					api.stopCT(srv1, lxc.getVmid());
					TimeUnit.SECONDS.sleep(3);
				}
				api.deleteCT(srv1, lxc.getVmid());
				System.out.println(lxc.getVmid() + " deleted ! \n");
			}
		}
		
		// Delete our CTs on srv2
		for (LXC lxc : cts2) {
			if (Integer.parseInt(lxc.getVmid())/100 == 45) {	
				if (lxc.getStatus().equals("running")) {
					api.stopCT(srv2, lxc.getVmid());
					TimeUnit.SECONDS.sleep(3);
				}
				api.deleteCT(srv2, lxc.getVmid());
				System.out.println(lxc.getVmid() + " deleted ! \n");
			}
		}		
		
		// Create and manage CTs
		while (true) {	
			// update CTs lists
			cts1 = api.getCTs(srv1);
			cts2 = api.getCTs(srv2);
			
			//update RAM info
			memory_srv1 = (api.getNode(srv1).getMemory_used()/api.getNode(srv1).getMemory_total())*100;
			memory_srv2 = (api.getNode(srv2).getMemory_used()/api.getNode(srv2).getMemory_total())*100;
			
			// SRV1
			if (memory_srv1 > 12) {
				// stop oldest CT
				long uptime_max = 0;
				for (LXC lxc : cts1) {
					if (Integer.parseInt(lxc.getVmid())/100 == 45) {
						if (uptime_max < lxc.getUptime()) {
							uptime_max = lxc.getUptime();
							oldest_1 = lxc.getVmid();
						}
					}
				}
				api.stopCT(srv1, oldest_1);
				System.out.println("CT stopped on px3!");
				TimeUnit.SECONDS.sleep(3);				
			}
			
			if (memory_srv1 < 16 && (repartition == 1 || repartition == 2)) {
				// random creation of CTs
				api.createCT(srv1, Integer.toString(ctId), basename+Integer.toString(ctId), 512);
				System.out.println("CT created on px3!");
				// starting of the CT
				TimeUnit.SECONDS.sleep(32);
				api.startCT(srv1, Integer.toString(ctId));
				System.out.println("CT started !");
			}
				
			// SRV2
			if (memory_srv2 > 12) {
				// stop oldest CT
				long uptime_max = 0;
				for (LXC lxc : cts2) {
					if (Integer.parseInt(lxc.getVmid())/100 == 45) {
						if (uptime_max < lxc.getUptime()) {
							uptime_max = lxc.getUptime();
							oldest_2 = lxc.getVmid();
						}
					}
				}
				api.stopCT(srv2, oldest_2);
				System.out.println("CT stopped on px4!");
				TimeUnit.SECONDS.sleep(3);
			}
			if (memory_srv2 < 16 && repartition == 3) {
				// random creation of CTs
				api.createCT(srv2, Integer.toString(ctId), basename+Integer.toString(ctId), 512);
				System.out.println("CT created on px4!");
				// starting of the CT
				TimeUnit.SECONDS.sleep(32);
				api.startCT(srv2, Integer.toString(ctId));
				System.out.println("CT started !");
				repartition = 0;
			}
			
			// migrate if one of the server has its ram over 8%
			if (memory_srv1 > 8 || memory_srv2 > 8) {
				if (memory_srv1 >= memory_srv2) {
					api.stopCT(srv1, oldest_1);
					System.out.println("CT stopped on px3!");
					api.wait(3);
					api.migrateCT(srv1, oldest_1, srv2);
					System.out.println("CT migrated on px4!");
					TimeUnit.SECONDS.sleep(10);
					System.out.println("CT started on px4!");
					api.startCT(srv2, oldest_1);					
				}
				else {
					api.stopCT(srv2, oldest_2);
					System.out.println("CT stopped on px4!");
					api.wait(3);
					api.migrateCT(srv2, oldest_2, srv1);
					System.out.println("CT migrate on px3!");
					TimeUnit.SECONDS.sleep(10);
					System.out.println("CT started on px3!");
					api.startCT(srv1, oldest_2);					
				}				
			}
			
			repartition++;
			
			if (ctId == 4599) {ctId = 4500 ;}
			else {ctId++;}
			
			TimeUnit.SECONDS.sleep(5);
		}
		
		//*****************************************************************************************
		
		/*while (memory_srv1 < 16 && memory_srv2 < 16) {
			// random creation of CTs
			if (repartition == 1 || repartition == 2) {
				api.createCT(srv1, Integer.toString(ctId), basename+Integer.toString(ctId), 512);
				System.out.println("CT created on px3!");
				// starting of the CT
				TimeUnit.SECONDS.sleep(32);
				api.startCT(srv1, Integer.toString(ctId));
				System.out.println("CT started !");
				repartition++;
			}
			else {
				api.createCT(srv2, Integer.toString(ctId), basename+Integer.toString(ctId), 512);
				System.out.println("CT created on px4!");
				// starting of the CT
				TimeUnit.SECONDS.sleep(32);
				api.startCT(srv2, Integer.toString(ctId));
				System.out.println("CT started !");
				repartition = 1;
			}
						
			if (ctId == 4599) {ctId = 4500 ;}
			else {ctId++;}
			
			TimeUnit.SECONDS.sleep(periode);
			
			memory_srv1 = (api.getNode(srv1).getMemory_used()/api.getNode(srv1).getMemory_total())*100;
			memory_srv2 = (api.getNode(srv2).getMemory_used()/api.getNode(srv2).getMemory_total())*100;	
			
		}*/

		// Create a CT
		//api.createCT("srv-px3", "999", "ct-tpiss-virt-D5-ct3", 512);
		
		// Information about our CT
		//api.startCT("srv-px3", "4500");
		
		// Information about a container
		/*System.out.println("Status : "+api.getCT("srv-px3", "999").getStatus());
		System.out.println("CPU Usage : "+api.getCT("srv-px3", "999").getCpu()*100+" %");
		System.out.println("Disk usage : "+api.getCT("srv-px3", "999").getDisk()/(1074000000.00)+" GiB");
		System.out.println("Memory usage : "+api.getCT("srv-px3", "999").getMem()/(1074000000.00)+" GiB");
		System.out.println(api.getNode("srv-px3"));*/
		
		//api.stopCT("srv-px3", "4000");
		//api.wait(3);
		//api.migrateCT("srv-px3", "4000", "srv-px4");
		/*List<String> listContainers = new ArrayList<String>();
		List<String> listContainers2 = new ArrayList<String>();
		listContainers.addAll(api.getCTList("srv-px3"));
		listContainers2.addAll(api.getCTList("srv-px4"));
		
		
		
		System.out.println(listContainers.size());
		System.out.println(listContainers2.size());
		*/
		

		/*api.createCT("srv-px4", "264", "ct-tpiss-virt-D5-ct2", 512);*/
		
		/*double cpuPX3 = api.getNode("srv-px3").getCpu()*100.0;
		double cpuPX4 = api.getNode("srv-px4").getCpu()*100.0;
		
		if(cpuPX3 > 8 && cpuPX4 > 8) {
			
			if(cpuPX3 > 12) {
				
				api.stopCT("srv-px3", "4000");
			}
			
			if(cpuPX4 > 12) {
				// Del oldest CT
			}
			
		} else if(cpuPX3 > 8 && cpuPX4 <= 8) {
			// Migrate from PX3 to PX4
			
			
		} else if(cpuPX3 <= 8 && cpuPX4 > 8) {
			// Migrate from PX4 to PX3
			
			
		} else {
			// Do nothing
			
		}
		
		// Supprimer un CT
		//api.deleteCT("srv-px1", "4000");*/
		
	}

}

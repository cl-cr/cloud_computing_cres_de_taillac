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


public class Main2 {

	public static void main(String[] args) throws LoginException, JSONException, IOException, InterruptedException {

		ProxmoxAPI api = new ProxmoxAPI();
		JSONObject response = new JSONObject();
		JSONArray responseArray = new JSONArray();
		
		api.login();
		
		// Collect informations about a Proxmox server
		
		// CPU Usage
		//System.out.println("CPU Usage : "+api.getNode("srv-px3").getCpu()*100+"%");
		
		// Disk Usage
		//System.out.println("Disk Usage : "+api.getNode("srv-px3").getRootfs_used()/(1074000000.00)+" GiB");
		
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
		
		// Delete CTs in a given server
		String srv = "srv-px3";
		List<LXC> cts = api.getCTs(srv);
		for (LXC lxc : cts) {
			if (lxc.getStatus().equals("running")) {
				api.stopCT(srv, lxc.getVmid());
				TimeUnit.SECONDS.sleep(3);
			}
			api.deleteCT(srv, lxc.getVmid());
			System.out.println(lxc.getVmid() + " deleted ! \n");
		}
		
		
		// Create a CT
		//api.createCT("srv-px3", "999", "ct-tpiss-virt-D5-ct3", 512);
		
		// Information about our CT
		//{"data":"UPID:srv-px3:000077FB:5C4901AC:5C38B2E1:vzcreate:4000:bartouil@Ldap-INSA:"}
		//api.startCT("srv-px3", "999");
		
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
		

		/*api.createCT("srv-px4", "264", "ct-tpiss-virt-D5-ct2", 512);
		
		double cpuPX3 = api.getNode("srv-px3").getCpu()*100.0;
		double cpuPX4 = api.getNode("srv-px4").getCpu()*100.0;
		
		if(cpuPX3 > 8 && cpuPX4 > 8) {
			
			if(cpuPX3 > 12) {
				// Stop oldest CT
			}
			
			if(cpuPX4 > 12) {
				// Stop oldest CT
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

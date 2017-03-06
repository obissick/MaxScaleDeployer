/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maxscaledeployer;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.InputStream;
import java.util.Scanner;

/**
 *
 * @author obissick
 */
public class MaxScaleDeployer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Scanner in = new Scanner(System.in);
        int numServers;
        int numDBServers;
        int exitStatus = 0;
        IPAddressValidator ipIsValid = new IPAddressValidator();
        String commands[] = {
            "sudo yum install -y corosync pcs pacemaker maxscale", 
            "sudo passwd hacluster",
            "sudo systemctl start pcsd",
            "sudo pcs cluster auth",
            "sudo pcs cluster setup --name clustername",
            "sudo pcs cluster start --all",
            "sudo pcs property set stonith-enabled=false",
            "sudo pcs property set no-quorum-policy=ignore",            
        };
        
        System.out.println("Enter number of servers to install MaxScale: ");
        numServers = in.nextInt();
        in.nextLine();
        
        if(numServers>=1){
            Server servers[] = new Server[numServers];

            for(int i = 0; i < numServers; i++){
                servers[i] = new Server();
                do{
                    System.out.println("Enter host(IP) "+(i+1));
                    servers[i].setHost(in.nextLine());
                }while(!ipIsValid.validate(servers[i].getHost()));
                System.out.println("Enter username "+(i+1));
                servers[i].setUser(in.nextLine());
                System.out.println("Enter password "+(i+1));
                servers[i].setpassword(in.nextLine());                                         
            }
            System.out.println("Enter number of Database servers to add to MaxScale: ");
            numDBServers = in.nextInt();
            in.nextLine();

            DBServer dbServers[] = new DBServer[numDBServers];

            for(int i = 0; i < numDBServers; i++){
                dbServers[i] = new DBServer();
                do{
                System.out.println("Enter host "+(i+1));
                dbServers[i].setHost(in.nextLine());
                }while(ipIsValid.validate(dbServers[i].getHost()));
                System.out.println("Enter port "+(i+1));
                dbServers[i].setPort(in.nextInt());
                System.out.println("Enter username "+(i+1));
                dbServers[i].setUser(in.nextLine());
                System.out.println("Enter password "+(i+1));
                dbServers[i].setpassword(in.nextLine());   
            }
            
            System.out.println("Installing pacemaker and maxscale on each node..");
            for(Server server : servers){
                exitStatus = runCom(server.getHost(),server.getUser(),server.getPassword(),commands[0]);
                if(exitStatus != 0){
                    break;
                }
            }
            for(DBServer server: dbServers){
                System.out.println(server.toString());
            }
        }else{
            System.out.println("You did not enter valid number of servers.");
        }
        
    }
    
    private static int runCom(String host, String user, String password, String command) {
        int exitStat = 0;
        
        try{
	    	
	    	java.util.Properties config = new java.util.Properties(); 
	    	config.put("StrictHostKeyChecking", "no");
	    	JSch jsch = new JSch();
	    	Session session = jsch.getSession(user, host, 22);
	    	session.setPassword(password);
	    	session.setConfig(config);
	    	session.connect();
	    	System.out.append("Running command..."+"\n");
	    	
	    	Channel channel=session.openChannel("exec");
	        ((ChannelExec)channel).setCommand(command);
	        channel.setInputStream(null);
	        ((ChannelExec)channel).setErrStream(System.err);
	        
	        InputStream in = channel.getInputStream();
	        channel.connect();
	        byte[] tmp = new byte[1024];
	        while(true){
	          while(in.available()>0){
	            int i = in.read(tmp, 0, 1024);
	            if(i<0)break;
	            System.out.append(new String(tmp, 0, i));
	          }
	          if(channel.isClosed()){
	            exitStat = channel.getExitStatus();
	            break;
	          }
	          try{
                      Thread.sleep(1000);
                  }catch(Exception ee){
                  
                  }
	        }
	        channel.disconnect();
	        session.disconnect();
	    }catch(Exception e){
	    	System.out.append(e.getMessage());
	    }
            return exitStat;
	}
    
}

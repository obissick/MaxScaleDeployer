/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maxscaledeployer;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.File;
import java.io.FileInputStream;
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
        String serverList = "";
        String hacluster;
        String vip;
        IPAddressValidator ipIsValid = new IPAddressValidator();
        String maxScaleLink = "https://downloads.mariadb.com/MaxScale/1.4.5/centos/7Server/x86_64/maxscale-1.4.5-1.centos.7.x86_64.rpm";
        MaxConfig maxscaleConfig;
        String commands[] = {
            "sudo yum install -y corosync pcs pacemaker", //0
            "sudo systemctl start pcsd", //1
            "sudo pcs cluster auth", //2
            "sudo pcs cluster setup --name maxscale", //3
            "sudo pcs cluster start --all", //4
            "sudo pcs property set stonith-enabled=false", //5
            "sudo pcs property set no-quorum-policy=ignore", //6
            "sudo pcs resource defaults resource-stickiness=100", //7
            "sudo systemctl stop maxscale", //8
            "sudo systemctl disable maxscale", //9
            "sudo pcs constraint colocation add maxscale_service virtual_ip INFINITY", //10
            "sudo pcs constraint order virtual_ip then maxscale_service", //11
            "sudo pcs cluster stop --all && sudo pcs cluster start --all", //12
            "sudo systemctl enable pcsd", //13
            "sudo systemctl enable corosync", //14
            "sudo systemctl enable pacemaker" //15
        };
        
        System.out.println("Enter number of servers to install MaxScale: ");
        numServers = in.nextInt();
        in.nextLine();
        
        if(numServers>=1){
            Server servers[] = new Server[numServers];

            for(int i = 0; i < numServers; i++){
                servers[i] = new Server();
                do{
                    System.out.println("Enter host(IP): "+(i+1));
                    servers[i].setHost(in.nextLine());
                    if(!ipIsValid.validate(servers[i].getHost())){
                        System.out.println("Enter Valid IP address.");
                    }
                }while(!ipIsValid.validate(servers[i].getHost()));
                System.out.println("Enter username: "+(i+1));
                servers[i].setUser(in.nextLine());
                System.out.println("Enter password: "+(i+1));
                servers[i].setPassword(in.nextLine());                                         
            }
            System.out.println("Enter number of Database servers to add to MaxScale: ");
            numDBServers = in.nextInt();
            in.nextLine();

            DBServer dbServers[] = new DBServer[numDBServers];

            for(int i = 0; i < numDBServers; i++){
                dbServers[i] = new DBServer();
                do{
                    System.out.println("Enter host: "+(i+1));
                    dbServers[i].setHost(in.nextLine());
                    if(!ipIsValid.validate(dbServers[i].getHost())){
                        System.out.println("Enter Valid IP address.");
                    }
                }while(!ipIsValid.validate(dbServers[i].getHost()));
                System.out.println("Enter MySQL username: "+(i+1));
                dbServers[i].setUser(in.nextLine());
                System.out.println("Enter MySQL password: "+(i+1));
                dbServers[i].setPassword(in.nextLine());
                System.out.println("Enter MySQL port: "+(i+1));
                dbServers[i].setPort(in.nextInt());
                in.nextLine();
            }
            System.out.println("Enter desired hacluster user password: ");
            hacluster = in.nextLine();
            System.out.println("Enter virtual IP for cluster: ");
            vip = in.nextLine();
            maxscaleConfig = new MaxConfig(dbServers);
            System.out.println("Installing corosync, pcs, pacemaker and maxscale on each node..");
            for(Server server : servers){
                serverList += " " + server.getHost();
                exitStatus = runCom(server.getHost(),server.getUser(),server.getPassword(),commands[0]);
                if(exitStatus != 0){
                    break;
                }
                runCom(server.getHost(),server.getUser(),server.getPassword(),"sudo yum install -y "+maxScaleLink);
                runCom(server.getHost(),server.getUser(),server.getPassword(),"echo "+hacluster+" | passwd --stdin hacluster");
                System.out.println("Starting cluster...");
                transferFile(server, "maxscale.cnf");
                runCom(server.getHost(),server.getUser(),server.getPassword(),commands[1]);
            }
            System.out.println("Authenticating cluster...");
            runCom(servers[0].getHost(),servers[0].getUser(),servers[0].getPassword(),commands[2]+serverList +" -u hacluster -p "+hacluster);
            System.out.println("Creating cluster...");
            runCom(servers[0].getHost(),servers[0].getUser(),servers[0].getPassword(),commands[3]+serverList);
            System.out.println("Starting cluster...");
            runCom(servers[0].getHost(),servers[0].getUser(),servers[0].getPassword(),commands[4]);
            System.out.println("Setting quorum...");
            runCom(servers[0].getHost(),servers[0].getUser(),servers[0].getPassword(),commands[5]);
            runCom(servers[0].getHost(),servers[0].getUser(),servers[0].getPassword(),commands[6]);
            System.out.println("Setting up virtual IP...");
            runCom(servers[0].getHost(),servers[0].getUser(),servers[0].getPassword(),
                    "sudo pcs resource create virtual_ip ocf:heartbeat:IPaddr2 ip="+vip+" cidr_netmask=24 op monitor interval=30s");
            runCom(servers[0].getHost(),servers[0].getUser(),servers[0].getPassword(),commands[7]);
            System.out.println("Creating MaxScale service resource...");
            runCom(servers[0].getHost(),servers[0].getUser(),servers[0].getPassword(),
                    "sudo pcs resource create maxscale_service systemd:maxscale op monitor interval=\"10s\" timeout=\"15s\" op start interval=\"0\" timeout=\"15s\" op stop interval=\"0\" timeout=\"30s\"");
            for(Server server: servers){
                runCom(server.getHost(),server.getUser(),server.getPassword(),commands[8]);
                runCom(server.getHost(),server.getUser(),server.getPassword(),commands[9]);
                runCom(server.getHost(),server.getUser(),server.getPassword(),commands[13]);
                runCom(server.getHost(),server.getUser(),server.getPassword(),commands[14]);
                runCom(server.getHost(),server.getUser(),server.getPassword(),commands[15]);
            }
            runCom(servers[0].getHost(),servers[0].getUser(),servers[0].getPassword(),commands[10]);
            runCom(servers[0].getHost(),servers[0].getUser(),servers[0].getPassword(),commands[11]);
            System.out.println("Restarting cluster...");
            runCom(servers[0].getHost(),servers[0].getUser(),servers[0].getPassword(),commands[12]);
            
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
    
    private static void transferFile(Server server, String file){
        String SFTPHOST = server.getHost();
        int    SFTPPORT = 22;
        String SFTPUSER = server.getUser();
        String SFTPPASS = server.getPassword();
        String SFTPWORKINGDIR = "/etc/";

        Session     session     = null;
        Channel     channel     = null;
        ChannelSftp channelSftp = null;

        try{
            JSch jsch = new JSch();
            session = jsch.getSession(SFTPUSER,SFTPHOST,SFTPPORT);
            session.setPassword(SFTPPASS);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            channel = session.openChannel("sftp");
            channel.connect();
            channelSftp = (ChannelSftp)channel;
            channelSftp.cd(SFTPWORKINGDIR);
            File f = new File(file);
            channelSftp.put(new FileInputStream(f), f.getName());
        }catch(Exception ex){
        ex.printStackTrace();
        }
    }
    
}

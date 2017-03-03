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
    }
    
    private void runCom(String host, String user, String password, String command1) {
        try{
	    	
	    	java.util.Properties config = new java.util.Properties(); 
	    	config.put("StrictHostKeyChecking", "no");
	    	JSch jsch = new JSch();
	    	Session session=jsch.getSession(user, host, 22);
	    	session.setPassword(password);
	    	session.setConfig(config);
	    	session.connect();
	    	System.out.append("Running command..."+"\n");
	    	
	    	Channel channel=session.openChannel("exec");
	        ((ChannelExec)channel).setCommand(command1);
	        channel.setInputStream(null);
	        ((ChannelExec)channel).setErrStream(System.err);
	        
	        InputStream in=channel.getInputStream();
	        channel.connect();
	        byte[] tmp=new byte[1024];
	        while(true){
	          while(in.available()>0){
	            int i=in.read(tmp, 0, 1024);
	            if(i<0)break;
	            System.out.append(new String(tmp, 0, i));
	          }
	          if(channel.isClosed()){
	            //result.append("exit-status: "+channel.getExitStatus() + "\n");
	            break;
	          }
	          try{
                      Thread.sleep(1000);
                  }catch(Exception ee){
                  
                  }
	        }
	        channel.disconnect();
	        session.disconnect();
	        //result.append("DONE");
	    }catch(Exception e){
	    	System.out.append(e.getMessage());
	    }

	}
    
}

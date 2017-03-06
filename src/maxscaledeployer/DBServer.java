/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maxscaledeployer;

/**
 *
 * @author obissick
 */
public class DBServer extends Server{
    
    private int port;
    
    DBServer(){
        
    }
    DBServer(String host, int port, String user, String password){
        super(host,user,password);     
        this.port = port;
    }

    public void setPort(int port){
        this.port = port;
    }

    public int getPort(){
        return this.port;
    }
    
    @Override
    public String toString(){
        return "host: " + this.getHost() + " port: " + this.getPort() + " username: " + this.getUser() + " password: " + this.getPassword();
    }
}

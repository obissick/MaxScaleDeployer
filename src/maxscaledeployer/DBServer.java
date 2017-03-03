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
public class DBServer {
    private String hostname;
    private int port;
    private String username;
    private String password;
    
    DBServer(){
        
    }
    DBServer(String host, int port, String user, String password){
        this.hostname = host;
        this.port = port;
        this.username = user;
        this.password = password;
    }
    
    public void setHost(String host){
        this.hostname = host;
    }
    public void setPort(int port){
        this.port = port;
    }
    public void setUser(String user){
        this.username = user;
    }
    public void setpassword(String password){
        this.password = password;
    }
    
    public String getHost(){
        return this.hostname;
    }
    public int getPort(){
        return this.port;
    }
    public String getUser(){
        return this.username;
    }
    public String getPassword(){
        return this.password;
    }
}

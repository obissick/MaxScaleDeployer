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
public class Server {
    private String hostname;
    private String username;
    private String password;
    
    Server(){
        
    }
    Server(String host, String user, String password){
        this.hostname = host;
        this.username = user;
        this.password = password;
    }
    
    public void setHost(String host){
        this.hostname = host;
    }
    public void setUser(String user){
        this.username = user;
    }
    public void setPassword(String password){
        this.password = password;
    }
    
    public String getHost(){
        return this.hostname;
    }
    public String getUser(){
        return this.username;
    }
    public String getPassword(){
        return this.password;
    }
    
    @Override
    public String toString(){
        return "host: " + this.getHost() + " username: " + this.getUser() + " password: " + this.getPassword();
    }
}

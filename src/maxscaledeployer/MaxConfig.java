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
public class MaxConfig {
    private String conf ; 
    private String hosts ="";
    private String serversList="";
    
    MaxConfig(DBServer[] servers){
        
        for(int i =0; i < servers.length;i++){
            hosts += "[server"+i+"]\n";
            hosts += servers[i].toString()+"\n";
            serversList += "server"+i+",";
        }
        this.conf = "# MaxScale documentation on GitHub:\n" +
            "# https://github.com/mariadb-corporation/MaxScale/blob/master/Documentation/Documentation-Contents.md\n" +
            "\n" +
            "# Global parameters\n" +
            "#\n" +
            "# Complete list of configuration options:\n" +
            "# https://github.com/mariadb-corporation/MaxScale/blob/master/Documentation/Getting-Started/Configuration-Guide.md\n" +
            "\n" +
            "[maxscale]\n" +
            "threads=1\n" +
            "\n" +
            "# Server definitions\n" +
            "#\n" +
            "# Set the address of the server to the network\n" +
            "# address of a MySQL server.\n" +
            "#\n" +
            "\n" +
            hosts +
            "\n" +
            "#\n" +
            "# This will keep MaxScale aware of the state of the servers.\n" +
            "# MySQL Monitor documentation:\n" +
            "# https://github.com/mariadb-corporation/MaxScale/blob/master/Documentation/Monitors/MySQL-Monitor.md\n" +
            "\n" +
            "[Galera Monitor]\n" +
            "type=monitor\n" +
            "module=galeramon\n" +
            "servers="+serversList +"\n" +
            "user="+servers[0].getUser()+"\n" +
            "passwd="+servers[0].getPassword()+"\n" +
            "monitor_interval=2000\n" +
            "disable_master_failback=1\n" +
            "available_when_donor=1\n" +
            "\n" +
            "# Service definitions\n" +
            "#\n" +
            "# Service Definition for a read-only service and\n" +
            "# a read/write splitting service.\n" +
            "#\n" +
            "\n" +
            "# ReadConnRoute documentation:\n" +
            "# https://github.com/mariadb-corporation/MaxScale/blob/master/Documentation/Routers/ReadConnRoute.md\n" +
            "\n" +
            "[Read-Only Service]\n" +
            "type=service\n" +
            "router=readconnroute\n" +
            "servers="+serversList+"\n" +
            "user="+servers[0].getUser()+"\n" +
            "passwd="+servers[0].getPassword()+"\n" +
            "router_options=slave\n" +
            "\n" +
            "# ReadWriteSplit documentation:\n" +
            "# https://github.com/mariadb-corporation/MaxScale/blob/master/Documentation/Routers/ReadWriteSplit.md\n" +
            "\n" +
            "[Read-Write Service]\n" +
            "type=service\n" +
            "router=readwritesplit\n" +
            "servers="+serversList+"\n" +
            "user="+servers[0].getUser()+"\n" +
            "passwd="+servers[0].getPassword()+"\n" +
            "max_slave_connections=100%\n" +
            "\n" +
            "# This service enables the use of the MaxAdmin interface\n" +
            "# MaxScale administration guide:\n" +
            "# https://github.com/mariadb-corporation/MaxScale/blob/master/Documentation/Reference/MaxAdmin.md\n" +
            "\n" +
            "[MaxAdmin Service]\n" +
            "type=service\n" +
            "router=cli\n" +
            "\n" +
            "# Listener definitions for the services\n" +
            "#\n" +
            "# These listeners represent the ports the\n" +
            "# services will listen on.\n" +
            "#\n" +
            "\n" +
            "[Read-Only Listener]\n" +
            "type=listener\n" +
            "service=Read-Only Service\n" +
            "protocol=MySQLClient\n" +
            "port=3307\n" +
            "\n" +
            "[Read-Write Listener]\n" +
            "type=listener\n" +
            "service=Read-Write Service\n" +
            "protocol=MySQLClient\n" +
            "port=3306\n" +
            "\n" +
            "[MaxAdmin Listener]\n" +
            "type=listener\n" +
            "service=MaxAdmin Service\n" +
            "protocol=maxscaled\n" +
            "port=6603";
    }
    
    public String getConfig(){
        return this.conf;
    }
}

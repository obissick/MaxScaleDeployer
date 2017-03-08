# MaxScaleDeployer
This tool:
 1. Installs corosync, pcs, pacemaker and maxscale on 1..n servers.
 2. Setup cluster service between hosts.
 3. Setup MaxScale config based on provided DB servers.
 4. Setup VIP for HA.
 5. Setup MaxScale service for HA.

Requirements:
- Java version 8 or higher
- Servers must be CentOS 7

Run Deployer:
<code>java -jar MaxScaleDeployer.jar</code>

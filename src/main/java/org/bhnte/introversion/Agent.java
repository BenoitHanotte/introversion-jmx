package org.bhnte.introversion;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Random;

public class Agent {

    private static final Random random = new Random();
    private static final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

    private Agent() {
    }

    public static void agentmain(final String args, final Instrumentation instrumentation) {
        premain(args, instrumentation);
    }

    /**
     * Start the JVM profiler
     * @param args
     * @param instrumentation
     */
    public static void premain(final String args, final Instrumentation instrumentation) {

        // generate random port
        int port = getJMXServerPort();

        try {
            // Start an RMI registry on the random port.
            System.out.println("Create RMI registry on port " + port);
            LocateRegistry.createRegistry(port);
        }
        catch (RemoteException e) {
            System.err.println("Unable to create registry on rmi port " + port);
            e.printStackTrace();
            return;
        }

        // build an environment map to set JMX properties
        System.out.println("Initializing environment map");
        HashMap<String, Object> env = getEnv(port);

        // hostname is only used as an indication to provide an url ready to be pasted in visualvm, but it may be wrong
        String address = getAddress();

        // start the JMX server
        try {
            String jmxUrl = "service:jmx:rmi:///jndi/rmi://:" + port + "/jmxrmi";
            System.out.println("Creating JMX server");
            final JMXConnectorServer jmxServer = createJMXServer(jmxUrl, env);
            System.out.println("Starting JMX server");
            jmxServer.start();
            String remoteUrl = address != null ? makeJmxUrl(address, port) : jmxUrl;
            System.out.println("Started JMX server at address \"" + remoteUrl + "\"");

            // register hook to shutdown the server at the end of the application
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        jmxServer.stop();
                        System.out.println("Stopped JMX server");
                    } catch (IOException e) {
                        System.err.println("Failed to stop JMX server");
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (Exception e) {
            System.err.println("Failed to start JMX server at port " + port);
            e.printStackTrace();
        }
    }

    private static String getAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            System.err.println("Unable to get host address");
            e.printStackTrace();
        }
        return null;
    }

    private static String makeJmxUrl(int port) {
        return "service:jmx:rmi:///jndi/rmi://:" + port + "/jmxrmi";
    }

    private static String makeJmxUrl(String hostname, int port) {
        return "service:jmx:rmi:///jndi/rmi://" + hostname + ":" + port + "/jmxrmi";
    }

    private static JMXConnectorServer createJMXServer(String jmxUrl, HashMap<String, Object> env) throws Exception {
        try {
            JMXServiceURL url = new JMXServiceURL(jmxUrl);
            return JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
        }
        catch (MalformedURLException e) {
            System.err.println("Incorrect URL for the JMX service: " + jmxUrl);
            e.printStackTrace();
            throw new IllegalArgumentException("Incorrect JMX url: " + jmxUrl);
        }
    }

    private static HashMap<String, Object> getEnv(int port) {
        HashMap<String,Object> env = new HashMap<String,Object>();
        env.put("com.sun.management.jmxremote.ssl", false);
        env.put("com.sun.management.jmxremote.authenticate", false);
        env.put("com.sun.management.jmxremote.port", port);
        env.put("java.net.preferIPv4Stack", true);
        return env;
    }

    private static int getJMXServerPort() {
        return (int)(1001 + Math.ceil(random.nextFloat() * 8998));
    }
}

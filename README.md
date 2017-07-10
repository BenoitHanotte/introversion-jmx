# Introversion jmx agent

Introversion-jmx is a simple __java agent__ that allows to attache and start a 
JMX server to a JVM using a random port between 1001 and 9999.

This simple agent was developed in order to profile distributed applications 
where multiple JVMs may be started on a given node, in which case it is 
necessary to assign different ports to the JMX servers attached to each JVM.

## Usage

### Basics

Add the java agent to you application using the `--javaagent` parameter 
as in the `HelloWorld sample application:

```
java -javaagent:introversion-jmx-1.0-SNAPSHOT.jar ...
````

### Using with a Spark application

Ship the package jar to the working directory of the Spark executors 
using the `files` configuration as below:

```
files="introversion-jmx-1.0-SNAPSHOT.jar"
```

and add the `--javaagent` parameter to the executor or driver JMV options
using the `spark.executor.extraJavaOptions` as following:

```
spark.executor.extraJavaOptions="-javaagent:introversion-jmx-1.0-SNAPSHOT.jar"
```

### Connecting to the agent

First, check your application log discover the random port attributed to the JVM instance
you wish to 

Use the visualvm shipped with the JDK or any other JMX monitoring application of
your choice and connect to 

```
service:jmx:rmi:///jndi/rmi://${THE_HOST}:7460/jmxrmi
```

## Build

Use maven to build the jar:

```
mvn clean package
```

## Security

There is currently no security enforced for the agent. For this reason, you should not 
use it in a publicly accessible environment.

You can add security by enabling authentification and encryption by modifying the 
`getEnv()` method in `Agent.java`. You may find a good example on how to do so on the 
following page: http://docs.oracle.com/javase/7/docs/technotes/guides/management/agent.html


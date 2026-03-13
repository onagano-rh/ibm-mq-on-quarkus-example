package org.acme.jmslistener;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.msg.client.jakarta.wmq.WMQConstants;

@ApplicationScoped
public class MqConnectionFactoryProvider {

    @ConfigProperty(name = "app.mq.host-name")
    String hostName;

    @ConfigProperty(name = "app.mq.port")
    int port;

    @ConfigProperty(name = "app.mq.channel")
    String channel;

    @ConfigProperty(name = "app.mq.queue-manager")
    String queueManager;

    @ConfigProperty(name = "app.mq.user")
    String user;

    @ConfigProperty(name = "app.mq.password")
    String password;

    @Produces
    @ApplicationScoped
    public ConnectionFactory createConnectionFactory() throws JMSException {
        MQConnectionFactory factory = new MQConnectionFactory();

        factory.setHostName(hostName);
        factory.setPort(port);
        factory.setQueueManager(queueManager);
        factory.setChannel(channel);

        factory.setTransportType(WMQConstants.WMQ_CM_CLIENT);

        if (user != null && !user.isEmpty()) {
            factory.setStringProperty(WMQConstants.USERID, user);
            factory.setStringProperty(WMQConstants.PASSWORD, password);
            factory.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
        }

        return factory;
    }
}

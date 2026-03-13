package org.acme.jmslistener;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.ExceptionListener;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Queue;
import jakarta.jms.Session;

@ApplicationScoped
public class MessageListenerManager implements ExceptionListener {

    private static final Logger LOGGER = Logger.getLogger(MessageListenerManager.class);

    @Inject
    ConnectionFactory connectionFactory;

    @Inject
    Instance<MyMessageListener> myMessageListenerInstance;

    @ConfigProperty(name = "app.jms.receive.concurrency", defaultValue = "1")
    int concurrencyLevel = 1;

    @ConfigProperty(name = "app.jms.receive.queue")
    String queueName;

    @ConfigProperty(name = "app.jms.receive.selector")
    Optional<String> selector;

    private Connection connection;
    private List<Session> sessions = new ArrayList<>();
    private List<MessageConsumer> consumers = new ArrayList<>();

    void onStart(@Observes StartupEvent ev) {
        try {
            connection = connectionFactory.createConnection();
            connection.setExceptionListener(this);
            connection.start();
            start();
            LOGGER.infof("Message listener started");
        } catch (JMSException e) {
            LOGGER.errorf(e, "Error creating connection: %s", e.getMessage());
        }
    }

    void onStop(@Observes ShutdownEvent ev) {
        try {
            stop();
            if (connection != null) {
                connection.close();
            }
            LOGGER.infof("Message listener stopped");
        } catch (JMSException e) {
            LOGGER.errorf(e, "Error stopping message listener: %s", e.getMessage());
        }
    }

    @Override
    public void onException(JMSException e) {
            LOGGER.errorf(e, "Error happened on connection: %s", e.getMessage());
            // Do what you want
            //stop();
            //start();
    }

    public synchronized void start() {
        if (sessions.isEmpty()) {
            try {
                for (int i = 0; i < concurrencyLevel; i++) {
                    Session session = connection.createSession();
                    sessions.add(session);
                    Queue queue = session.createQueue(queueName);
                    MessageConsumer consumer;
                    if (selector.isPresent()) {
                        consumer = session.createConsumer(queue, selector.get());
                    } else {
                        consumer = session.createConsumer(queue);
                    }
                    consumers.add(consumer);
                    consumer.setMessageListener(myMessageListenerInstance.get());
                }
                LOGGER.infof("Message listsener started with %d consumers", concurrencyLevel);
            } catch (JMSException e) {
                LOGGER.errorf(e, "Error creating connection: %s", e.getMessage());
            }
        } else {
            LOGGER.infof("Message listener already started");
        }
    }

    public synchronized void stop() {
        if (sessions.isEmpty()) {
            LOGGER.infof("Message listener already stopped");
        } else {
            try {
                for (MessageConsumer consumer : consumers) {
                    consumer.close();
                }
                consumers.clear();
                for (Session sessions : sessions) {
                    sessions.close();
                }
                sessions.clear();
                LOGGER.infof("Message listener stopped");
            } catch (JMSException e) {
                LOGGER.errorf(e, "Error closing sessions: %s", e.getMessage());
            }
        }
    }
}

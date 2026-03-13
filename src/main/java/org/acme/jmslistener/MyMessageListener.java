package org.acme.jmslistener;

import jakarta.jms.MessageListener;
import jakarta.jms.Message;

import jakarta.enterprise.context.Dependent;

import org.jboss.logging.Logger;

@Dependent
public class MyMessageListener implements MessageListener {

    private static final Logger LOGGER = Logger.getLogger(MyMessageListener.class);

    @Override
    public void onMessage(Message message) {
        try {
            //Thread.sleep(5000L);
            LOGGER.infof("Message: %s", message.getBody(String.class));
        } catch (Exception e) {
            LOGGER.errorf(e, "Error onMessage: %s", e.getMessage());
        }
    }
}

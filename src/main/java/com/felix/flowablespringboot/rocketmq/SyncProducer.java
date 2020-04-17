package com.felix.flowablespringboot.rocketmq;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

public class SyncProducer {
    public static void main(String[] args) throws Exception {
        //Instantiate with a producer group name.
        DefaultMQProducer producer = new DefaultMQProducer("TaskFlowTests_group_name");
        // Specify name server addresses.
        producer.setNamesrvAddr("localhost:9876");
        //Launch the instance.
        producer.start();
        //Create a message instance, specifying topic, tag and message body.
        Message sendMsg = new Message("client-side" /* Topic */,
                "TagA" /* Tag */,
                "5480c1be-7fd4-11ea-966f-00ff07db1431".getBytes(RemotingHelper.DEFAULT_CHARSET) /* Message body */
        );
        //Call send message to deliver message to one of brokers.
        SendResult sendResult = producer.send(sendMsg);
        System.out.printf("%s%n", sendResult);
        //Shut down once the producer instance is not longer in use.
        producer.shutdown();
    }
}
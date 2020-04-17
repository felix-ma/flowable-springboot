package com.felix.flowablespringboot;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;

/**
 * @author Felix
 * @date 2020/4/16
 **/
//@RunWith(SpringRunner.class)
@SpringBootTest
public class TaskFlowTests {

    RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> {
            try {
                new TaskFlowTests().consumerTest();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    @Test
    public void consumerTest() throws MQClientException, InterruptedException {
        //Instantiate with a producer group name.
        DefaultMQProducer producer = new DefaultMQProducer("TaskFlowTests_client_side_pd");
        producer.setInstanceName("consumerProducer");
        // Specify name server addresses.
        producer.setNamesrvAddr("localhost:9876");
        //Launch the instance.
        producer.start();

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("TaskFlowTests_client_side");
        consumer.setInstanceName("consumerConsumer");
        consumer.setNamesrvAddr("localhost:9876");
        consumer.subscribe("server-side", "*");
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            System.out.println("consumerTest获取消息条数：   " + msgs.size());
            for (MessageExt msg : msgs) {
                // 拟定发送taskId
                String taskId = new String(msg.getBody());
                System.out.println(LocalDateTime.now() + "获取消息 " + msg.getMsgId() + " taskID:  " + taskId);
                try {
                    Message sendMsg = new Message("client-side" /* Topic */,
                            "TagA" /* Tag */,
                            taskId.getBytes(RemotingHelper.DEFAULT_CHARSET) /* Message body */
                    );
                    SendResult send = producer.send(sendMsg);
                    System.out.println("消息发送成功  " + send.getMsgId());
                } catch (MQClientException | RemotingException | InterruptedException | UnsupportedEncodingException | MQBrokerException e) {
                    e.printStackTrace();
                }

            }
            System.out.println("consumerTest 消息回执 success" + msgs.size());
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        //Launch the consumer instance.
        consumer.start();
        System.out.printf("consumerTest Started.%n");
    }


    @Test
    public void flowableServerTest() throws MQClientException, InterruptedException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("TaskFlowTests_group_name");
        consumer.setNamesrvAddr("localhost:9876");
        consumer.subscribe("client-side", "*");
        consumer.setInstanceName("flowableConsumer");
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            System.out.println("flowableServerTest获取消息条数：   " + msgs.size());
            for (MessageExt msg : msgs) {
                // 拟定发送taskId
                String taskId = new String(msg.getBody());
                System.out.println(LocalDateTime.now() + "获取消息 " + msg.getMsgId() + " taskID:  " + taskId);
                ResponseEntity<String> forEntity = null;
                try {
                    forEntity = restTemplate.getForEntity("http://localhost:8081/flowable/expense/complete?taskId=" + taskId, String.class);
                    System.out.println(LocalDateTime.now() + "任务执行   " + forEntity.getBody());
                } catch (RestClientException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("flowableServerTest 消息回执 success" + msgs.size());
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        //Launch the consumer instance.
        consumer.start();
        System.out.printf("flowableServerTest Started.%n");
    }
}

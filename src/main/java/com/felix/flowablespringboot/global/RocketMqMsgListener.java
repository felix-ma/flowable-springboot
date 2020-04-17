package com.felix.flowablespringboot.global;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

/**
 * @author Felix
 * @date 2020/4/17
 */
@Component
@Slf4j
public class RocketMqMsgListener {
    @Autowired
    private TaskService taskService;
    DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("TaskFlowTests_server_side");

    @PostConstruct
    public void init() throws MQClientException {

        consumer.setNamesrvAddr("localhost:9876");
        consumer.subscribe("client-side", "*");
        consumer.setInstanceName("flowableConsumer");
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            System.out.println("flowableServerTest获取消息条数：   " + msgs.size());
            boolean ok = false;
            for (MessageExt msg : msgs) {
                // 拟定发送taskId
                String taskId = new String(msg.getBody());
                System.out.println(LocalDateTime.now() + "获取消息 " + msg.getMsgId() + " taskID:  " + taskId);
//                ResponseEntity<String> forEntity = null;
                try {
                    Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
                    if (task == null) {
                        System.out.println(LocalDateTime.now() + "no task find!!   " + taskId);
                    } else {
                        taskService.complete(taskId);
                        System.out.println(LocalDateTime.now() + "任务执行   " + taskId);
                        ok = true;
                    }
                } catch (RestClientException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("flowableServerTest 消息回执 success" + msgs.size());
            if (ok) {
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            } else {
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        });
        //Launch the consumer instance.
        consumer.start();
        System.out.printf("flowableServerTest Started.%n");
    }
}

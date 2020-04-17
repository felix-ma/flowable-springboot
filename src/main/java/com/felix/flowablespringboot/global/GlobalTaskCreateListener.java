package com.felix.flowablespringboot.global;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.engine.FormService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.delegate.event.impl.FlowableEntityWithVariablesEventImpl;
import org.flowable.engine.form.FormProperty;
import org.flowable.engine.form.TaskFormData;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Felix
 * @Description: 全局的任务创建监听
 * @date 2020/4/15
 */
@Component
@Slf4j
public class GlobalTaskCreateListener extends AbstractFlowableEngineEventListener {

    @Autowired
    private ProcessEngine processEngine;

    DefaultMQProducer producer;

    public GlobalTaskCreateListener() {
        super();
        try {
            log.info("创建rocketmq连接");
            //Instantiate with a producer group name.
            producer = new DefaultMQProducer("TaskFlowTests_server_side_pd");
            producer.setInstanceName("flowableProducer");
            // Specify name server addresses.
            producer.setNamesrvAddr("localhost:9876");
            //Launch the instance.
            producer.start();
            log.info("rocketmq连接成功");
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void taskCompleted(FlowableEngineEntityEvent event) {
        //得到流程定义id
        String processDefinitionId = event.getProcessDefinitionId();
        //得到流程实例id
        String processInstanceId = event.getProcessInstanceId();
        Map variables = null;
        if (event instanceof FlowableEntityWithVariablesEventImpl) {
            FlowableEntityWithVariablesEventImpl eventImpl = (FlowableEntityWithVariablesEventImpl) event;
            variables = eventImpl.getVariables();
        }
        //得到任务实例
        TaskEntity entity = (TaskEntity) event.getEntity();
        //得到传递的数据
        Map<String, Object> runVariables = processEngine.getRuntimeService().getVariables(processInstanceId);
        // 获取rocketmq信息发送
        log.info("taskCompletedr：processDefinitionId【{}】，processInstanceId【{}】，taskEntity【{}】，variables【{}】，runVariables【{}】"
                , processDefinitionId, processInstanceId, entity, variables, runVariables);
    }

    @Override
    protected void taskCreated(FlowableEngineEntityEvent event) {
        //得到流程定义id
        String processDefinitionId = event.getProcessDefinitionId();
        //得到流程实例id
        String processInstanceId = event.getProcessInstanceId();
        //得到任务实例
        TaskEntity entity = (TaskEntity) event.getEntity();
        FormService formService = processEngine.getFormService();
        TaskFormData taskFormData = formService.getTaskFormData(entity.getId());
        Map<String, String> properties = taskFormData.getFormProperties().stream()
                .collect(Collectors.toMap(FormProperty::getId, FormProperty::getValue));

        //TODO 根据 properties中的信息进行判断发送到mq里
        try {
            Message sendMsg = new Message("server-side" /* Topic */,
                    "TagA" /* Tag */,
                    entity.getId().getBytes(RemotingHelper.DEFAULT_CHARSET) /* Message body */
            );
            SendResult sendResult = producer.send(sendMsg);
            System.out.println("流程引擎发送消息：  " + sendResult.getMsgId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 将数据存储到流程变量里面
        processEngine.getRuntimeService().setVariables(processInstanceId, properties);
        log.info("taskCreated：processDefinitionId【{}】，processInstanceId【{}】，taskEntity【{}】"
                , processDefinitionId, processInstanceId, entity);
    }

}

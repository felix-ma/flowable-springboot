package com.felix.flowablespringboot.global;

import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.impl.event.FlowableEntityEventImpl;
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

    @Override
    protected void taskCompleted(FlowableEngineEntityEvent event) {
        if (event instanceof FlowableEntityWithVariablesEventImpl) {
            //得到流程定义id
            String processDefinitionId = event.getProcessDefinitionId();
            //得到流程实例id
            String processInstanceId = event.getProcessInstanceId();
            FlowableEntityWithVariablesEventImpl eventImpl = (FlowableEntityWithVariablesEventImpl) event;
            //得到任务实例
            TaskEntity entity = (TaskEntity) eventImpl.getEntity();
            //得到传递的数据
            Map variables = eventImpl.getVariables();
            Map<String, Object> runVariables = processEngine.getRuntimeService().getVariables(processInstanceId);
            // 获取rocketmq信息发送
            log.info("taskCompletedr：processDefinitionId【{}】，processInstanceId【{}】，taskEntity【{}】，variables【{}】，runVariables【{}】"
                    , processDefinitionId, processInstanceId, entity, variables, runVariables);
        }
    }

    @Override
    protected void taskCreated(FlowableEngineEntityEvent event) {
        if (event instanceof FlowableEntityEventImpl) {
            //得到流程定义id
            String processDefinitionId = event.getProcessDefinitionId();
            //得到流程实例id
            String processInstanceId = event.getProcessInstanceId();
            FlowableEntityEventImpl eventImpl = (FlowableEntityEventImpl) event;
            //得到任务实例
            TaskEntity entity = (TaskEntity) eventImpl.getEntity();
            FormService formService = processEngine.getFormService();
            TaskFormData taskFormData = formService.getTaskFormData(entity.getId());
            Map<String, String> properties = taskFormData.getFormProperties().stream()
                    .collect(Collectors.toMap(FormProperty::getId, FormProperty::getValue));
            //TODO 根据 properties中的信息进行判断发送到mq里
            // 将数据存储到流程变量里面
            processEngine.getRuntimeService().setVariables(processInstanceId, properties);
            log.info("taskCreated：processDefinitionId【{}】，processInstanceId【{}】，taskEntity【{}】"
                    , processDefinitionId, processInstanceId, entity);
        }
    }

}

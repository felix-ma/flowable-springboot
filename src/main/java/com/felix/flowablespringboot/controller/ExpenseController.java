package com.felix.flowablespringboot.controller;

import liquibase.util.StringUtils;
import org.flowable.bpmn.constants.BpmnXMLConstants;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.flowable.ui.modeler.domain.AbstractModel;
import org.flowable.ui.modeler.domain.Model;
import org.flowable.ui.modeler.serviceapi.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 报销demoController
 *
 * @author Felix
 * @date 2019/5/8
 */
@RestController
@RequestMapping(value = "expense")
public class ExpenseController {
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RepositoryService repositoryService;
    @Qualifier("processEngine")
    @Autowired
    private ProcessEngine processEngine;

/***************此处为业务代码******************/
    /**
     * 添加报销
     *
     * @param money     报销金额
     * @param descption 描述
     */
    @GetMapping("add")
    public String addExpense(Integer money, String descption) {
        //启动流程
        HashMap<String, Object> map = new HashMap<>();
        map.put("money", money);
        map.put("descption", descption);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Expense", map);
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());
        return "提交成功.流程Id为：" + processInstance.getId();
    }


    @Autowired
    private ModelService modelService;

    @GetMapping("deployModel")
    public String deployModel(String id) {
        Model modelData = modelService.getModel(id);
        //获取模型
        byte[] bytes = modelService.getBpmnXML(modelData);

        if (bytes == null) {
            return "模型数据为空，请先设计流程并成功保存，再进行发布。";
        }

        BpmnModel model = modelService.getBpmnModel(modelData);
        if (model.getProcesses().size() == 0) {
            return "数据模型不符要求，请至少设计一条主线流程。";
        }
        //发布流程
        String processName = modelData.getName() + ".bpmn20.xml";
        repositoryService.createDeployment()
                .name(modelData.getName())
                .addBpmnModel(processName, model)
                .deploy();
        return "部署成功";
    }


    @GetMapping("listDeploy")
    public List listDeploy() {
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().list();
        ArrayList<HashMap<String, Object>> result = new ArrayList<>();

        list.forEach(processDefinition -> {
            HashMap<String, Object> item = new HashMap<>();
            item.put("id", processDefinition.getId());
            item.put("name", processDefinition.getName());
            item.put("version", processDefinition.getVersion());
            item.put("category", processDefinition.getCategory());
            item.put("deploymentId", processDefinition.getDeploymentId());
            item.put("resourceName", processDefinition.getResourceName());
            result.add(item);
        });

        return result;
    }

    @GetMapping(value = "/listModels")
    public List<AbstractModel> pageModel() {
        List<AbstractModel> datas = modelService.getModelsByModelType(AbstractModel.MODEL_TYPE_BPMN);
        datas.forEach(abstractModel -> {
            abstractModel.setModelEditorJson(null);
            // 缩略图，前端可以添加  data:image/png;base64,    查看
            ((Model) abstractModel).setThumbnail(null);
        });
        return datas;
    }


    @GetMapping("startProcess")
    public String startProcess(String processName) {
        //启动流程
        processName = StringUtils.isEmpty(processName) ? "taskflow" : processName;
        HashMap<String, Object> params = new HashMap<>();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processName, params);
//        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
//        taskService.complete(task.getId());
        return "提交成功.流程Id为：" + processInstance.getId();
    }

    @GetMapping("complete")
    public String complete(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new RuntimeException("流程不存在");
        }
        //通过审核
        HashMap<String, Object> map = new HashMap<>();
        taskService.complete(taskId, map);
        return "processed ok!";
    }

    /**
     * 获取审批管理列表
     */
    @GetMapping("/list")
    public Object list(String userId) {
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(userId).orderByTaskCreateTime().desc().list();
        for (Task task : tasks) {
            System.out.println(task.toString());
        }
        return tasks.toString();
    }

    /**
     * 批准
     *
     * @param taskId 任务ID
     */
    @GetMapping("apply")
    public String apply(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new RuntimeException("流程不存在");
        }
        //通过审核
        HashMap<String, Object> map = new HashMap<>();
        map.put("outcome", "通过");
        taskService.complete(taskId, map);
        return "processed ok!";
    }

    /**
     * 拒绝
     */
    @GetMapping("reject")
    public String reject(String taskId) {
        HashMap<String, Object> map = new HashMap<>();
//        map.put("outcome", "驳回");
        taskService.complete(taskId, map);
        return "reject";
    }

    /**
     * 生成流程图
     *
     * @param processId 任务ID
     */
    @GetMapping("processDiagram")
    public void genProcessDiagram(HttpServletResponse httpServletResponse, String processId) throws Exception {
        HistoryService historyService = processEngine.getHistoryService();
        //1.获取当前的流程实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();
        String processDefinitionId = null;
        List<String> activeActivityIds = new ArrayList<>();
        List<String> highLightedFlows = new ArrayList<>();
        //2.获取所有的历史轨迹线对象
        List<HistoricActivityInstance> historicSquenceFlows = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processId).activityType(BpmnXMLConstants.ELEMENT_SEQUENCE_FLOW).list();
        historicSquenceFlows.forEach(historicActivityInstance -> highLightedFlows.add(historicActivityInstance.getActivityId()));
        //3. 获取流程定义id和高亮的节点id
        if (processInstance != null) {
            //3.1. 正在运行的流程实例
            processDefinitionId = processInstance.getProcessDefinitionId();
            activeActivityIds = runtimeService.getActiveActivityIds(processId);
        } else {
            //3.2. 已经结束的流程实例
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processId).singleResult();
            processDefinitionId = historicProcessInstance.getProcessDefinitionId();
            //3.3. 获取结束节点列表
            List<HistoricActivityInstance> historicEnds = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(processId).activityType(BpmnXMLConstants.ELEMENT_EVENT_END).list();
            List<String> finalActiveActivityIds = activeActivityIds;
            historicEnds.forEach(historicActivityInstance -> finalActiveActivityIds.add(historicActivityInstance.getActivityId()));
        }
        //4. 获取bpmnModel对象
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        //5. 生成图片流
        ProcessEngineConfiguration engconf = processEngine.getProcessEngineConfiguration();
        ProcessDiagramGenerator diagramGenerator = engconf.getProcessDiagramGenerator();

        byte[] buf = new byte[1024];
        int legth = 0;
        try (InputStream in = diagramGenerator.generateDiagram(bpmnModel, "png", activeActivityIds, highLightedFlows,
                engconf.getActivityFontName(), engconf.getLabelFontName(), engconf.getAnnotationFontName(), engconf.getClassLoader(), 1.0, true);
             OutputStream out = httpServletResponse.getOutputStream()) {
            while ((legth = in.read(buf)) != -1) {
                out.write(buf, 0, legth);
            }
        }
    }
}

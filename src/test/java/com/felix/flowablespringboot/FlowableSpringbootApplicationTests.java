package com.felix.flowablespringboot;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FlowableSpringbootApplicationTests {

    @Autowired
    private TaskService taskService;

    @Qualifier("processEngine")
    @Autowired
    private ProcessEngine processEngine;

    @Test
    public void contextLoads() {
        taskService.complete("10003");
    }

    @Test
    public void doAllTask() {
        List<Task> list = processEngine.getTaskService().createTaskQuery().list();
        if (!list.isEmpty()) {
            System.out.println(list);
            list.forEach(task -> {
                taskService.complete(task.getId());
            });
            doAllTask();
        }
    }

}


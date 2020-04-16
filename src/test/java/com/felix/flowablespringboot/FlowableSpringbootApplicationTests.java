package com.felix.flowablespringboot;

import org.flowable.engine.TaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FlowableSpringbootApplicationTests {

    @Autowired
    private TaskService taskService;

    @Test
    public void contextLoads() {
        taskService.complete("10003");
    }

    @Test
    public void doAllTask(){

    }

}


package com.felix.flowablespringboot;

import com.felix.flowablespringboot.config.AppDispatcherServletConfiguration;
import com.felix.flowablespringboot.config.ApplicationConfiguration;
import org.flowable.ui.modeler.conf.DatabaseConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * FlowableSpringbootApplication
 *
 * @author Felix
 * @date 2019/5/8
 */
@Import({
        ApplicationConfiguration.class,
        AppDispatcherServletConfiguration.class,
        // 引入 DatabaseConfiguration 表更新转换
        DatabaseConfiguration.class
})
@EnableScheduling
// 移除 Security 自动配置
@SpringBootApplication(
        exclude = {SecurityAutoConfiguration.class})
public class FlowableSpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowableSpringbootApplication.class, args);
    }

}

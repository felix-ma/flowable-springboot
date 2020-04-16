//package com.felix.flowablespringboot.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import springfox.documentation.builders.ApiInfoBuilder;
//import springfox.documentation.builders.OAuthBuilder;
//import springfox.documentation.builders.PathSelectors;
//import springfox.documentation.builders.RequestHandlerSelectors;
//import springfox.documentation.service.*;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spi.service.contexts.SecurityContext;
//import springfox.documentation.spring.web.plugins.Docket;
//import springfox.documentation.swagger2.annotations.EnableSwagger2;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
///**
// * @FileName: Swagger2
// * @Description:
// * @author: xueyj
// * @create: 2019-04-16 11:07
// */
//@Configuration
//@EnableSwagger2
//public class Swagger2 {
//    final String TOKEN_URL = "http://localhost:8080/oauth/token";
//
//    @Bean
//    public Docket createRestApi() {
//        return new Docket(DocumentationType.SWAGGER_2)
//                .apiInfo(apiInfo())
//                .select()
//                .apis(RequestHandlerSelectors.basePackage("com.felix.flowablespringboot"))
//                .paths(PathSelectors.any())
//                .build()
//                .securityContexts(securityContext())
//                .securitySchemes(securityScheme());
//    }
//
//    private ApiInfo apiInfo() {
//        return new ApiInfoBuilder()
//                .title("flowable-springboot")
//                .description("")
//                .termsOfServiceUrl("")
//                .version("1.0")
//                .build();
//    }
//
//    /**
//     * 认证方式
//     */
//    private List<SecurityScheme> securityScheme() {
//        GrantType grantType = new ResourceOwnerPasswordCredentialsGrant(TOKEN_URL);
//        OAuth springOauth = new OAuthBuilder()
//                .name("SpringOauth")
//                .grantTypes(Collections.singletonList(grantType))
//                .scopes(Arrays.asList(scopes()))
//                .build();
//        List<SecurityScheme> securitySchemes = new ArrayList<>();
//        securitySchemes.add(new ApiKey("token", "token", "header"));
//        securitySchemes.add(springOauth);
//        return securitySchemes;
//    }
//
//    /**
//     * swagger2 认证的安全上下文
//     */
//    private List<SecurityContext> securityContext() {
//        List<SecurityContext> securityContexts = new ArrayList<>();
//        SecurityContext springOauth = SecurityContext.builder()
//                .securityReferences(Collections.singletonList(new SecurityReference("SpringOauth", scopes())))
//                .forPaths(PathSelectors.any())
//                .build();
//        SecurityContext token = SecurityContext.builder()
//                .forPaths(PathSelectors.regex("^(?!auth).*$"))
//                .build();
//        securityContexts.add(springOauth);
//        securityContexts.add(token);
//        return securityContexts;
//    }
//
//    /**
//     * 允许认证的scope
//     */
//    private AuthorizationScope[] scopes() {
//        return new AuthorizationScope[]{
//                new AuthorizationScope("all", "All scope is trusted!")
//        };
//    }
//
//}
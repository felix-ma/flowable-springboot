package com.felix.flowablespringboot.services;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author : bruce.liu
 * @projectName : flowable
 * @description: 模型service
 * @date : 2019/11/1920:56
 */
public interface IFlowableModelService {

    /**
     * 导入模型
     * @param file 文件
     * @return
     */
    public String importProcessModel(MultipartFile file);

}

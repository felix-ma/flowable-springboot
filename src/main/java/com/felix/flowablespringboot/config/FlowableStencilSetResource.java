package com.felix.flowablespringboot.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flowable.ui.common.model.UserRepresentation;
import org.flowable.ui.common.security.DefaultPrivileges;
import org.flowable.ui.common.service.exception.InternalServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/app")
public class FlowableStencilSetResource {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(FlowableStencilSetResource.class);

    @Autowired
    protected ObjectMapper objectMapper;


    /**
     * 取消用户认证，配置死用户信息
     *
     * @param
     * @return org.flowable.ui.common.model.UserRepresentation
     * @author Felix
     * @date 2020/4/16
     */
    @RequestMapping(value = "/rest/account", method = RequestMethod.GET, produces =
            "application/json")
    public UserRepresentation getAccount() {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId("admin");
        userRepresentation.setEmail("admin@flowable.org");
        userRepresentation.setFullName("EmbraceSource");
        userRepresentation.setLastName("Felix");
        userRepresentation.setFirstName("EmbraceSource");
        List<String> privileges = new ArrayList<>();
        privileges.add(DefaultPrivileges.ACCESS_MODELER);
        privileges.add(DefaultPrivileges.ACCESS_IDM);
        privileges.add(DefaultPrivileges.ACCESS_ADMIN);
        privileges.add(DefaultPrivileges.ACCESS_TASK);
        privileges.add(DefaultPrivileges.ACCESS_REST_API);
        userRepresentation.setPrivileges(privileges);
        return userRepresentation;
    }


    @RequestMapping(value = "/rest/stencil-sets/editor", method = RequestMethod.GET, produces =
            "application/json")
    public JsonNode getStencilSetForEditor() {
        try {
            JsonNode stencilNode = objectMapper.readTree(this.getClass().getClassLoader().
                    getResourceAsStream("stencilset/stencilset_bpmn.json"));
            return stencilNode;
        } catch (Exception e) {
            LOGGER.error("Error reading bpmn stencil set json", e);
            throw new InternalServerErrorException("Error reading bpmn stencil set json");
        }
    }

    @RequestMapping(value = "/rest/stencil-sets/cmmneditor", method = RequestMethod.GET,
            produces = "application/json")
    public JsonNode getCmmnStencilSetForEditor() {
        try {
            JsonNode stencilNode =
                    objectMapper.readTree(this.getClass().getClassLoader().
                            getResourceAsStream("stencilset/stencilset_cmmn.json"));
            return stencilNode;
        } catch (Exception e) {
            LOGGER.error("Error reading bpmn stencil set json", e);
            throw new InternalServerErrorException("Error reading bpmn stencil set json");
        }
    }
}
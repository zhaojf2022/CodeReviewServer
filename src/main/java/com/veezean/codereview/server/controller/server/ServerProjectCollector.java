package com.veezean.codereview.server.controller.server;

import com.veezean.codereview.server.entity.ProjectEntity;
import com.veezean.codereview.server.model.Response;
import com.veezean.codereview.server.model.SaveProjectReqBody;
import com.veezean.codereview.server.model.UserProjectBindReqBody;
import com.veezean.codereview.server.model.UserShortInfo;
import com.veezean.codereview.server.service.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <类功能简要描述>
 *
 * @author Veezean
 * @since 2023/3/24
 */
@RestController
@RequestMapping("/server/project")
public class ServerProjectCollector {
    private final ProjectService projectService;

    @Autowired
    public ServerProjectCollector(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/createProject")
    public Response<String> createProject(@RequestBody SaveProjectReqBody reqBody) {
        projectService.createProject(reqBody);
        return Response.simpleSuccessResponse();
    }
    @PostMapping("/modifyProject")
    public Response<String> modifyProject(@RequestParam long projId, @RequestBody SaveProjectReqBody reqBody) {
        projectService.modifyProject(projId, reqBody);
        return Response.simpleSuccessResponse();
    }
    @GetMapping("/deleteProject")
    public Response<String> deleteProject(@RequestParam long projectId) {
        projectService.deleteProject(projectId);
        return Response.simpleSuccessResponse();
    }

    @GetMapping("/deleteProjects")
    public Response<String> deleteProjects(@RequestParam List<Long> projectIds) {
        projectService.deleteProjects(projectIds);
        return Response.simpleSuccessResponse();
    }
    @GetMapping("/queryProject")
    public Response<ProjectEntity> queryProject(@RequestParam long projectId) {
        ProjectEntity projectEntity = projectService.queryProject(projectId);
        return Response.simpleSuccessResponse(projectEntity);
    }
    @GetMapping("/queryProjectInDept")
    public Response<List<ProjectEntity>> queryProjectInDept(@RequestParam(required = false, defaultValue = "-1") String deptId) {
        List<ProjectEntity> projectEntities = projectService.queryAccessableProjectInDept(deptId);
        return Response.simpleSuccessResponse(projectEntities);
    }

    @PostMapping("/saveProjectMembers")
    public Response<String> saveProjectMembers(@RequestBody UserProjectBindReqBody reqBody) {
        projectService.saveProjectMembers(reqBody);
        return Response.simpleSuccessResponse();
    }

    @GetMapping("/queryProjectMembers")
    public Response<List<String>> queryProjectMembers(long projectId) {
        List<String> userShortInfos = projectService.queryProjectMembers(projectId);
        return Response.simpleSuccessResponse(userShortInfos);
    }
}

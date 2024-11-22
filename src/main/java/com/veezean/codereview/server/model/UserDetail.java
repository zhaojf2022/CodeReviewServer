package com.veezean.codereview.server.model;

import com.veezean.codereview.server.entity.DepartmentEntity;
import com.veezean.codereview.server.entity.RoleEntity;
import lombok.Data;

import java.util.List;

/**
 * 应用层使用的用户详情信息
 *
 * @author Veezean
 * @since 2023/3/22
 */
@Data
public class UserDetail {
    private String account;
    private String name;
    private DepartmentEntity department;
    private List<RoleEntity> roles;
    private String phoneNumber;
}

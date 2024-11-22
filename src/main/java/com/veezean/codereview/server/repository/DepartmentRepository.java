package com.veezean.codereview.server.repository;

import com.veezean.codereview.server.entity.DepartmentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * <类功能简要描述>
 *
 * @author Veezean
 * @since 2023/3/22
 */
@Repository
public interface DepartmentRepository extends MongoRepository<DepartmentEntity, Long> {
}

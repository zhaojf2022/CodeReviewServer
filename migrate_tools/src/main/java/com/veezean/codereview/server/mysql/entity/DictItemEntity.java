package com.veezean.codereview.server.mysql.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * <类功能简要描述>
 *
 * @author Veezean
 * @since 2023/3/22
 */
@Data
@Entity
@Table(name = "t_dict_item", schema = "code_review", catalog = "")
public class DictItemEntity extends BaseEntity{
    private String value;
    private String showName;
    private String itemDesc;
    private int sort;
    @ManyToOne
    private MySqlDictCollectionEntity collection;
}

package com.veezean.codereview.server.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.veezean.codereview.server.common.*;
import com.veezean.codereview.server.entity.ColumnDefineEntity;
import com.veezean.codereview.server.entity.ProjectEntity;
import com.veezean.codereview.server.entity.ReviewCommentEntity;
import com.veezean.codereview.server.model.*;
import com.veezean.codereview.server.repository.ReviewCommentRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <类功能简要描述>
 *
 * @author Veezean
 * @since 2023/3/25
 */
@Service
@Slf4j
public class MongoDbReviewCommentService {

    private static final int NORMAL = 0;
    private static final int DELETED = 1;

    @Autowired
    private ReviewCommentRepository reviewCommentRepository;
    @Resource
    private MongoTemplate mongoTemplate;
    @Autowired
    private ColumnDefineService columnDefineService;

    @Autowired
    private ProjectService projectService;

    private List<CommentFieldVO> buildCommentFieldVO(java.util.function.Predicate<ColumnDefineEntity> showPredicate,
                                                     java.util.function.Predicate<ColumnDefineEntity> editPredicate) {
        // 策略是所有字段都给到前端，但是告知前端是否显示、是否可编辑、是否必填，前端控制显示策略
        return columnDefineService.queryColumns()
                .map(columnDefineEntity -> {
                    CommentFieldVO editModel = new CommentFieldVO();
                    editModel.setCode(columnDefineEntity.getColumnCode());
                    editModel.setEditable(editPredicate.test(columnDefineEntity));
                    editModel.setShow(showPredicate.test(columnDefineEntity));
                    editModel.setRequired(columnDefineEntity.isRequired());
                    editModel.setInputType(columnDefineEntity.getInputType());
                    editModel.setEnumValues(columnDefineEntity.getEnumValues());
                    editModel.setShowName(columnDefineEntity.getShowName());
                    editModel.setValuePair(new ValuePair());
                    return editModel;
                })
                .collect(Collectors.toList());
    }

    public SaveReviewCommentReqBody initCreateReqBody() {
        SaveReviewCommentReqBody reqBody = new SaveReviewCommentReqBody();
        List<CommentFieldVO> commentFieldVOs = buildCommentFieldVO(ColumnDefineEntity::isShowInAddPage,
                ColumnDefineEntity::isEditableInAddPage);
        commentFieldVOs.forEach(commentFieldVO -> {
            String code = commentFieldVO.getCode();
            // 新增场景，字段初始赋值
            if (SystemCommentFieldKey.IDENTIFIER.getCode().equals(code)) {
                commentFieldVO.setValuePair(ValuePair.build(IdUtil.getSnowflakeNextIdStr()));
            }
        });
        reqBody.setFieldModelList(commentFieldVOs);
        return reqBody;
    }

    public SaveReviewCommentReqBody initEditReqBody(String identifier) {
        ReviewCommentEntity commentEntity = queryCommentDetail(identifier);
        Map<String, ValuePair> columnCodeValues = commentEntity.getValues();
        SaveReviewCommentReqBody reqBody = new SaveReviewCommentReqBody();
        List<CommentFieldVO> commentFieldVOS = buildCommentFieldVO(ColumnDefineEntity::isShowInEditPage,
                ColumnDefineEntity::isEditableInEditPage);
        commentFieldVOS.forEach(editModel -> {
            // 赋值， 防止客户端提交数据字段缺失（或者版本差异导致的缺失），此处增加保护
            ValuePair valuePair = columnCodeValues.remove(editModel.getCode());
            if (valuePair == null) {
                valuePair = new ValuePair();
            }
            editModel.setValuePair(valuePair);
        });

        // columnCodeValues中剩余的不在column定义的支持范围的字段，以不可见字段方式，始终透传，保证原始提交数据不会因为edit等原因丢失
        columnCodeValues.forEach((code, valuePair) -> {
            if (valuePair == null) {
                valuePair = new ValuePair();
            }
            CommentFieldVO fieldVO = CommentFieldVO.buildInvisableFieldVO(code, valuePair);
            commentFieldVOS.add(fieldVO);
        });

        reqBody.setFieldModelList(commentFieldVOS);
        reqBody.setDataVersion(commentEntity.getDataVersion());
        return reqBody;
    }


    public SaveReviewCommentReqBody initConfirmReqBody(String identifier) {
        ReviewCommentEntity commentEntity = queryCommentDetail(identifier);
        Map<String, ValuePair> columnCodeValues = commentEntity.getValues();
        SaveReviewCommentReqBody reqBody = new SaveReviewCommentReqBody();
        List<CommentFieldVO> commentFieldVOS = buildCommentFieldVO(ColumnDefineEntity::isShowInConfirmPage,
                ColumnDefineEntity::isEditableInConfirmPage);
        commentFieldVOS.forEach(editModel -> {
            // 赋值， 防止客户端提交数据字段缺失（或者版本差异导致的缺失），此处增加保护
            ValuePair valuePair = columnCodeValues.remove(editModel.getCode());
            if (valuePair == null) {
                valuePair = new ValuePair();
            }
            editModel.setValuePair(valuePair);
        });

        // columnCodeValues中剩余的不在column定义的支持范围的字段，以不可见字段方式，始终透传，保证原始提交数据不会因为edit等原因丢失
        columnCodeValues.forEach((code, valuePair) -> {
            if (valuePair == null) {
                valuePair = new ValuePair();
            }
            CommentFieldVO fieldVO = CommentFieldVO.buildInvisableFieldVO(code, valuePair);
            commentFieldVOS.add(fieldVO);
        });

        reqBody.setFieldModelList(commentFieldVOS);
        reqBody.setDataVersion(commentEntity.getDataVersion());
        return reqBody;
    }

    public SaveReviewCommentReqBody initViewReqBody(String identifier) {
        ReviewCommentEntity commentEntity = queryCommentDetail(identifier);
        Map<String, ValuePair> columnCodeValues = commentEntity.getValues();
        SaveReviewCommentReqBody reqBody = new SaveReviewCommentReqBody();
        List<CommentFieldVO> commentFieldVOS = buildCommentFieldVO(columnDefineEntity -> true,
                columnDefineEntity -> false);
        commentFieldVOS.forEach(editModel -> {
            // 赋值， 防止客户端提交数据字段缺失（或者版本差异导致的缺失），此处增加保护
            ValuePair valuePair = columnCodeValues.remove(editModel.getCode());
            if (valuePair == null) {
                valuePair = new ValuePair();
            }
            editModel.setValuePair(valuePair);
        });

        // columnCodeValues中剩余的不在column定义的支持范围的字段，以不可见字段方式，始终透传，保证原始提交数据不会因为edit等原因丢失
        columnCodeValues.forEach((code, valuePair) -> {
            if (valuePair == null) {
                valuePair = new ValuePair();
            }
            CommentFieldVO fieldVO = CommentFieldVO.buildInvisableFieldVO(code, valuePair);
            commentFieldVOS.add(fieldVO);
        });

        reqBody.setFieldModelList(commentFieldVOS);
        reqBody.setDataVersion(commentEntity.getDataVersion());

        // 根据后缀推测代码类型，如果不支持，默认为JAVA类型
        String codeType = commentEntity.findByKey(SystemCommentFieldKey.FILE_PATH)
                .map(ValuePair::getValue)
                .map(FileNameUtil::extName)
                .map(String::toLowerCase)
                .map(CommonConsts.fileSuffixAndCodeTypeMaps::get)
                .filter(StringUtils::isNotEmpty)
                .orElse("java");
        reqBody.setCodeType(codeType);

        return reqBody;
    }

    @Transactional
    public void createComment(SaveReviewCommentReqBody reqBody) {
        if (reqBody == null || CollectionUtils.isEmpty(reqBody.getFieldModelList())) {
            throw new CodeReviewException("请求内容不合法");
        }
        ReviewCommentEntity commentEntity = buildCommentEntity(
                reqBody,
                s -> new ReviewCommentEntity(),
                resultMap -> {
                    resultMap.put(SystemCommentFieldKey.CONFIRM_RESULT.getCode(),
                            ValuePair.build(CommonConsts.UNCONFIRMED,
                                    "待确认"));
                    String dateTime = DateUtil.formatDateTime(new Date());
                    resultMap.put(SystemCommentFieldKey.REVIEW_DATE.getCode(), ValuePair.build(dateTime, dateTime));
                    UserDetail currentUser = CurrentUserHolder.getCurrentUser();
                    resultMap.put(SystemCommentFieldKey.REVIEWER.getCode(),
                            ValuePair.build(currentUser.getAccount(), currentUser.getName())
                    );
                });

        // 保持与client端处理逻辑一致，新增的时候dataVersion也+1
        commentEntity.increaseDataVersion();
        // 记录下本次操作类型，方便后续通知发送
        commentEntity.setLatestOperateType(CommentOperateType.COMMIT.getValue());
        // 存储到数据库中,此处直接存储，不做字段内容校验
        reviewCommentRepository.save(commentEntity);
    }

    @Transactional
    public void modifyComment(SaveReviewCommentReqBody reqBody) {
        if (reqBody == null || CollectionUtils.isEmpty(reqBody.getFieldModelList())) {
            throw new CodeReviewException("请求内容不合法");
        }

        ReviewCommentEntity commentEntity = buildCommentEntity(
                reqBody,
                identifier -> reviewCommentRepository.findFirstByIdAndStatus(identifier, NORMAL),
                resultMap -> {
                });
        commentEntity.increaseDataVersion();
        // 记录下本次操作类型，方便后续通知发送
        commentEntity.setLatestOperateType(CommentOperateType.MODIFY.getValue());
        // 存储到数据库中,此处直接存储，不做字段内容校验
        reviewCommentRepository.save(commentEntity);
    }

    @Transactional
    public void confirmComment(SaveReviewCommentReqBody reqBody) {
        if (reqBody == null || CollectionUtils.isEmpty(reqBody.getFieldModelList())) {
            throw new CodeReviewException("请求内容不合法");
        }

        if (CommonConsts.UNCONFIRMED.equals(reqBody.getValueByKey(SystemCommentFieldKey.CONFIRM_RESULT))) {
            throw new CodeReviewException("请填写确认结果");
        }

        ReviewCommentEntity commentEntity = buildCommentEntity(
                reqBody,
                identifier -> reviewCommentRepository.findFirstByIdAndStatus(identifier, NORMAL),
                resultMap -> {
                    String dateTime = DateUtil.formatDateTime(new Date());
                    resultMap.put(SystemCommentFieldKey.CONFIRM_DATE.getCode(), ValuePair.build(dateTime, dateTime));
                    UserDetail currentUser = CurrentUserHolder.getCurrentUser();
                    resultMap.put(SystemCommentFieldKey.REAL_CONFIRMER.getCode(),
                            ValuePair.build(currentUser.getAccount(), currentUser.getName())
                    );
                });
        commentEntity.increaseDataVersion();
        // 记录下本次操作类型，方便后续通知发送
        commentEntity.setLatestOperateType(CommentOperateType.CONFIRM.getValue());
        // 存储到数据库中,此处直接存储，不做字段内容校验
        reviewCommentRepository.save(commentEntity);
    }

    private ReviewCommentEntity buildCommentEntity(SaveReviewCommentReqBody reqBody,
                                                   Function<String, ReviewCommentEntity> reviewEntityProvider,
                                                   Consumer<Map<String, ValuePair>> resultConsumer) {
        // 必填字段非空校验
        reqParamValidate(reqBody);
        String identifier = reqBody.findIdentifier();
        ReviewCommentEntity commentEntity = reviewEntityProvider.apply(identifier);
        if (commentEntity == null) {
            throw new CodeReviewException("操作的目标记录不存在:" + identifier);
        }

        // CAS版本控制
        if (commentEntity.getDataVersion() != reqBody.getDataVersion()) {
            throw new CodeReviewException("保存失败，记录已被更新，请基于最新记录基础上进行修改");
        }

        Map<String, ValuePair> valuePairMap =
                reqBody.getFieldModelList().stream().
                        filter(commentFieldVO -> commentFieldVO.getValuePair() != null)
                        .collect(Collectors.toMap(CommentFieldVO::getCode,
                                CommentFieldVO::getValuePair));
        resultConsumer.accept(valuePairMap);
        commentEntity.setValues(valuePairMap);
        commentEntity.setId(identifier);
        return commentEntity;
    }

    private void reqParamValidate(SaveReviewCommentReqBody reqBody) {
        reqBody.getFieldModelList().stream()
                .filter(commentFieldVO ->
                        commentFieldVO.isShow()
                                && commentFieldVO.isEditable()
                                && commentFieldVO.isRequired()
                                && (commentFieldVO.getValuePair() == null || StringUtils.isEmpty(commentFieldVO.getValuePair().getValue())))
                .findAny()
                .ifPresent(commentFieldVO -> {
                    throw new CodeReviewException("必填字段校验失败，请检查");
                });
    }

    @Transactional
    public void deleteComment(String identifier) {
        // 软删除
        ReviewCommentEntity commentEntity = reviewCommentRepository.findFirstByIdAndStatus(identifier, NORMAL);
        if (commentEntity == null) {
            return;
        }
        commentEntity.setStatus(DELETED);
        // 记录下本次操作类型，方便后续通知发送
        commentEntity.setLatestOperateType(CommentOperateType.DELETE.getValue());
        reviewCommentRepository.save(commentEntity);
    }

    @Transactional
    public void deleteBatch(List<String> commentIds) {
        // 软删除
        List<ReviewCommentEntity> commentEntities = reviewCommentRepository.findAllByIdInAndStatus(commentIds, NORMAL);
        if (commentEntities != null) {
            commentEntities.forEach(reviewCommentEntity -> {
                // 记录下本次操作类型，方便后续通知发送
                reviewCommentEntity.setLatestOperateType(CommentOperateType.DELETE.getValue());
                reviewCommentEntity.setStatus(DELETED);
            });
            reviewCommentRepository.saveAll(commentEntities);
        }

    }

    public ReviewCommentEntity queryCommentDetail(String identifier) {
        ReviewCommentEntity commentEntity = reviewCommentRepository.findFirstByIdAndStatus(identifier, NORMAL);
        if (commentEntity == null) {
            throw new CodeReviewException("评审意见不存在： " + identifier);
        }
        return commentEntity;
    }

    public PageBeanList<Map<String, String>> queryCommentDetails(PageQueryRequest<QueryCommentReqBody> request) {
        Pageable pageable = PageUtil.buildPageable(request);
        QueryCommentReqBody queryParams = request.getQueryParams();
        Query query = buildListQuery(queryParams);
        long count = mongoTemplate.count(query, ReviewCommentEntity.class);
        List<Map<String, String>> commentEntities =
                mongoTemplate.find(query
                                        .skip((long) (pageable.getPageNumber() - 1) * pageable.getPageSize())
                                        .limit(pageable.getPageSize()),
                                ReviewCommentEntity.class)
                        .stream()
                        .map(entity -> {
                            Map<String, String> result = new HashMap<>();
                            entity.getValues().forEach((key, value) -> {
                                result.put(key,
                                        CommentFieldShowContentHelper.getColumnShowContent(value)
                                );
                            });
                            return result;
                        })
                        .collect(Collectors.toList());

        Page<Map<String, String>> page = new PageImpl<>(commentEntities, pageable, count);
        return PageBeanList.create(page, pageable);
    }


    public void exportComments(QueryCommentReqBody queryParams, HttpServletResponse response) {
        try (OutputStream outputStream = response.getOutputStream()) {
            // 读取当前系统配置的字段数据,过滤出允许导出到excel中的数据
            List<ColumnDefineEntity> fieldDefines = columnDefineService.queryColumns().collect(Collectors.toList())
                    .stream()
                    .filter(ColumnDefineEntity::isSupportInExcel)
                    .sorted(Comparator.comparingInt(ColumnDefineEntity::getSortIndex))
                    .collect(Collectors.toList());

            // 根据过滤条件拉取数据，限制最大导出10000条数据
            Query query = buildListQuery(queryParams);
            List<List<String>> commentEntities =
                    mongoTemplate.find(query.limit(10000), ReviewCommentEntity.class)
                            .stream()
                            .map(entity -> {
                                // 对每一条评审记录进行处理
                                Map<String, String> columnValueMap = new HashMap<>();
                                // 对当条记录的各个字段进行排序
                                entity.getValues().forEach((s, valuePair) -> {
                                    columnValueMap.put(s,
                                            CommentFieldShowContentHelper.getColumnShowContent(valuePair));
                                });

                                // 按照指定顺序生成各个字段的值
                                List<String> rowValues = new ArrayList<>();
                                for (ColumnDefineEntity columnDefine : fieldDefines) {
                                    String columnCode = columnDefine.getColumnCode();
                                    rowValues.add(Optional.ofNullable(columnValueMap.get(columnCode)).orElse(""));
                                }
                                return rowValues;
                            })
                            .collect(Collectors.toList());

            // 生成表头信息
            List<String> headerNames = fieldDefines.stream()
                    .map(ColumnDefineEntity::getShowName)
                    .collect(Collectors.toList());
            commentEntities.add(0, headerNames);

            // 设置响应头信息
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setHeader("Content-Disposition",
                    "attachment;filename=review_comment_" + System.currentTimeMillis() + ".xlsx");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");

            // 根据字段设置以及查询到的数据，写入excel中
            ExcelWriter excelWriter = ExcelUtil.getWriter(true);
            excelWriter.getStyleSet().setBorder(BorderStyle.THIN, IndexedColors.GREY_50_PERCENT);
            for (int i = 0; i < fieldDefines.size(); i++) {
                excelWriter.getSheet().setColumnWidth(i, 256 * fieldDefines.get(i).getExcelColumnWidth());
            }
            excelWriter.getCellStyle().setWrapText(true);
            excelWriter.getCellStyle().setAlignment(HorizontalAlignment.LEFT);
            excelWriter.write(commentEntities);
            excelWriter.flush(outputStream);
            excelWriter.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private Query buildListQuery(QueryCommentReqBody queryParams) {
        Criteria criteria = Criteria.where("id").ne(null).and("status").is(0);

        // 自己有权限查看的项目对应的数据
        List<String> bindedProjectIds = projectService.getUserAccessableProjectIds();

        if (queryParams != null) {

            if (StringUtils.isNotEmpty(queryParams.getConfirmResult())) {
                criteria.and("values." + SystemCommentFieldKey.CONFIRM_RESULT.getCode() + ".value").is(queryParams.getConfirmResult());
            }
            if (StringUtils.isNotEmpty(queryParams.getIdentifier())) {
                criteria.and("values." + SystemCommentFieldKey.IDENTIFIER.getCode() + ".value").is(queryParams.getIdentifier());
            }
            if (StringUtils.isNotEmpty(queryParams.getCommitUser())) {
                criteria.and("values." + SystemCommentFieldKey.REVIEWER.getCode() + ".value").is(queryParams.getCommitUser());
            }
            if (StringUtils.isNotEmpty(queryParams.getAssignConfirmUser())) {
                criteria.and("values." + SystemCommentFieldKey.ASSIGN_CONFIRMER.getCode() + ".value").is(queryParams.getAssignConfirmUser());
            }
            if (StringUtils.isNotEmpty(queryParams.getRealConfirmUser())) {
                criteria.and("values." + SystemCommentFieldKey.REAL_CONFIRMER.getCode() + ".value").is(queryParams.getRealConfirmUser());
            }
            if (queryParams.getProjectId() != null && queryParams.getProjectId() > 0L) {
                String projId = String.valueOf(queryParams.getProjectId());
                if (bindedProjectIds.contains(projId)) {
                    // 如果指定具体项目，则限制查看指定的项目
                    bindedProjectIds.clear();
                    bindedProjectIds.add(projId);
                } else {
                    // 异常兜底，传入的项目是自己无权查看的项目
                    bindedProjectIds.clear();
                }
            }

            if (queryParams.getDepartmentId() != null && queryParams.getDepartmentId() > 0L) {
                // 如果指定了部门，则限定在部门内的项目中查询
                List<String> deptProjIds =
                        projectService.queryAccessableProjectInDept(queryParams.getDepartmentId() + "")
                        .stream()
                        .map(ProjectEntity::getId)
                        .map(String::valueOf)
                        .filter(bindedProjectIds::contains)
                        .collect(Collectors.toList());
                bindedProjectIds.clear();
                bindedProjectIds.addAll(deptProjIds);
            }
        }

        // 固定限制只能看自己有权限的部分(取有权访问的项目+手动选择的项目过滤条件的交集)
        criteria.and("values." + SystemCommentFieldKey.PROJECT_ID.getCode() + ".value").in(bindedProjectIds);

        return new Query(criteria);
    }

    @Transactional
    public CommitResult clientCommit(CommitComment commitComment) {
        CommitResult result = new CommitResult();
        List<ReviewCommentEntity> passEntitiespassEntities = new ArrayList<>();
        commitComment.getComments().forEach(reviewCommentEntity -> {
            ReviewCommentEntity existEntity =
                    reviewCommentRepository.findFirstByIdAndStatus(reviewCommentEntity.getId(), NORMAL);

            if (existEntity != null && !hasModified(existEntity, reviewCommentEntity)) {
                // 提交的内容没有任何变化，直接忽略
                log.info("no content changed, ignore this comment...id:{}", existEntity.getId());
                return;
            }

            if (existEntity != null && existEntity.getDataVersion() != reviewCommentEntity.getDataVersion()) {
                result.addFailedId(reviewCommentEntity.getId());
            } else {
                // 版本+1，CAS控制
                reviewCommentEntity.increaseDataVersion();
                // 空值保护，防止客户端提交异常数据上来
                reviewCommentEntity.getValues().forEach((s, valuePair) -> {
                    if (valuePair == null) {
                        valuePair = new ValuePair();
                    }
                    reviewCommentEntity.getValues().put(s, valuePair);
                });
                // 设置此条记录的提交类型
                reviewCommentEntity.setLatestOperateType(clientOperateType(existEntity, reviewCommentEntity).getValue());
                passEntitiespassEntities.add(reviewCommentEntity);
                result.putVersion(reviewCommentEntity.getId(), reviewCommentEntity.getDataVersion());
            }
        });
        reviewCommentRepository.saveAll(passEntitiespassEntities);
        log.info("本次提交{}条记录，最终成功入库{}条记录", commitComment.getComments().size(), passEntitiespassEntities.size());
        if (result.getFailedIds().isEmpty()) {
            result.setSuccess(true);
            return result;
        } else {
            result.setErrDesc("数据版本冲突，请先备份本地数据，然后更新至服务端最新数据，解决冲突再提交。");
            log.error("存在数据提交失败：{}", result.getFailedIds());
        }
        return result;
    }

    /**
     * 判断提交的内容与DB中已有内容是否有变更
     *
     * @param existEntity
     * @param reviewCommentEntity
     * @return
     */
    private boolean hasModified(ReviewCommentEntity existEntity, ReviewCommentEntity reviewCommentEntity) {
        return !StringUtils.equals(JSON.toJSONString(existEntity.getValues()),
                JSON.toJSONString(reviewCommentEntity.getValues()));
    }

    private CommentOperateType clientOperateType(ReviewCommentEntity existEntity,
                                                 ReviewCommentEntity reviewCommentEntity) {
        if (existEntity == null) {
            return CommentOperateType.COMMIT;
        }

        if (!existEntity.findByKey(SystemCommentFieldKey.REAL_CONFIRMER).isPresent()
                && reviewCommentEntity.findByKey(SystemCommentFieldKey.REAL_CONFIRMER).isPresent()) {
            return CommentOperateType.CONFIRM;
        }

        return CommentOperateType.MODIFY;
    }

    public CommitComment clientQueryComments(ReviewQueryParams queryParams) {
        QueryCommentReqBody reqBody = new QueryCommentReqBody();
        reqBody.setProjectId(queryParams.getProjectId());
        switch (queryParams.getType()) {
            case "我提交的":
                reqBody.setCommitUser(CurrentUserHolder.getCurrentUser().getAccount());
                break;
            case "待我确认":
                reqBody.setAssignConfirmUser(CurrentUserHolder.getCurrentUser().getAccount());
                reqBody.setConfirmResult(CommonConsts.UNCONFIRMED);
                break;
            default:
                break;
        }
        Query query = buildListQuery(reqBody);
        List<ReviewCommentEntity> reviewCommentEntities = mongoTemplate.find(query, ReviewCommentEntity.class);
        CommitComment commitComment = new CommitComment();
        commitComment.setComments(reviewCommentEntities);
        return commitComment;
    }
}

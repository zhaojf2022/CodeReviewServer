package com.veezean.codereview.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <类功能简要描述>
 *
 * @author Veezean, 公众号 @架构悟道
 * @since 2021/6/5
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPwdCheckRespBody {
    private boolean pass;
    private ValuePair userInfo;
}

/**
 * betahouse.us
 * CopyRight (c) 2012 - 2018
 */
package us.betahouse.haetae.serviceimpl.activity.service;

import us.betahouse.haetae.activity.model.ActivityRecordBO;
import us.betahouse.haetae.activity.request.ActivityRecordRequest;
import us.betahouse.haetae.serviceimpl.common.OperateContext;

import java.util.List;

/**
 * 活动记录服务
 *
 * @author MessiahJK
 * @version : ActivityRecordService.java 2018/11/22 20:31 MessiahJK
 */
public interface ActivityRecordService {
    /**
     * 创建活动记录
     *
     * @param request
     * @param context
     * @return
     */
    ActivityRecordBO create(ActivityRecordRequest request, OperateContext context);

    /**
     * 通过用户id查询活动记录
     *
     * @param request
     * @param context
     * @return
     */
    List<ActivityRecordBO> findByUserId(ActivityRecordRequest request,OperateContext context);

    /**
     * 通过用户id和类型查找活动记录
     *
     * @param request
     * @param context
     * @return
     */
    List<ActivityRecordBO> findByUserIdAndType(ActivityRecordRequest request,OperateContext context);

    /**
     * 通过活动id统计活动记录条数
     *
     * @param request
     * @param context
     * @return
     */
    Long countByActivityId(ActivityRecordRequest request,OperateContext context);
}
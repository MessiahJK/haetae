/*
  betahouse.us
  CopyRight (c) 2012 - 2018
 */
package us.betahouse.haetae.serviceimpl.activity.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import us.betahouse.haetae.activity.dal.service.ActivityRepoService;
import us.betahouse.haetae.activity.dal.service.OrganizationRepoService;
import us.betahouse.haetae.activity.enums.ActivityStateEnum;
import us.betahouse.haetae.activity.enums.ActivityTypeEnum;
import us.betahouse.haetae.activity.manager.ActivityManager;
import us.betahouse.haetae.activity.model.basic.ActivityBO;
import us.betahouse.haetae.activity.model.basic.OrganizationBO;
import us.betahouse.haetae.serviceimpl.activity.constant.ActivityExtInfoKey;
import us.betahouse.haetae.serviceimpl.activity.constant.ActivityPermType;
import us.betahouse.haetae.serviceimpl.activity.constant.PermExInfokey;
import us.betahouse.haetae.serviceimpl.activity.manager.ActivityOperateManager;
import us.betahouse.haetae.serviceimpl.activity.request.ActivityManagerRequest;
import us.betahouse.haetae.serviceimpl.activity.service.ActivityService;
import us.betahouse.haetae.serviceimpl.common.OperateContext;
import us.betahouse.haetae.serviceimpl.common.verify.VerifyPerm;
import us.betahouse.haetae.serviceimpl.user.enums.UserRoleCode;
import us.betahouse.haetae.user.dal.service.RoleRepoService;
import us.betahouse.haetae.user.dal.service.UserInfoRepoService;
import us.betahouse.haetae.user.manager.PermManager;
import us.betahouse.haetae.user.manager.UserManager;
import us.betahouse.haetae.user.model.basic.UserInfoBO;
import us.betahouse.haetae.user.model.basic.perm.PermBO;
import us.betahouse.haetae.user.request.PermManageRequest;
import us.betahouse.haetae.user.request.UserManageRequest;
import us.betahouse.haetae.user.user.builder.PermBOBuilder;
import us.betahouse.util.utils.AssertUtil;
import us.betahouse.util.utils.CollectionUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 活动服务实现
 *
 * @author MessiahJK
 * @version : ActivityServiceImpl.java 2018/11/22 20:56 MessiahJK
 */
@Service
public class ActivityServiceImpl implements ActivityService {

    /**
     * 系统结束标志
     */
    private final static String SYSTEM_FINISH_SIGN = "systemFinish";

    @Autowired
    private ActivityManager activityManager;

    @Autowired
    private PermManager permManager;

    @Autowired
    private ActivityRepoService activityRepoService;

    @Autowired
    private UserManager userManager;

    @Autowired
    private UserInfoRepoService userInfoRepoService;

    @Autowired
    private RoleRepoService roleRepoService;

    @Autowired
    private ActivityOperateManager activityOperateManager;

    @Autowired
    private OrganizationRepoService organizationRepoService;

    @Override
    @VerifyPerm(permType = ActivityPermType.ACTIVITY_CREATE)
    @Transactional
    public ActivityBO create(ActivityManagerRequest request, OperateContext context) {
        ActivityTypeEnum activityType = ActivityTypeEnum.getByCode(request.getType());
        AssertUtil.assertNotNull(activityType, "该活动类型不存在");

        AssertUtil.assertStringNotBlank(request.getOrganizationMessage(), "活动主办组织信息不能为空");
        OrganizationBO organizationBO = organizationRepoService.queryOrganizationByName(request.getOrganizationMessage());
        AssertUtil.assertNotNull(organizationBO, MessageFormat.format("组织不存在, {0}", request.getOrganizationMessage()));


        // 创建活动
        ActivityBO activityBO = activityManager.create(request);
        // 创建权限
        PermBO permBO = PermBOBuilder.getInstance(ActivityPermType.ACTIVITY_STAMPER, request.getActivityName() + "_盖章权限").build();
        // 将活动id 冗余到权限拓展信息
        permBO.putExtInfo(PermExInfokey.ACTIVITY_ID, activityBO.getActivityId());
        // 构建请求
        PermManageRequest permManageRequest = new PermManageRequest();
        permManageRequest.setPermBO(permBO);
        permManageRequest.setUserIds(Collections.singletonList(request.getUserId()));
        permBO = permManager.createPerm(permManageRequest);

        // 更新信息
        request.setActivityId(activityBO.getActivityId());
        request.putExtInfo(ActivityExtInfoKey.ACTIVITY_STAMP_PERM, permBO.getPermId());
        return activityManager.update(request);
    }

    @Override
    public List<ActivityBO> findAll(ActivityManagerRequest request, OperateContext context) {
        if (StringUtils.isBlank(request.getState())) {
            return activityManager.findAll();
        } else {
            ActivityStateEnum state = ActivityStateEnum.getByCode(request.getState());
            AssertUtil.assertNotNull(state, "活动状态不存在");
            return activityManager.findByState(state);
        }
    }

    @Override
    public ActivityBO update(ActivityManagerRequest request, OperateContext operateContext) {
        return activityManager.update(request);
    }

    @Override
    public ActivityBO operate(ActivityManagerRequest request, OperateContext operateContext) {
        return activityOperateManager.operate(request, operateContext);
    }

    @Override
    @VerifyPerm(permType = ActivityPermType.STAMPER_MANAGE)
    @Transactional
    public void bindStamper(ActivityManagerRequest request, OperateContext context) {
        ActivityBO activity = activityRepoService.queryActivityByActivityId(request.getActivityId());
        AssertUtil.assertNotNull(activity, "活动不存在");
        String stampPermId = activity.fetchExtInfo(ActivityExtInfoKey.ACTIVITY_STAMP_PERM);
        AssertUtil.assertStringNotBlank(stampPermId, "活动没有盖章权限");

        String stamperStuId = request.fetchStamperStuId();

        UserInfoBO userInfo = userInfoRepoService.queryUserInfoByStuId(stamperStuId);
        AssertUtil.assertNotNull(userInfo, "用户不存在或该用户未绑定个人信息");


        UserManageRequest userManageRequest = new UserManageRequest();
        userManageRequest.setUserId(userInfo.getUserId());
        userManageRequest.setPermIds(Collections.singletonList(stampPermId));
        // 绑定 权限
        userManager.batchBindPerm(userManageRequest);

        // 绑定角色
        roleRepoService.userBindRolesByCode(userInfo.getUserId(), UserRoleCode.ACTIVITY_STAMPER);
    }

    @Override
    @VerifyPerm(permType = ActivityPermType.STAMPER_MANAGE)
    public List<UserInfoBO> getStampers(ActivityManagerRequest request, OperateContext context) {
        ActivityBO activity = activityRepoService.queryActivityByActivityId(request.getActivityId());
        AssertUtil.assertNotNull(activity, "活动不存在");
        String stampPermId = activity.fetchExtInfo(ActivityExtInfoKey.ACTIVITY_STAMP_PERM);
        AssertUtil.assertStringNotBlank(stampPermId, "活动没有盖章权限");

        // 获取全体扫码员id
        List<String> stamperUserIds = permManager.getPermUsers(stampPermId);
        return userInfoRepoService.batchQueryByUserIds(stamperUserIds);
    }

    @Override
    @VerifyPerm(permType = ActivityPermType.STAMPER_MANAGE)
    public void unbindStamper(ActivityManagerRequest request, OperateContext context) {
        ActivityBO activity = activityRepoService.queryActivityByActivityId(request.getActivityId());
        AssertUtil.assertNotNull(activity, "活动不存在");
        String stampPermId = activity.fetchExtInfo(ActivityExtInfoKey.ACTIVITY_STAMP_PERM);
        AssertUtil.assertStringNotBlank(stampPermId, "活动没有盖章权限");

        // 获取指定扫描员stuId
        String stamperStuId = request.fetchStamperStuId();
        UserInfoBO userInfo = userInfoRepoService.queryUserInfoByStuId(stamperStuId);
        AssertUtil.assertNotNull(userInfo, "用户不存在或该用户未绑定个人信息");

        // 解除绑定
        PermManageRequest permManageRequest = new PermManageRequest();
        permManageRequest.setUserIds(Collections.singletonList(userInfo.getUserId()));
        permManageRequest.setPermId(stampPermId);
        permManager.batchUsersUnbindPerms(permManageRequest);
    }

    @Override
    public List<ActivityBO> systemFinishActivity() {
        List<ActivityBO> activityBOList = activityRepoService.queryActivitiesByState(ActivityStateEnum.PUBLISHED.getCode());
        List<ActivityBO> systemFinishActivities = new ArrayList<>();
        for (ActivityBO activityBO : activityBOList) {
            if (activityBO.canFinish()) {
                activityBO.setState(ActivityStateEnum.FINISHED.getCode());
                activityBO.putExtInfo(SYSTEM_FINISH_SIGN, SYSTEM_FINISH_SIGN);
                String activityPermId = activityBO.fetchExtInfo(ActivityExtInfoKey.ACTIVITY_STAMP_PERM);
                // 结束活动时 保留前两个权限关系 其他全去除
                if (StringUtils.isNotBlank(activityPermId)) {
                    List<String> userIds = permManager.getPermUsers(activityPermId);
                    // 保留两个权限 其他都移除
                    permManager.batchUsersUnbindPerms(buildUnbindRequest(activityPermId, CollectionUtils.subSuffixList(userIds, 2)));
                }
                systemFinishActivities.add(activityRepoService.updateActivity(activityBO));
            }


        }
        return systemFinishActivities;
    }

    /**
     * 生成权限移除请求
     *
     * @param permId
     * @param userIds
     * @return
     */
    private PermManageRequest buildUnbindRequest(String permId, List<String> userIds) {
        PermManageRequest permManageRequest = new PermManageRequest();
        permManageRequest.setPermId(permId);
        permManageRequest.setUserIds(userIds);
        return permManageRequest;
    }
}

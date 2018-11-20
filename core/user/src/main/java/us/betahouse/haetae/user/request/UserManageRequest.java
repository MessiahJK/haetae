/**
 * betahouse.us
 * CopyRight (c) 2012 - 2018
 */
package us.betahouse.haetae.user.request;

import org.hibernate.validator.constraints.NotBlank;
import us.betahouse.haetae.user.user.model.basic.UserInfoBO;
import us.betahouse.haetae.user.user.model.basic.perm.UserBO;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户创建请求
 *
 * @author dango.yxm
 * @version : UserManageRequest.java 2018/11/18 下午10:41 dango.yxm
 */
public class UserManageRequest extends BaseRequest {

    private static final long serialVersionUID = -1457199050759374073L;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名
     */
    @NotBlank
    private String userName;

    /**
     * 密码
     */
    @NotBlank
    private String password;

    /**
     * 盐
     */
    @NotBlank
    private String salt;

    /**
     * 小程序用户id
     */
    private String openId;

    /**
     * 用户信息
     */
    private UserInfoBO userInfoBO;

    /**
     * 绑定的角色ids
     */
    List<String> roleIds = new ArrayList<>();

    /**
     * 绑定的权限ids
     */
    List<String> permIds = new ArrayList<>();

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public UserInfoBO getUserInfoBO() {
        return userInfoBO;
    }

    public void setUserInfoBO(UserInfoBO userInfoBO) {
        this.userInfoBO = userInfoBO;
    }

    public List<String> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<String> roleIds) {
        this.roleIds = roleIds;
    }

    public List<String> getPermIds() {
        return permIds;
    }

    public void setPermIds(List<String> permIds) {
        this.permIds = permIds;
    }
}
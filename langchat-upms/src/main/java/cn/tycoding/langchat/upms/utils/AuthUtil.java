package cn.tycoding.langchat.upms.utils;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.tycoding.langchat.common.constant.CacheConst;
import cn.tycoding.langchat.upms.dto.UserInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 权限相关方法
 *
 * @author tycoding
 * @since 2024/4/15
 */
public class AuthUtil {

    /**
     * 系统预制固定超级管理员角色别名
     * 作用：提供一个角色摆脱权限体系的控制，允许词角色访问所有菜单权限
     * 使用：所有涉及根据角色查询的地方都排除对此角色的限制
     */
    public static final String ADMINISTRATOR = "administrator";

    /**
     * 系统默认演示环境角色别名
     * 作用：在 langchat.auth.isDemoEnv 配置开启后，将会对所有按钮级权限拦截并提示前端
     */
    public static final String DEMO_ENV = "demo_env";

    /**
     * 客户端用户角色
     */
    public static final String CLIENT_ROLE = "client_role";

    /* 登录表单验证码Key标识 */
    public static final String CAPTCHA_FORM_KEY = "captcha";
    /* 登录验证码Header Key标识 */
    public static final String CAPTCHA_HEADER_KEY = "Captcha-Key";
    /* 验证码错误信息 */
    public static final String CAPTCHA_ERROR_INFO = "验证码不正确";
    /* 没有查询到用户名 */
    public static final String NOT_ROLE_ERROR = "没有查询到用户角色信息";

    /**
     * 获取Request对象
     */
    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
    }

    /**
     * 获取Response对象
     */
    public static HttpServletResponse getResponse() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
    }

    /**
     * 截取前端Token字符串中不包含`Bearer`的部分
     */
    public static String getToken(String token) {
        if (token != null && token.toLowerCase().startsWith("bearer")) {
            return token.replace("bearer", "").trim();
        }
        return token;
    }

    /**
     * 获取用户数据
     */
    public static UserInfo getUserInfo() {
        try {
            return (UserInfo) StpUtil.getSession().get(CacheConst.AUTH_USER_INFO_KEY);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取用户名
     */
    public static String getUsername() {
        UserInfo userInfo = getUserInfo();
        if (userInfo == null) {
            return null;
        }
        return userInfo.getUsername();
    }

    /**
     * 获取登录用户ID
     */
    public static Long getUserId() {
        UserInfo userInfo = getUserInfo();
        if (userInfo == null) {
            return null;
        }
        return userInfo.getId();
    }

    /**
     * 获取用户角色Id集合
     */
    public static List<Long> getRoleIds() {
//        List<Long> roleIds = new ArrayList<>();
//        Authentication authentication = getAuthentication();
//        if (authentication == null) {
//            return roleIds;
//        }
//        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
//        authorities.stream().filter(granted -> StringUtils.startsWith(granted.getAuthority(), AuthConstant.ROLE_PREFIX)).forEach(granted -> {
//            String id = StringUtils.substringBetween(granted.getAuthority(), AuthConstant.ROLE_PREFIX, AuthConstant.ROLE_SUFFIX);
//            roleIds.add(Long.parseLong(id));
//        });
//        return roleIds;
        return new ArrayList<>();
    }

    /**
     * 获取用户角色Alias集合
     */
    public static List<String> getRoleNames() {
//        List<String> roleNames = new ArrayList<>();
//        Authentication authentication = getAuthentication();
//        if (authentication == null) {
//            return roleNames;
//        }
//        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
//        authorities.stream().filter(granted -> StringUtils.startsWith(granted.getAuthority(), AuthConstant.ROLE_PREFIX)).forEach(granted -> {
//            String name = StringUtils.substringAfter(granted.getAuthority(), AuthConstant.ROLE_SUFFIX);
//            roleNames.add(name);
//        });
//        return roleNames;
        return new ArrayList<>();
    }

    /**
     * 密码加密
     */
    public static String encode(String salt, String password) {
        return SaSecureUtil.aesEncrypt(salt, password);
    }

    /**
     * 密码解密
     */
    public static String decrypt(String salt, String password) {
        return SaSecureUtil.aesDecrypt(salt, password);
    }

    public static void main(String[] args) {
        System.out.println(encode("langchat-salt", "123456"));
    }
}

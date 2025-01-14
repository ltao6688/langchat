package cn.tycoding.langchat.auth.endpoint;

import static cn.tycoding.langchat.common.constant.CacheConst.AUTH_SESSION_PREFIX;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import cn.tycoding.langchat.auth.service.TokenInfo;
import cn.tycoding.langchat.common.constant.CacheConst;
import cn.tycoding.langchat.common.exception.ServiceException;
import cn.tycoding.langchat.common.properties.AuthProps;
import cn.tycoding.langchat.common.utils.MybatisUtil;
import cn.tycoding.langchat.common.utils.QueryPage;
import cn.tycoding.langchat.common.utils.R;
import cn.tycoding.langchat.upms.dto.UserInfo;
import cn.tycoding.langchat.upms.service.SysUserService;
import cn.tycoding.langchat.upms.utils.AuthUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author tycoding
 * @since 2024/1/5
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthEndpoint {

    private final SysUserService userService;
    private final AuthProps authProps;
    private final StringRedisTemplate redisTemplate;

    @PostMapping("/login")
    public R login(@RequestBody UserInfo user) {
        if (StrUtil.isBlank(user.getUsername()) || StrUtil.isBlank(user.getPassword())) {
            throw new ServiceException("用户名或密码为空");
        }
        UserInfo userInfo = userService.info(user.getUsername());
        if (userInfo == null) {
            throw new ServiceException("用户名或密码错误");
        }

        String decryptPass = AuthUtil.decrypt(authProps.getSaltKey(), userInfo.getPassword());
        if (!decryptPass.equals(user.getPassword())) {
            throw new ServiceException("用户名或密码错误");
        }

        StpUtil.login(userInfo.getId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        StpUtil.getSession()
                .set(CacheConst.AUTH_USER_INFO_KEY, userInfo)
                .set(CacheConst.AUTH_TOKEN_INFO_KEY, tokenInfo);
        log.info("====> 登陆成功，token={}", tokenInfo.getTokenValue());
        return R.ok(new TokenInfo().setToken(tokenInfo.tokenValue).setExpiration(tokenInfo.tokenTimeout));
    }

    @DeleteMapping("/logout")
    public R logout() {
        StpUtil.logout();
        return R.ok();
    }

    @DeleteMapping("/token/{token}")
    public R tokenDel(@PathVariable String token) {
        StpUtil.kickoutByTokenValue(token);
        return R.ok();
    }

    @GetMapping("/token/page")
    public R tokenPage(QueryPage queryPage) {
        List<String> list = StpUtil.searchTokenValue("", queryPage.getPage() - 1, queryPage.getLimit(), true);
        List ids = redisTemplate.opsForValue().multiGet(list);
        Set<String> keys = redisTemplate.keys(AUTH_SESSION_PREFIX + "*");

        List<Object> result = new ArrayList<>();
        ids.forEach(id -> {
            Dict data = Dict.create();
            Map<String, Object> dataMap = StpUtil.getSessionByLoginId(id).getDataMap();
            UserInfo userInfo = (UserInfo)dataMap.get(CacheConst.AUTH_USER_INFO_KEY);
            SaTokenInfo tokenInfo = (SaTokenInfo)dataMap.get(CacheConst.AUTH_TOKEN_INFO_KEY);
            data.set("token", tokenInfo.tokenValue);
            data.set("perms", userInfo.getPerms());
            data.set("roles", userInfo.getRoles());
            data.set("email", userInfo.getEmail());
            data.set("id", userInfo.getId());
            data.set("username", userInfo.getUsername());
            data.set("realName", userInfo.getRealName());

            long expiration = StpUtil.getTokenTimeout();
            Date targetDate = new Date(System.currentTimeMillis() + expiration);
            String formattedDate = DateUtil.format(targetDate, DatePattern.NORM_DATETIME_PATTERN);
            data.set("expiration", formattedDate);

            result.add(data);
        });

        IPage page = new Page(queryPage.getPage(), queryPage.getLimit());
        page.setRecords(result);
        page.setTotal(keys == null ? 0 : keys.size());
        return R.ok(MybatisUtil.getData(page));
    }

}

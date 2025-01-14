package cn.tycoding.langchat.aigc.controller;

import cn.hutool.core.lang.Dict;
import cn.tycoding.langchat.aigc.entity.AigcUser;
import cn.tycoding.langchat.aigc.service.AigcUserService;
import cn.tycoding.langchat.aigc.utils.AigcAuthUtil;
import cn.tycoding.langchat.common.annotation.ApiLog;
import cn.tycoding.langchat.common.utils.MybatisUtil;
import cn.tycoding.langchat.common.utils.QueryPage;
import cn.tycoding.langchat.common.utils.R;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author tycoding
 * @since 2024/4/15
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/aigc/user")
public class AigcUserController {

    private final AigcUserService userService;

    @GetMapping("/info")
    public R<AigcUser> info() {
        AigcUser userInfo = userService.info(AigcAuthUtil.getUsername());
        userInfo.setPassword(null);
        return R.ok(userInfo);
    }

    @GetMapping("/checkName")
    public R<Boolean> checkName(AigcUser sysUser) {
        return R.ok(userService.checkName(sysUser));
    }

    @GetMapping("/list")
    public R<List<AigcUser>> list(AigcUser data) {
        return R.ok(userService.list(Wrappers.lambdaQuery()));
    }

    @GetMapping("/page")
    public R<Dict> page(AigcUser user, QueryPage queryPage) {
        return R.ok(MybatisUtil.getData(userService.page(user, queryPage)));
    }

    @GetMapping("/{id}")
    public R<AigcUser> findById(@PathVariable Long id) {
        return R.ok(userService.getById(id));
    }

    @PostMapping
    @ApiLog("新增用户")
//    @PreAuthorize("@auth.hasAuth('upms:user:add')")
    public R<AigcUser> add(@RequestBody AigcUser data) {
        userService.save(data);
        return R.ok();
    }

    @PutMapping
    @ApiLog("修改用户")
    public R update(@RequestBody AigcUser data) {
        userService.updateById(data);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @ApiLog("删除用户")
    public R delete(@PathVariable Long id) {
        AigcUser user = userService.getById(id);
        if (user != null) {
            userService.removeById(id);
        }
        return R.ok();
    }
}

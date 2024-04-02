package cn.tycoding.langchat.biz.service;

import cn.tycoding.langchat.biz.entity.SysLog;
import cn.tycoding.langchat.common.utils.QueryPage;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author tycoding
 * @since 2024/1/19
 */
public interface LogService extends IService<SysLog> {

    /**
     * 分页、条件查询
     */
    IPage<SysLog> list(SysLog sysLog, QueryPage queryPage);

    void add(SysLog sysLog);

    void delete(Long id);
}

package com.study.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.study.constant.AccountConstant;
import com.study.constant.IdConstant;
import com.study.constant.JwtClaimsConstant;
import com.study.constant.MessageConstant;
import com.study.context.BaseContext;
import com.study.dto.adminuser.AdminUserDTO;
import com.study.dto.adminuser.AdminUserLoginDTO;
import com.study.dto.adminuser.AdminUserPageQueryDTO;
import com.study.entity.AdminUser;
import com.study.exception.*;
import com.study.mapper.AdminUserMapper;
import com.study.properties.JwtProperties;
import com.study.result.PageResult;
import com.study.service.AdminUserService;
import com.study.utils.IdGeneratorUtil;
import com.study.utils.JwtUtil;
import com.study.vo.AdminUserLoginVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 新增B端用户
     *
     * @param adminUserDTO B端用户DTO对象
     */
    @Override
    public void add(AdminUserDTO adminUserDTO) {
        // 校验当前管理员账号是否有权限
        Long creatorId = BaseContext.getCurrentId();
        Integer status = adminUserMapper.checkById(creatorId);
        if (!Objects.equals(status, AccountConstant.PERMISSION))
            throw new AdminUserLevelException(MessageConstant.PERMISSION_DENIED);

        AdminUser adminUser = new AdminUser();
        BeanUtils.copyProperties(adminUserDTO, adminUser);
        adminUser.setPassword(DigestUtils.md5DigestAsHex(adminUserDTO.getPassword().getBytes()));

        Long id = IdGeneratorUtil.generateId(IdConstant.ADMIN_SIGNAL);
        adminUser.setId(id);

        adminUserMapper.insert(adminUser);
    }

    /**
     * 用户登录
     *
     * @param adminUserLoginDTO B端用户登录DTO对象
     * @return AdminUserLoginVO B端用户登录VO对象
     */
    @Override
    public AdminUserLoginVO login(AdminUserLoginDTO adminUserLoginDTO) {
        String username = adminUserLoginDTO.getUsername();

        // 根据用户登录账号查询用户库数据
        AdminUser adminUserDB = adminUserMapper.getByUsername(username);
        if (adminUserDB == null)
            // 账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);

        if (!Objects.equals(adminUserDB.getStatus(), AccountConstant.ENABLED))
            // 账号被锁定无法登陆
            throw new AccountStatusException(MessageConstant.ACCOUNT_LOCKED);

        String password = adminUserLoginDTO.getPassword();
        // 密码加密成暗文，在数据库中密码以暗文形式存储
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        if (!password.equals(adminUserDB.getPassword()))
            // 密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);

        // 生成JWT令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.ADMIN_ID, adminUserDB.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        return AdminUserLoginVO.builder()
                .id(adminUserDB.getId())
                .name(adminUserDB.getName())
                .username(username)
                .token(token)
                .build();
    }

    /**
     * 启用、禁用B端用户登录权限
     *
     * @param id     用户id
     * @param status 目标状态
     */
    @Override
    public void editStatus(Long id, Integer status) {
        Long userId = BaseContext.getCurrentId();

        AdminUser adminUserDB = adminUserMapper.getById(userId);

        if (Objects.equals(adminUserDB.getLevel(), AccountConstant.PERMISSION))
            adminUserMapper.update(AdminUser.builder()
                    .id(id)
                    .status(status)
                    .build());
        else
            throw new AdminUserLevelException(MessageConstant.PERMISSION_DENIED);
    }

    /**
     * 启用、禁用B端用户修改权限
     *
     * @param id    用户id
     * @param level 目标权限
     */
    @Override
    public void editLevel(Long id, Integer level) {
        Long userId = BaseContext.getCurrentId();

        AdminUser adminUserDB = adminUserMapper.getById(userId);

        if (Objects.equals(adminUserDB.getLevel(), AccountConstant.PERMISSION))
            adminUserMapper.update(AdminUser.builder()
                    .id(id)
                    .level(level)
                    .build());
        else
            throw new AdminUserLevelException(MessageConstant.PERMISSION_DENIED);
    }

    /**
     * B端用户分页查询
     *
     * @param adminUserPageQueryDTO B端用户分页查询DTO对象
     * @return PageResult<AdminUser> AdminUser类的分页查询对象
     */
    @Override
    public PageResult<AdminUser> getAdminListPage(AdminUserPageQueryDTO adminUserPageQueryDTO) {
        PageHelper.startPage(adminUserPageQueryDTO.getPage(), adminUserPageQueryDTO.getPageSize());

        Page<AdminUser> page = adminUserMapper.getListPage(adminUserPageQueryDTO);

        return new PageResult<>(page.getTotal(), page.getResult());
    }

    /**
     * B端用户数据查询
     *
     * @param id 用户id
     * @return AdminUser类Admin用户实体对象
     */
    @Override
    public AdminUser getById(Long id) {
        return adminUserMapper.getById(id);
    }

    /**
     * B端用户数据更新
     *
     * @param adminUser B端用户
     */
    @Override
    public void updateAdmin(AdminUser adminUser) {
        Long userId = BaseContext.getCurrentId();

        if (Objects.equals(adminUser.getId(), userId))
            throw new OperationErrorException(MessageConstant.INVALID_OPERATION);

        AdminUser adminUserDB = adminUserMapper.getById(userId);

        if (!Objects.equals(adminUserDB.getLevel(), AccountConstant.PERMISSION))
            throw new AdminUserLevelException(MessageConstant.PERMISSION_DENIED);

        String password = adminUser.getPassword();
        adminUser.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));

        adminUserMapper.update(adminUser);
    }
}

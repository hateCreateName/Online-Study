package com.study.controller.admin;

import com.study.context.BaseContext;
import com.study.dto.AdminUserDTO;
import com.study.result.Result;
import com.study.service.AdminUserService;
import com.study.vo.AdminUserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("AdminUserController")
@RequestMapping("/admin/user")
@Slf4j
public class UserController {

    @Autowired
    private AdminUserService adminUserService;

    /**
     * 新增B端用户
     *
     * @param adminUserDTO B端用户DTO
     */
    @PostMapping("/add")
    public Result<String> add(@RequestBody AdminUserDTO adminUserDTO) {
        adminUserService.add(adminUserDTO);
        log.info("新增B端用户：{}", adminUserDTO);
        return Result.success();
    }

    /**
     * B端用户登录
     *
     * @param adminUserDTO B端用户DTO
     */
    @PostMapping("/login")
    public Result<AdminUserLoginVO> login(@RequestBody AdminUserDTO adminUserDTO) {
        AdminUserLoginVO adminUserLoginVO = adminUserService.login(adminUserDTO);
        log.info("B端用户登录：{}", adminUserDTO);
        return Result.success(adminUserLoginVO);
    }

    /**
     * 设置B端用户登录权限
     *
     * @param adminUserDTO B端用户DTO
     */
    @PutMapping("/editStatus")
    public Result<String> editStatus(@RequestBody AdminUserDTO adminUserDTO) {
        adminUserService.editStatus(adminUserDTO);
        log.info("设置B端用户(id：{})登录权限为：{}", adminUserDTO.getId(), adminUserDTO.getStatus());
        return Result.success();
    }

    /**
     * 设置B端用户修改权限
     *
     * @param adminUserDTO B端用户DTO
     */
    @PutMapping("/editLevel")
    public Result<String> editLevel(@RequestBody AdminUserDTO adminUserDTO) {
        adminUserService.editLevel(adminUserDTO);
        log.info("设置B端用户(id：{})修改权限为：{}", adminUserDTO.getId(), adminUserDTO.getLevel());
        return Result.success();
    }

    /**
     * B端用户退出
     *
     * @param token jwt令牌
     */
    @GetMapping("/logout")
    public Result<String> logout(@RequestHeader String token) {
        adminUserService.logout(token);
        log.info("B端用户退出，用户id：{}", BaseContext.getCurrentId());
        return Result.success();
    }
}

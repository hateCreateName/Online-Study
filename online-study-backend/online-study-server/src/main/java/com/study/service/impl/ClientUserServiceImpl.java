package com.study.service.impl;

import com.study.constant.AccountConstant;
import com.study.constant.IdConstant;
import com.study.constant.JwtClaimsConstant;
import com.study.constant.MessageConstant;
import com.study.dto.ClientUserLoginDTO;
import com.study.dto.ClientUserRegistDTO;
import com.study.entity.ClientUser;
import com.study.exception.AccountNotFoundException;
import com.study.exception.AccountStatusException;
import com.study.exception.PasswordErrorException;
import com.study.exception.VerificationCodeErrorException;
import com.study.mapper.ClientUserMapper;
import com.study.properties.JwtProperties;
import com.study.service.ClientUserService;
import com.study.utils.EmailUtils;
import com.study.utils.IdGeneratorUtil;
import com.study.utils.JwtUtil;
import com.study.vo.ClientUserLoginVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ClientUserServiceImpl implements ClientUserService {

    @Autowired
    private ClientUserMapper clientUserMapper;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 新增C端用户
     *
     * @param clientUserRegistDTO C端用户注册DTO对象
     */
    @Override
    public void add(ClientUserRegistDTO clientUserRegistDTO) {
        //  获取用户邮箱和验证码
        String email = clientUserRegistDTO.getEmail();
        String verificationCodeRedis = stringRedisTemplate.opsForValue().get(email);
        String verificationCode = clientUserRegistDTO.getVerificationCode();

        //  验证码比对
        if (verificationCodeRedis == null || !Objects.equals(verificationCode, verificationCodeRedis)) {
            throw new VerificationCodeErrorException(MessageConstant.VERIFICATION_CODE_ERROR);
        }

        //  验证码正确，删除 Redis 中的验证码
        stringRedisTemplate.delete(email);

        //  复制数据并加密密码
        ClientUser clientUser = new ClientUser();
        BeanUtils.copyProperties(clientUserRegistDTO, clientUser);
        clientUser.setPassword(DigestUtils.md5DigestAsHex(clientUserRegistDTO.getPassword().getBytes()));

        //  生成唯一 ID 并赋值
        clientUser.setId(IdGeneratorUtil.generateId(IdConstant.CLIENT_SIGNAL));

        //  插入数据库
        clientUserMapper.insert(clientUser);
    }

    /**
     * 用户登录
     *
     * @param clientUserLoginDTO C端用户登录DTO对象
     * @return ClientUserLoginVO C端用户登录VO对象
     */
    @Override
    public ClientUserLoginVO login(ClientUserLoginDTO clientUserLoginDTO) {
        String email = clientUserLoginDTO.getEmail();

        // 根据用户邮箱号、登录账号查询用户库数据
        ClientUser clientUserDB = clientUserMapper.getByEmail(email);
        if (clientUserDB == null)
            // 账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);

        if (!Objects.equals(clientUserDB.getStatus(), AccountConstant.ENABLED))
            // 账号被封禁无法登录
            throw new AccountStatusException(MessageConstant.ACCOUNT_LOCKED);

        String password = clientUserLoginDTO.getPassword();
        // 密码加密成暗文，在数据库中密码以暗文形式存储
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        if (!password.equals(clientUserDB.getPassword()))
            // 密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);

        // 生成JWT令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.CLIENT_ID, clientUserDB.getId());
        String authentication = JwtUtil.createJWT(
                jwtProperties.getClientSecretKey(),
                jwtProperties.getClientTtl(),
                claims);

        return ClientUserLoginVO.builder()
                .id(clientUserDB.getId())
                .name(clientUserDB.getName())
                .email(email)
                .authentication(authentication)
                .build();
    }

    /**
     * 发送验证码
     *
     * @param toEmail 目标邮箱
     */
    @Override
    public void sendMsg(String toEmail) {
        String verificationCode = UUID.randomUUID().toString().substring(
                AccountConstant.VERIFICATION_CODE_START,
                AccountConstant.VERIFICATION_CODE_LENGTH);
        EmailUtils.sendVerificationCode(toEmail, verificationCode);

        stringRedisTemplate.opsForValue()
                .set(toEmail, verificationCode, AccountConstant.VERIFICATION_CODE_TTL, TimeUnit.MINUTES);
    }
}

package com.study.service.impl;

import com.study.constant.MessageConstant;
import com.study.context.BaseContext;
import com.study.dto.post.PostDTO;
import com.study.entity.Post;
import com.study.exception.AccountPermissionsException;
import com.study.exception.PostNotFoundException;
import com.study.mapper.PostMapper;
import com.study.service.PostService;
import com.study.vo.PostVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostMapper postMapper;

    /**
     * 新增帖子
     *
     * @param postDTO 帖子新增对象
     */
    @Override
    public void insert(PostDTO postDTO) {
        Post post = new Post();
        BeanUtils.copyProperties(postDTO, post);

        Long userId = BaseContext.getCurrentId();
        post.setUserId(userId);

        postMapper.insert(post);
    }

    /**
     * 获取帖子信息
     *
     * @param id 帖子Id
     * @return PostVO帖子VO对象
     */
    @Override
    public PostVO get(Long id) {
        Post post = postMapper.getById(id);

        if (post == null)
            throw new PostNotFoundException(MessageConstant.POST_NOT_FOUND);

        PostVO postVO = new PostVO();
        BeanUtils.copyProperties(post, postVO);

        return postVO;
    }

    /**
     * 修改帖子信息
     *
     * @param postDTO 帖子DTO对象
     */
    @Override
    public void update(PostDTO postDTO) {
        Post post = postMapper.getById(postDTO.getId());
        Long userId = BaseContext.getCurrentId();

        if (!Objects.equals(post.getUserId(), userId))
            throw new AccountPermissionsException(MessageConstant.PERMISSION_DENIED);

        BeanUtils.copyProperties(postDTO, post);
        postMapper.update(post);
    }
}

package com.study.controller.client;

import com.study.context.BaseContext;
import com.study.dto.PostDTO;
import com.study.result.Result;
import com.study.service.PostService;
import com.study.vo.PostVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/client/post")
@RestController
@Slf4j
public class PostController {

    @Autowired
    private PostService postService;

    /**
     * 新增帖子
     *
     * @param postDTO 帖子DTO对象
     */
    @PostMapping("/add")
    public Result<String> add(@RequestBody PostDTO postDTO) {
        postService.add(postDTO);
        log.info("C端用户(id：{})新增帖子：{}", BaseContext.getCurrentId(), postDTO);
        return Result.success();
    }

    /**
     * 获取帖子信息
     *
     * @param id 帖子id
     */
    @GetMapping("/get")
    public Result<PostVO> get(Long id) {
        PostVO postVO = postService.get(id);
        log.info("C端用户(id：{})查看帖子：{}", BaseContext.getCurrentId(), id);
        return Result.success(postVO);
    }

    /**
     * 删除帖子
     *
     * @param id 帖子id
     */
    @DeleteMapping("/delete")
    public Result<String> delete(Long id) {
        postService.delete(id);
        log.info("C端用户(id：{})删除帖子：{}", BaseContext.getCurrentId(), id);
        return Result.success();
    }
}
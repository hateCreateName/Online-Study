package com.study.dto.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostInsertDTO {

    // 帖子标题
    private String title;

    // 帖子内容
    private String content;

    // 帖子种类id
    private Integer categoryId;
}

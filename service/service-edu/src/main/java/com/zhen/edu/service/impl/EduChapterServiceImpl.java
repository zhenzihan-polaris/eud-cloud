package com.zhen.edu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhen.edu.entity.EduChapter;
import com.zhen.edu.entity.EduVideo;
import com.zhen.edu.entity.vo.ChapterVo;
import com.zhen.edu.entity.vo.VideoVo;
import com.zhen.edu.mapper.EduChapterMapper;
import com.zhen.edu.service.EduChapterService;
import com.zhen.edu.service.EduVideoService;
import com.zhen.servicebase.exception.SystemException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhen
 * @since 2023-02-09 16:53:31
 */
@Service("eduChapterService")
public class EduChapterServiceImpl extends ServiceImpl<EduChapterMapper, EduChapter> implements EduChapterService {


        @Autowired
        private EduVideoService eduVideoService;

        @Override
        public List<ChapterVo> getChapterVideoByCourseId(String courseId) {
            //最终要的数据列表
            ArrayList<ChapterVo> finalChapterVos = new ArrayList<>();

            //查询章节信息
            QueryWrapper<EduChapter> wrapper = new QueryWrapper<>();
            wrapper.eq("course_id",courseId);
            List<EduChapter> eduChapters = baseMapper.selectList(wrapper);

            //查询小节信息
            QueryWrapper<EduVideo> wrapper1 = new QueryWrapper<>();
            wrapper1.eq("course_id",courseId);
            List<EduVideo> eduVideos = eduVideoService.list(wrapper1);

            //填充章节vo数据
            for (int i = 0; i < eduChapters.size(); i++) {
                EduChapter chapter = eduChapters.get(i);

                //创建章节vo对象
                ChapterVo chapterVo = new ChapterVo();
                BeanUtils.copyProperties(chapter,chapterVo);
                finalChapterVos.add(chapterVo);

                //填充课时vo对象
                ArrayList<VideoVo> finalVideoVos = new ArrayList<>();
                for (int j = 0; j < eduVideos.size(); j++) {
                    EduVideo video = eduVideos.get(j);

                    if (chapter.getId().equals(video.getChapterId())){
                        VideoVo videoVo = new VideoVo();
                        BeanUtils.copyProperties(video,videoVo);
                        finalVideoVos.add(videoVo);
                    }
                }

                chapterVo.setChildren(finalVideoVos);

            }
            return finalChapterVos;

        }

    //删除章节的方法
    @Override
    public boolean deleteChapter(String chapterId) {
        //根据chapter章节id 查询查询小节表，如果查询有数据，则不删除
        QueryWrapper<EduVideo> wrapper = new QueryWrapper<>();
        wrapper.eq("chapter_id",chapterId);
        int count = eduVideoService.count(wrapper);
        //判断
        if (count>0){
            //能查询出来小节，不进行删除
            throw new SystemException(20001,"还有小节数据，不能删除");
        }else {
            //不能查询出小节，进行删除
            int delete = baseMapper.deleteById(chapterId);
            return delete>0;
        }
    }



}


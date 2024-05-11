package com.linkcao.controller;

import com.linkcao.entity.Task;
import com.linkcao.entity.Users;
import com.linkcao.enums.TaskStatus;
import com.linkcao.service.ChatService;
import com.linkcao.service.TaskService;
import com.linkcao.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final UserService userService;
    private final TaskService taskService;


    /**
     * 依靠线程池批量请求GPT
     * @param promptFile 传入的批量提示文件，每一行为一个提示语句
     * @param username 调用的用户
     * @return 处理状态
     */
    @PostMapping("/batch")
    public String batchPrompt(MultipartFile promptFile, String username){
        if (promptFile.isEmpty()) {
            return "上传的文件为空";
        }
        // 批量请求任务
        Task task = new Task();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(promptFile.getInputStream()));
            List<String> prompts = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                prompts.add(line);
            }
            // 用户信息请求
            Users user = userService.findByUsername(username);
            // 任务状态设置
            task.setFileName(promptFile.getName());
            task.setStartTime(LocalDateTime.now());
            task.setUserId(user.getUserId());
            task.setStatus(TaskStatus.PROCESSING);
            // 线程池处理
            chatService.processPrompts(prompts, user, task);
            return "文件上传成功，已开始批量处理提示";
        } catch ( IOException e) {
            // 处理失败
            e.printStackTrace();
            task.setStatus(TaskStatus.FAILED);
            return "上传文件时出错：" + e.getMessage();
        } finally {
            // 任务状态保存
            taskService.setTask(task);
        }
    }

}

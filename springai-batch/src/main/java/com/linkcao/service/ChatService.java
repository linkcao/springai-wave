package com.linkcao.service;

import com.linkcao.entity.*;
import com.linkcao.enums.TaskStatus;
import com.linkcao.repository.AnswersRepository;
import com.linkcao.repository.KeyInfoRepository;
import com.linkcao.repository.QuestionsRepository;
import com.linkcao.repository.UsersRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {

    private final KeyInfoRepository keyRepository;
    private final QuestionsRepository questionRepository;
    private final AnswersRepository answerRepository;
    private final TaskService taskService;

    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    @PostConstruct
    public void initData() {

    }

    /**
     * 多线程请求提示
     * @param prompts
     * @param user
     * @param task
     * @return
     */
    @Async
    public CompletableFuture<Void> processPrompts(List<String> prompts, Users user, Task task) {
        for (int i = 0; i < prompts.size();i++) {
            int finalI = i;
            executor.submit(() -> processPrompt(prompts.get(finalI), user, finalI));
        }
        task.setStatus(TaskStatus.COMPLETED);
        taskService.setTask(task);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * 处理单条提示文本
     * @param prompt 提示文本
     * @param user 用户
     * @param index 所在队列下标
     */
    public void processPrompt(String prompt, Users user, int index) {
        // 获取Api Key
        OpenAiApi openAiApi = getApiByIndex(user, index);
        assert openAiApi != null;
        ChatClient client = new OpenAiChatClient(openAiApi);
        // 提示文本请求
        String response = client.call(prompt);
        log.info("提示信息" + prompt );
        log.info("输出" + response );
        // 回答保存数据库
        saveQuestionAndAnswer(user, prompt, response);
    }

    /**
     * 保存问题和答案
     * @param user 用户
     * @param questionContent 问题
     * @param answerContent 答案
     */
    public void saveQuestionAndAnswer(Users user, String questionContent, String answerContent) {
        // 保存问题
        Questions question = new Questions();
        question.setUser(user);
        question.setQuestionContent(questionContent);
        question.setQuestionTime(LocalDateTime.now());
        questionRepository.save(question);

        // 保存回答
        Answers answer = new Answers();
        answer.setUser(user);
        answer.setQuestion(question);
        answer.setAnswerContent(answerContent);
        answer.setAnswerTime(LocalDateTime.now());
        answerRepository.save(answer);
    }


    /**
     * 采用任务下标分配key的方式进行负载均衡
     * @param index 任务下标
     * @return OpenAiApi
     */
    private OpenAiApi getApiByIndex(Users user, int index){
        List<KeyInfo> keyInfoList = keyRepository.findByUserUserId(user.getUserId());
        if (keyInfoList.isEmpty()) {
            return null;
        }
        // 根据任务队列下标分配 Key
        KeyInfo keyInfo = keyInfoList.get(index % keyInfoList.size());
        return new OpenAiApi(keyInfo.getApi(),keyInfo.getKeyValue());
    }



}

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

    // 阻塞式
    public ChatClient getChatClient() {
        OpenAiApi openAiApi = randomGetApi();
        assert openAiApi != null;
        return new OpenAiChatClient(openAiApi);
    }

    @Async
    public CompletableFuture<Void> processPrompts(List<String> prompts, Users user, Task task) {
        for (String prompt : prompts) {
            executor.submit(() -> processPrompt(prompt, user));
        }
        task.setStatus(TaskStatus.COMPLETED);
        taskService.setTask(task);
        return CompletableFuture.completedFuture(null);
    }

    public void processPrompt(String prompt, Users user) {
        OpenAiApi openAiApi = randomGetApi();
        assert openAiApi != null;
        ChatClient client = new OpenAiChatClient(openAiApi);
        String response = client.call(prompt);
        log.info("提示信息" + prompt );
        log.info("输出" + response );

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 回答保存数据库
        saveQuestionAndAnswer(user, prompt, response);
    }

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
    // 流式
    public StreamingChatClient getStreamChatClient() {
        OpenAiApi openAiApi = randomGetApi();
        assert openAiApi != null;
        return new OpenAiChatClient(openAiApi);
    }


    // 随机获取一个OpenAiApi
    private OpenAiApi randomGetApi(){
        List<KeyInfo> keyInfoList = keyRepository.findAll();
        // 如果数据库中没有KeyInfo对象，则返回null
        if (keyInfoList.isEmpty()) {
            return null;
        }
        // 随机选择一个KeyInfo对象
        Random random = new Random();
        KeyInfo randomKeyInfo = keyInfoList.get(random.nextInt(keyInfoList.size()));
        return new OpenAiApi(randomKeyInfo.getApi(),randomKeyInfo.getKeyValue());
    }



}

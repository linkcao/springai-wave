package com.linkcao.service;

import com.linkcao.entity.Task;
import com.linkcao.enums.TaskStatus;
import com.linkcao.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public void setTask(Task task){
        if (task != null && ( task.getStatus().equals(TaskStatus.FAILED) || task.getStatus().equals(TaskStatus.COMPLETED) )) {
            task.setEndTime(LocalDateTime.now());
        }
        taskRepository.save(task);
    }

}

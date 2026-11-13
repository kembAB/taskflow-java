package com.abel.taskflow.repository;

import com.abel.taskflow.model.Task;
import com.abel.taskflow.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatus(TaskStatus status);

    long countByStatus(TaskStatus status);
}

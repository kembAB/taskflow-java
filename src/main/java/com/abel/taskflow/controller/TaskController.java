package com.abel.taskflow.controller;

import com.abel.taskflow.model.Task;
import com.abel.taskflow.model.TaskStatus;
import com.abel.taskflow.repository.TaskRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository repository;

    public TaskController(TaskRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Task> list(@RequestParam(required = false) TaskStatus status) {
        if (status != null) {
            return repository.findByStatus(status);
        }
        return repository.findAll();
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", repository.count());
        result.put("todo", repository.countByStatus(TaskStatus.TODO));
        result.put("inProgress", repository.countByStatus(TaskStatus.IN_PROGRESS));
        result.put("done", repository.countByStatus(TaskStatus.DONE));
        return result;
    }

    @GetMapping("/{id}")
    public Task get(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task not found: " + id));
    }

    @PostMapping
    public ResponseEntity<Task> create(@Valid @RequestBody Task task) {
        task.setId(null);
        Task saved = repository.save(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public Task update(@PathVariable Long id, @Valid @RequestBody Task incoming) {
        Task existing = get(id);
        existing.setTitle(incoming.getTitle());
        existing.setDescription(incoming.getDescription());
        existing.setStatus(incoming.getStatus());
        existing.setPriority(incoming.getPriority());
        existing.setDueDate(incoming.getDueDate());
        return repository.save(existing);
    }

    @PatchMapping("/{id}/complete")
    public Task complete(@PathVariable Long id) {
        Task existing = get(id);
        existing.setStatus(TaskStatus.DONE);
        return repository.save(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Task not found: " + id);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

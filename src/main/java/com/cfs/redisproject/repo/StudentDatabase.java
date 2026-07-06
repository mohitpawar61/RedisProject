package com.cfs.redisproject.repo;

import com.cfs.redisproject.entity.Student;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository
public class StudentDatabase {

    private static final Logger log = LoggerFactory.getLogger(StudentDatabase.class);

    private final Map<Long, Student> database = new ConcurrentHashMap<>();

    public Student save(Student student) throws InterruptedException
    {
        log.info("DATABASE: Saving student with id {}",student.getId());
        Thread.sleep(100);
        database.put(student.getId(),student);
        return student;
    }

    public List<Student> findAll() throws InterruptedException
    {
        log.info("DATABASE: Finding all student");
        Thread.sleep(100);
        return new ArrayList<>(database.values());
    }

    public List<Student> findByEmail(String email) throws InterruptedException
    {
        log.info("DATABASE: Finding student by email");
        Thread.sleep(100);
        return database.values().stream()
                .filter(s->email.equals(s.getEmail()))
                .collect(Collectors.toList());
    }

    public void deleteById(Long id)throws InterruptedException
    {
        log.info("DATABASE: deleting student by id");
        Thread.sleep(100);
        database.remove(id);
    }

    public void deleteAll()throws InterruptedException
    {
        log.info("DATABASE: deleting all students");
        Thread.sleep(100);
        database.clear();
    }
}

package com.cfs.redisproject.service;

import com.cfs.redisproject.entity.Student;
import com.cfs.redisproject.repo.StudentDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentService.class);

    private final StudentDatabase studentDatabase;


    public StudentService(StudentDatabase studentDatabase) {
        this.studentDatabase = studentDatabase;
    }

    @CachePut(value = "students",key = "#result.id")
    public Student createStudent(Student student) throws InterruptedException
    {
       log.info("SERVICE: creating student with id {}",student.getId());
       Student std = studentDatabase.save(student);
        log.info("CACHE: Student cached with key {}",student.getId());
        return std;
    }


    public List<Student> getAllStudents() throws InterruptedException
    {
        log.info("SERVICE: Getting all students");
        return studentDatabase.findAll();
    }

    @Cacheable(value = "studentByEmail",key = "#email")
    public List<Student> getStudentByEmail(String email) throws InterruptedException
    {
        log.info("SERVICE: Setting all students by email {}",email);
        log.info("CACHE MISS: fetching data from DB {}",email);
       List<Student> byEmail = studentDatabase.findByEmail(email);
        log.info("CACHE: storing data into CACHE");
        return byEmail;
    }

    @CacheEvict(value = {"student","studentByEmail"},allEntries = true)
    public void deleteAllStudents() throws InterruptedException
    {
        log.info("SERVICE: deleting all students");
        studentDatabase.deleteAll();
        log.info("CACHE: clear All");

    }

    @CacheEvict(value = "student",key = "#id")
    public void deleteStudentById(Long id)  throws InterruptedException
    {
        log.info("SERVICE: delete students by id {}",id);
        studentDatabase.deleteById(id);
        log.info("CACHE: student deleted by id{}",id);

    }
}

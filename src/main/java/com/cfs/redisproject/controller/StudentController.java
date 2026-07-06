package com.cfs.redisproject.controller;

import com.cfs.redisproject.entity.Student;
import com.cfs.redisproject.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Student API",description = "CRUD operations for students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @Operation(summary = "create a new student",description = "create a new student in redis")
    @ApiResponses(value ={
    @ApiResponse(responseCode = "201",description = "student creates successfully")})
    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestBody Student student) throws InterruptedException
    {
       Student res =  studentService.createStudent(student);
       return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all students",description = "get all student record from redis")
    @ApiResponses(value ={
    @ApiResponse(responseCode = "200",description = "List of students")
})
    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() throws InterruptedException
    {
        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    @Operation(summary = "Search by email",description = "find student in redis by email")
    @GetMapping("/search/email")
    public ResponseEntity<List<Student>> getStudentByEmail(@RequestParam String email) throws InterruptedException
    {
        List<Student> students = studentService.getStudentByEmail(email);
        return ResponseEntity.ok(students);
    }
    @Operation(summary = "delete all students",description = "delete all student record from redis")
    @ApiResponses(value ={
            @ApiResponse(responseCode = "204",description = "All students deleted")
    })
    @DeleteMapping
    public ResponseEntity<Void> deleteAllStudents() throws InterruptedException
    {
        studentService.deleteAllStudents();
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "delete student by id",description = "delete student record by id from redis")
    @ApiResponses(value ={
            @ApiResponse(responseCode = "200",description = "All students deleted"),
            @ApiResponse(responseCode = "404",description = "student not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudentById(@PathVariable Long id) throws InterruptedException
    {
        studentService.deleteStudentById(id);
        return ResponseEntity.noContent().build();
    }
}

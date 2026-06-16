package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String, // e.g. "admin", student registration number, or parent's phone
    val password: String,
    val role: String, // "ADMIN", "STUDENT", "PARENT"
    val associatedId: Int? = null // References either student.id or parent.id
) : Serializable

@Entity(tableName = "admissions")
data class AdmissionApplied(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val dateOfBirth: String,
    val gender: String,
    val gradeApplied: String, // e.g., "Nursery 1", "Primary 1"
    val parentName: String,
    val parentPhone: String,
    val parentEmail: String,
    val previousSchool: String,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val appliedDate: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val admissionNo: String, // Unique generated, e.g., HG/2026/104
    val fullName: String,
    val dateOfBirth: String,
    val gender: String,
    val className: String, // Matches ClassEntity.className
    val parentName: String,
    val parentPhone: String,
    val parentEmail: String,
    val address: String,
    val registrationDate: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "classes")
data class ClassEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val className: String, // e.g., "Nursery 1", "Primary 1" (Unique)
    val teacherName: String,
    val roomNumber: String
) : Serializable

@Entity(tableName = "results")
data class TermResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int, // Student.id
    val term: String, // "First Term", "Second Term", "Third Term"
    val session: String, // e.g., "2025/2026"
    val subject: String, // e.g., "Mathematics", "English", "Science"
    val testScore: Int, // Out of 30 or 40
    val examScore: Int, // Out of 70 or 60
    val totalScore: Int, // testScore + examScore
    val grade: String, // A, B, C, D, E, F
    val remarks: String
) : Serializable

@Entity(tableName = "announcements")
data class Announcement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val targetAudience: String, // "ALL", "STUDENTS", "PARENTS"
    val publishedDate: Long = System.currentTimeMillis(),
    val author: String = "School Administration"
) : Serializable

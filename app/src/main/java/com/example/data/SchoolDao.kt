package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolDao {

    // --- Users ---
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<User>>

    @Query("UPDATE users SET password = :newPassword WHERE id = :userId")
    suspend fun updateUserPassword(userId: Int, newPassword: String)


    // --- Admissions ---
    @Query("SELECT * FROM admissions ORDER BY appliedDate DESC")
    fun getAllAdmissionsFlow(): Flow<List<AdmissionApplied>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmission(admission: AdmissionApplied): Long

    @Query("UPDATE admissions SET status = :status WHERE id = :id")
    suspend fun updateAdmissionStatus(id: Int, status: String)

    @Query("SELECT * FROM admissions WHERE id = :id LIMIT 1")
    suspend fun getAdmissionById(id: Int): AdmissionApplied?


    // --- Students ---
    @Query("SELECT * FROM students ORDER BY fullName ASC")
    fun getAllStudentsFlow(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    fun getStudentByIdFlow(id: Int): Flow<Student?>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentByIdDirect(id: Int): Student?

    @Query("SELECT * FROM students WHERE admissionNo = :admissionNo LIMIT 1")
    suspend fun getStudentByAdmissionNo(admissionNo: String): Student?

    @Query("SELECT * FROM students WHERE className = :className ORDER BY fullName ASC")
    fun getStudentsInClassFlow(className: String): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE parentPhone = :parentPhone")
    suspend fun getStudentsByParentPhone(parentPhone: String): List<Student>

    @Query("SELECT * FROM students WHERE parentPhone = :parentPhone")
    fun getStudentsByParentPhoneFlow(parentPhone: String): Flow<List<Student>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)


    // --- Classes ---
    @Query("SELECT * FROM classes ORDER BY className ASC")
    fun getAllClassesFlow(): Flow<List<ClassEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(classEntity: ClassEntity): Long

    @Query("DELETE FROM classes WHERE id = :id")
    suspend fun deleteClassById(id: Int)


    // --- Term Results ---
    @Query("SELECT * FROM results ORDER BY id DESC")
    fun getAllResultsFlow(): Flow<List<TermResult>>

    @Query("SELECT * FROM results WHERE studentId = :studentId")
    fun getResultsForStudentFlow(studentId: Int): Flow<List<TermResult>>

    @Query("SELECT * FROM results WHERE studentId = :studentId")
    suspend fun getResultsForStudentDirect(studentId: Int): List<TermResult>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: TermResult): Long

    @Query("DELETE FROM results WHERE id = :resultId")
    suspend fun deleteResultById(resultId: Int)


    // --- Announcements ---
    @Query("SELECT * FROM announcements ORDER BY publishedDate DESC")
    fun getAllAnnouncementsFlow(): Flow<List<Announcement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: Announcement): Long

    @Query("DELETE FROM announcements WHERE id = :announcementId")
    suspend fun deleteAnnouncementById(announcementId: Int)
}

package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SchoolRepository(private val dao: SchoolDao) {

    // Streams
    val allAdmissions: Flow<List<AdmissionApplied>> = dao.getAllAdmissionsFlow()
    val allStudents: Flow<List<Student>> = dao.getAllStudentsFlow()
    val allClasses: Flow<List<ClassEntity>> = dao.getAllClassesFlow()
    val allResults: Flow<List<TermResult>> = dao.getAllResultsFlow()
    val allAnnouncements: Flow<List<Announcement>> = dao.getAllAnnouncementsFlow()
    val allUsers: Flow<List<User>> = dao.getAllUsersFlow()

    // Users
    suspend fun getUserByUsername(username: String): User? = withContext(Dispatchers.IO) {
        dao.getUserByUsername(username)
    }

    suspend fun insertUser(user: User): Long = withContext(Dispatchers.IO) {
        dao.insertUser(user)
    }

    suspend fun updateUserPassword(userId: Int, newPassword: String) = withContext(Dispatchers.IO) {
        dao.updateUserPassword(userId, newPassword)
    }

    // Admissions
    suspend fun submitAdmission(admission: AdmissionApplied): Long = withContext(Dispatchers.IO) {
        dao.insertAdmission(admission)
    }

    suspend fun getAdmissionById(id: Int): AdmissionApplied? = withContext(Dispatchers.IO) {
        dao.getAdmissionById(id)
    }

    suspend fun approveAdmission(admissionId: Int, classToAssign: String): Student? = withContext(Dispatchers.IO) {
        val admission = dao.getAdmissionById(admissionId) ?: return@withContext null
        
        // 1. Mark status as APPROVED
        dao.updateAdmissionStatus(admissionId, "APPROVED")

        // 2. Generate admission number
        val year = 2026 // Current test year
        val randomNum = (100..999).random()
        val admissionNo = "HG/$year/$randomNum"

        // 3. Create Student record
        val newStudent = Student(
            admissionNo = admissionNo,
            fullName = admission.fullName,
            dateOfBirth = admission.dateOfBirth,
            gender = admission.gender,
            className = classToAssign,
            parentName = admission.parentName,
            parentPhone = admission.parentPhone,
            parentEmail = admission.parentEmail,
            address = "Not specified"
        )
        val studentId = dao.insertStudent(newStudent).toInt()

        // 4. Create Student Login Portal Account
        val studentUser = User(
            username = admissionNo,
            password = "student123", // Default password
            role = "STUDENT",
            associatedId = studentId
        )
        dao.insertUser(studentUser)

        // 5. Create Parent Portal Account if it doesn't already exist
        val parentUserCheck = dao.getUserByUsername(admission.parentPhone)
        if (parentUserCheck == null) {
            val parentUser = User(
                username = admission.parentPhone,
                password = "parent123", // Default parent login
                role = "PARENT",
                associatedId = studentId
            )
            dao.insertUser(parentUser)
        }

        return@withContext newStudent.copy(id = studentId)
    }

    suspend fun rejectAdmission(admissionId: Int) = withContext(Dispatchers.IO) {
        dao.updateAdmissionStatus(admissionId, "REJECTED")
    }

    // Students
    fun getStudentByIdFlow(id: Int): Flow<Student?> = dao.getStudentByIdFlow(id)

    suspend fun getStudentByAdmissionNo(admissionNo: String): Student? = withContext(Dispatchers.IO) {
        dao.getStudentByAdmissionNo(admissionNo)
    }

    suspend fun getStudentsByParentPhone(parentPhone: String): List<Student> = withContext(Dispatchers.IO) {
        dao.getStudentsByParentPhone(parentPhone)
    }
    
    suspend fun getStudentByIdDirect(id: Int): Student? = withContext(Dispatchers.IO) {
        dao.getStudentByIdDirect(id)
    }

    fun getStudentsInClass(className: String): Flow<List<Student>> = dao.getStudentsInClassFlow(className)

    fun getStudentsByParentPhoneFlow(parentPhone: String): Flow<List<Student>> = dao.getStudentsByParentPhoneFlow(parentPhone)

    suspend fun registerStudentDirectly(student: Student): Long = withContext(Dispatchers.IO) {
        val studentId = dao.insertStudent(student)
        
        // Register Student Login
        val studentUser = User(
            username = student.admissionNo,
            password = "student123",
            role = "STUDENT",
            associatedId = studentId.toInt()
        )
        dao.insertUser(studentUser)

        // Register Parent Login
        val parentUserCheck = dao.getUserByUsername(student.parentPhone)
        if (parentUserCheck == null) {
            val parentUser = User(
                username = student.parentPhone,
                password = "parent123",
                role = "PARENT",
                associatedId = studentId.toInt()
            )
            dao.insertUser(parentUser)
        }
        studentId
    }

    suspend fun updateStudent(student: Student) = withContext(Dispatchers.IO) {
        dao.updateStudent(student)
    }

    suspend fun deleteStudent(student: Student) = withContext(Dispatchers.IO) {
        dao.deleteStudent(student)
    }

    // Classes
    suspend fun addClass(classEntity: ClassEntity): Long = withContext(Dispatchers.IO) {
        dao.insertClass(classEntity)
    }

    suspend fun deleteClass(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteClassById(id)
    }

    // Term Results
    fun getResultsForStudentFlow(studentId: Int): Flow<List<TermResult>> = dao.getResultsForStudentFlow(studentId)

    suspend fun saveResult(result: TermResult): Long = withContext(Dispatchers.IO) {
        dao.insertResult(result)
    }

    suspend fun deleteResult(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteResultById(id)
    }

    // Announcements
    suspend fun addAnnouncement(announcement: Announcement): Long = withContext(Dispatchers.IO) {
        dao.insertAnnouncement(announcement)
    }

    suspend fun deleteAnnouncement(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteAnnouncementById(id)
    }

    // Core Seeding function
    suspend fun seedDatabaseIfNeeded() = withContext(Dispatchers.IO) {
        val adminCheck = dao.getUserByUsername("admin")
        if (adminCheck == null) {
            // 1. Seed Admin
            dao.insertUser(
                User(
                    username = "admin",
                    password = "admin123",
                    role = "ADMIN"
                )
            )

            // 2. Seed default Classes
            val classesToInsert = listOf(
                ClassEntity(className = "Playgroup", teacherName = "Mrs. Agnes Benson", roomNumber = "Classroom A"),
                ClassEntity(className = "Nursery 1", teacherName = "Miss Sarah Davies", roomNumber = "Classroom B"),
                ClassEntity(className = "Nursery 2", teacherName = "Mrs. Clara Egboh", roomNumber = "Classroom C"),
                ClassEntity(className = "Primary 1", teacherName = "Mr. Solomon Idowu", roomNumber = "Building Alpha 1"),
                ClassEntity(className = "Primary 2", teacherName = "Miss Joy Obi", roomNumber = "Building Alpha 2"),
                ClassEntity(className = "Primary 3", teacherName = "Mr. David Alao", roomNumber = "Building Beta 1")
            )
            for (c in classesToInsert) {
                dao.insertClass(c)
            }

            // 3. Seed default Students
            val s1Id = dao.insertStudent(
                Student(
                    admissionNo = "HG/2026/001",
                    fullName = "Emeka Okafor",
                    dateOfBirth = "2019-10-12",
                    gender = "Male",
                    className = "Primary 1",
                    parentName = "Chief Edwin Okafor",
                    parentPhone = "08055551234",
                    parentEmail = "okafor.edwin@example.com",
                    address = "12 Grace Close, Lekki, Lagos"
                )
            ).toInt()

            dao.insertUser(
                User(
                    username = "HG/2026/001",
                    password = "student123",
                    role = "STUDENT",
                    associatedId = s1Id
                )
            )
            dao.insertUser(
                User(
                    username = "08055551234",
                    password = "parent123",
                    role = "PARENT",
                    associatedId = s1Id
                )
            )

            val s2Id = dao.insertStudent(
                Student(
                    admissionNo = "HG/2026/002",
                    fullName = "Olarotimi Adebayo",
                    dateOfBirth = "2020-04-05",
                    gender = "Male",
                    className = "Nursery 2",
                    parentName = "Pastor Kolawole Adebayo",
                    parentPhone = "08122334455",
                    parentEmail = "kola.adebayo@example.com",
                    address = "Plot 64, His Grace Estate, Ikorodu"
                )
            ).toInt()

            dao.insertUser(
                User(
                    username = "HG/2026/002",
                    password = "student123",
                    role = "STUDENT",
                    associatedId = s2Id
                )
            )
            dao.insertUser(
                User(
                    username = "08122334455",
                    password = "parent123",
                    role = "PARENT",
                    associatedId = s2Id
                )
            )

            val s3Id = dao.insertStudent(
                Student(
                    admissionNo = "HG/2026/003",
                    fullName = "Aisha Ibrahim",
                    dateOfBirth = "2018-01-20",
                    gender = "Female",
                    className = "Primary 2",
                    parentName = "Alhaji Bello Ibrahim",
                    parentPhone = "09044331122",
                    parentEmail = "bello_ibro@example.com",
                    address = "Hassan Avenue, Ikeja"
                )
            ).toInt()

            dao.insertUser(
                User(
                    username = "HG/2026/003",
                    password = "student123",
                    role = "STUDENT",
                    associatedId = s3Id
                )
            )
            dao.insertUser(
                User(
                    username = "09044331122",
                    password = "parent123",
                    role = "PARENT",
                    associatedId = s3Id
                )
            )

            // 4. Seed Terms Results
            val results = listOf(
                TermResult(studentId = s1Id, term = "First Term", session = "2025/2026", subject = "Mathematics", testScore = 28, examScore = 56, totalScore = 84, grade = "A", remarks = "Excellent performance"),
                TermResult(studentId = s1Id, term = "First Term", session = "2025/2026", subject = "English Language", testScore = 25, examScore = 51, totalScore = 76, grade = "A", remarks = "Very good expression"),
                TermResult(studentId = s1Id, term = "First Term", session = "2025/2026", subject = "Basic Science", testScore = 29, examScore = 60, totalScore = 89, grade = "A", remarks = "Outstanding intellect"),
                TermResult(studentId = s1Id, term = "First Term", session = "2025/2026", subject = "Quantitative Reasoning", testScore = 21, examScore = 48, totalScore = 69, grade = "B", remarks = "Good logic, can do better"),

                TermResult(studentId = s2Id, term = "First Term", session = "2025/2026", subject = "Handwriting", testScore = 30, examScore = 55, totalScore = 85, grade = "A", remarks = "Very neat legibility"),
                TermResult(studentId = s2Id, term = "First Term", session = "2025/2026", subject = "Numeracy & Counting", testScore = 20, examScore = 45, totalScore = 65, grade = "B", remarks = "Developing well"),
                TermResult(studentId = s2Id, term = "First Term", session = "2025/2026", subject = "Phonics & Literacy", testScore = 24, examScore = 42, totalScore = 66, grade = "B", remarks = "Very attentive student")
            )
            for (res in results) {
                dao.insertResult(res)
            }

            // 5. Seed Announcements
            val announcements = listOf(
                Announcement(
                    title = "Resumption Notice for 2026/2027 Session",
                    content = "His Grace Nursery & Primary School is welcoming all new and returning pupils for the upcoming term on September 7th, 2026. Please ensure all uniforms and materials are ready.",
                    targetAudience = "ALL"
                ),
                Announcement(
                    title = "End of year P.T.A Meeting",
                    content = "The Parents-Teachers Association meeting will take place at the school assembly hall on July 10th at 10:00 AM. Key discussions about school bus expansion plans and security will be finalized.",
                    targetAudience = "PARENTS"
                ),
                Announcement(
                    title = "Inter-House Sports Festival 2026",
                    content = "Dear Students, training for the Blue, Yellow, Red, and Green houses will begin next Tuesday. Let us get active and stay healthy!",
                    targetAudience = "STUDENTS"
                )
            )
            for (ann in announcements) {
                dao.insertAnnouncement(ann)
            }

            // 6. Seed Pending Admissions Applied
            val pendingAdmissions = listOf(
                AdmissionApplied(
                    fullName = "Adebayo Williams",
                    dateOfBirth = "2021-08-14",
                    gender = "Male",
                    gradeApplied = "Primary 1",
                    parentName = "Solomon Williams",
                    parentPhone = "08011223344",
                    parentEmail = "solomon_w@example.com",
                    previousSchool = "Avenue Montessori Pre-school",
                    status = "PENDING"
                ),
                AdmissionApplied(
                    fullName = "Kanyinsola Alao",
                    dateOfBirth = "2022-03-12",
                    gender = "Female",
                    gradeApplied = "Playgroup",
                    parentName = "Dr. Gbenga Alao",
                    parentPhone = "08033005511",
                    parentEmail = "g_alao@hospital.com",
                    previousSchool = "None (Home care)",
                    status = "PENDING"
                )
            )
            for (pa in pendingAdmissions) {
                dao.insertAdmission(pa)
            }
        }
    }
}

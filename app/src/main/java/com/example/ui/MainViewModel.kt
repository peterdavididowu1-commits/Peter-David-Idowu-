package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SchoolRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SchoolRepository(database.schoolDao())
        
        // Seed database with default admin & pupils on startup
        viewModelScope.launch {
            repository.seedDatabaseIfNeeded()
        }
    }

    // Streams of Database content
    val admissionsList: StateFlow<List<AdmissionApplied>> = repository.allAdmissions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studentsList: StateFlow<List<Student>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val classesList: StateFlow<List<ClassEntity>> = repository.allClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val announcementsList: StateFlow<List<Announcement>> = repository.allAnnouncements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val resultsList: StateFlow<List<TermResult>> = repository.allResults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Session State
    private val _currentLoggedInUser = MutableStateFlow<User?>(null)
    val currentLoggedInUser: StateFlow<User?> = _currentLoggedInUser.asStateFlow()

    // For Student Portal
    private val _currentStudentProfile = MutableStateFlow<Student?>(null)
    val currentStudentProfile: StateFlow<Student?> = _currentStudentProfile.asStateFlow()

    // For Parent Portal
    private val _parentChildrenList = MutableStateFlow<List<Student>>(emptyList())
    val parentChildrenList: StateFlow<List<Student>> = _parentChildrenList.asStateFlow()

    private val _selectedChild = MutableStateFlow<Student?>(null)
    val selectedChild: StateFlow<Student?> = _selectedChild.asStateFlow()

    // Login/Auth Logic
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    fun clearAuthError() {
        _authError.value = null
    }

    fun login(username: String, pin: String, onCompleted: (role: String) -> Unit) {
        viewModelScope.launch {
            _authError.value = null
            val user = repository.getUserByUsername(username.trim())
            if (user != null && user.password == pin.trim()) {
                _currentLoggedInUser.value = user
                
                // Route according to roles
                when (user.role) {
                    "STUDENT" -> {
                        val student = user.associatedId?.let { repository.getStudentByIdDirect(it) }
                            ?: repository.getStudentByAdmissionNo(username.trim())
                        _currentStudentProfile.value = student
                    }
                    "PARENT" -> {
                        // Parents use phone number to log in, and can see multiple children
                        val children = repository.getStudentsByParentPhone(username.trim())
                        _parentChildrenList.value = children
                        if (children.isNotEmpty()) {
                            _selectedChild.value = children.first()
                        }
                    }
                }
                onCompleted(user.role)
            } else {
                _authError.value = "Invalid username/ID or password pin. Default student: HG/2026/001 (pass: student123), Parent: 08055551234 (pass: parent123), Admin: admin (pass: admin123)."
            }
        }
    }

    fun logout() {
        _currentLoggedInUser.value = null
        _currentStudentProfile.value = null
        _parentChildrenList.value = emptyList()
        _selectedChild.value = null
        _authError.value = null
    }

    fun selectActiveChild(student: Student) {
        _selectedChild.value = student
    }

    // Smart Admissions
    fun submitOnlineAdmission(
        fullName: String,
        dob: String,
        gender: String,
        gradeApplied: String,
        parentName: String,
        parentPhone: String,
        parentEmail: String,
        previousSchool: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val admission = AdmissionApplied(
                fullName = fullName,
                dateOfBirth = dob,
                gender = gender,
                gradeApplied = gradeApplied,
                parentName = parentName,
                parentPhone = parentPhone,
                parentEmail = parentEmail,
                previousSchool = previousSchool
            )
            repository.submitAdmission(admission)
            onSuccess()
        }
    }

    fun approveAdmission(admissionId: Int, targetClass: String) {
        viewModelScope.launch {
            repository.approveAdmission(admissionId, targetClass)
        }
    }

    fun rejectAdmission(admissionId: Int) {
        viewModelScope.launch {
            repository.rejectAdmission(admissionId)
        }
    }

    // Student administration
    fun registerStudentManually(student: Student) {
        viewModelScope.launch {
            repository.registerStudentDirectly(student)
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            repository.updateStudent(student)
            // If the updated student is the active logged-in student, refresh
            if (_currentStudentProfile.value?.id == student.id) {
                _currentStudentProfile.value = student
            }
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
        }
    }

    // Classes administration
    fun createClass(className: String, teacherName: String, roomNumber: String) {
        viewModelScope.launch {
            val classEntity = ClassEntity(
                className = className,
                teacherName = teacherName,
                roomNumber = roomNumber
            )
            repository.addClass(classEntity)
        }
    }

    fun removeClass(id: Int) {
        viewModelScope.launch {
            repository.deleteClass(id)
        }
    }

    // Results management
    fun addTermResult(
        studentId: Int,
        term: String,
        session: String,
        subject: String,
        testScore: Int,
        examScore: Int,
        remarks: String
    ) {
        viewModelScope.launch {
            val totalScore = testScore + examScore
            val grade = when {
                totalScore >= 80 -> "A"
                totalScore >= 70 -> "B"
                totalScore >= 60 -> "C"
                totalScore >= 50 -> "D"
                totalScore >= 40 -> "E"
                else -> "F"
            }
            val result = TermResult(
                studentId = studentId,
                term = term,
                session = session,
                subject = subject,
                testScore = testScore,
                examScore = examScore,
                totalScore = totalScore,
                grade = grade,
                remarks = remarks
            )
            repository.saveResult(result)
        }
    }

    fun deleteTermResult(id: Int) {
        viewModelScope.launch {
            repository.deleteResult(id)
        }
    }

    // Announcement functions
    fun publishAnnouncement(title: String, content: String, audience: String, author: String) {
        viewModelScope.launch {
            val announcement = Announcement(
                title = title,
                content = content,
                targetAudience = audience,
                author = author
            )
            repository.addAnnouncement(announcement)
        }
    }

    fun deleteAnnouncement(id: Int) {
        viewModelScope.launch {
            repository.deleteAnnouncement(id)
        }
    }
}

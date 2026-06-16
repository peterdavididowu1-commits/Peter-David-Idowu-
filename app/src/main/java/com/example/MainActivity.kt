package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.screens.*
import com.example.ui.theme.HisGraceTheme

sealed interface Screen {
    object Welcome : Screen
    object AdmissionForm : Screen
    object Login : Screen
    object StudentDashboard : Screen
    object ParentDashboard : Screen
    object AdminDashboard : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HisGraceTheme {
                // Instantiate central state manager JVM viewModel
                val viewModel: MainViewModel = viewModel()

                // State collections
                val announcements by viewModel.announcementsList.collectAsStateWithLifecycle()
                val admissions by viewModel.admissionsList.collectAsStateWithLifecycle()
                val students by viewModel.studentsList.collectAsStateWithLifecycle()
                val classes by viewModel.classesList.collectAsStateWithLifecycle()
                val results by viewModel.resultsList.collectAsStateWithLifecycle()

                val loggedUser by viewModel.currentLoggedInUser.collectAsStateWithLifecycle()
                val studentProfile by viewModel.currentStudentProfile.collectAsStateWithLifecycle()
                val childrenList by viewModel.parentChildrenList.collectAsStateWithLifecycle()
                val selectedChild by viewModel.selectedChild.collectAsStateWithLifecycle()
                val authError by viewModel.authError.collectAsStateWithLifecycle()

                var currentScreen by remember { mutableStateOf<Screen>(Screen.Welcome) }

                // Synchronize screens on logout
                LaunchedEffect(loggedUser) {
                    if (loggedUser == null && currentScreen != Screen.Welcome && currentScreen != Screen.AdmissionForm) {
                        currentScreen = Screen.Welcome
                    }
                }

                // Layout coordinator matching routes
                when (currentScreen) {
                    is Screen.Welcome -> {
                        WelcomeScreen(
                            announcements = announcements,
                            onNavigateToLogin = {
                                viewModel.clearAuthError()
                                currentScreen = Screen.Login
                            },
                            onNavigateToAdmission = { currentScreen = Screen.AdmissionForm },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    is Screen.AdmissionForm -> {
                        AdmissionScreen(
                            onSubmit = { name, dob, gender, grade, parentName, parentPhone, parentEmail, prevSchool ->
                                viewModel.submitOnlineAdmission(
                                    fullName = name,
                                    dob = dob,
                                    gender = gender,
                                    gradeApplied = grade,
                                    parentName = parentName,
                                    parentPhone = parentPhone,
                                    parentEmail = parentEmail,
                                    previousSchool = prevSchool,
                                    onSuccess = {}
                                )
                            },
                            onNavigateBack = { currentScreen = Screen.Welcome },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    is Screen.Login -> {
                        LoginScreen(
                            authError = authError,
                            onLoginClick = { username, password ->
                                viewModel.login(username, password) { role ->
                                    currentScreen = when (role) {
                                        "STUDENT" -> Screen.StudentDashboard
                                        "PARENT" -> Screen.ParentDashboard
                                        else -> Screen.AdminDashboard
                                    }
                                }
                            },
                            onNavigateBack = { currentScreen = Screen.Welcome },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    is Screen.StudentDashboard -> {
                        StudentPortal(
                            student = studentProfile,
                            allResults = results,
                            announcements = announcements,
                            onLogout = {
                                viewModel.logout()
                                currentScreen = Screen.Welcome
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    is Screen.ParentDashboard -> {
                        ParentPortal(
                            childrenList = childrenList,
                            selectedChild = selectedChild,
                            allResults = results,
                            announcements = announcements,
                            onSelectChild = { viewModel.selectActiveChild(it) },
                            onLogout = {
                                viewModel.logout()
                                currentScreen = Screen.Welcome
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    is Screen.AdminDashboard -> {
                        AdminPortal(
                            admissions = admissions,
                            students = students,
                            classes = classes,
                            results = results,
                            announcements = announcements,
                            onApproveAdmission = { id, targetClass ->
                                viewModel.approveAdmission(id, targetClass)
                            },
                            onRejectAdmission = { id ->
                                viewModel.rejectAdmission(id)
                            },
                            onAddStudent = { viewModel.registerStudentManually(it) },
                            onDeleteStudent = { viewModel.deleteStudent(it) },
                            onCreateClass = { clName, teacher, room ->
                                viewModel.createClass(clName, teacher, room)
                            },
                            onRemoveClass = { id ->
                                viewModel.removeClass(id)
                            },
                            onPublishResult = { studentId, term, sess, subject, test, exam, rem ->
                                viewModel.addTermResult(studentId, term, sess, subject, test, exam, rem)
                            },
                            onDeleteResult = { id ->
                                viewModel.deleteTermResult(id)
                            },
                            onPublishAnnouncement = { title, content, audience, author ->
                                viewModel.publishAnnouncement(title, content, audience, author)
                            },
                            onDeleteAnnouncement = { id ->
                                viewModel.deleteAnnouncement(id)
                            },
                            onLogout = {
                                viewModel.logout()
                                currentScreen = Screen.Welcome
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

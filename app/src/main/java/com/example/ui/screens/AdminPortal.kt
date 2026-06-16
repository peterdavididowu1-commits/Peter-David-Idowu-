package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPortal(
    admissions: List<AdmissionApplied>,
    students: List<Student>,
    classes: List<ClassEntity>,
    results: List<TermResult>,
    announcements: List<Announcement>,
    onApproveAdmission: (id: Int, targetClass: String) -> Unit,
    onRejectAdmission: (id: Int) -> Unit,
    onAddStudent: (Student) -> Unit,
    onDeleteStudent: (Student) -> Unit,
    onCreateClass: (className: String, teacherName: String, roomNumber: String) -> Unit,
    onRemoveClass: (id: Int) -> Unit,
    onPublishResult: (studentId: Int, term: String, session: String, subject: String, test: Int, exam: Int, remarks: String) -> Unit,
    onDeleteResult: (id: Int) -> Unit,
    onPublishAnnouncement: (title: String, content: String, audience: String, author: String) -> Unit,
    onDeleteAnnouncement: (id: Int) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf("Admissions") } // Admissions, Students, Results, Classes, Announcements
    val tabs = listOf("Admissions", "Students", "Results", "Classes", "Announcements")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("School Admin Desk", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary) },
                actions = {
                    IconButton(onClick = onLogout, modifier = Modifier.testTag("admin_logout_button")) {
                        Icon(Icons.Default.Logout, contentDescription = "Log Out", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Analytical Metrics Top Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Metric 1: Pupils
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Active Pupils", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${students.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                // Metric 2: Pending Admissions
                val pendingCount = admissions.count { it.status == "PENDING" }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (pendingCount > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Admissions Applied", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "$pendingCount",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (pendingCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Metric 3: Group Classes
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Sections", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${classes.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Tab Navigation Selection Bar
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOf(activeTab),
                modifier = Modifier.fillMaxWidth().testTag("admin_panel_tab_row"),
                edgePadding = 16.dp
            ) {
                tabs.forEach { tab ->
                    Tab(
                        selected = activeTab == tab,
                        onClick = { activeTab = tab },
                        text = { Text(tab, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    )
                }
            }

            // Content Area depending on choice
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                when (activeTab) {
                    "Admissions" -> AdminAdmissionsTab(
                        admissions = admissions,
                        classes = classes,
                        onApprove = onApproveAdmission,
                        onReject = onRejectAdmission
                    )
                    "Students" -> AdminStudentsTab(
                        students = students,
                        classes = classes,
                        onRegister = onAddStudent,
                        onDelete = onDeleteStudent
                    )
                    "Results" -> AdminResultsTab(
                        students = students,
                        results = results,
                        onPublish = onPublishResult,
                        onDelete = onDeleteResult
                    )
                    "Classes" -> AdminClassesTab(
                        classes = classes,
                        onCreate = onCreateClass,
                        onRemove = onRemoveClass
                    )
                    "Announcements" -> AdminAnnouncementsTab(
                        announcements = announcements,
                        onPublish = onPublishAnnouncement,
                        onDelete = onDeleteAnnouncement
                    )
                }
            }
        }
    }
}

// ---------------- SUB-COMPONENTS FOR EACH TAB ----------------

@Composable
fun AdminAdmissionsTab(
    admissions: List<AdmissionApplied>,
    classes: List<ClassEntity>,
    onApprove: (id: Int, targetClass: String) -> Unit,
    onReject: (id: Int) -> Unit
) {
    val pendingAdmissionsList = remember(admissions) {
        admissions.filter { it.status == "PENDING" }
    }

    if (pendingAdmissionsList.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(56.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Admissions Clearance Clear!",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "All submitted online applications have been screened.",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                Text(
                    text = "Pending Admissions Applications (${pendingAdmissionsList.size})",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(pendingAdmissionsList) { application ->
                var selectedAppClass by remember { mutableStateOf(classes.firstOrNull()?.className ?: "Primary 1") }
                var showClassMenu by remember { mutableStateOf(false) }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("admission_item_${application.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(application.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            SuggestionChip(onClick = {}, label = { Text(application.gradeApplied) })
                        }
                        
                        Divider()

                        Text("• Date of Birth: ${application.dateOfBirth} | Gender: ${application.gender}", fontSize = 12.sp)
                        Text("• Previous Nursery: ${application.previousSchool}", fontSize = 12.sp)
                        Text("• Guardian: ${application.parentName} (${application.parentPhone})", fontSize = 12.sp)
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Placement Class Classify:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        
                        // Select local class
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { showClassMenu = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Route to: $selectedAppClass")
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = showClassMenu,
                                onDismissRequest = { showClassMenu = false },
                                modifier = Modifier.fillMaxWidth(0.81f)
                            ) {
                                if (classes.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("No custom classes. Route to default Primary 1") },
                                        onClick = {
                                            selectedAppClass = "Primary 1"
                                            showClassMenu = false
                                        }
                                    )
                                } else {
                                    classes.forEach { cl ->
                                        DropdownMenuItem(
                                            text = { Text(cl.className) },
                                            onClick = {
                                                selectedAppClass = cl.className
                                                showClassMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Decision Actions row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onReject(application.id) },
                                modifier = Modifier.weight(1f).testTag("reject_button_${application.id}"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reject", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { onApprove(application.id, selectedAppClass) },
                                modifier = Modifier.weight(1f).testTag("approve_button_${application.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Approve & Enroll", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStudentsTab(
    students: List<Student>,
    classes: List<ClassEntity>,
    onRegister: (Student) -> Unit,
    onDelete: (Student) -> Unit
) {
    var registerExpanded by remember { mutableStateOf(false) }

    // Form fields
    var name by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("2020-01-01") }
    var gender by remember { mutableStateOf("Male") }
    var className by remember { mutableStateOf("Nursery 1") }
    var parentName by remember { mutableStateOf("") }
    var parentPhone by remember { mutableStateOf("") }
    var parentEmail by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    var classMenuExpanded by remember { mutableStateOf(false) }
    var genderMenuExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Toggle Manual Registration trigger
        item {
            ElevatedCard(
                onClick = { registerExpanded = !registerExpanded },
                modifier = Modifier.fillMaxWidth().testTag("toggle_register_form_button")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Enlist/Register pupil manually", fontWeight = FontWeight.Bold)
                    }
                    Icon(
                        imageVector = if (registerExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
            }
        }

        // Expanded Registration Form fields
        item {
            AnimatedVisibility(visible = registerExpanded) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Add New Student Record", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth().testTag("add_student_name_field"),
                            singleLine = true
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = dob,
                                onValueChange = { dob = it },
                                label = { Text("Date of Birth") },
                                placeholder = { Text("YYYY-MM-DD") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            // Gender Dropdown
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = gender,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Gender") },
                                    trailingIcon = { IconButton(onClick = { genderMenuExpanded = true }) { Icon(Icons.Default.ArrowDropDown, contentDescription = null) } },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                DropdownMenu(expanded = genderMenuExpanded, onDismissRequest = { genderMenuExpanded = false }) {
                                    DropdownMenuItem(text = { Text("Male") }, onClick = { gender = "Male"; genderMenuExpanded = false })
                                    DropdownMenuItem(text = { Text("Female") }, onClick = { gender = "Female"; genderMenuExpanded = false })
                                }
                            }
                        }

                        // Class list menu dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = className,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Assign Class") },
                                trailingIcon = { IconButton(onClick = { classMenuExpanded = true }) { Icon(Icons.Default.ArrowDropDown, contentDescription = null) } },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(expanded = classMenuExpanded, onDismissRequest = { classMenuExpanded = false }) {
                                if (classes.isEmpty()) {
                                    DropdownMenuItem(text = { Text("Primary 1") }, onClick = { className = "Primary 1"; classMenuExpanded = false })
                                } else {
                                    classes.forEach { c ->
                                        DropdownMenuItem(text = { Text(c.className) }, onClick = { className = c.className; classMenuExpanded = false })
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = parentName,
                            onValueChange = { parentName = it },
                            label = { Text("Parent / Guardian Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = parentPhone,
                            onValueChange = { parentPhone = it },
                            label = { Text("Parent Phone Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = parentEmail,
                            onValueChange = { parentEmail = it },
                            label = { Text("Parent Email") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Home Residential Address") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                if (name.isNotBlank() && parentPhone.isNotBlank()) {
                                    val newAdNo = "HG/2026/${(100..999).random()}"
                                    val newStud = Student(
                                        admissionNo = newAdNo,
                                        fullName = name,
                                        dateOfBirth = dob,
                                        gender = gender,
                                        className = className,
                                        parentName = parentName,
                                        parentPhone = parentPhone,
                                        parentEmail = parentEmail,
                                        address = address
                                    )
                                    onRegister(newStud)
                                    // Reset fields
                                    name = ""
                                    parentName = ""
                                    parentPhone = ""
                                    parentEmail = ""
                                    address = ""
                                    registerExpanded = false
                                }
                            },
                            enabled = name.isNotBlank() && parentPhone.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().testTag("save_student_button")
                        ) {
                            Text("Save Student Register")
                        }
                    }
                }
            }
        }

        // Student List Items
        item {
            Text("Registered Pupils List (${students.size})", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall)
        }

        if (students.isEmpty()) {
            item {
                Text("No active student profiles. Use manually register or approve pending admissions to view rosters.", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(16.dp))
            }
        } else {
            items(students) { student ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("student_item_${student.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(student.fullName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("ID: ${student.admissionNo} | Class: ${student.className}", fontSize = 12.sp, color = Color.DarkGray)
                            Text("Parent: ${student.parentName} (${student.parentPhone})", fontSize = 11.sp, color = Color.Gray)
                        }
                        IconButton(onClick = { onDelete(student) }, modifier = Modifier.testTag("delete_student_button_${student.id}")) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete student", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminResultsTab(
    students: List<Student>,
    results: List<TermResult>,
    onPublish: (studentId: Int, term: String, session: String, subject: String, test: Int, exam: Int, remarks: String) -> Unit,
    onDelete: (id: Int) -> Unit
) {
    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    var selectedTerm by remember { mutableStateOf("First Term") }
    var session by remember { mutableStateOf("2025/2026") }
    var subject by remember { mutableStateOf("") }
    var testScoreStr by remember { mutableStateOf("") }
    var examScoreStr by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }

    var studentMenuExpanded by remember { mutableStateOf(false) }
    var termMenuExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Publish Academic Grade Scores", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    // Student Picker
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedStudent?.fullName ?: "Select Student",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Target Pupil") },
                            trailingIcon = { IconButton(onClick = { studentMenuExpanded = true }) { Icon(Icons.Default.ArrowDropDown, contentDescription = null) } },
                            modifier = Modifier.fillMaxWidth().clickable { studentMenuExpanded = true }.testTag("scores_student_dropdown")
                        )
                        DropdownMenu(expanded = studentMenuExpanded, onDismissRequest = { studentMenuExpanded = false }) {
                            if (students.isEmpty()) {
                                DropdownMenuItem(text = { Text("No student records") }, onClick = { studentMenuExpanded = false })
                            } else {
                                students.forEach { stu ->
                                    DropdownMenuItem(
                                        text = { Text("${stu.fullName} (${stu.className})") },
                                        onClick = { selectedStudent = stu; studentMenuExpanded = false }
                                    )
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Term picker
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = selectedTerm,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Term Period") },
                                trailingIcon = { IconButton(onClick = { termMenuExpanded = true }) { Icon(Icons.Default.ArrowDropDown, contentDescription = null) } },
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(expanded = termMenuExpanded, onDismissRequest = { termMenuExpanded = false }) {
                                DropdownMenuItem(text = { Text("First Term") }, onClick = { selectedTerm = "First Term"; termMenuExpanded = false })
                                DropdownMenuItem(text = { Text("Second Term") }, onClick = { selectedTerm = "Second Term"; termMenuExpanded = false })
                                DropdownMenuItem(text = { Text("Third Term") }, onClick = { selectedTerm = "Third Term"; termMenuExpanded = false })
                            }
                        }

                        OutlinedTextField(
                            value = session,
                            onValueChange = { session = it },
                            label = { Text("Session Year") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject (e.g. Mathematics, English)") },
                        modifier = Modifier.fillMaxWidth().testTag("scores_subject_field"),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = testScoreStr,
                            onValueChange = { testScoreStr = it },
                            label = { Text("Test Score (CA /30)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("scores_ca_field"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = examScoreStr,
                            onValueChange = { examScoreStr = it },
                            label = { Text("Exam Score (/70)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("scores_exam_field"),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("Teacher Remarks") },
                        placeholder = { Text("e.g. Excellent progress") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            val stId = selectedStudent?.id
                            val testInt = testScoreStr.toIntOrNull() ?: 0
                            val examInt = examScoreStr.toIntOrNull() ?: 0
                            if (stId != null && subject.isNotBlank()) {
                                onPublish(stId, selectedTerm, session, subject, testInt, examInt, remarks)
                                // Reset
                                subject = ""
                                testScoreStr = ""
                                examScoreStr = ""
                                remarks = ""
                            }
                        },
                        enabled = selectedStudent != null && subject.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().testTag("publish_scores_button")
                    ) {
                        Text("Publish Report Entrance Entry")
                    }
                }
            }
        }

        // Listed Results
        item {
            Text("Published Report Entries list", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall)
        }

        if (results.isEmpty()) {
            item {
                Text("No published report entries yet.", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(16.dp))
            }
        } else {
            items(results) { res ->
                val stuName = remember(students, res.studentId) {
                    students.find { it.id == res.studentId }?.fullName ?: "Unknown Student"
                }
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("result_item_${res.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("$stuName - ${res.subject}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Term: ${res.term} | Test: ${res.testScore} | Exam: ${res.examScore} | Total: ${res.totalScore} (${res.grade})", fontSize = 12.sp, color = Color.DarkGray)
                            if (res.remarks.isNotBlank()) {
                                Text("Remarks: \"${res.remarks}\"", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        IconButton(onClick = { onDelete(res.id) }, modifier = Modifier.testTag("delete_result_button_${res.id}")) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete score", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminClassesTab(
    classes: List<ClassEntity>,
    onCreate: (String, String, String) -> Unit,
    onRemove: (Int) -> Unit
) {
    var className by remember { mutableStateOf("") }
    var teacherName by remember { mutableStateOf("") }
    var roomNumber by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Add Dynamic Class Section", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = className,
                        onValueChange = { className = it },
                        label = { Text("Class Name (e.g. Primary 1, Nursery 2)") },
                        modifier = Modifier.fillMaxWidth().testTag("class_name_field"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = teacherName,
                        onValueChange = { teacherName = it },
                        label = { Text("Assigned Teacher") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = roomNumber,
                        onValueChange = { roomNumber = it },
                        label = { Text("Room / Location Designation") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (className.isNotBlank() && teacherName.isNotBlank()) {
                                onCreate(className, teacherName, roomNumber)
                                className = ""
                                teacherName = ""
                                roomNumber = ""
                            }
                        },
                        enabled = className.isNotBlank() && teacherName.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().testTag("save_class_button")
                    ) {
                        Text("Create Class Section")
                    }
                }
            }
        }

        item {
            Text("Active Sections", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall)
        }

        if (classes.isEmpty()) {
            item {
                Text("No Class Sections created yet.", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(16.dp))
            }
        } else {
            items(classes) { cl ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(cl.className, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Teacher: ${cl.teacherName} | Location: ${cl.roomNumber}", fontSize = 12.sp, color = Color.DarkGray)
                        }
                        IconButton(onClick = { onRemove(cl.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Class", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAnnouncementsTab(
    announcements: List<Announcement>,
    onPublish: (String, String, String, String) -> Unit,
    onDelete: (Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var targetAudience by remember { mutableStateOf("ALL") }

    var targetMenuExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Publish Notice bulletin", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Bulletin Brief Title") },
                        modifier = Modifier.fillMaxWidth().testTag("announcement_title_field"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Bulletin Body Content") },
                        modifier = Modifier.fillMaxWidth().testTag("announcement_content_field"),
                        minLines = 3
                    )

                    // Target Selector Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = targetAudience,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Target Audience") },
                            trailingIcon = { IconButton(onClick = { targetMenuExpanded = true }) { Icon(Icons.Default.ArrowDropDown, contentDescription = null) } },
                            modifier = Modifier.fillMaxWidth().clickable { targetMenuExpanded = true }
                        )
                        DropdownMenu(expanded = targetMenuExpanded, onDismissRequest = { targetMenuExpanded = false }) {
                            DropdownMenuItem(text = { Text("ALL") }, onClick = { targetAudience = "ALL"; targetMenuExpanded = false })
                            DropdownMenuItem(text = { Text("STUDENTS") }, onClick = { targetAudience = "STUDENTS"; targetMenuExpanded = false })
                            DropdownMenuItem(text = { Text("PARENTS") }, onClick = { targetAudience = "PARENTS"; targetMenuExpanded = false })
                        }
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank() && content.isNotBlank()) {
                                onPublish(title, content, targetAudience, "School Administration")
                                title = ""
                                content = ""
                            }
                        },
                        enabled = title.isNotBlank() && content.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().testTag("publish_announcement_button")
                    ) {
                        Text("Broadcast Announcement")
                    }
                }
            }
        }

        item {
            Text("Broadcast Notices", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall)
        }

        if (announcements.isEmpty()) {
            item {
                Text("No active bulletins.", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(16.dp))
            }
        } else {
            items(announcements) { notice ->
                val formattedDate = remember(notice.publishedDate) {
                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    sdf.format(Date(notice.publishedDate))
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(notice.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Audience: ${notice.targetAudience} | Date: $formattedDate", fontSize = 11.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(notice.content, fontSize = 12.sp, color = Color.DarkGray)
                        }
                        IconButton(onClick = { onDelete(notice.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Announcement", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

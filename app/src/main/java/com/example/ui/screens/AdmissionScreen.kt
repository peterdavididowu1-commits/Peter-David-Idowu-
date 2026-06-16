package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdmissionScreen(
    onSubmit: (
        fullName: String,
        dob: String,
        gender: String,
        gradeApplied: String,
        parentName: String,
        parentPhone: String,
        parentEmail: String,
        previousSchool: String
    ) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Form States
    var fullName by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var gradeApplied by remember { mutableStateOf("Nursery 1") }
    var parentName by remember { mutableStateOf("") }
    var parentPhone by remember { mutableStateOf("") }
    var parentEmail by remember { mutableStateOf("") }
    var previousSchool by remember { mutableStateOf("") }

    // Dropdowns
    var showGenderMenu by remember { mutableStateOf(false) }
    var showGradeMenu by remember { mutableStateOf(false) }

    // Validation & Success States
    var submittedSuccessfully by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf("") }

    val gradeLevels = listOf(
        "Playgroup",
        "Nursery 1",
        "Nursery 2",
        "Primary 1",
        "Primary 2",
        "Primary 3",
        "Primary 4",
        "Primary 5",
        "Primary 6"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admission Application", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (submittedSuccessfully) {
            // Success view
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(48.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Application Submitted!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Thank you for applying to His Grace Nursery & Primary School. Your admission database profile is registered as 'PENDING'.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Next Steps:",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• The administrative office will review your application.\n" +
                                   "• Once approved, a customized Student Admission ID will be generated.\n" +
                                   "• Your student and parent login portals will be unlocked automatically.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Return to Homepage")
                }
            }
        } else {
            // Standard form view
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Admission Application Form",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Please provide accurate pupil details and parent contact coordinates below.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                AnimatedVisibility(visible = validationError.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = validationError,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Pupil Info Section
                Text("1. Pupil's Personal Information", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Pupil's Full Name") },
                    placeholder = { Text("e.g. John Doe Okafor") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("admission_fullname_field"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = dob,
                        onValueChange = { dob = it },
                        label = { Text("Date of Birth") },
                        placeholder = { Text("YYYY-MM-DD") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        modifier = Modifier.weight(1f).testTag("admission_dob_field"),
                        singleLine = true
                    )

                    // Gender dropdown representation
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Gender") },
                            trailingIcon = {
                                IconButton(onClick = { showGenderMenu = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().clickable { showGenderMenu = true }
                        )
                        DropdownMenu(
                            expanded = showGenderMenu,
                            onDismissRequest = { showGenderMenu = false },
                            modifier = Modifier.fillMaxWidth(0.45f)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Male") },
                                onClick = {
                                    gender = "Male"
                                    showGenderMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Female") },
                                onClick = {
                                    gender = "Female"
                                    showGenderMenu = false
                                }
                            )
                        }
                    }
                }

                // Grade selection dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = gradeApplied,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Grade/Class Applied For") },
                        leadingIcon = { Icon(Icons.Default.Grade, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showGradeMenu = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { showGradeMenu = true }
                    )
                    DropdownMenu(
                        expanded = showGradeMenu,
                        onDismissRequest = { showGradeMenu = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        gradeLevels.forEach { level ->
                            DropdownMenuItem(
                                text = { Text(level) },
                                onClick = {
                                    gradeApplied = level
                                    showGradeMenu = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = previousSchool,
                    onValueChange = { previousSchool = it },
                    label = { Text("Previous School Attended") },
                    placeholder = { Text("e.g. Hope Academy Nursery, Lagos") },
                    leadingIcon = { Icon(Icons.Default.HistoryEdu, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Parent/Guardian Info Section
                Text("2. Parent / Guardian Contact Details", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)

                OutlinedTextField(
                    value = parentName,
                    onValueChange = { parentName = it },
                    label = { Text("Parent/Guardian Full Name") },
                    placeholder = { Text("e.g. Chief Edwin Okafor") },
                    leadingIcon = { Icon(Icons.Default.SupervisorAccount, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = parentPhone,
                    onValueChange = { parentPhone = it },
                    label = { Text("Parent phone number") },
                    placeholder = { Text("e.g. 08055551234") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("admission_phone_field"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = parentEmail,
                    onValueChange = { parentEmail = it },
                    label = { Text("Parent email address") },
                    placeholder = { Text("e.g. parent@example.com") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (fullName.isBlank() || parentName.isBlank() || parentPhone.isBlank()) {
                            validationError = "Please fill out the Pupil name, Parent Name, and Parent Phone Number."
                        } else if (!dob.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                            validationError = "Please format Date of Birth correctly: YYYY-MM-DD (e.g., 2021-05-18)."
                        } else {
                            validationError = ""
                            onSubmit(
                                fullName,
                                dob,
                                gender,
                                gradeApplied,
                                parentName,
                                parentPhone,
                                parentEmail,
                                previousSchool
                            )
                            submittedSuccessfully = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_admission_form"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Submit Application Profile", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

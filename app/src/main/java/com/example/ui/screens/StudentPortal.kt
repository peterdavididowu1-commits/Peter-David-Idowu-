package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Announcement
import com.example.data.Student
import com.example.data.TermResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentPortal(
    student: Student?,
    allResults: List<TermResult>,
    announcements: List<Announcement>,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTerm by remember { mutableStateOf("First Term") }
    var showTermSelector by remember { mutableStateOf(false) }

    val terms = listOf("First Term", "Second Term", "Third Term")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Portal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary) },
                actions = {
                    IconButton(onClick = onLogout, modifier = Modifier.testTag("student_logout_button")) {
                        Icon(Icons.Default.Logout, contentDescription = "Log Out", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (student == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Filter results for active student and selected term
            val studentResultsFiltered = remember(allResults, student, selectedTerm) {
                allResults.filter { it.studentId == student.id && it.term == selectedTerm }
            }

            // Filter notices for students or all
            val studentNotices = remember(announcements) {
                announcements.filter { it.targetAudience == "STUDENTS" || it.targetAudience == "ALL" }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Welcoming Header
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Face, contentDescription = null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Welcome back,",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = student.fullName,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("ADMISSION NO", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(student.admissionNo, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("ASSIGNED CLASS", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(student.className, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                    }
                }

                // Profile card details toggle
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Bio Details", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text("Gender:", color = Color.Gray, fontSize = 13.sp)
                                Text(student.gender, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text("Date of Birth:", color = Color.Gray, fontSize = 13.sp)
                                Text(student.dateOfBirth, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text("Parent Name:", color = Color.Gray, fontSize = 13.sp)
                                Text(student.parentName, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Report Card Header and Term Selector
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Academic Report Card",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Box {
                            Button(
                                onClick = { showTermSelector = true },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(36.dp).testTag("select_term_dropdown_button")
                            ) {
                                Text(selectedTerm, fontSize = 12.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            DropdownMenu(
                                expanded = showTermSelector,
                                onDismissRequest = { showTermSelector = false }
                            ) {
                                terms.forEach { term ->
                                    DropdownMenuItem(
                                        text = { Text(term) },
                                        onClick = {
                                            selectedTerm = term
                                            showTermSelector = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                if (studentResultsFiltered.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No report card results have been uploaded yet by the administration for $selectedTerm.",
                                    fontSize = 13.sp,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    // Score list sheet
                    items(studentResultsFiltered) { result ->
                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = result.subject,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                color = when (result.grade) {
                                                    "A" -> Color(0xFFE6F4EA)
                                                    "B" -> Color(0xFFE8F0FE)
                                                    "C" -> Color(0xFFFEF7E0)
                                                    else -> Color(0xFFFCE8E6)
                                                },
                                                shape = RoundedCornerShape(18.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = result.grade,
                                            fontWeight = FontWeight.Bold,
                                            color = when (result.grade) {
                                                "A" -> Color(0xFF137333)
                                                "B" -> Color(0xFF1A73E8)
                                                "C" -> Color(0xFFB06000)
                                                else -> Color(0xFFC5221F)
                                            },
                                            fontSize = 15.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row {
                                        Text("Continuos Assessment (CA): ", fontSize = 12.sp, color = Color.Gray)
                                        Text("${result.testScore}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row {
                                        Text("Exam: ", fontSize = 12.sp, color = Color.Gray)
                                        Text("${result.examScore}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row {
                                        Text("Total: ", fontSize = 12.sp, color = Color.Gray)
                                        Text("${result.totalScore}/100", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (result.remarks.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Remarks: \"${result.remarks}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.DarkGray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Report metric aggregation card
                    item {
                        val totalSum = studentResultsFiltered.sumOf { it.totalScore }
                        val averageSum = totalSum.toFloat() / studentResultsFiltered.size

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Term Summary Metrics", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Text("Registered Subject count:", fontSize = 13.sp)
                                    Text("${studentResultsFiltered.size}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Text("Cumulative Score:", fontSize = 13.sp)
                                    Text("$totalSum", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Text("Average score performance:", fontSize = 13.sp)
                                    Text(String.format(java.util.Locale.US, "%.2f%%", averageSum), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                    }
                }

                // Student Notices section
                item {
                    Text(
                        text = "Announcements for You",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (studentNotices.isEmpty()) {
                    item {
                        Text(
                            text = "No notices specific to students.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    items(studentNotices) { notice ->
                        AnnouncementCard(announcement = notice)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

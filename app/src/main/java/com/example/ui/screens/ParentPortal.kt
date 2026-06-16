package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
fun ParentPortal(
    childrenList: List<Student>,
    selectedChild: Student?,
    allResults: List<TermResult>,
    announcements: List<Announcement>,
    onSelectChild: (Student) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTerm by remember { mutableStateOf("First Term") }
    var showTermSelector by remember { mutableStateOf(false) }
    var showChildSelector by remember { mutableStateOf(false) }

    val terms = listOf("First Term", "Second Term", "Third Term")

    // Fee state simulation
    var tuitionPaid by remember { mutableStateOf(true) }
    var busServicePaid by remember { mutableStateOf(false) }
    var booksFeePaid by remember { mutableStateOf(true) }
    var sportsFestivalPaid by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parent Portal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary) },
                actions = {
                    IconButton(onClick = onLogout, modifier = Modifier.testTag("parent_logout_button")) {
                        Icon(Icons.Default.Logout, contentDescription = "Log Out", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (childrenList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Student Profiles Linked",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your parent account is active, but no student records in the database match your phone number. Verify that the school administrator registers your child with this matching phone number.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            val child = selectedChild ?: childrenList.first()

            val studentResultsFiltered = remember(allResults, child, selectedTerm) {
                allResults.filter { it.studentId == child.id && it.term == selectedTerm }
            }

            val parentNotices = remember(announcements) {
                announcements.filter { it.targetAudience == "PARENTS" || it.targetAudience == "ALL" }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Parent welcome tile & Child Selection dropdown
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FamilyRestroom,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Parent Guardian Portal",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            text = child.parentName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Interactive Child Switcher
                            Text("Active Ward/Child:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showChildSelector = true },
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = child.fullName,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = "Class: ${child.className} | ID: ${child.admissionNo}",
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                }
                                
                                DropdownMenu(
                                    expanded = showChildSelector,
                                    onDismissRequest = { showChildSelector = false },
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    childrenList.forEach { student ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(student.fullName, fontWeight = FontWeight.Bold)
                                                    Text("Class: ${student.className} | Admission ID: ${student.admissionNo}", fontSize = 11.sp, color = Color.Gray)
                                                }
                                            },
                                            onClick = {
                                                onSelectChild(student)
                                                showChildSelector = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Subsections tabs choice
                item {
                    Text(
                        text = "Outstanding Financial Billings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Billing simulations
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Bill 1: Tuition
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Term School Tuition Fees", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text("Amount: ₦75,000", fontSize = 12.sp, color = Color.Gray)
                                }
                                if (tuitionPaid) {
                                    SuggestionChip(onClick = {}, label = { Text("PAID", color = Color(0xFF137333)) })
                                } else {
                                    Button(
                                        onClick = { tuitionPaid = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Pay ₦75k", fontSize = 10.sp)
                                    }
                                }
                            }
                            
                            // Bill 2: Bus Route service
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Term School Bus Transport Route", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text("Amount: ₦25,000", fontSize = 12.sp, color = Color.Gray)
                                }
                                if (busServicePaid) {
                                    SuggestionChip(onClick = {}, label = { Text("PAID", color = Color(0xFF137333)) })
                                } else {
                                    Button(
                                        onClick = { busServicePaid = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Pay ₦25k", fontSize = 10.sp)
                                    }
                                }
                            }

                            // Bill 3: Textbooks and Learning Materials
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Essential Learning Books package", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text("Amount: ₦18,000", fontSize = 12.sp, color = Color.Gray)
                                }
                                if (booksFeePaid) {
                                    SuggestionChip(onClick = {}, label = { Text("PAID", color = Color(0xFF137333)) })
                                } else {
                                    Button(
                                        onClick = { booksFeePaid = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Pay ₦18k", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Child Report section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Stars, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Academic Report Board",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Box {
                            Button(
                                onClick = { showTermSelector = true },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(36.dp).testTag("parent_term_dropdown_button")
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
                                Icon(Icons.Default.Assessment, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No report card entries are published for ${child.fullName} for $selectedTerm.",
                                    fontSize = 13.sp,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
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
                                        Text("CA Score: ", fontSize = 12.sp, color = Color.Gray)
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
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Remarks: \"${result.remarks}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }

                    // Averages aggregation card
                    item {
                        val totalSum = studentResultsFiltered.sumOf { it.totalScore }
                        val averageSum = totalSum.toFloat() / studentResultsFiltered.size

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Term Summary Metrics - ${child.fullName}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
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

                // General Bulletins for Parents
                item {
                    Text(
                        text = "Announcements for Parents",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (parentNotices.isEmpty()) {
                    item {
                        Text(
                            text = "No notices specific to parents.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    items(parentNotices) { notice ->
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

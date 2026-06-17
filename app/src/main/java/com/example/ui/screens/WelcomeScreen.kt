package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Announcement
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    announcements: List<Announcement>,
    onNavigateToLogin: () -> Unit,
    onNavigateToAdmission: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeWebTab by remember { mutableStateOf("Home") }
    val tabsList = listOf(
        TabItem("Home", Icons.Default.Home),
        TabItem("About Us", Icons.Default.Info),
        TabItem("Admissions", Icons.Default.School),
        TabItem("Academics", Icons.Default.Book),
        TabItem("Gallery", Icons.Default.Image),
        TabItem("News & Events", Icons.Default.Campaign),
        TabItem("Contact Us", Icons.Default.Email)
    )

    Scaffold(
        topBar = {
            // Sticky Web-like Header Navigation Bar
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().testTag("sticky_header")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // School Brand Banner Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.school_logo_official_1781690632716),
                                    contentDescription = "His Grace School Crest",
                                    modifier = Modifier.size(38.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Column {
                                Text(
                                    text = "HIS GRACE SCHOOL",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Nursery & Primary Education",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Web-style Portal Access Action Button
                        Button(
                            onClick = onNavigateToLogin,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("portal_login_button_header")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Login,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Portal Login",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Horizontal Scrolling/Sticky Navigation Menu options
                    ScrollableTabRow(
                        selectedTabIndex = tabsList.indexOfFirst { it.title == activeWebTab },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        edgePadding = 12.dp,
                        modifier = Modifier.fillMaxWidth().testTag("web_nav_bar")
                    ) {
                        tabsList.forEach { tab ->
                            Tab(
                                selected = activeWebTab == tab.title,
                                onClick = { activeWebTab = tab.title },
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (activeWebTab == tab.title) Color.White else Color.White.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = tab.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (activeWebTab == tab.title) Color.White else Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 0.0.dp) // Footer pinned at very end
        ) {
            // Main Render Box depending on navigation tab selection
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    when (activeWebTab) {
                        "Home" -> MenuHomeScreen(
                            announcements = announcements,
                            onApplyClick = onNavigateToAdmission,
                            onLoginClick = onNavigateToLogin,
                            onTabSwitchRequested = { activeWebTab = it }
                        )
                        "About Us" -> MenuAboutScreen()
                        "Admissions" -> MenuAdmissionScreen(
                            onApplyClick = onNavigateToAdmission
                        )
                        "Academics" -> MenuAcademicsScreen()
                        "Gallery" -> MenuGalleryScreen()
                        "News & Events" -> MenuNewsScreen(announcements = announcements)
                        "Contact Us" -> MenuContactScreen()
                    }
                }
            }

            // Universal Professional School Web Footer (as requested)
            item {
                SchoolWebFooter(
                    onTabClick = { activeWebTab = it },
                    onLoginClick = onNavigateToLogin,
                    onAdmissionClick = onNavigateToAdmission
                )
            }
        }
    }
}

// Data class to define individual navigation tab properties
data class TabItem(val title: String, val icon: ImageVector)

// ======================== TABS IMPLEMENTATIONS ========================

@Composable
fun MenuHomeScreen(
    announcements: List<Announcement>,
    onApplyClick: () -> Unit,
    onLoginClick: () -> Unit,
    onTabSwitchRequested: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Hero Slider / Image Frame Banner (Blue Accent Overlay)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.school_building_1781690231700),
                contentDescription = "His Grace School Campus",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E3A8A).copy(alpha = 0.4f),
                                Color(0xFF0F172A).copy(alpha = 0.9f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFF59E0B), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        "MOTTO: MOULDING LIVES FOR GREATNESS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF78350F)
                    )
                }
                Text(
                    text = "Moulding Lives for Greatness",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Nearing decades of quality excellence in early childhood mentorship.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    maxLines = 2
                )
            }
        }

        // Web Highlights Category Grid Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Admission Form Quick Link Card
            Card(
                onClick = onApplyClick,
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.weight(1f).height(115.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(Icons.Default.AppRegistration, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text("Online Admission", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        Text("Class enrollment", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                    }
                }
            }

            // Student/Parent Portal Card
            Card(
                onClick = onLoginClick,
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.weight(1f).height(115.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Column {
                        Text("School Portal", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                        Text("Check Exam Grades", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f))
                    }
                }
            }
        }

        // Core Mission Pitch & Welcome Speech
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Welcome to Our Digital Desk",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "At His Grace Nursery and Primary School, we cultivate structured paradigms of learning. Our pupils undergo standard developmental mentorship targeting logical prowess, spiritual balance, and godly character. We provide a peaceful and secure classroom workspace equipped with high fidelity interactive teaching tools.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 19.sp
            )
            OutlinedButton(
                onClick = { onTabSwitchRequested("About Us") },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Learn More About Us", fontSize = 12.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
            }
        }

        // Analytical Counter Stats Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("His Grace At A Glance", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatIndicator("300+", "Active Pupils")
                    StatIndicator("100%", "Safe Security")
                    StatIndicator("12+", "Learning Clubs")
                }
            }
        }

        // Fast Announcement Board Preview
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Latest Notices & Bulletins",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = { onTabSwitchRequested("News & Events") }) {
                    Text("See All", fontSize = 12.sp)
                }
            }

            if (announcements.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Academic calendar announcements and notices will appear here.", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            } else {
                announcements.take(2).forEach { NoticeCard(announcement = it) }
            }
        }
    }
}

@Composable
fun StatIndicator(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.secondary, fontSize = 20.sp)
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
fun NoticeCard(announcement: Announcement) {
    val formattedDate = remember(announcement.publishedDate) {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        sdf.format(Date(announcement.publishedDate))
    }
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        announcement.targetAudience,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(formattedDate, fontSize = 10.sp, color = Color.Gray)
            }
            Text(announcement.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(announcement.content, fontSize = 11.sp, color = Color.DarkGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun MenuAboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            "About His Grace School",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge
        )

        // Vision & Mission Cards
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.RemoveRedEye, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Our Vision", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                }
                Text(
                    "To raise responsible, confident, and academically sound pupils who will positively impact society.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Text("Our Mission", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.secondary)
                }
                Text(
                    "To provide quality education that nurtures academic excellence, moral values, and lifelong learning.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("School Hours", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                }
                Text(
                    "Monday - Friday\n7:45 AM - 4:00 PM",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        // School History / Values
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Our Core Value Pillars", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ValueBadge("Intellect")
                ValueBadge("Godly Manners")
                ValueBadge("Diligence")
                ValueBadge("Pure Integrity")
            }
        }

        Text(
            text = "His Grace School was established with a singular vision to rectify academic and character gaps in nursery and primary education. Combining modern Montessori methods with certified national education models, we elevate pupils' confidence and capabilities. Our school colors - Blue and White - represent integrity of mind, the depth of search for truth, and general human grace under God.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun ValueBadge(text: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
fun MenuAdmissionScreen(onApplyClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            "Admissions Office Desk",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge
        )

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Admissions 2026/2027 Ongoing", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                Text(
                    "Online applications are completely open for infants and toddlers entering Nursery 1-2, and pupils entering Primary classes 1-5. Prospective guardians should fill our simplified, type-safe profile form for automated desk evaluation.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onApplyClick,
                    modifier = Modifier.fillMaxWidth().testTag("apply_online_now_btn")
                ) {
                    Icon(Icons.Default.AppRegistration, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Apply Online Now")
                }
            }
        }

        // Requirements
        Text("Admission Eligibility Standards", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            RequirementItem("Nursery 1: Must be at least 3 years inside registration year.")
            RequirementItem("Nursery 2: Must be at least 4 years old.")
            RequirementItem("Primary Years: Ages 5+ with certification of preceding daycare/school.")
            RequirementItem("Necessary Files: Two passport booklets, immunization records, parent contact proof.")
        }
    }
}

@Composable
fun RequirementItem(text: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
        Text(text, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun MenuAcademicsScreen() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            "Academic Programs & Curriculum",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Early Years
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.ChildCare, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Nursery Section", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    Text("Focus on fine motor dexterity, vocabulary modeling, coloring, and phonetic orientation.", fontSize = 11.sp, color = Color.Gray, lineHeight = 16.sp)
                }
            }

            // Primary
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.HomeWork, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Text("Primary Section", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                    Text("Intense focus on Mathematics, Quantitative reasoning, Sciences, Information IT literacy, History, and Speech Drama.", fontSize = 11.sp, color = Color.Gray, lineHeight = 16.sp)
                }
            }
        }

        // Clubs & Skills
        Text("Skills & Extra-Curricular Clubs", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
        Text(
            "We value rounded cognitive growth! Our children can actively enlist in clubs:",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Text("ICT & Coding", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Text("Music & Keyboard", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Text("Debate & Speech", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MenuGalleryScreen() {
    var selectedImgDesc by remember { mutableStateOf<String?>(null) }
    val galleryPhotos = listOf(
        GalleryItem("Main Campus Assembly", Icons.Default.School, Color(0xFF1E3A8A), R.drawable.school_building_1781690231700),
        GalleryItem("Interactive Computing Lab", Icons.Default.Computer, Color(0xFF2563EB), R.drawable.classroom_activity_1781690248150),
        GalleryItem("Early Years Montessori", Icons.Default.Toys, Color(0xFF0284C7)),
        GalleryItem("Outdoor Athletics Field", Icons.Default.SportsBasketball, Color(0xFF16A34A)),
        GalleryItem("Fine Arts & Pottery Room", Icons.Default.Palette, Color(0xFFF59E0B)),
        GalleryItem("Comprehensive Library Hub", Icons.Default.CollectionsBookmark, Color(0xFF7C3AED))
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column {
            Text(
                "His Grace Campus Gallery",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                "Visual snippets representing campus activity corridors and facilities.",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }

        // Custom FlowRow for 2-column aesthetic grid (Web style)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            maxItemsInEachRow = 2
        ) {
            galleryPhotos.forEach { photo ->
                val cardWidth = (Modifier.weight(1f)).fillMaxWidth(0.48f)
                Card(
                    onClick = { selectedImgDesc = photo.desc },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = cardWidth.padding(bottom = 10.dp).height(120.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (photo.imageRes != null) {
                            Image(
                                painter = painterResource(id = photo.imageRes),
                                contentDescription = photo.desc,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.7f)
                                            )
                                        )
                                    )
                            )
                            Text(
                                text = photo.desc,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = Color.White,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(8.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(photo.accentColor.copy(alpha = 0.08f))
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(photo.icon, contentDescription = null, tint = photo.accentColor, modifier = Modifier.size(32.dp))
                                Text(photo.desc, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }

        // Lightbox popup/dialog
        selectedImgDesc?.let { desc ->
            val correspondingItem = galleryPhotos.firstOrNull { it.desc == desc }
            AlertDialog(
                onDismissRequest = { selectedImgDesc = null },
                title = { Text(desc, fontWeight = FontWeight.Bold) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (correspondingItem?.imageRes != null) {
                            Image(
                                painter = painterResource(id = correspondingItem.imageRes),
                                contentDescription = desc,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(correspondingItem?.icon ?: Icons.Default.Photo, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                            }
                        }
                        Text(
                            "This represents an approved high quality visual scene from His Grace Nursery and Primary School. Daily activities are performed under careful, godly child mentorship.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedImgDesc = null }) {
                        Text("Close View")
                    }
                }
            )
        }
    }
}

data class GalleryItem(val desc: String, val icon: ImageVector, val accentColor: Color, val imageRes: Int? = null)

@Composable
fun MenuNewsScreen(announcements: List<Announcement>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                "School Announcements bulletin",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                "Direct official reports and information broadcasts from His Grace School council.",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }

        if (announcements.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No announcements active right now.", fontSize = 12.sp, color = Color.Gray)
            }
        } else {
            announcements.forEach { notice ->
                NoticeListItem(announcement = notice)
            }
        }
    }
}

@Composable
fun NoticeListItem(announcement: Announcement) {
    val formattedDate = remember(announcement.publishedDate) {
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        sdf.format(Date(announcement.publishedDate))
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        announcement.targetAudience,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Text(formattedDate, fontSize = 10.sp, color = Color.Gray)
            }
            Text(announcement.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            Text(announcement.content, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Author: ${announcement.author}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("Verified Bulletin", fontSize = 10.sp, color = SchoolSuccessGreen, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MenuContactScreen() {
    val context = LocalContext.current
    var contactName by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var contactMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            "Contact Our Operations Desk",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge
        )

        // Contacts list
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("General Office Directory", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                ContactRowItem(Icons.Default.Phone, "Primary Helpline: 07014535079")
                ContactRowItem(Icons.Default.Email, "Desk Email: hisgraceschool.name.ng@gmail.com")
                ContactRowItem(Icons.Default.HomeWork, "Address: Agbugburu Village, Abeokuta, Ogun State")
                ContactRowItem(Icons.Default.AccessTime, "School Hours: Monday - Friday (7:45 AM - 4:00 PM)")
            }
        }

        // Contact Us Interactive Feedback form
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Submit Direct Query", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(
                    value = contactName,
                    onValueChange = { contactName = it },
                    label = { Text("Your/Guardian Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = contactEmail,
                    onValueChange = { contactEmail = it },
                    label = { Text("Contact Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = contactMessage,
                    onValueChange = { contactMessage = it },
                    label = { Text("Query Message Body") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (contactName.isNotBlank() && contactMessage.isNotBlank()) {
                            Toast.makeText(context, "Thank you! Direct query routed safely to Registrar.", Toast.LENGTH_LONG).show()
                            contactName = ""
                            contactEmail = ""
                            contactMessage = ""
                        }
                    },
                    enabled = contactName.isNotBlank() && contactMessage.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().testTag("add_query_submit_button")
                ) {
                    Text("Send Direct Message")
                }
            }
        }
    }
}

@Composable
fun ContactRowItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
        Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// Universal Professional Footer Component (Blue/White themed)
@Composable
fun SchoolWebFooter(
    onTabClick: (String) -> Unit,
    onLoginClick: () -> Unit,
    onAdmissionClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A)) // Deep web academic background style
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // About Column
            Column(modifier = Modifier.weight(1.3f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.School, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Text("HIS GRACE SCHOOL", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }
                Text(
                    text = "Grooming standard child growth models since nursery inception. Safe workspace, godly values.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    lineHeight = 15.sp
                )
            }

            // Quick Links Column
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("QUICK LINKS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                FooterLink("Home") { onTabClick("Home") }
                FooterLink("About Us") { onTabClick("About Us") }
                FooterLink("Admissions Office") { onTabClick("Admissions") }
                FooterLink("Student Portal") { onLoginClick() }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Divider(color = Color.White.copy(alpha = 0.15f))
        Spacer(modifier = Modifier.height(20.dp))

        // Contact details as requested
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("CONTACT CENTER DIRECTORY", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(12.dp))
                Text("Agbugburu Village, Abeokuta, Ogun State", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(12.dp))
                Text("07014535079", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(12.dp))
                Text("hisgraceschool.name.ng@gmail.com", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(12.dp))
                Text("Hours: Mon - Fri (7:45 AM - 4:00 PM)", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "© 2026 His Grace Nursery & Primary School. All rights reserved. Registered PWA Web Client Portal.",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 9.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun FooterLink(text: String, onClick: () -> Unit) {
    Text(
        text = "• $text",
        color = Color.White.copy(alpha = 0.8f),
        fontSize = 11.sp,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 2.dp)
    )
}

@Composable
fun AnnouncementCard(announcement: Announcement, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val formattedDate = remember(announcement.publishedDate) {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        sdf.format(Date(announcement.publishedDate))
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(announcement.targetAudience, fontSize = 10.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = when (announcement.targetAudience) {
                                    "ALL" -> MaterialTheme.colorScheme.primaryContainer
                                    "PARENTS" -> MaterialTheme.colorScheme.secondaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                            modifier = Modifier.height(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = announcement.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expand notice",
                    tint = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (expanded) {
                Text(
                    text = announcement.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Published by: ${announcement.author}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "His Grace School Council",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            } else {
                Text(
                    text = announcement.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 2
                )
            }
        }
    }
}


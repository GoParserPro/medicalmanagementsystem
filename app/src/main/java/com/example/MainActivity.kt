package com.example

import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MedicalViewModel
import com.example.viewmodel.MedicalViewModelFactory
import com.example.viewmodel.UserRole
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Initialize Room db and repository here
                val context = LocalContext.current
                val database = MedicalDatabase.getDatabase(context)
                val repository = MedicalRepository(database.medicalDao())
                val app = context.applicationContext as Application

                val factory = MedicalViewModelFactory(app, repository)
                val viewModel: MedicalViewModel = viewModel(factory = factory)

                MedicalAppRoot(viewModel)
            }
        }
    }
}

@Composable
fun MedicalAppRoot(viewModel: MedicalViewModel) {
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Constant Security Signature matching user secure requirements
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Encrypted",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🔒 AES-128 Local Encrypted Database Active",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentRole) {
                UserRole.ROLE_SELECTION -> RoleSelectionScreen(viewModel)
                UserRole.PATIENT -> PatientPortalScreen(viewModel)
                UserRole.DOCTOR -> DoctorPortalScreen(viewModel)
            }
        }
    }
}

// ==================== ROLE SELECTION SCREEN ====================

@Composable
fun RoleSelectionScreen(viewModel: MedicalViewModel) {
    val patientList by viewModel.patients.collectAsStateWithLifecycle()
    val doctorList by viewModel.doctors.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // 1. High-Polished Illustration generated dynamically
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(24.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_medical_hero),
                contentDescription = "Clinical System Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Title pairing
        Text(
            text = "Clinical Hub",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Secure Patient & Physician Management Console",
            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
        )

        // Portal Choices
        PortalCard(
            title = "PATIENT PORTAL",
            subtitle = "Book clinic checkups, view Encrypted Treatment charts, and pay billing ledgers securely.",
            icon = Icons.Default.AccessibilityNew,
            statsText = "Active Patients Rec: ${patientList.size}",
            colorBrush = Brush.linearGradient(
                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
            ),
            onClick = { viewModel.selectRole(UserRole.PATIENT) }
        )

        Spacer(modifier = Modifier.height(18.dp))

        PortalCard(
            title = "PHYSICIAN CONSOLE",
            subtitle = "Manage Appointments Agenda, update Patient Diagnosis/Prescription histories, view AI Clinical Trend charts.",
            icon = Icons.Default.MedicalServices,
            statsText = "Onboarded Professionals: ${doctorList.size}",
            colorBrush = Brush.linearGradient(
                colors = listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f))
            ),
            onClick = { viewModel.selectRole(UserRole.DOCTOR) }
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun PortalCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    statsText: String,
    colorBrush: Brush,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(title.lowercase().replace(" ", "_") + "_card"),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .background(colorBrush)
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.2f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.2.sp
                        )
                    )
                    Text(
                        text = statsText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.85f)),
                lineHeight = 20.sp
            )
        }
    }
}


// ==================== PATIENT PORTAL SCREEN ====================

@Composable
fun PatientPortalScreen(viewModel: MedicalViewModel) {
    var activeTab by remember { mutableStateOf(0) }
    val patientsList by viewModel.patients.collectAsStateWithLifecycle()
    val activePatient by viewModel.selectedPatient.collectAsStateWithLifecycle()
    var showRegisterForm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Patient Header Bar
        Surface(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.selectRole(UserRole.ROLE_SELECTION) }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(
                        text = "Patient Portal",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    if (patientsList.isNotEmpty()) {
                        Button(
                            onClick = { showRegisterForm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Create", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Profile", style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                        }
                    }
                }

                // Profile Selector / Register state
                if (patientsList.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "No patient files found. Please register an account below first.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    // Quick Dropdown profile Switcher
                    var expandedProfileMenu by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.padding(top = 4.dp, start = 8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { expandedProfileMenu = true }
                                .background(Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AccountBox, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Viewing: ${activePatient?.name ?: "No Profile Selected"}",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = expandedProfileMenu,
                            onDismissRequest = { expandedProfileMenu = false }
                        ) {
                            patientsList.forEach { pt ->
                                DropdownMenuItem(
                                    text = { Text(pt.name, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        viewModel.selectPatientProfile(pt)
                                        expandedProfileMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Main body depending on state
        if (patientsList.isEmpty() || showRegisterForm) {
            PatientRegisterForm(viewModel = viewModel, onDismiss = { showRegisterForm = false }, canCancel = patientsList.isNotEmpty())
        } else {
            // Display tabs and views
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Profile", fontWeight = FontWeight.Bold) },
                    icon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Book Appt", fontWeight = FontWeight.Bold) },
                    icon = { Icon(imageVector = Icons.Default.DateRange, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("Treatments", fontWeight = FontWeight.Bold) },
                    icon = { Icon(imageVector = Icons.Default.HistoryEdu, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    text = { Text("Bills", fontWeight = FontWeight.Bold) },
                    icon = { Icon(imageVector = Icons.Default.Receipt, contentDescription = null) }
                )
            }

            AnimatedContent(
                targetState = activeTab,
                label = "patient_tab_anim"
            ) { targetTab ->
                when (targetTab) {
                    0 -> PatientProfileTab(viewModel, activePatient!!)
                    1 -> PatientAppointmentTab(viewModel, activePatient!!)
                    2 -> PatientTreatmentTab(viewModel, activePatient!!)
                    3 -> PatientBillsTab(viewModel, activePatient!!)
                }
            }
        }
    }
}

@Composable
fun PatientRegisterForm(viewModel: MedicalViewModel, onDismiss: () -> Unit, canCancel: Boolean) {
    var name by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "🔒 Advanced Patient Registration",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = "This form automatically wraps and encrypts all submitted details using local AES-128 algorithms prior to database insertion.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Patient Full Name") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("patient_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    label = { Text("Date of Birth (YYYY-MM-DD)") },
                    leadingIcon = { Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null) },
                    placeholder = { Text("e.g. 1990-05-12") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("patient_dob_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Phone String") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("patient_phone_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Contact") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("patient_email_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = nationalId,
                    onValueChange = { nationalId = it },
                    label = { Text("SSN / National Identification ID") },
                    leadingIcon = { Icon(imageVector = Icons.Default.ContactPage, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("patient_nid_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (name.isEmpty() || dob.isEmpty() || phone.isEmpty() || email.isEmpty() || nationalId.isEmpty()) {
                            Toast.makeText(context, "Please complete all patient questions.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.registerPatient(name, dob, phone, email, nationalId)
                        Toast.makeText(context, "Patient Card Registered & Encrypted!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("register_patient_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Secure Record Creation", fontWeight = FontWeight.Bold)
                }

                if (canCancel) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel & View Selected Profile")
                    }
                }
            }
        }
    }
}

@Composable
fun PatientProfileTab(viewModel: MedicalViewModel, patient: Patient) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Aesthetic Patient Medical ID card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = patient.name.take(2).uppercase(),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color.White)
                        )
                    }
                    Spacer(modifier = Modifier.width(18.dp))
                    Column {
                        Text(
                            text = patient.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                        Text(
                            text = "Authenticated Patient",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 18.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))

                // Encrypted Metadata entries
                ProfileDetailItem(label = "Date of Birth", value = patient.dob, icon = Icons.Default.CalendarToday)
                ProfileDetailItem(label = "Phone Contact", value = patient.phone, icon = Icons.Default.Phone)
                ProfileDetailItem(label = "Email Address", value = patient.email, icon = Icons.Default.Email)
                ProfileDetailItem(label = "National ID Symbol", value = patient.nationalId, icon = Icons.Default.ContactPage)

                Spacer(modifier = Modifier.height(14.dp))
                Badge(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Decrypted on demand using system cryptographic keys.", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileDetailItem(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)))
            Text(text = value, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
fun PatientAppointmentTab(viewModel: MedicalViewModel, patient: Patient) {
    val doctorsList by viewModel.doctors.collectAsStateWithLifecycle()
    val rawAppointments by viewModel.appointments.collectAsStateWithLifecycle()
    val patientAppointments = rawAppointments.filter { it.patientId == patient.id }

    var expandedDoctorList by remember { mutableStateOf(false) }
    var selectedDoc by remember { mutableStateOf<DoctorEntity?>(null) }
    var dateInput by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var reasonInput by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Prep dynamic clinical times
    val sampleTimes = listOf("09:00 AM", "10:30 AM", "01:00 PM", "03:30 PM")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Book A checkup Slot",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    )

                    // Doc spinner
                    Box(modifier = Modifier.padding(vertical = 8.dp)) {
                        OutlinedButton(
                            onClick = { expandedDoctorList = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = selectedDoc?.let { "${it.name} (${it.specialty})" } ?: "Select Physician Doctor Contact",
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = expandedDoctorList,
                            onDismissRequest = { expandedDoctorList = false }
                        ) {
                            doctorsList.forEach { doc ->
                                DropdownMenuItem(
                                    text = { Text("${doc.name} - ${doc.specialty}", fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        selectedDoc = doc
                                        expandedDoctorList = false
                                    }
                                )
                            }
                        }
                    }

                    // Date Input
                    OutlinedTextField(
                        value = dateInput,
                        onValueChange = { dateInput = it },
                        label = { Text("Appointment Date (YYYY-MM-DD)") },
                        leadingIcon = { Icon(imageVector = Icons.Default.CalendarViewDay, contentDescription = null) },
                        placeholder = { Text(viewModel.getTodayDateString()) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Quick Select Time tags
                    Text(
                        text = "Target Clinic Slot",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        sampleTimes.forEach { slot ->
                            val isSelected = selectedTime == slot
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedTime = slot },
                                label = { Text(slot, style = MaterialTheme.typography.labelSmall) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    // Symptoms Notes
                    OutlinedTextField(
                        value = reasonInput,
                        onValueChange = { reasonInput = it },
                        label = { Text("What symptoms / checkup reason?") },
                        leadingIcon = { Icon(imageVector = Icons.Default.NoteAlt, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (selectedDoc == null || dateInput.isEmpty() || selectedTime.isEmpty() || reasonInput.isEmpty()) {
                                Toast.makeText(context, "Please complete booking inputs.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.bookAppointment(
                                doctorId = selectedDoc!!.id,
                                date = dateInput,
                                time = selectedTime,
                                notes = reasonInput
                            )
                            Toast.makeText(context, "Checkup slot requested!", Toast.LENGTH_SHORT).show()
                            // Clear inputs
                            dateInput = ""
                            selectedTime = ""
                            reasonInput = ""
                            selectedDoc = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirm Appointment", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "My Scheduled Timeline",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (patientAppointments.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "No clinic appointments listed in your timeline.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(patientAppointments) { appt ->
                AppointmentRowItem(appt = appt, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AppointmentRowItem(appt: AppointmentEntity, viewModel: MedicalViewModel) {
    val statusColor = when (appt.status) {
        "PENDING" -> MaterialTheme.colorScheme.tertiary
        "ACCEPTED" -> MaterialTheme.colorScheme.primary
        "COMPLETED" -> Color(0xFF4CAF50)
        else -> Color.Red
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(statusColor.copy(alpha = 0.15f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (appt.status) {
                        "COMPLETED" -> Icons.Default.CheckCircle
                        "PENDING" -> Icons.Default.HourglassEmpty
                        else -> Icons.Default.BookmarkAdded
                    },
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = viewModel.getDoctorName(appt.doctorId),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Specialty: ${viewModel.getDoctorSpecialty(appt.doctorId)}",
                    style = MaterialTheme.typography.labelMedium.copy(color = Color.Gray)
                )
                Text(
                    text = "Time slot: ${appt.appointmentDate} @ ${appt.appointmentTime}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
                if (!appt.patientNotes.isNullOrEmpty()) {
                    Text(
                        text = "Notes: \"${appt.patientNotes}\"",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium, color = Color.DarkGray),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Badged Status Indicators
            Box(
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = appt.status,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, color = statusColor)
                )
            }
        }
    }
}

@Composable
fun PatientTreatmentTab(viewModel: MedicalViewModel, patient: Patient) {
    val treatmentsList by viewModel.treatments.collectAsStateWithLifecycle()
    val patientTreatments = treatmentsList.filter { it.patientId == patient.id }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "My Certified Treatment History",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
        )
        Text(
            text = "Locked medical diagnostics chart. Verified clinic entry.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (patientTreatments.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "No verified treatment records recorded on your file yet. Clinical summaries publish as checkups complete.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(patientTreatments) { tr ->
                    TreatmentCardItem(tr = tr, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun TreatmentCardItem(tr: Treatment, viewModel: MedicalViewModel) {
    val progressColor = when (tr.progress) {
        "FULLY_RECOVERED" -> Color(0xFF4CAF50)
        "RECOVERING" -> MaterialTheme.colorScheme.primary
        "STABLE" -> MaterialTheme.colorScheme.tertiary
        else -> Color.Red
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MedicalInformation,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tr.disease,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        text = "Attender: ${viewModel.getDoctorName(tr.doctorId)}",
                        style = MaterialTheme.typography.labelMedium.copy(color = Color.Gray)
                    )
                }

                Box(
                    modifier = Modifier
                        .background(progressColor.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = tr.progress.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, color = progressColor)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))

            Row(verticalAlignment = Alignment.Top) {
                Icon(imageVector = Icons.Default.Handshake, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "Prescribed Medication Plan:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.Gray))
                    Text(
                        text = tr.prescription,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            val dateStr = SimpleDateFormat("MMM dd, yyyy @ hh:mm a", Locale.getDefault()).format(Date(tr.treatmentDate))
            Text(
                text = "Clinical Log: $dateStr",
                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontWeight = FontWeight.Bold),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun PatientBillsTab(viewModel: MedicalViewModel, patient: Patient) {
    val billsList by viewModel.bills.collectAsStateWithLifecycle()
    val patientBillsList = billsList.filter { it.patientId == patient.id }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Billing Ledgers & Receipts",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
        )
        Text(
            text = "Pay outstanding doctor consultation fees securely.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (patientBillsList.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "No clinical invoices registered to this profile.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(patientBillsList) { b ->
                    BillCardItem(bill = b, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun BillCardItem(bill: BillEntity, viewModel: MedicalViewModel) {
    var showPaymentSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = null,
                    tint = if (bill.isPaid) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Checkout Total: $${String.format(Locale.getDefault(), "%.2f", bill.totalAmount)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        text = "Billed by: ${viewModel.getDoctorName(bill.doctorId)}",
                        style = MaterialTheme.typography.labelMedium.copy(color = Color.Gray)
                    )
                }
                Box(
                    modifier = Modifier
                        .background(
                            color = if (bill.isPaid) Color(0xFF4CAF50).copy(alpha = 0.12f) else MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (bill.isPaid) "PAID" else "UNPAID",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (bill.isPaid) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                        )
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))

            // Cost breakdowns
            CostRow(label = "Physician Diagnostics Consultation Fee", price = bill.diagnosisCharges)
            CostRow(label = "Prescribed Pharmaceutical Item Expenses", price = bill.medicineCharges)
            CostRow(label = "Hospital Facility Surcharges", price = bill.otherCharges)

            Spacer(modifier = Modifier.height(14.dp))

            if (!bill.isPaid) {
                Button(
                    onClick = { showPaymentSheet = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pay_bill_button_${bill.id}"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.CreditCard, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Secure Portal Remittance Check", fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF4CAF50).copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Receipt Cleared. Remittance settlement complete.",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    if (showPaymentSheet) {
        AlertDialog(
            onDismissRequest = { showPaymentSheet = false },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.payBill(bill.id)
                        Toast.makeText(context, "remittance authorized successfully!", Toast.LENGTH_SHORT).show()
                        showPaymentSheet = false
                    },
                    modifier = Modifier.testTag("confirm_payment_submit")
                ) {
                    Text("Defray Balance", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentSheet = false }) {
                    Text("Cancel Remit")
                }
            },
            title = { Text("Secure Stripe Remit Box", fontWeight = FontWeight.ExtraBold) },
            text = {
                Column {
                    Text("Authorized gateway settlement. Deducts payment parameter instantly.")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Amount due to transfer: $${String.format(Locale.getDefault(), "%.2f", bill.totalAmount)}", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }
}

@Composable
fun CostRow(label: String, price: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
        Text(text = "$${String.format(Locale.getDefault(), "%.2f", price)}", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
    }
}


// ==================== DOCTOR PORTAL SCREEN ====================

@Composable
fun DoctorPortalScreen(viewModel: MedicalViewModel) {
    var activeTab by remember { mutableStateOf(0) }
    val doctorsList by viewModel.doctors.collectAsStateWithLifecycle()
    val activeDoctor by viewModel.selectedDoctor.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Doctor Header Panel
        Surface(
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.selectRole(UserRole.ROLE_SELECTION) }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(
                        text = "Physician Console",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    // Doctor Profile Switcher
                    if (doctorsList.isNotEmpty()) {
                        var expandedDocSelMenu by remember { mutableStateOf(false) }
                        Box {
                            Row(
                                modifier = Modifier
                                    .clickable { expandedDocSelMenu = true }
                                    .background(Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.MedicalServices, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = activeDoctor?.let { "Dr. ${it.name.substringAfter("Dr. ").take(10)}..." } ?: "Choose Doc",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                            }
                            DropdownMenu(
                                expanded = expandedDocSelMenu,
                                onDismissRequest = { expandedDocSelMenu = false }
                            ) {
                                doctorsList.forEach { doc ->
                                    DropdownMenuItem(
                                        text = { Text(doc.name, fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            viewModel.selectDoctorProfile(doc)
                                            expandedDocSelMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                if (activeDoctor != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = activeDoctor!!.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.ExtraBold)
                                )
                                Text(
                                    text = "${activeDoctor!!.specialty} • ${activeDoctor!!.qualification}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.8f))
                                )
                            }
                        }
                    }
                }
            }
        }

        if (activeDoctor != null) {
            // TabRow selector for Doctors
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.secondary
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Today", fontWeight = FontWeight.Bold) },
                    icon = { Icon(imageVector = Icons.Default.Today, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Pending Inbox", fontWeight = FontWeight.Bold) },
                    icon = { Icon(imageVector = Icons.Default.Inbox, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("Patients Clinical", fontWeight = FontWeight.Bold) },
                    icon = { Icon(imageVector = Icons.Default.FolderShared, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    text = { Text("AI Trends", fontWeight = FontWeight.Bold) },
                    icon = { Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null) }
                )
            }

            AnimatedContent(
                targetState = activeTab,
                label = "doctor_tab_anim"
            ) { targetTab ->
                when (targetTab) {
                    0 -> DoctorTodayTab(viewModel, activeDoctor!!)
                    1 -> DoctorPendingTab(viewModel, activeDoctor!!)
                    2 -> DoctorPatientHistoryTab(viewModel, activeDoctor!!)
                    3 -> DoctorAIReportsTab(viewModel)
                }
            }
        }
    }
}

@Composable
fun DoctorTodayTab(viewModel: MedicalViewModel, doctor: DoctorEntity) {
    val rawAppointments by viewModel.appointments.collectAsStateWithLifecycle()
    val todayDateString = viewModel.getTodayDateString()

    // Filter appointments for this physician on current day
    val todayAgenda = rawAppointments.filter {
        it.doctorId == doctor.id && it.appointmentDate == todayDateString
    }

    var showDiagnosisDialog = remember { mutableStateOf<AppointmentEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Today's Clinic Checkups Agenda",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
            )
            Text(
                text = "Live physician roster schedule for date ${todayDateString}.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (todayAgenda.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No clinical schedule slots booked for today.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                        )
                    }
                }
            }
        } else {
            items(todayAgenda) { appt ->
                DoctorAppointmentCard(
                    appt = appt,
                    isToday = true,
                    viewModel = viewModel,
                    onDiagnoseSelected = { showDiagnosisDialog.value = appt }
                )
            }
        }
    }

    if (showDiagnosisDialog.value != null) {
        DiagnosisFormDialog(
            appointment = showDiagnosisDialog.value!!,
            viewModel = viewModel,
            onDismiss = { showDiagnosisDialog.value = null }
        )
    }
}

@Composable
fun DoctorPendingTab(viewModel: MedicalViewModel, doctor: DoctorEntity) {
    val rawAppointments by viewModel.appointments.collectAsStateWithLifecycle()
    val todayDateString = viewModel.getTodayDateString()

    // Filter appointments that are "PENDING" but NOT for today's date (or all pending except completed)
    val pendingCalendar = rawAppointments.filter {
        it.doctorId == doctor.id && it.status == "PENDING" && it.appointmentDate != todayDateString
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Outstanding Pending Desk Requests",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
            )
            Text(
                text = "Incoming patient booking queries waiting for validation.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (pendingCalendar.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No pending patient appointment requests.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                        )
                    }
                }
            }
        } else {
            items(pendingCalendar) { appt ->
                DoctorAppointmentCard(
                    appt = appt,
                    isToday = false,
                    viewModel = viewModel,
                    onDiagnoseSelected = {}
                )
            }
        }
    }
}

@Composable
fun DoctorAppointmentCard(
    appt: AppointmentEntity,
    isToday: Boolean,
    viewModel: MedicalViewModel,
    onDiagnoseSelected: () -> Unit
) {
    val statusColor = when (appt.status) {
        "PENDING" -> MaterialTheme.colorScheme.tertiary
        "ACCEPTED" -> MaterialTheme.colorScheme.primary
        "COMPLETED" -> Color(0xFF4CAF50)
        else -> Color.Red
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("doctor_appt_card_${appt.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = viewModel.getPatientName(appt.patientId),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Time requested: ${appt.appointmentDate} @ ${appt.appointmentTime}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                }
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = appt.status,
                        style = MaterialTheme.typography.labelSmall.copy(color = statusColor, fontWeight = FontWeight.Bold)
                    )
                }
            }

            if (!appt.patientNotes.isNullOrEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                ) {
                    Text(
                        text = "Reported Symptoms:\n\"${appt.patientNotes}\"",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Interactive Actions corresponding to required features
            if (appt.status == "PENDING") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { viewModel.updateAppointmentStatus(appt.id, "ACCEPTED") },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("accept_appt_${appt.id}"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Approve / Accept", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { viewModel.updateAppointmentStatus(appt.id, "REJECTED") },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reject_appt_${appt.id}"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Decline Request", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            } else if (appt.status == "ACCEPTED" && isToday) {
                Button(
                    onClick = onDiagnoseSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("diagnose_trigger_${appt.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.EditCalendar, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Update Diagnosis & Generate Bill", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DiagnosisFormDialog(
    appointment: AppointmentEntity,
    viewModel: MedicalViewModel,
    onDismiss: () -> Unit
) {
    var disease by remember { mutableStateOf("") }
    var prescription by remember { mutableStateOf("") }
    var progressFactor by remember { mutableStateOf("RECOVERING") }
    var expandedProgressMenu by remember { mutableStateOf(false) }

    var diagnosisChg by remember { mutableStateOf("") }
    var medicineChg by remember { mutableStateOf("") }
    var hospitalityChg by remember { mutableStateOf("") }

    val progressOptions = listOf("RECOVERING", "STABLE", "CRITICAL", "FULLY_RECOVERED")
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (disease.isEmpty() || prescription.isEmpty() || diagnosisChg.isEmpty() || medicineChg.isEmpty()) {
                        Toast.makeText(context, "Please fill clinical fields & charges.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val diagCost = diagnosisChg.toDoubleOrNull() ?: 0.0
                    val medCost = medicineChg.toDoubleOrNull() ?: 0.0
                    val hospiceCost = hospitalityChg.toDoubleOrNull() ?: 0.0

                    viewModel.completeAppointmentAndBill(
                        appointmentId = appointment.id,
                        disease = disease,
                        prescription = prescription,
                        progress = progressFactor,
                        diagnosisCharges = diagCost,
                        medicineCharges = medCost,
                        otherCharges = hospiceCost
                    )

                    Toast.makeText(context, "Diagnostics logs archived & Billing generated!", Toast.LENGTH_LONG).show()
                    onDismiss()
                },
                modifier = Modifier.testTag("submit_diagnosis_btn")
            ) {
                Text("Archive Logs", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close Form")
            }
        },
        title = {
            Text(
                "Complete Consultation Record",
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.secondary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Log Diagnosis & pharmacy for patient ${viewModel.getPatientName(appointment.patientId)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = disease,
                    onValueChange = { disease = it },
                    label = { Text("Disease Name (Diagnosed)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = prescription,
                    onValueChange = { prescription = it },
                    label = { Text("Medication Plan / Prescription") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                // Progress state factor selection
                Box(modifier = Modifier.padding(vertical = 4.dp)) {
                    OutlinedButton(
                        onClick = { expandedProgressMenu = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Patient Progress: ${progressFactor.replace("_", " ")}", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = expandedProgressMenu,
                        onDismissRequest = { expandedProgressMenu = false }
                    ) {
                        progressOptions.forEach { factor ->
                            DropdownMenuItem(
                                text = { Text(factor.replace("_", " "), fontWeight = FontWeight.Bold) },
                                onClick = {
                                    progressFactor = factor
                                    expandedProgressMenu = false
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Text("Generate Billing Statements", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                OutlinedTextField(
                    value = diagnosisChg,
                    onValueChange = { diagnosisChg = it },
                    label = { Text("Diagnostics Consultation fee ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = medicineChg,
                    onValueChange = { medicineChg = it },
                    label = { Text("Medicines / Pharmacy fee ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = hospitalityChg,
                    onValueChange = { hospitalityChg = it },
                    label = { Text("Other Surcharges ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    )
}

@Composable
fun DoctorPatientHistoryTab(viewModel: MedicalViewModel, doctor: DoctorEntity) {
    val treatmentsList by viewModel.treatments.collectAsStateWithLifecycle()
    // Doctor can view ALL historical patient records, or filters his patient history
    val completeDoctorRecord = treatmentsList.filter { it.doctorId == doctor.id }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "My Treated Patients Repository",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
        )
        Text(
            text = "Total treatments issued: ${completeDoctorRecord.size}",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (completeDoctorRecord.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "No patient archives logged. Treat a patient today to register clinical logs.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(completeDoctorRecord) { tr ->
                    TreatmentCardItem(tr = tr, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun DoctorAIReportsTab(viewModel: MedicalViewModel) {
    val isGenerating by viewModel.isGeneratingReport.collectAsStateWithLifecycle()
    val reportText by viewModel.aiReportText.collectAsStateWithLifecycle()
    val treatmentsList by viewModel.treatments.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "AI Recovery Trend reports",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
        )
        Text(
            text = "Generates live markdown recovery analytics through Gemini services.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Summary Statistics Widgets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatsWidget(
                label = "Total Case Logs",
                value = "${treatmentsList.size}",
                icon = Icons.Default.History,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            val recoveryRate = if (treatmentsList.isNotEmpty()) {
                val resolved = treatmentsList.count { it.progress == "FULLY_RECOVERED" || it.progress == "RECOVERING" }
                (resolved * 100) / treatmentsList.size
            } else 0
            StatsWidget(
                label = "Recovery Success Index",
                value = "$recoveryRate%",
                icon = Icons.Default.TrendingUp,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Request AI Clinical Report",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Calculates local recovery statuses, hospital admissions duration, and trends, sending secure metrics to context-aware clinical modules.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                Button(
                    onClick = { viewModel.generateRecoveryReport() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_report_trigger"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isGenerating
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Trigger Clinical AI Engine", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(18.dp))

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                Spacer(modifier = Modifier.height(14.dp))

                if (isGenerating) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Analyzing Clinical Histories. Compiling trends...",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontWeight = FontWeight.Bold)
                        )
                    }
                } else if (reportText.isNotEmpty()) {
                    SelectionContainer {
                        Text(
                            text = reportText,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.03f), shape = RoundedCornerShape(12.dp))
                                .padding(16.dp)
                                .testTag("ai_report_text")
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Click above to authorize Gemini clinical processing models.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun StatsWidget(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold))
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.Gray))
        }
    }
}

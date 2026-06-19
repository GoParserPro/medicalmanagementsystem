package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.utils.CryptoUtils
import com.example.utils.GeminiReportGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MedicalViewModel(
    application: Application,
    private val repository: MedicalRepository
) : AndroidViewModel(application) {

    // Roles and Selection State
    var currentRole = MutableStateFlow<UserRole>(UserRole.ROLE_SELECTION)
        private set

    var selectedPatient = MutableStateFlow<Patient?>(null)
        private set

    var selectedDoctor = MutableStateFlow<DoctorEntity?>(null)
        private set

    // DB Collected State Flows
    val patients: StateFlow<List<Patient>> = repository.allPatients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val doctors: StateFlow<List<DoctorEntity>> = repository.allDoctors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appointments: StateFlow<List<AppointmentEntity>> = repository.allAppointments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val treatments: StateFlow<List<Treatment>> = repository.allTreatments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bills: StateFlow<List<BillEntity>> = repository.allBills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Gemini Reports State
    var aiReportText = MutableStateFlow<String>("")
        private set

    var isGeneratingReport = MutableStateFlow<Boolean>(false)
        private set

    init {
        // Pre-populate Database with standard Clinical Data on first boot if doctors or patients are empty
        viewModelScope.launch {
            doctors.take(1).collect { docList ->
                if (docList.isEmpty()) {
                    setupInitialMockData()
                }
            }
        }
    }

    private suspend fun setupInitialMockData() {
        val todayStr = getTodayDateString()
        val yesterdayStr = getYesterdayDateString()

        // 1. Insert doctors
        val d1Id = repository.insertDoctor(
            DoctorEntity(
                name = "Dr. Elizabeth Vance",
                specialty = "Cardiology Specialist",
                phone = "312-555-0143",
                email = "elizabeth.vance@hospital.org",
                rating = 4.9f,
                qualification = "MD, FACC - Harvard Medical"
            )
        ).toInt()

        val d2Id = repository.insertDoctor(
            DoctorEntity(
                name = "Dr. Marcus Thorne",
                specialty = "Neurologist",
                phone = "312-555-0982",
                email = "marcus.thorne@hospital.org",
                rating = 4.8f,
                qualification = "PhD, MD, Board Certified Neurology"
            )
        ).toInt()

        // 2. Insert dummy patients
        val p1Id = repository.insertPatient(
            Patient(
                name = "John Doe",
                dob = "1988-11-14",
                phone = "773-555-5201",
                email = "johndoe@email.com",
                nationalId = "NID-10029-X",
                profileImageUri = null
            )
        ).toInt()

        val p2Id = repository.insertPatient(
            Patient(
                name = "Sarah Jenkins",
                dob = "1994-06-25",
                phone = "773-555-8941",
                email = "sarah.j@gmail.com",
                nationalId = "NID-88421-Y",
                profileImageUri = null
            )
        ).toInt()

        // 3. Insert mock appointments:
        // P1 with Doc1: Pending for Today
        repository.insertAppointment(
            AppointmentEntity(
                patientId = p1Id,
                doctorId = d1Id,
                appointmentDate = todayStr,
                appointmentTime = "09:30 AM",
                status = "PENDING",
                patientNotes = "Aches around upper chest while jogging."
            )
        )

        // P2 with Doc1: Pending for Today
        repository.insertAppointment(
            AppointmentEntity(
                patientId = p2Id,
                doctorId = d1Id,
                appointmentDate = todayStr,
                appointmentTime = "11:15 AM",
                status = "PENDING",
                patientNotes = "Routine blood pressure monitor call."
            )
        )

        // P2 with Doc2: Complete appointment from Yesterday
        val compAppId = repository.insertAppointment(
            AppointmentEntity(
                patientId = p2Id,
                doctorId = d2Id,
                appointmentDate = yesterdayStr,
                appointmentTime = "02:00 PM",
                status = "COMPLETED",
                patientNotes = "Stressed and having recurrent headaches."
            )
        ).toInt()

        // 4. Record associated Treatment & Bill for completed Appointment
        repository.insertTreatment(
            Treatment(
                appointmentId = compAppId,
                patientId = p2Id,
                doctorId = d2Id,
                disease = "Complicated Tension Headache",
                prescription = "Acetaminophen 500mg, Daily walking sessions 30 min, Stress management protocol",
                progress = "FULLY_RECOVERED"
            )
        )

        repository.insertBill(
            BillEntity(
                appointmentId = compAppId,
                patientId = p2Id,
                doctorId = d2Id,
                diagnosisCharges = 80.00,
                medicineCharges = 25.50,
                otherCharges = 15.00,
                totalAmount = 120.50,
                isPaid = true
            )
        )
    }

    // Role state modifiers
    fun selectRole(role: UserRole) {
        currentRole.value = role
        if (role == UserRole.PATIENT) {
            // Auto select first patient if matches or keep null
            viewModelScope.launch {
                val ptList = patients.first()
                if (ptList.isNotEmpty() && selectedPatient.value == null) {
                    selectedPatient.value = ptList.first()
                }
            }
        } else if (role == UserRole.DOCTOR) {
            // Auto select first doctor
            viewModelScope.launch {
                val docList = doctors.first()
                if (docList.isNotEmpty() && selectedDoctor.value == null) {
                    selectedDoctor.value = docList.first()
                }
            }
        }
    }

    fun selectPatientProfile(patient: Patient) {
        selectedPatient.value = patient
    }

    fun selectDoctorProfile(doctor: DoctorEntity) {
        selectedDoctor.value = doctor
    }

    // Patient Actions: Create patient profile
    fun registerPatient(name: String, dob: String, phone: String, email: String, nationalId: String) {
        viewModelScope.launch {
            val patient = Patient(
                name = name,
                dob = dob,
                phone = phone,
                email = email,
                nationalId = nationalId
            )
            val id = repository.insertPatient(patient)
            // Auto-select newly created patient
            selectedPatient.value = patient.copy(id = id.toInt())
        }
    }

    // Patient Actions: Book new appointment
    fun bookAppointment(doctorId: Int, date: String, time: String, notes: String) {
        val patientId = selectedPatient.value?.id ?: return
        viewModelScope.launch {
            repository.insertAppointment(
                AppointmentEntity(
                    patientId = patientId,
                    doctorId = doctorId,
                    appointmentDate = date,
                    appointmentTime = time,
                    status = "PENDING",
                    patientNotes = notes
                )
            )
        }
    }

    // Doctor Actions: Select/Reject Todays appointments (Accept or Decline)
    fun updateAppointmentStatus(appointmentId: Int, newStatus: String) {
        viewModelScope.launch {
            val appt = repository.getAppointmentById(appointmentId) ?: return@launch
            repository.updateAppointment(appt.copy(status = newStatus))
        }
    }

    // Doctor Actions: Completed diagnosis, update History, & generate Bill
    fun completeAppointmentAndBill(
        appointmentId: Int,
        disease: String,
        prescription: String,
        progress: String,
        diagnosisCharges: Double,
        medicineCharges: Double,
        otherCharges: Double
    ) {
        viewModelScope.launch {
            val appt = repository.getAppointmentById(appointmentId) ?: return@launch
            
            // 1. Update status to completed
            repository.updateAppointment(appt.copy(status = "COMPLETED"))

            // 2. Insert treatment record
            val treatment = Treatment(
                appointmentId = appointmentId,
                patientId = appt.patientId,
                doctorId = appt.doctorId,
                disease = disease,
                prescription = prescription,
                progress = progress
            )
            repository.insertTreatment(treatment)

            // 3. Generate associated bill
            val total = diagnosisCharges + medicineCharges + otherCharges
            val bill = BillEntity(
                appointmentId = appointmentId,
                patientId = appt.patientId,
                doctorId = appt.doctorId,
                diagnosisCharges = diagnosisCharges,
                medicineCharges = medicineCharges,
                otherCharges = otherCharges,
                totalAmount = total,
                isPaid = false
            )
            repository.insertBill(bill)
        }
    }

    // Patient action: Pay pending bill
    fun payBill(billId: Int) {
        viewModelScope.launch {
            val allBillsList = bills.value
            val match = allBillsList.firstOrNull { it.id == billId } ?: return@launch
            // Room update helper isn't custom-specific, let's just insert with REPLACE constraint using DAO insert
            repository.insertBill(match.copy(isPaid = true))
        }
    }

    // Doctor Actions: Generate Gemini Recovery Trends Analysis Report
    fun generateRecoveryReport() {
        viewModelScope.launch {
            isGeneratingReport.value = true
            aiReportText.value = ""

            // Combine patient treatments into a rich summarized dataset block
            val treatmentsList = treatments.value
            val patientsList = patients.value
            val doctorsList = doctors.value

            if (treatmentsList.isEmpty()) {
                aiReportText.value = "No patient treatment histories recorded. Treat some patients to compile a medical data report."
                isGeneratingReport.value = false
                return@launch
            }

            val builder = StringBuilder()
            builder.append("Summary of Patients Clinical Histories:\n")
            treatmentsList.forEachIndexed { idx, tr ->
                val patName = patientsList.firstOrNull { it.id == tr.patientId }?.name ?: "Patient #${tr.patientId}"
                val docName = doctorsList.firstOrNull { it.id == tr.doctorId }?.name ?: "Doctor #${tr.doctorId}"
                builder.append("${idx + 1}. Patient Name: $patName, Diagnosed with: ${tr.disease}, Recovery State: ${tr.progress}, Attending: $docName\n")
            }

            val report = GeminiReportGenerator.generateRecoveryTrendsReport(builder.toString())
            aiReportText.value = report
            isGeneratingReport.value = false
        }
    }

    // Helper Date generators
    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun getYesterdayDateString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    // Helper UI maps since Room has static entity models
    fun getPatientName(id: Int): String {
        return patients.value.firstOrNull { it.id == id }?.name ?: "Unknown Patient"
    }

    fun getDoctorName(id: Int): String {
        return doctors.value.firstOrNull { it.id == id }?.name ?: "Unknown Doctor"
    }

    fun getDoctorSpecialty(id: Int): String {
        return doctors.value.firstOrNull { it.id == id }?.specialty ?: "Medical Staff"
    }
}

enum class UserRole {
    ROLE_SELECTION,
    PATIENT,
    DOCTOR
}

class MedicalViewModelFactory(
    private val application: Application,
    private val repository: MedicalRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MedicalViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.utils.CryptoUtils

// ==================== ROOM ENTITIES (DATABASE MODELS) ====================

@Entity(tableName = "patients")
data class PatientEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val encryptedName: String,
    val encryptedDob: String,
    val encryptedPhone: String,
    val encryptedEmail: String,
    val encryptedNationalId: String,
    val profileImageUri: String? = null
)

@Entity(tableName = "doctors")
data class DoctorEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val specialty: String,
    val phone: String,
    val email: String,
    val rating: Float = 4.8f,
    val qualification: String
)

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientId: Int,
    val doctorId: Int,
    val appointmentDate: String, // String representation formatted as YYYY-MM-DD
    val appointmentTime: String,
    val status: String, // "PENDING", "ACCEPTED", "REJECTED", "COMPLETED"
    val patientNotes: String? = null
)

@Entity(tableName = "treatments")
data class TreatmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val appointmentId: Int,
    val patientId: Int,
    val doctorId: Int,
    val encryptedDisease: String,
    val encryptedPrescription: String,
    val encryptedProgress: String, // "RECOVERING", "STABLE", "CRITICAL", "FULLY_RECOVERED"
    val treatmentDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val appointmentId: Int,
    val patientId: Int,
    val doctorId: Int,
    val diagnosisCharges: Double,
    val medicineCharges: Double,
    val otherCharges: Double,
    val totalAmount: Double,
    val isPaid: Boolean = false,
    val billDate: Long = System.currentTimeMillis()
)

// ==================== DOMAIN MODELS (PLAINTEXT CLEAN DATA FOR UI) ====================

data class Patient(
    val id: Int = 0,
    val name: String,
    val dob: String,
    val phone: String,
    val email: String,
    val nationalId: String,
    val profileImageUri: String? = null
) {
    fun toEntity(): PatientEntity {
        return PatientEntity(
            id = id,
            encryptedName = CryptoUtils.encrypt(name),
            encryptedDob = CryptoUtils.encrypt(dob),
            encryptedPhone = CryptoUtils.encrypt(phone),
            encryptedEmail = CryptoUtils.encrypt(email),
            encryptedNationalId = CryptoUtils.encrypt(nationalId),
            profileImageUri = profileImageUri
        )
    }
}

data class Treatment(
    val id: Int = 0,
    val appointmentId: Int,
    val patientId: Int,
    val doctorId: Int,
    val disease: String,
    val prescription: String,
    val progress: String,
    val treatmentDate: Long = System.currentTimeMillis()
) {
    fun toEntity(): TreatmentEntity {
        return TreatmentEntity(
            id = id,
            appointmentId = appointmentId,
            patientId = patientId,
            doctorId = doctorId,
            encryptedDisease = CryptoUtils.encrypt(disease),
            encryptedPrescription = CryptoUtils.encrypt(prescription),
            encryptedProgress = CryptoUtils.encrypt(progress),
            treatmentDate = treatmentDate
        )
    }
}

// Function mapper for clean decoding of sensitive entities
fun PatientEntity.toDomain(): Patient {
    return Patient(
        id = id,
        name = CryptoUtils.decrypt(encryptedName),
        dob = CryptoUtils.decrypt(encryptedDob),
        phone = CryptoUtils.decrypt(encryptedPhone),
        email = CryptoUtils.decrypt(encryptedEmail),
        nationalId = CryptoUtils.decrypt(encryptedNationalId),
        profileImageUri = profileImageUri
    )
}

fun TreatmentEntity.toDomain(): Treatment {
    return Treatment(
        id = id,
        appointmentId = appointmentId,
        patientId = patientId,
        doctorId = doctorId,
        disease = CryptoUtils.decrypt(encryptedDisease),
        prescription = CryptoUtils.decrypt(encryptedPrescription),
        progress = CryptoUtils.decrypt(encryptedProgress),
        treatmentDate = treatmentDate
    )
}

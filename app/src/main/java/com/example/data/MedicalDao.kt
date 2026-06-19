package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicalDao {

    // Patients
    @Query("SELECT * FROM patients")
    fun getAllPatients(): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE id = :id")
    suspend fun getPatientById(id: Int): PatientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: PatientEntity): Long

    // Doctors
    @Query("SELECT * FROM doctors")
    fun getAllDoctors(): Flow<List<DoctorEntity>>

    @Query("SELECT * FROM doctors WHERE id = :id")
    suspend fun getDoctorById(id: Int): DoctorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctor(doctor: DoctorEntity): Long

    // Appointments
    @Query("SELECT * FROM appointments")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE patientId = :patientId ORDER BY appointmentDate DESC")
    fun getAppointmentsForPatient(patientId: Int): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE doctorId = :doctorId ORDER BY appointmentDate DESC")
    fun getAppointmentsForDoctor(doctorId: Int): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE id = :id")
    suspend fun getAppointmentById(id: Int): AppointmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity): Long

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)

    // Treataments
    @Query("SELECT * FROM treatments")
    fun getAllTreatments(): Flow<List<TreatmentEntity>>

    @Query("SELECT * FROM treatments WHERE patientId = :patientId ORDER BY treatmentDate DESC")
    fun getTreatmentsForPatient(patientId: Int): Flow<List<TreatmentEntity>>

    @Query("SELECT * FROM treatments WHERE doctorId = :doctorId ORDER BY treatmentDate DESC")
    fun getTreatmentsForDoctor(doctorId: Int): Flow<List<TreatmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTreatment(treatment: TreatmentEntity): Long

    // Bills
    @Query("SELECT * FROM bills")
    fun getAllBills(): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE patientId = :patientId ORDER BY billDate DESC")
    fun getBillsForPatient(patientId: Int): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE doctorId = :doctorId ORDER BY billDate DESC")
    fun getBillsForDoctor(doctorId: Int): Flow<List<BillEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillEntity): Long
}

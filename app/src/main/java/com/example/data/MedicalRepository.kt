package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MedicalRepository(private val medicalDao: MedicalDao) {

    // Patients
    val allPatients: Flow<List<Patient>> = medicalDao.getAllPatients().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun getPatientById(id: Int): Patient? {
        return medicalDao.getPatientById(id)?.toDomain()
    }

    suspend fun insertPatient(patient: Patient): Long {
        return medicalDao.insertPatient(patient.toEntity())
    }

    // Doctors
    val allDoctors: Flow<List<DoctorEntity>> = medicalDao.getAllDoctors()

    suspend fun getDoctorById(id: Int): DoctorEntity? {
        return medicalDao.getDoctorById(id)
    }

    suspend fun insertDoctor(doctor: DoctorEntity): Long {
        return medicalDao.insertDoctor(doctor)
    }

    // Appointments
    val allAppointments: Flow<List<AppointmentEntity>> = medicalDao.getAllAppointments()

    fun getAppointmentsForPatient(patientId: Int): Flow<List<AppointmentEntity>> {
        return medicalDao.getAppointmentsForPatient(patientId)
    }

    fun getAppointmentsForDoctor(doctorId: Int): Flow<List<AppointmentEntity>> {
        return medicalDao.getAppointmentsForDoctor(doctorId)
    }

    suspend fun getAppointmentById(id: Int): AppointmentEntity? {
        return medicalDao.getAppointmentById(id)
    }

    suspend fun insertAppointment(appointment: AppointmentEntity): Long {
        return medicalDao.insertAppointment(appointment)
    }

    suspend fun updateAppointment(appointment: AppointmentEntity) {
        medicalDao.updateAppointment(appointment)
    }

    // Treatments
    val allTreatments: Flow<List<Treatment>> = medicalDao.getAllTreatments().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getTreatmentsForPatient(patientId: Int): Flow<List<Treatment>> {
        return medicalDao.getTreatmentsForPatient(patientId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getTreatmentsForDoctor(doctorId: Int): Flow<List<Treatment>> {
        return medicalDao.getTreatmentsForDoctor(doctorId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun insertTreatment(treatment: Treatment): Long {
        return medicalDao.insertTreatment(treatment.toEntity())
    }

    // Bills
    val allBills: Flow<List<BillEntity>> = medicalDao.getAllBills()

    fun getBillsForPatient(patientId: Int): Flow<List<BillEntity>> {
        return medicalDao.getBillsForPatient(patientId)
    }

    fun getBillsForDoctor(doctorId: Int): Flow<List<BillEntity>> {
        return medicalDao.getBillsForDoctor(doctorId)
    }

    suspend fun insertBill(bill: BillEntity): Long {
        return medicalDao.insertBill(bill)
    }
}

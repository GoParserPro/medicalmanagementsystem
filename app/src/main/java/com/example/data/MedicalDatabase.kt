package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PatientEntity::class,
        DoctorEntity::class,
        AppointmentEntity::class,
        TreatmentEntity::class,
        BillEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MedicalDatabase : RoomDatabase() {

    abstract fun medicalDao(): MedicalDao

    companion object {
        @Volatile
        private var INSTANCE: MedicalDatabase? = null

        fun getDatabase(context: Context): MedicalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedicalDatabase::class.java,
                    "medical_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

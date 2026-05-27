package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DeviceHealthLog::class,
        DeviceAlert::class,
        PhoneUsageEvent::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PhoneHealthDatabase : RoomDatabase() {

    abstract fun phoneHealthDao(): PhoneHealthDao

    companion object {
        @Volatile
        private var INSTANCE: PhoneHealthDatabase? = null

        fun getDatabase(context: Context): PhoneHealthDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PhoneHealthDatabase::class.java,
                    "phone_health_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

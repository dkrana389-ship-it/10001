package com.keepfocus.app.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "daily_stats")
data class DailyStat(@PrimaryKey val day: String, val reels: Int = 0, val reminders: Int = 0, val closed: Int = 0)

@Dao interface StatsDao {
    @Query("SELECT * FROM daily_stats WHERE day = :day LIMIT 1") suspend fun get(day: String): DailyStat?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun put(stat: DailyStat)
    @Query("SELECT * FROM daily_stats ORDER BY day DESC LIMIT 7") fun lastWeek(): Flow<List<DailyStat>>
}

@Database(entities = [DailyStat::class], version = 1, exportSchema = false)
abstract class FocusDatabase : RoomDatabase() { abstract fun statsDao(): StatsDao }

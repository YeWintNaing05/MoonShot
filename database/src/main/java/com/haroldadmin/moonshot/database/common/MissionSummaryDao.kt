package com.haroldadmin.moonshot.database.common

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.haroldadmin.moonshot.models.common.MissionSummary

@Dao
interface MissionSummaryDao {

    @Query("SELECT * FROM mission_summaries")
    suspend fun getAllMissionSummaries(): List<MissionSummary>

    @Insert
    suspend fun saveMissionSummary(missionSummary: MissionSummary)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMissionSummaries(vararg missionSummary: MissionSummary)

    @Delete
    suspend fun deleteMissionSummary(missionSummary: MissionSummary)

    @Delete
    suspend fun deleteMissionSummaries(vararg missionSummaries: MissionSummary)
}
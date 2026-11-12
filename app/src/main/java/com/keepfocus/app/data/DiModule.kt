package com.keepfocus.app.data

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class)
object DiModule {
    @Provides @Singleton fun database(@ApplicationContext context: Context): FocusDatabase = Room.databaseBuilder(context, FocusDatabase::class.java, "keep_focus.db").build()
    @Provides fun statsDao(db: FocusDatabase): StatsDao = db.statsDao()
}

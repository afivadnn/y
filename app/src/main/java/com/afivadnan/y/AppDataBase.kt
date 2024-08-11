package com.afivadnan.y
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Friend::class],
    version = 3)
abstract class AppDataBase : RoomDatabase() {

    abstract fun friendDao():FriendDao
    companion object{
        @Volatile
        private var INSTANCE : AppDataBase? = null

        fun getInstance(context: Context): AppDataBase{
            val tempinstance = INSTANCE
            if (tempinstance != null ) {return tempinstance}

            val instance = Room.databaseBuilder(context.applicationContext, AppDataBase::class.java,"my_database")
                .fallbackToDestructiveMigration().build()

            INSTANCE=instance
            return instance
        }
    }
}
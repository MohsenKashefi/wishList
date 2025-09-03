package eu.tutorials.mywishlistapp

import android.content.Context
import androidx.room.Room
import eu.tutorials.mywishlistapp.data.WishDatabase
import eu.tutorials.mywishlistapp.data.WishRepository

object Graph {
    lateinit var database: WishDatabase
        private set

    val wishRepository: WishRepository by lazy {
        WishRepository(wishDao = database.wishDao())
    }

    fun provide(context: Context) {
        database = Room
            .databaseBuilder(context, WishDatabase::class.java, "wishlist.db")
            .fallbackToDestructiveMigration() // safe for dev; remove if you add proper migrations
            .build()
    }
}

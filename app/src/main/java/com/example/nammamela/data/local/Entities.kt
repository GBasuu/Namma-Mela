package com.example.nammamela.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val username: String,
    val email: String,
    val phoneNumber: String,
    val password: String,
    val role: String = "user"
)

@Entity(tableName = "seats", primaryKeys = ["seatId", "showTime"])
data class Seat(
    val seatId: String, // e.g., "A1", "B2"
    val showTime: String, // e.g., "6PM", "9PM"
    val isBooked: Boolean = false
)

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "waiting_list")
data class WaitingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val showTime: String,
    val requestedSeats: String
)

@Entity(tableName = "shows")
data class ShowEntity(
    @PrimaryKey val showTime: String // e.g., "6PM", "9PM", "11AM"
)

@Entity(tableName = "cast_members")
data class CastEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: String,
    val actorName: String
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val showTime: String,
    val review: String,
    val rating: Int, // 1 to 5 stars
    val timestamp: Long = System.currentTimeMillis()
)

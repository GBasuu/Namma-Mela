package com.example.nammamela.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE username = :u")
    fun getUser(u: String): User?

    @Query("SELECT * FROM users")
    fun getAllUsers(): List<User>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    fun findByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    fun login(username: String, password: String): User?
}

@Dao
interface SeatDao {
    @Query("SELECT * FROM seats WHERE showTime = :showTime")
    fun getSeatsByShow(showTime: String): Flow<List<Seat>>

    @Query("SELECT * FROM seats WHERE showTime = :showTime")
    fun getSeatsByShowSync(showTime: String): List<Seat>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeat(seat: Seat)

    @Query("DELETE FROM seats")
    suspend fun deleteAll()

    @Query("DELETE FROM seats WHERE showTime = :showTime")
    suspend fun resetSeatsByShow(showTime: String)
}

@Dao
interface ShowDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShow(show: ShowEntity)

    @Query("SELECT * FROM shows")
    fun getAllShows(): Flow<List<ShowEntity>>
}

@Dao
interface CastDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCast(cast: CastEntity)

    @Query("SELECT * FROM cast_members")
    fun getAllCast(): Flow<List<CastEntity>>

    @Query("DELETE FROM cast_members WHERE id = :castId")
    suspend fun deleteCast(castId: Int)
}

@Dao
interface ReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Query("SELECT * FROM reviews ORDER BY timestamp DESC")
    fun getAllReviews(): Flow<List<ReviewEntity>>

    @Query("DELETE FROM reviews WHERE id = :id")
    suspend fun deleteReview(id: Int)
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments ORDER BY timestamp DESC")
    fun getAllComments(): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)
}

@Dao
interface WaitingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaiting(waiting: WaitingEntity)

    @Query("SELECT * FROM waiting_list WHERE showTime = :show")
    fun getWaitingList(show: String): Flow<List<WaitingEntity>>
}

@Database(entities = [User::class, Seat::class, Comment::class, WaitingEntity::class, ShowEntity::class, CastEntity::class, ReviewEntity::class], version = 9)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun seatDao(): SeatDao
    abstract fun commentDao(): CommentDao
    abstract fun waitingDao(): WaitingDao
    abstract fun showDao(): ShowDao
    abstract fun castDao(): CastDao
    abstract fun reviewDao(): ReviewDao
}



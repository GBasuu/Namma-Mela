package com.example.nammamela.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.nammamela.data.local.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val db by lazy {
        Room.databaseBuilder(
            application,
            AppDatabase::class.java, "seat_db"
        ).fallbackToDestructiveMigration().build()
    }

    private val prefs = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val _currentShow = MutableStateFlow("6PM")
    val currentShow: StateFlow<String> = _currentShow

    val shows: StateFlow<List<ShowEntity>> = flow {
        emitAll(db.showDao().getAllShows())
    }.flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val seats: StateFlow<List<Seat>> = _currentShow.flatMapLatest { show ->
        db.seatDao().getSeatsByShow(show)
    }.flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isHouseFull: StateFlow<Boolean> = seats.map { list ->
        list.filter { it.isBooked }.size >= 25
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val bookedSeatsString: StateFlow<String> = seats.map { list ->
        list.filter { it.isBooked }.joinToString(", ") { it.seatId }
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    @OptIn(ExperimentalCoroutinesApi::class)
    val waitingList: StateFlow<List<WaitingEntity>> = _currentShow.flatMapLatest { show ->
        db.waitingDao().getWaitingList(show)
    }.flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCast: StateFlow<List<CastEntity>> = flow {
        emitAll(db.castDao().getAllCast())
    }.flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reviews: StateFlow<List<ReviewEntity>> = flow {
        emitAll(db.reviewDao().getAllReviews())
    }.flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val comments: StateFlow<List<Comment>> = flow {
        emitAll(db.commentDao().getAllComments())
    }.flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSeats = MutableStateFlow<Set<String>>(emptySet())
    val selectedSeats: StateFlow<Set<String>> = _selectedSeats

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        viewModelScope.launch(Dispatchers.IO) {
            db.userDao().insertUser(User("admin", "admin@gmail.com", "9999999999", "1234", "user"))
            db.userDao().insertUser(User("manager", "manager@gmail.com", "8888888888", "admin123", "manager"))
            
            // Insert default shows if none exist
            val currentShows = db.showDao().getAllShows().first()
            if (currentShows.isEmpty()) {
                db.showDao().insertShow(ShowEntity("6PM"))
                db.showDao().insertShow(ShowEntity("9PM"))
            }

            // Insert default cast if none exist
            val currentCast = db.castDao().getAllCast().first()
            if (currentCast.isEmpty()) {
                db.castDao().insertCast(CastEntity(role = "Lead Actor 🎭", actorName = "Rajkumar"))
                db.castDao().insertCast(CastEntity(role = "Comedian 😂", actorName = "Bramhanandam"))
                db.castDao().insertCast(CastEntity(role = "Singer 🎤", actorName = "Sid Sriram"))
            }

            val loggedInName = prefs.getString("logged_user", null)
            if (loggedInName != null) {
                _currentUser.value = db.userDao().getUser(loggedInName)
            }
        }
    }

    private var generatedOtp: String? = null

    fun sendOtp(onSent: (String) -> Unit) {
        val otp = (1000..9999).random().toString()
        generatedOtp = otp
        onSent(otp)
    }

    fun register(user: User, otp: String, onResult: (String) -> Unit) {
        if (otp != generatedOtp) {
            onResult("Invalid OTP")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val existing = db.userDao().findByUsername(user.username)
            if (existing != null) {
                withContext(Dispatchers.Main) { onResult("Username already exists") }
                return@launch
            }

            db.userDao().insertUser(user)
            withContext(Dispatchers.Main) {
                onResult("Registration Successful")
            }
        }
    }

    fun switchShow() {
        val nextShow = if (_currentShow.value == "6PM") "9PM" else "6PM"
        setShowTime(nextShow)
    }

    fun setShowTime(show: String) {
        _currentShow.value = show
        _selectedSeats.value = emptySet()
    }

    fun toggleSeatSelection(seatId: String) {
        if (isHouseFull.value) return
        _selectedSeats.update { current ->
            if (current.contains(seatId)) current - seatId else current + seatId
        }
    }

    fun confirmBooking(onComplete: (String) -> Unit) {
        if (isHouseFull.value) {
            onComplete("No seats available")
            return
        }
        val selected = _selectedSeats.value
        if (selected.isEmpty()) {
            onComplete("Please select seats")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                selected.forEach { seatId ->
                    db.seatDao().insertSeat(Seat(seatId, _currentShow.value, isBooked = true))
                }
                val show = _currentShow.value
                Log.d("DB_CHECK", "Show=$show, Booked=$selected")

                val bookedList = selected.joinToString(", ")
                _selectedSeats.value = emptySet()
                
                withContext(Dispatchers.Main) {
                    onComplete("Booked Seats: $bookedList")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete("Booking failed: ${e.message}")
                }
            }
        }
    }

    fun login(u: String, p: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = db.userDao().login(u, p)
            withContext(Dispatchers.Main) {
                if (user != null) {
                    prefs.edit().putString("logged_user", u).apply()
                    _currentUser.value = user
                    onResult(true)
                } else {
                    onResult(false)
                }
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        prefs.edit().remove("logged_user").apply()
        _currentUser.value = null
        onComplete()
    }

    fun isLoggedIn(): Boolean = prefs.contains("logged_user")

    fun addToWaitingList(name: String, show: String, seats: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.waitingDao().insertWaiting(WaitingEntity(username = name, showTime = show, requestedSeats = seats))
        }
    }

    fun addComment(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            db.commentDao().insertComment(Comment(content = text))
        }
    }

    fun addReview(text: String, rating: Int) {
        if (text.isBlank()) return
        val user = _currentUser.value?.username ?: "Anonymous"
        val show = _currentShow.value
        viewModelScope.launch(Dispatchers.IO) {
            db.reviewDao().insertReview(
                ReviewEntity(
                    username = user,
                    showTime = show,
                    review = text,
                    rating = rating
                )
            )
        }
    }

    fun deleteReview(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            db.reviewDao().deleteReview(id)
        }
    }

    fun addShow(showTime: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.showDao().insertShow(ShowEntity(showTime))
        }
    }

    fun resetSeats(showTime: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.seatDao().resetSeatsByShow(showTime)
        }
    }

    fun addCast(role: String, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.castDao().insertCast(CastEntity(role = role, actorName = name))
        }
    }

    fun deleteCast(castId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            db.castDao().deleteCast(castId)
        }
    }

    fun initSeats() {
        // No longer needed to load manually as seats is a StateFlow observing currentShow
        val show = _currentShow.value
        viewModelScope.launch(Dispatchers.IO) {
            val booked = db.seatDao().getSeatsByShowSync(show).filter { it.isBooked }.map { it.seatId }
            Log.d("DB_CHECK", "Show=$show, Booked=$booked")
        }
    }
}
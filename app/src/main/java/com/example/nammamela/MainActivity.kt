package com.example.nammamela

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.nammamela.data.local.User
import com.example.nammamela.data.local.ReviewEntity
import com.example.nammamela.ui.AppViewModel
import com.example.nammamela.ui.theme.NammaMelaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("DEBUG", "MainActivity Started")
        enableEdgeToEdge()
        setContent {
            val viewModel: AppViewModel = viewModel()
            var showProfileDialog by remember { mutableStateOf(false) }
            val currentUser by viewModel.currentUser.collectAsState()

            LaunchedEffect(Unit) {
                viewModel.initSeats()
            }

            NammaMelaTheme(darkTheme = true) {
                AppNavigation(
                    viewModel = viewModel,
                    onProfileClick = { showProfileDialog = true }
                )

                if (showProfileDialog) {
                    ProfileDialog(
                        user = currentUser,
                        onDismiss = { showProfileDialog = false },
                        onLogout = {
                            viewModel.logout {
                                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(viewModel: AppViewModel, onProfileClick: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = when(currentRoute) {
                            "seat_map" -> "Seat Selection"
                            "cast" -> "Meet the Cast"
                            "fan_wall" -> "Fan Wall"
                            else -> "Namma Mela"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ) 
                },
                navigationIcon = {
                    if (currentRoute != "main") {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    // Profile Button
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = Color.White)
                    }

                    // Logout Button
                    IconButton(onClick = {
                        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                        prefs.edit().clear().apply()
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(navController = navController, startDestination = "main") {
                composable("main") { MainScreen(navController) }
                composable("seat_map") { SeatMapScreen(viewModel) }
                composable("cast") { CastScreen() }
                composable("fan_wall") { FanWallScreen(viewModel) }
            }
        }
    }
}

@Composable
fun ProfileDialog(user: User?, onDismiss: () -> Unit, onLogout: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "User Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                ProfileRow("Username", user?.username ?: "Unknown")
                ProfileRow("Email", user?.email ?: "Unknown")
                ProfileRow("Phone", user?.phoneNumber ?: "Unknown")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        },
        dismissButton = {
            TextButton(onClick = onLogout) {
                Text("Logout", color = MaterialTheme.colorScheme.error)
            }
        },
        containerColor = Color(0xFF0F172A),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}

@Composable
fun ProfileRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = "$label: ", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
        Text(text = value, color = Color.White, fontSize = 14.sp)
    }
}

@Composable
fun MainScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Namma Mela",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "The Great Folk Fest", fontSize = 18.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Duration: 2h 30m", fontSize = 16.sp, color = Color.LightGray)
        
        Spacer(modifier = Modifier.height(48.dp))
        
        GradientButton("Book Seats") { navController.navigate("seat_map") }
        Spacer(modifier = Modifier.height(16.dp))
        GradientButton("View Cast") { navController.navigate("cast") }
        Spacer(modifier = Modifier.height(16.dp))
        GradientButton("Fan Wall") { navController.navigate("fan_wall") }
    }
}

@Composable
fun GradientButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                brush = Brush.horizontalGradient(listOf(Color(0xFF6200EE), Color(0xFF3700B3))),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Text(text = text, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatMapScreen(viewModel: AppViewModel) {
    val seats by viewModel.seats.collectAsState()
    val selectedSeats by viewModel.selectedSeats.collectAsState()
    val isFull by viewModel.isHouseFull.collectAsState()
    val bookedSeatsText by viewModel.bookedSeatsString.collectAsState()
    val currentShow by viewModel.currentShow.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val context = LocalContext.current
    var waitingName by remember { mutableStateOf("") }

    val seatList = remember {
        val list = mutableListOf<String>()
        for (row in 'A'..'E') {
            for (col in 1..5) {
                list.add("$row$col")
            }
        }
        list
    }

    val bookedCount = seats.count { it.isBooked }
    val availableCount = 25 - bookedCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Show Time Selection
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            val showEntities by viewModel.shows.collectAsState()
            showEntities.forEach { showEntity ->
                val show = showEntity.showTime
                FilterChip(
                    selected = currentShow == show,
                    onClick = { viewModel.setShowTime(show) },
                    label = { Text(show) },
                    modifier = Modifier.padding(horizontal = 4.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF6200EE),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Show: $currentShow", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        if (isFull) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Red)
            ) {
                Text(
                    text = "🎭 House Full - Book Next Show",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally)
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Total Seats: 25", fontWeight = FontWeight.Bold)
                Text(text = "Booked Seats: $bookedCount", color = Color.Red)
                Text(text = "Available Seats: $availableCount", color = Color.Green)
                if (bookedSeatsText.isNotEmpty()) {
                    Text(text = "Booked Seats: $bookedSeatsText", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(seatList) { seatId ->
                val isBooked = seats.find { it.seatId == seatId }?.isBooked ?: false
                val isSelected = selectedSeats.contains(seatId)
                
                SeatItem(seatId, isBooked, isSelected) {
                    if (isFull) {
                        Toast.makeText(context, "House Full!", Toast.LENGTH_SHORT).show()
                    } else if (isBooked) {
                        Toast.makeText(context, "Already Booked", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.toggleSeatSelection(seatId)
                    }
                }
            }
        }

        if (isFull) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                TextField(
                    value = waitingName,
                    onValueChange = { waitingName = it },
                    placeholder = { Text("Enter name for waiting list...") },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (waitingName.isNotBlank()) {
                            val selectedText = if (selectedSeats.isEmpty()) "Any" else selectedSeats.joinToString(", ")
                            viewModel.addToWaitingList(waitingName, currentShow, selectedText)
                            Toast.makeText(context, "Added to waiting list", Toast.LENGTH_SHORT).show()
                            waitingName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)), // Orange accent
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Join")
                }
            }
        }
        
        Row(modifier = Modifier.padding(8.dp)) {
            LegendItem("Available", Color.Green)
            Spacer(modifier = Modifier.width(16.dp))
            LegendItem("Selected", Color.Yellow)
            Spacer(modifier = Modifier.width(16.dp))
            LegendItem("Booked", Color.Red)
        }

        Spacer(modifier = Modifier.height(8.dp))
        GradientButton(if (isFull) "House Full" else "Confirm Booking") {
            if (isFull) {
                Toast.makeText(context, "No seats available", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.confirmBooking { message ->
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

@Composable
fun SeatItem(id: String, isBooked: Boolean, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .background(
                when {
                    isBooked -> Color.Red
                    isSelected -> Color.Yellow
                    else -> Color.Green
                },
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = id, color = if(isSelected) Color.Black else Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LegendItem(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(16.dp).background(color, RoundedCornerShape(4.dp)))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}

@Composable
fun CastScreen(viewModel: AppViewModel = viewModel()) {
    val castMembers by viewModel.allCast.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (castMembers.isEmpty()) {
            Text("No cast members found", color = Color.Gray)
        } else {
            LazyColumn {
                items(castMembers) { cast ->
                    CastItem(cast.role, cast.actorName)
                }
            }
        }
    }
}

@Composable
fun CastItem(role: String, name: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = role, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = name, fontSize = 16.sp, color = Color.Gray)
        }
    }
}

@Composable
fun FanWallScreen(viewModel: AppViewModel) {
    val reviews by viewModel.reviews.collectAsState()
    var text by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(5) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Post a Review", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
        
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Write your review...") },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Rating: ", color = Color.White)
                    (1..5).forEach { i ->
                        Text(
                            text = if (i <= rating) "⭐" else "☆",
                            modifier = Modifier
                                .clickable { rating = i }
                                .padding(horizontal = 4.dp),
                            fontSize = 20.sp
                        )
                    }
                }

                Button(
                    onClick = {
                        if (text.isNotBlank()) {
                            viewModel.addReview(text, rating)
                            text = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                ) {
                    Text("Submit Review")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Fan Reviews", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)

        if (reviews.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No reviews yet", color = Color.Gray)
            }
        } else {
            LazyColumn {
                items(reviews) { review ->
                    ReviewItem(review)
                }
            }
        }
    }
}

@Composable
fun ReviewItem(review: ReviewEntity, onRemove: (() -> Unit)? = null) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "User: ${review.username}", fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = "Show: ${review.showTime}", color = Color.Cyan, fontSize = 12.sp)
            }
            Text(text = "Rating: ${"⭐".repeat(review.rating)}", modifier = Modifier.padding(vertical = 4.dp))
            Text(text = review.review, color = Color.LightGray)
            
            if (onRemove != null) {
                Button(
                    onClick = onRemove,
                    modifier = Modifier.align(Alignment.End).padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Delete", fontSize = 12.sp)
                }
            }
        }
    }
}

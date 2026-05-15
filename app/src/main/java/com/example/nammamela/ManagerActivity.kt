package com.example.nammamela

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nammamela.data.local.User
import com.example.nammamela.ui.AppViewModel
import com.example.nammamela.ui.theme.NammaMelaTheme

class ManagerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: AppViewModel = viewModel()
            val currentUser by viewModel.currentUser.collectAsState()

            NammaMelaTheme(darkTheme = true) {
                ManagerDashboard(
                    viewModel = viewModel,
                    user = currentUser,
                    onLogout = {
                        viewModel.logout {
                            val intent = Intent(this@ManagerActivity, LoginActivity::class.java)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerDashboard(viewModel: AppViewModel, user: User?, onLogout: () -> Unit) {
    val waitingList by viewModel.waitingList.collectAsState()
    val currentShow by viewModel.currentShow.collectAsState()
    val showEntities by viewModel.shows.collectAsState()
    val seats by viewModel.seats.collectAsState()
    val bookedSeatsText by viewModel.bookedSeatsString.collectAsState()
    val isFull by viewModel.isHouseFull.collectAsState()
    val context = LocalContext.current

    val bookedCount = seats.count { it.isBooked }
    val availableCount = 25 - bookedCount

    var showAddShowDialog by remember { mutableStateOf(false) }
    var showAddCastDialog by remember { mutableStateOf(false) }
    var showReviews by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Theatre Control Panel", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A), titleContentColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0F172A))
                .padding(16.dp)
        ) {
            // Show Selection
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                IconButton(onClick = { showAddShowDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Show", tint = Color.Green)
                }
            }

            // Show Status Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Status: $currentShow", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Seats: 25", color = Color.LightGray)
                        Text("Booked: $bookedCount", color = if(bookedCount > 20) Color.Red else Color.Cyan)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Available: $availableCount", color = Color.Green)
                        Text("Status: ${if(isFull) "HOUSE FULL" else "AVAILABLE"}", color = if(isFull) Color.Red else Color.Green)
                    }
                    if (bookedSeatsText.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Booked: $bookedSeatsText", fontSize = 12.sp, color = Color.Gray)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { 
                            viewModel.resetSeats(currentShow)
                            Toast.makeText(context, "Seats Reset for $currentShow", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Reset Seats")
                    }
                }
            }

            // Actions
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Button(onClick = { showAddCastDialog = true }, modifier = Modifier.weight(1f)) {
                    Text("Manage Cast")
                }
            }
            Button(
                onClick = { showReviews = !showReviews },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Text(if (showReviews) "Hide Reviews" else "Moderate Reviews")
            }

            if (showReviews) {
                val reviews by viewModel.reviews.collectAsState()
                Text("User Reviews", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
                if (reviews.isEmpty()) {
                    Text("No reviews found", color = Color.Gray)
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(reviews) { review ->
                            ReviewItem(review, onRemove = { viewModel.deleteReview(review.id) })
                        }
                    }
                }
            }

            Text(text = "Waiting List ($currentShow)", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
            
            if (waitingList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(text = "Empty", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(waitingList) { waiting ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "User: ${waiting.username}", fontWeight = FontWeight.Bold, color = Color.White)
                                Text(text = "Seats: ${waiting.requestedSeats}", color = Color.Cyan, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddShowDialog) {
        var newShowTime by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddShowDialog = false },
            title = { Text("Add Show") },
            text = { TextField(value = newShowTime, onValueChange = { newShowTime = it }, placeholder = { Text("e.g. 11 AM") }) },
            confirmButton = {
                Button(onClick = {
                    if (newShowTime.isNotBlank()) {
                        viewModel.addShow(newShowTime)
                        showAddShowDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddShowDialog = false }) { Text("Cancel") } }
        )
    }

    if (showAddCastDialog) {
        val castMembers by viewModel.allCast.collectAsState()
        var newRole by remember { mutableStateOf("") }
        var newName by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showAddCastDialog = false },
            title = { Text("Manage Cast") },
            text = {
                Column {
                    TextField(value = newRole, onValueChange = { newRole = it }, placeholder = { Text("Role") })
                    TextField(value = newName, onValueChange = { newName = it }, placeholder = { Text("Name") }, modifier = Modifier.padding(top = 8.dp))
                    Button(onClick = {
                        if (newRole.isNotBlank() && newName.isNotBlank()) {
                            viewModel.addCast(newRole, newName)
                            newRole = ""
                            newName = ""
                        }
                    }, modifier = Modifier.padding(top = 8.dp).fillMaxWidth()) {
                        Text("Add Member")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(castMembers) { cast ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("${cast.actorName} (${cast.role})", fontSize = 14.sp)
                                IconButton(onClick = { viewModel.deleteCast(cast.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { Button(onClick = { showAddCastDialog = false }) { Text("Close") } }
        )
    }
}

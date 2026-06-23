package com.example.systalk

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.systalk.ui.theme.SysTalkTheme
import kotlinx.coroutines.launch

private const val TAG = "SysTalk"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SysTalkTheme(darkTheme = true) {
                val navController = rememberNavController()
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(onNavigateToMessages = {
                                Log.d(TAG, "Navigating to Messages")
                                navController.navigate("messages")
                            })
                        }
                        composable("messages") {
                            MessagesScreen(onBack = {
                                Log.d(TAG, "Navigating back to Home")
                                navController.popBackStack()
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(onNavigateToMessages: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Home Screen", fontSize = 24.sp, color = Color.White)
        Button(
            onClick = onNavigateToMessages,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "Go to Messages")
        }
    }
}

data class Message(val text: String, val isSent: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(onBack: () -> Unit) {
    val messages = remember { mutableStateListOf<Message>() }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Surface(
                        color = Color(0xFF202124),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.clickable { Log.d(TAG, "Address Bar clicked") }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "3042 Glenmore St, Vista",
                                fontSize = 14.sp,
                                color = Color.White,
                                textDecoration = TextDecoration.Underline
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Favorite",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { Log.d(TAG, "Star clicked") }
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        bottomBar = {
            ChatBottomBar(onMessageSent = { text ->
                Log.d(TAG, "Message Sent: $text")
                messages.add(Message(text, true))
                coroutineScope.launch {
                    if (messages.isNotEmpty()) {
                        listState.animateScrollToItem(messages.size - 1)
                    }
                }
            })
        },
        containerColor = Color.Black
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(messages) { message ->
                ChatBubble(message)
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message) {
    val bubbleColor = if (message.isSent) Color(0xFF624099) else Color(0xFF202124)
    val alignment = if (message.isSent) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (message.isSent) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Surface(
            color = bubbleColor,
            shape = shape,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                color = Color.White,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun ChatBottomBar(onMessageSent: (String) -> Unit) {
    var inputText by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp)
            .navigationBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { Log.d(TAG, "Add Button clicked") }) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.LightGray)
        }
        Surface(
            modifier = Modifier.weight(1f),
            color = Color(0xFF202124),
            shape = RoundedCornerShape(28.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)) {
                    if (inputText.isEmpty()) {
                        Text(
                            text = "RCS message",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                        cursorBrush = SolidColor(Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                IconButton(
                    onClick = { Log.d(TAG, "Emoji Button clicked") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(imageVector = Icons.Default.Face, contentDescription = "Emoji", tint = Color.LightGray)
                }
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = { Log.d(TAG, "Image Button clicked") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(imageVector = Icons.Default.Image, contentDescription = "Image", tint = Color.LightGray)
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Surface(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF8B2C2C))
                .clickable {
                    if (inputText.isNotBlank()) {
                        onMessageSent(inputText)
                        inputText = ""
                    } else {
                        Log.d(TAG, "Mic Button clicked")
                    }
                },
            color = Color(0xFF8B2C2C)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (inputText.isNotBlank()) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                    contentDescription = if (inputText.isNotBlank()) "Send" else "Mic",
                    tint = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SysTalkTheme(darkTheme = true) {
        HomeScreen(onNavigateToMessages = {})
    }
}

@Preview(showBackground = true)
@Composable
fun MessagesScreenPreview() {
    SysTalkTheme(darkTheme = true) {
        MessagesScreen(onBack = {})
    }
}

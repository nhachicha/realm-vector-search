package com.mongodb.app.ui.tasks

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mongodb.app.SearchResultActivity
import com.mongodb.app.ui.theme.MyApplicationTheme

@ExperimentalMaterial3Api
@Composable
fun TaskAppToolbar() {
    var isDialogOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    val context = LocalContext.current

    TopAppBar(
        title = {
            Text(text = "Realm Vector Store")
        },
        actions = {
            IconButton(onClick = { isDialogOpen = true }) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")
            }
        }
    )

    if (isDialogOpen) {
        SearchDialog(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onDismissRequest = { isDialogOpen = false },
            onSearch = { searchType ->
                // Navigate to SearchResultActivity
                isDialogOpen = false
                val intent = Intent(context, SearchResultActivity::class.java).apply {
                    putExtra("searchQuery", searchQuery.text)
                    putExtra("searchType", searchType)
                }
                context.startActivity(intent)
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDialog(
    searchQuery: TextFieldValue,
    onSearchQueryChange: (TextFieldValue) -> Unit,
    onDismissRequest: () -> Unit,
    onSearch: (String) -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Search") },
        text = {
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "Search...") },
                singleLine = true
            )
        },
        confirmButton = {
            Column {
                Button(
                    onClick = {
                        onSearch("Semantic Search")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(text = "Semantic Search")
                }
                Button(
                    onClick = {
                        onSearch("RAG Search")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "RAG Search")
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun TaskAppToolbarPreview() {
    MyApplicationTheme {
        MyApplicationTheme {
            TaskAppToolbar()
        }
    }
}

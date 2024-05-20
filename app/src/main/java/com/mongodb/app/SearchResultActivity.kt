package com.mongodb.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongodb.app.data.EmbeddingData
import com.mongodb.app.data.MockRepository
import com.mongodb.app.data.OpenAiRepository
import com.mongodb.app.data.RealmSyncRepository
import com.mongodb.app.domain.Item
import com.mongodb.app.presentation.tasks.ItemContextualMenuViewModel
import com.mongodb.app.ui.tasks.TaskItem
import com.mongodb.app.ui.theme.Blue
import com.mongodb.app.ui.theme.SearchToolbarTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SearchResultState {
    object Loading : SearchResultState()
    data class Success(val embedding: List<Item>) : SearchResultState()
    data class Completion(val answer: String) : SearchResultState()
    data class Error(val message: String) : SearchResultState()
}

class SearchResultViewModel : ViewModel() {
    private val _state = MutableStateFlow<SearchResultState>(SearchResultState.Loading)
    val state: StateFlow<SearchResultState> get() = _state

    fun getEmbedding(input: String) {
        viewModelScope.launch {
            _state.value = SearchResultState.Loading
            try {
                val embedding = OpenAiRepository.getEmbedding(input)
                _state.value =
                    SearchResultState.Success(RealmSyncRepository.querySimilar(embedding[0].embedding))
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = SearchResultState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun getCompletion(searchQuery: String) {
        viewModelScope.launch {
            _state.value = SearchResultState.Loading
            try {
                // First we get the Vector for our local search
                var ragContext = ""
                val searchVector: List<EmbeddingData> = OpenAiRepository.getEmbedding(searchQuery)

                // We search semantically locally for related argument to the prompt
                searchVector.firstOrNull()?.also {
                    RealmSyncRepository.querySimilar(it.embedding).firstOrNull()?.also { news ->
                        ragContext = "${news.title} ${news.summary}$"
                    }
                }

                val answer: String? =
                    OpenAiRepository.getCompletion(rag = ragContext, prompt = searchQuery)
                _state.value = SearchResultState.Completion(answer!!)
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = SearchResultState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

class SearchResultActivity : ComponentActivity() {
    private val viewModel: SearchResultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val searchQuery = intent.getStringExtra("searchQuery") ?: ""
        val searchType = intent.getStringExtra("searchType") ?: ""

        setContent {
            SearchToolbarTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    SearchResultScreen(
                        viewModel = viewModel,
                        searchQuery = searchQuery,
                        searchType = searchType
                    )
                }
            }
        }

        if ("RAG Search".equals(searchType, ignoreCase = true)) {
            viewModel.getCompletion(searchQuery)
        } else {
            viewModel.getEmbedding(searchQuery)
        }
    }
}

@Composable
fun SearchResultScreen(viewModel: SearchResultViewModel, searchQuery: String, searchType: String) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$searchType: \"$searchQuery\"",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(8.dp)
        )

        when (state) {
            is SearchResultState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }

            is SearchResultState.Success -> {
                LazyColumn(
                    state = rememberLazyListState(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1F)
                )
                {
                    val taskList = (state as SearchResultState.Success).embedding
                    items(taskList.size) { index: Int ->
                        TaskItem(
                            ItemContextualMenuViewModel(MockRepository()),
                            taskList[index]
                        )
                        Divider()
                    }
                }
            }

            is SearchResultState.Completion -> {
                Text(
                    text = (state as SearchResultState.Completion).answer,
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(8.dp),
                    textAlign = TextAlign.Center,
                    color = Blue,
                    fontWeight = FontWeight.Bold
                )

            }

            is SearchResultState.Error -> {
                Text(
                    text = "Error: ${(state as SearchResultState.Error).message}",
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@file:OptIn(ExperimentalMaterial3Api::class)

package com.mongodb.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.mongodb.app.data.MockRepository
import com.mongodb.app.data.RealmSyncRepository
import com.mongodb.app.data.Repository
import com.mongodb.app.presentation.tasks.AddItemEvent
import com.mongodb.app.presentation.tasks.AddItemViewModel
import com.mongodb.app.presentation.tasks.TaskViewModel
import com.mongodb.app.ui.tasks.AddItemPrompt
import com.mongodb.app.ui.tasks.TaskAppToolbar
import com.mongodb.app.ui.tasks.TaskList
import com.mongodb.app.ui.theme.MyApplicationTheme
import com.mongodb.app.ui.theme.Purple200
import kotlinx.coroutines.launch

class ComposeItemActivity : ComponentActivity() {

    private val repository = RealmSyncRepository

    private val addItemViewModel: AddItemViewModel by viewModels {
        AddItemViewModel.factory(repository, this)
    }
    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModel.factory(repository, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            taskViewModel.event
                .collect {
                    Toast.makeText(
                        this@ComposeItemActivity,
                        getString(R.string.permissions_warning),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        lifecycleScope.launch {
            addItemViewModel.addItemEvent
                .collect { fabEvent ->
                    when (fabEvent) {
                        is AddItemEvent.Error ->
                            Log.e("REALM", "${fabEvent.message}: ${fabEvent.throwable.message}")

                        is AddItemEvent.Info ->
                            Log.e("REALM", fabEvent.message)
                    }
                }
        }


        setContent {
            MyApplicationTheme {
                TaskListScaffold(
                    repository,
                    addItemViewModel,
                    taskViewModel
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        repository.close()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun TaskListScaffold(
    repository: Repository,
    addItemViewModel: AddItemViewModel,
    taskViewModel: TaskViewModel
) {
    Scaffold(
        topBar = { TaskAppToolbar() },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
                contentColor = Color.White,
                containerColor = Purple200,
                onClick = {
                    addItemViewModel.openAddTaskDialog()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task"
                )
            }

            if (addItemViewModel.addItemPopupVisible.value) {
                AddItemPrompt(addItemViewModel)
            }
        },
        content = {
            Column {
                Spacer(modifier = Modifier.height(61.dp))
                Divider(color = Color.Red, modifier = Modifier.fillMaxWidth())
                TaskList(repository, taskViewModel)

            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ItemActivityPreview() {
    MyApplicationTheme {
        val repository = MockRepository()
        val tasks = (1..30).map { index ->
            MockRepository.getMockTask(index)
        }.toMutableStateList()

        MyApplicationTheme {
            TaskListScaffold(
                repository,
                AddItemViewModel(repository),
                TaskViewModel(repository, tasks)
            )
        }
    }
}

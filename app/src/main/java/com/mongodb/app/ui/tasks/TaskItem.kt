package com.mongodb.app.ui.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mongodb.app.data.MockRepository
import com.mongodb.app.domain.Item
import com.mongodb.app.presentation.tasks.ItemContextualMenuViewModel
import com.mongodb.app.ui.theme.Blue
import com.mongodb.app.ui.theme.MyApplicationTheme

@Composable
fun TaskItem(
    itemContextualMenuViewModel: ItemContextualMenuViewModel,
    task: Item
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(80.dp)
    ) {
        Column {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                color = Blue,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = task.summary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                overflow = TextOverflow.Ellipsis,
                maxLines = 5
            )
        }

        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            ItemContextualMenu(itemContextualMenuViewModel, task)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskItemPreview() {
    MyApplicationTheme {
        val repository = MockRepository()
        TaskItem(
            ItemContextualMenuViewModel(repository),
            MockRepository.getMockTask(42)
        )
    }
}

package com.mongodb.app.data

import com.mongodb.app.domain.Item
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf


interface Repository {

    /**
     * Returns a flow with the tasks for the current subscription.
     */
    fun getTaskList(): Flow<ResultsChange<Item>>

    /**
     * Adds a task that belongs to the current user using the specified [taskSummary].
     */
    suspend fun addNews(title: String, summary: String)

    /**
     * Deletes a given task.
     */
    suspend fun deleteTask(task: Item)


    /**
     * Closes the realm instance held by this repository.
     */
    fun close()

    fun querySimilar(queryVector: List<Float>): List<Item>
}

/**
 * Repo implementation used in runtime.
 */
object RealmSyncRepository : Repository {

    private val realm: Realm
    private val config: RealmConfiguration = RealmConfiguration.Builder(setOf(Item::class))
        .build()

    init {
        realm = Realm.open(config)
    }

    override fun getTaskList(): Flow<ResultsChange<Item>> {
        return realm.query<Item>()
            .sort(Pair("_id", Sort.ASCENDING))
            .asFlow()
    }

    override suspend fun addNews(title: String, summary: String) {
        val item = Item().apply {
            this.summary = summary
            this.title = title
        }
        val managedTask: Item = realm.write {
            copyToRealm(item)
        }

        OpenAiRepository.getEmbedding("$title $summary").also { embeddingVector: List<EmbeddingData> ->
            realm.writeBlocking {
                findLatest(managedTask)?.embedding?.addAll(embeddingVector[0].embedding.toTypedArray())
            }
        }
    }

    override suspend fun deleteTask(task: Item) {
        realm.write {
            delete(findLatest(task)!!)
        }
    }

    override fun querySimilar(queryVector: List<Float>): List<Item> {
        return realm.query<Item>().knn("embedding", queryVector.toTypedArray(), 2).distinct()
    }

    override fun close() = realm.close()
}

/**
 * Mock repo for generating the Compose layout preview.
 */
class MockRepository: Repository {
    override fun getTaskList(): Flow<ResultsChange<Item>> = flowOf()
    override suspend fun addNews(title: String, summary: String) = Unit
    override suspend fun deleteTask(task: Item) = Unit
    override fun close() = Unit
    override fun querySimilar(queryVector: List<Float>): List<Item> = emptyList()

    companion object {
        fun getMockTask(index: Int): Item = Item().apply {
            this.summary = "Summary $index"
            this.title = "Title $index"
        }
    }
}

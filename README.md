# Demo app using Realm as a Vector Store

## This is a modified version of [mongodb/template-app-kotlin-todo](https://github.com/mongodb/template-app-kotlin-todo.git) that showcase a local semantic search and a Retrieval-Augmented Generation with a GenAI

## Note: This is based on a custom version of Realm Kotlin SDK that uses a [POC](https://github.com/realm/realm-core/pull/6759) of KNN search.

The Demo uses 4 recent articles from https://www.mongodb.com/developer/news/ that are not yet indexed byt the GPT mode `gpt-3.5-turbo` at the time of this demo
![Local_Articles.png](images%2FLocal_Articles.png)

Then offer two search modes:
![Search_Modes.png](images%2FSearch_Modes.png)

### Semantic Search:
We can search local articles (The question is embedded using `text-embedding-ada-002` but the KNN search is done locally)
![Semantic_Search.png](images%2FSemantic_Search.png)

### Retrieval-Augmented Generation Search:
![RAG_search.png](images%2FRAG_search.png)
The GPT search brings outdated answer _(Laravel 10)_. Whereas the RAG search uses local articles for a more up-to-date answer 
![GPT_Search.png](images%2FGPT_Search.png)

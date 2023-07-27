package com.example.mvvmnewsapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.mvvmnewsapp.data.api.RetrofitInstance
import com.example.mvvmnewsapp.data.database.ArticleDatabase
import com.example.mvvmnewsapp.data.model.Article
import com.example.mvvmnewsapp.data.model.NewsResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class NewsRepository(private val database: ArticleDatabase) {

    suspend fun getBreakingNews(countryCode: String, pageNumber: Int): Response<NewsResponse> {
        return RetrofitInstance.newsApi.getBreakingNews(
            countryCode = countryCode,
            pageNumber = pageNumber
        )
    }

    suspend fun searchNews(searchQuery: String, pageNumber: Int): Response<NewsResponse> {
        return RetrofitInstance.newsApi.searchForNews(
            searchQuery = searchQuery,
            pageNumber = pageNumber
        )
    }

    suspend fun insertArticle(article: Article) {
        database.getArticleDao().insert(article)
    }

    suspend fun updateArticle(article: Article) {
        database.getArticleDao().update(article)
    }

    suspend fun deleteArticle(article: Article) {
        database.getArticleDao().delete(article)
    }

    fun getSavedArticles(): LiveData<List<Article>> {
        return database.getArticleDao().getAllArticles()
    }
}
package com.example.mvvmnewsapp.data.repository

import com.example.mvvmnewsapp.data.api.RetrofitInstance
import com.example.mvvmnewsapp.data.database.ArticleDatabase
import com.example.mvvmnewsapp.data.model.NewsResponse
import retrofit2.Response

class NewsRepository(private val database: ArticleDatabase) {

    suspend fun getBreakingNews(countryCode: String, pageNumber: Int): Response<NewsResponse> {
        return RetrofitInstance.newsApi.getBreakingNews(
            countryCode = countryCode,
            pageNumber = pageNumber
        )
    }
}
package com.example.mvvmnewsapp.api

import com.example.mvvmnewsapp.NewsResponse
import com.example.mvvmnewsapp.util.Constants.Companion.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {
    @GET("v2/top-headlines")
    suspend fun getBreakingNews(
        @Query("apiKey")
        apiKey: String = API_KEY,
        @Query("country")
        countyCode: String = "us",
        @Query("page")
        pageNumber: Int = 1
    ): Response<NewsResponse>

    @GET("v2/everything")
    suspend fun searchForNews(
        @Query("apiKey")
        apiKey: String = API_KEY,
        @Query("q")
        searchQuery: String,
        @Query("page")
        pageNumber: Int = 1
    ): Response<NewsResponse>
}
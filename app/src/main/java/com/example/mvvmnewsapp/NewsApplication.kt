package com.example.mvvmnewsapp

import android.app.Application
import com.example.mvvmnewsapp.data.database.ArticleDatabase
import com.example.mvvmnewsapp.data.repository.NewsRepository

class NewsApplication : Application() {
    val database: ArticleDatabase by lazy {
        ArticleDatabase.getDatabase(this)
    }

    val newsRepository: NewsRepository by lazy {
        NewsRepository(database)
    }
}
package com.example.mvvmnewsapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "article")
data class Article(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val author: String,
    val content: String,
    val description: String,
    @ColumnInfo(name = "published_at")
    val publishedAt: String,
    val source: Source,
    val title: String,
    val url: String,
    @ColumnInfo(name = "url_to_image")
    val urlToImage: String
)
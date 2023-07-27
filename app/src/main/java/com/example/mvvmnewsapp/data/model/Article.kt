package com.example.mvvmnewsapp.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

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
) : Serializable {
    override fun hashCode(): Int {
        var result = id.hashCode()
        if (url.isEmpty()) {
            result = 31 * result + url.hashCode()
        }
        return result
    }
}
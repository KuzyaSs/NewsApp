package com.example.mvvmnewsapp.ui.viewModel

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_ETHERNET
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mvvmnewsapp.NewsApplication
import com.example.mvvmnewsapp.data.model.Article
import com.example.mvvmnewsapp.data.model.NewsResponse
import com.example.mvvmnewsapp.data.repository.NewsRepository
import com.example.mvvmnewsapp.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.Response

class NewsViewModel(
    application: NewsApplication,
    private val newsRepository: NewsRepository
) : AndroidViewModel(application) {
    val breakingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var breakingNewsPage = 1
    private var breakingNewsResponse: NewsResponse? = null

    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse: NewsResponse? = null

    val savedArticles: LiveData<List<Article>> = newsRepository.getSavedArticles()

    init {
        getBreakingNews("us")
    }

    fun getBreakingNews(countryCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            safeBreakingNewsCall(countryCode)
        }
    }

    private suspend fun safeBreakingNewsCall(countryCode: String) {
        breakingNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.getBreakingNews(countryCode, breakingNewsPage)
                breakingNews.postValue(handleBreakingNewsResponse(response))
            } else {
                breakingNews.postValue(Resource.Error(message = "No internet connection"))
            }
        } catch (throwable: Throwable) {
            when(throwable) {
                is IOException -> breakingNews.postValue(Resource.Error(message = "Network failure"))
                else -> breakingNews.postValue(Resource.Error(message = "Conversion error"))
            }
        }
    }

    fun getSearchNews(searchQuery: String) {
        viewModelScope.launch(Dispatchers.IO) {
            safeSearchNewsCall(searchQuery)
        }
    }

    private suspend fun safeSearchNewsCall(searchQuery: String) {
        searchNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.searchNews(searchQuery, searchNewsPage)
                searchNews.postValue(handleSearchNewsResponse(response))
            } else {
                searchNews.postValue(Resource.Error(message = "No internet connection"))
            }
        } catch (throwable: Throwable) {
            when(throwable) {
                is IOException -> searchNews.postValue(Resource.Error(message = "Network failure"))
                else -> searchNews.postValue(Resource.Error(message = "Conversion error"))
            }
        }
    }

    private fun handleBreakingNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { newsResponse ->
                breakingNewsPage++
                if (breakingNewsResponse == null) {
                    breakingNewsResponse = newsResponse
                } else {
                    val newArticles = newsResponse.articles
                    breakingNewsResponse?.articles?.addAll(newArticles)
                }
                return Resource.Success(breakingNewsResponse ?: newsResponse)
            }
        }
        return Resource.Error(message = response.message())
    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { newsResponse ->
                searchNewsPage++
                if (searchNewsResponse == null) {
                    searchNewsResponse = newsResponse
                } else {
                    val newArticles = newsResponse.articles
                    searchNewsResponse?.articles?.addAll(newArticles)
                }
                return Resource.Success(searchNewsResponse ?: newsResponse)
            }
        }
        return Resource.Error(message = response.message())
    }

    fun saveArticle(article: Article) {
        viewModelScope.launch(Dispatchers.IO) {
            newsRepository.insertArticle(article)
        }
    }

    fun updateArticle(article: Article) {
        viewModelScope.launch(Dispatchers.IO) {
            newsRepository.updateArticle(article)
        }
    }

    fun deleteArticle(article: Article) {
        viewModelScope.launch(Dispatchers.IO) {
            newsRepository.deleteArticle(article)
        }
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<NewsApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(TRANSPORT_WIFI) -> true
            capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    class NewsViewModelFactory(
        private val application: NewsApplication,
        private val newsRepository: NewsRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NewsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NewsViewModel(application, newsRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
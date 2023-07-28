package com.example.mvvmnewsapp.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvmnewsapp.NewsApplication
import com.example.mvvmnewsapp.databinding.FragmentSearchNewsBinding
import com.example.mvvmnewsapp.ui.adapter.NewsAdapter
import com.example.mvvmnewsapp.ui.viewModel.NewsViewModel
import com.example.mvvmnewsapp.util.Constants
import com.example.mvvmnewsapp.util.Constants.Companion.SEARCH_NEWS_TIME_DELAY
import com.example.mvvmnewsapp.util.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchNewsFragment : Fragment() {
    private var _binding: FragmentSearchNewsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NewsViewModel by activityViewModels {
        NewsViewModel.NewsViewModelFactory(
            (activity?.application as NewsApplication),
            (activity?.application as NewsApplication).newsRepository
        )
    }

    private lateinit var newsAdapter: NewsAdapter

    private var isScrolling = false
    private var isLoading = false
    private var isLastPage = false

    private val TAG = "SearchNewsFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewSetup()

        var job: Job? = null
        binding.editTextSearch.addTextChangedListener { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(SEARCH_NEWS_TIME_DELAY)
                editable?.let {
                    if (editable.toString().isNotBlank()) {
                        viewModel.searchNewsResponse = null
                        viewModel.searchNewsPage = 1
                        viewModel.getSearchNews(editable.toString())
                    }
                }
            }
        }

        viewModel.searchNews.observe(viewLifecycleOwner) { resourceResponse ->
            when(resourceResponse) {
                is Resource.Success -> {
                    hideProgressBar()
                    resourceResponse.data?.let { newsResponse ->
                        newsAdapter.submitList(newsResponse.articles)

                        val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.searchNewsPage == totalPages

                        if (isLastPage) {
                            binding.recyclerViewSearchNews.setPadding(0, 0, 0, 0)
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    resourceResponse.message?.let { errorMessage ->
                        Toast.makeText(context, "An error occurred: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
    }

    private fun recyclerViewSetup() {
        newsAdapter = NewsAdapter { article ->
            val action = SearchNewsFragmentDirections.actionSearchNewsFragmentToArticleFragment(article)
            findNavController().navigate(action)
        }

        binding.recyclerViewSearchNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(scrollListenerSetup())
        }
    }

    private fun scrollListenerSetup() : RecyclerView.OnScrollListener {
        val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = binding.recyclerViewSearchNews.layoutManager as LinearLayoutManager
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val totalVisibleItems = layoutManager.childCount
                val totalItems = layoutManager.itemCount
                val isAtLastItem = firstVisibleItemPosition >= totalItems - totalVisibleItems

                if (isScrolling && !isLoading && !isLastPage && isAtLastItem) {
                    viewModel.getSearchNews(binding.editTextSearch.text.toString())
                    isScrolling = false
                }
            }
        }
        return scrollListener
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
        isLoading = true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        Log.e("TAG", "onDestroy")
    }
}
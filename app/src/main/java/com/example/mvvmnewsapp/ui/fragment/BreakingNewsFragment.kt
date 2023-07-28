package com.example.mvvmnewsapp.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.example.mvvmnewsapp.NewsApplication
import com.example.mvvmnewsapp.R
import com.example.mvvmnewsapp.databinding.FragmentBreakingNewsBinding
import com.example.mvvmnewsapp.ui.adapter.NewsAdapter
import com.example.mvvmnewsapp.ui.viewModel.NewsViewModel
import com.example.mvvmnewsapp.util.Constants.Companion.QUERY_PAGE_SIZE
import com.example.mvvmnewsapp.util.Resource

class BreakingNewsFragment : Fragment() {
    private var _binding: FragmentBreakingNewsBinding? = null
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

    private val TAG = "BreakingNewsFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBreakingNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewSetup()

        viewModel.breakingNews.observe(viewLifecycleOwner) { resourceResponse ->
            when(resourceResponse) {
                is Resource.Success -> {
                    hideProgressBar()
                    resourceResponse.data?.let { newsResponse ->
                        newsAdapter.submitList(newsResponse.articles)

                        val totalPages = newsResponse.totalResults / QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.breakingNewsPage == totalPages

                        if (isLastPage) {
                            binding.recyclerViewBreakingNews.setPadding(0, 0, 0, 0)
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
            val action = BreakingNewsFragmentDirections.actionBreakingNewsFragmentToArticleFragment(article)
            findNavController().navigate(action)
        }

        binding.recyclerViewBreakingNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(scrollListenerSetup())
        }
    }

    private fun scrollListenerSetup() : OnScrollListener {
        val scrollListener = object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = binding.recyclerViewBreakingNews.layoutManager as LinearLayoutManager
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val totalVisibleItems = layoutManager.childCount
                val totalItems = layoutManager.itemCount
                val isAtLastItem = firstVisibleItemPosition >= totalItems - totalVisibleItems

                if (isScrolling && !isLoading && !isLastPage && isAtLastItem) {
                    viewModel.getBreakingNews("us")
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
    }
}
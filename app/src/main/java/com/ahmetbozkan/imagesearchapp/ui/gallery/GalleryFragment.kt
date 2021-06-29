package com.ahmetbozkan.imagesearchapp.ui.gallery

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.ahmetbozkan.imagesearchapp.R
import com.ahmetbozkan.imagesearchapp.data.UnsplashPhoto
import com.ahmetbozkan.imagesearchapp.databinding.FragmentGalleryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GalleryFragment : Fragment(R.layout.fragment_gallery), UnsplashPhotoAdapter.OnItemClickListener {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GalleryViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGalleryBinding.bind(view)

        val adapter = UnsplashPhotoAdapter(this)

        binding.apply {
            recyclerViewGallery.setHasFixedSize(true)
            recyclerViewGallery.itemAnimator = null
            recyclerViewGallery.adapter = adapter.withLoadStateHeaderAndFooter(
                header = UnsplashPhotoLoadStateAdapter { adapter.retry() },
                footer = UnsplashPhotoLoadStateAdapter { adapter.retry() }
            )

            buttonRetry.setOnClickListener {
                adapter.retry()
            }
        }

        viewModel.photos.observe(viewLifecycleOwner) {
            adapter.submitData(viewLifecycleOwner.lifecycle, it)
        }

        adapter.addLoadStateListener { loadstate ->
            binding.apply {
                progressBarLoadingPhotos.isVisible = loadstate.source.refresh is LoadState.Loading
                recyclerViewGallery.isVisible = loadstate.source.refresh is LoadState.NotLoading
                buttonRetry.isVisible = loadstate.source.refresh is LoadState.Error
                textViewError.isVisible = loadstate.source.refresh is LoadState.Error

                //Empty view (NO RESULT)
                if (loadstate.source.refresh is LoadState.NotLoading &&
                    loadstate.append.endOfPaginationReached &&
                    adapter.itemCount < 1
                ) {
                    recyclerViewGallery.isVisible = false
                    textViewEmptyResult.isVisible = true
                } else {
                    textViewEmptyResult.isVisible = false
                }
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onItemClick(photo: UnsplashPhoto) {
        val action = GalleryFragmentDirections.actionGalleryFragmentToDetailsFragment(
            photo, photo.user.username
        )
        findNavController().navigate(action)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.menu_gallery, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    binding.recyclerViewGallery.scrollToPosition(0)
                    viewModel.searchPhotos(query)
                    searchView.clearFocus()
                }

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
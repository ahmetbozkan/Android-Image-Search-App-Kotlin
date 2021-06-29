package com.ahmetbozkan.imagesearchapp.api

import com.ahmetbozkan.imagesearchapp.data.UnsplashPhoto

data class UnsplashResponse(
    val results: List<UnsplashPhoto>
)

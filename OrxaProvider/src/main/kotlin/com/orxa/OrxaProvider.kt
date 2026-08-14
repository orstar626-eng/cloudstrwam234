package com.orxa

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor

class OrxaProvider : MainAPI() {
    override var mainUrl = "https://orxa.vercel.app"
    override var name = "ORXA"
    override var lang = "ar"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
    override val hasMainPage = true

    data class Item(
        val id: String,
        val providerId: String? = null,
        val title: String? = null,
        val titleEn: String? = null,
        val overview: String? = null,
        val poster: String? = null,
        val backdrop: String? = null,
        val year: Int? = null,
        val rating: Double? = null,
        val genres: List<String> = emptyList()
    )

    data class ResultsResponse(val results: List<Item> = emptyList())
    data class DetailResponse(val details: Item)
    data class MoviesResponse(val rows: Map<String, List<Item>> = emptyMap())
    data class Season(val seasonNumber: Int, val episodeCount: Int, val name: String? = null)
    data class SeriesDetails(
        val id: String,
        val providerId: String? = null,
        val title: String? = null,
        val titleEn: String? = null,
        val overview: String? = null,
        val poster: String? = null,
        val backdrop: String? = null,
        val year: Int? = null,
        val genres: List<String> = emptyList(),
        val seasons: List<Season> = emptyList()
    )
    data class SeriesDetailResponse(val details: SeriesDetails)

    private fun Item.isSeries() = providerId == "vidsrc-tv" || id.startsWith("vidsrc-tv:")
    private fun Item.displayTitle() = title ?: titleEn ?: "بدون عنوان"

    private fun Item.toSearchResponse(): SearchResponse {
        return if (isSeries()) {
            newTvSeriesSearchResponse(displayTitle(), id, TvType.TvSeries) {
                posterUrl = poster
                year = this@toSearchResponse.year
            }
        } else {
            newMovieSearchResponse(displayTitle(), id, TvType.Movie) {
                posterUrl = poster
                year = this@toSearchResponse.year
            }
        }
    }


    override suspend fun search(query: String): List<SearchResponse> {
        val response = app.get("$mainUrl/api/movies?lang=ar").parsed<MoviesResponse>()
        val q = query.trim().lowercase()
        return response.rows.values
            .flatten()
            .distinctBy { it.id }
            .filter {
                val text = "${it.title.orEmpty()} ${it.titleEn.orEmpty()} ${it.overview.orEmpty()}".lowercase()
                q.isNotBlank() && text.contains(q)
            }
            .map { it.toSearchResponse() }
    }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        if (page > 1) return HomePageResponse(emptyList())
        val response = app.get("$mainUrl/api/movies?lang=ar").parsed<MoviesResponse>()
        val sections = response.rows.mapNotNull { (name, items) ->
            val cards = items.map { it.toSearchResponse() }
            if (cards.isEmpty()) null else HomePageList(name, cards, isHorizontalImages = true)
        }
        return HomePageResponse(sections)
    }

    override suspend fun load(url: String): LoadResponse {
        val id = url.substringAfterLast("/")
        val response = app.get("$mainUrl/api/movie?id=${id.urlEncode()}&lang=ar")

        if (id.startsWith("vidsrc-tv:")) {
            val details = response.parsed<SeriesDetailResponse>().details
            val episodes = details.seasons.flatMap { season ->
                (1..season.episodeCount).map { episode ->
                    newEpisode("${id}|${season.seasonNumber}|$episode") {
                        name = "الحلقة $episode"
                        season = season.seasonNumber
                        episode = episode
                        posterUrl = details.poster
                    }
                }
            }
            return newTvSeriesLoadResponse(
                details.title ?: details.titleEn ?: "بدون عنوان",
                id,
                TvType.TvSeries,
                episodes
            ) {
                plot = details.overview
                posterUrl = details.poster
                backgroundPosterUrl = details.backdrop
                year = details.year
                tags = details.genres
            }
        }

        val details = response.parsed<DetailResponse>().details
        return newMovieLoadResponse(
            details.title ?: details.titleEn ?: "بدون عنوان",
            id,
            TvType.Movie,
            id
        ) {
            plot = details.overview
            posterUrl = details.poster
            backgroundPosterUrl = details.backdrop
            year = details.year
            tags = details.genres
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val parts = data.split('|')
        val id = parts[0].removePrefix("vidsrc-movie:").removePrefix("vidsrc-tv:")
        val sourceUrl = if (parts.size >= 3) {
            "https://vidsrc.to/embed/tv/$id/${parts[1]}/${parts[2]}"
        } else {
            "https://vidsrc.to/embed/movie/$id"
        }
        return loadExtractor(sourceUrl, subtitleCallback, callback)
    }
}

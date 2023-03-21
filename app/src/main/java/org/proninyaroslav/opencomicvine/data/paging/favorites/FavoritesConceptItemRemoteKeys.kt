package org.proninyaroslav.opencomicvine.data.paging.favorites

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.proninyaroslav.opencomicvine.data.paging.ComicVineRemoteKeys

@Entity
data class FavoritesConceptItemRemoteKeys(
    @PrimaryKey
    override val id: Int,
    override val prevOffset: Int?,
    override val nextOffset: Int?,
) : ComicVineRemoteKeys
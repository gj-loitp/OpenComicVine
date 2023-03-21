package org.proninyaroslav.opencomicvine.data.item

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow
import org.proninyaroslav.opencomicvine.data.LocationInfo
import org.proninyaroslav.opencomicvine.model.repo.FavoriteFetchResult

@Immutable
class LocationItem(
    val info: LocationInfo,
    override val isFavorite: Flow<FavoriteFetchResult>,
) : BaseItem {
    override val id: Int
        get() = info.id

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocationItem

        if (info != other.info) return false

        return true
    }

    override fun hashCode(): Int = info.hashCode()


    override fun toString(): String =
        "LocationItem(info=$info, isFavorite=$isFavorite, id=$id)"
}
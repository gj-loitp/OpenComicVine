package org.proninyaroslav.opencomicvine.model.repo.paging

import androidx.paging.PagingSource
import org.proninyaroslav.opencomicvine.data.paging.ComicVinePagingItem
import org.proninyaroslav.opencomicvine.data.paging.ComicVineRemoteKeys
import java.io.IOException

interface ComicVinePagingRepository<Item : ComicVinePagingItem, RemoteKey : ComicVineRemoteKeys> {
    suspend fun saveItems(
        items: List<Item>,
        remoteKeys: List<RemoteKey>,
        clearBeforeSave: Boolean,
    ): Result<Unit>

    suspend fun getRemoteKeysById(id: Int): Result<RemoteKey?>

    suspend fun getItemById(id: Int): Result<Item?>

    fun getAllSavedItems(): PagingSource<Int, Item>

    fun getSavedItems(count: Int): PagingSource<Int, Item>

    sealed interface Result<out T> {
        data class Success<T>(val value: T) : Result<T>

        sealed interface Failed : Result<Nothing> {
            data class IO(val exception: IOException) : Failed
        }
    }
}
package dev.sasikanth.pinnit.data.source

import androidx.lifecycle.LiveData
import dev.sasikanth.pinnit.data.NotifItem
import dev.sasikanth.pinnit.data.Result
import dev.sasikanth.pinnit.data.source.local.NotifLocalDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotifRepository
@Inject constructor(
    private val notifDataSource: NotifLocalDataSource
) {

    fun getNotifs(): LiveData<List<NotifItem>> {
        return notifDataSource.getNotifs()
    }

    fun getPinnedNotifs(): LiveData<List<NotifItem>> {
        return notifDataSource.getPinnedNotifs()
    }

    suspend fun getNotif(id: Long): Result<NotifItem> {
        return notifDataSource.getNotif(id)
    }

    suspend fun saveNotif(notifItem: NotifItem): Long {
        val lastItemByKey = notifDataSource.getNotif(notifItem.notifKey)
        if (lastItemByKey != null) {
            val isItemMatch = notifItem.equalsLastItem(lastItemByKey)
            if (isItemMatch) {
                return 0
            }
        }
        return notifDataSource.saveNotif(notifItem)
    }

    suspend fun updateNotif(notifItem: NotifItem) {
        notifDataSource.updateNotif(notifItem)
    }

    suspend fun pinNotif(id: Long) {
        notifDataSource.pinNotif(id)
    }

    suspend fun unPinNotif(id: Long) {
        notifDataSource.unPinNotif(id)
    }

    suspend fun deleteNotif(id: Long) = notifDataSource.deleteNotif(id)

    suspend fun deleteAllNotifs() {
        notifDataSource.deleteAllNotifs()
    }

    suspend fun deleteUnPinnedNotifs() {
        notifDataSource.deleteUnPinnedNotifs()
    }
}
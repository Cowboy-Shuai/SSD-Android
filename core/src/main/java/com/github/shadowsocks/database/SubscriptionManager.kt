//region SSD
package com.github.shadowsocks.database

import android.database.sqlite.SQLiteCantOpenDatabaseException
import com.github.shadowsocks.preference.DataStore
import com.github.shadowsocks.utils.DirectBoot
import com.github.shadowsocks.utils.printLog
import java.io.IOException
import java.sql.SQLException

object SubscriptionManager {
    interface Listener {
        fun onAdd(subscription: Subscription)
        fun onRemove(subscriptionId: Long)
    }

    var listener: Listener? = null

    @Throws(SQLException::class)
    fun createSubscription(subscription: Subscription = Subscription()): Subscription {
        subscription.id = PrivateDatabase.subscriptionDao.create(subscription)
        listener?.onAdd(subscription)
        return subscription
    }

    /**
     * Note: It's caller's responsibility to update DirectBoot profile if necessary.
     */
    @Throws(SQLException::class)
    fun updateSubscription(subscription: Subscription) = check(PrivateDatabase.subscriptionDao.update(subscription) == 1)

    @Throws(IOException::class)
    fun getSubscription(id: Long): Subscription? = try {
        PrivateDatabase.subscriptionDao[id]
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        printLog(ex)
        null
    }

    @Throws(SQLException::class)
    fun delSubscriptionWithProfiles(id: Long) {
        check(PrivateDatabase.subscriptionDao.delete(id) == 1)
        PrivateDatabase.profileDao.deleteSubscription(id)
        //ProfilesFragment.instance?.subscriptionsAdapter?.removeSubscriptionId(id)
        listener?.onRemove(id)
        if (id ==ProfileManager.getProfile(DataStore.profileId)?.subscription && DataStore.directBootAware) DirectBoot.clean()
    }

    @Throws(IOException::class)
    fun isNotEmpty(): Boolean = try {
        PrivateDatabase.subscriptionDao.isNotEmpty()
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        printLog(ex)
        false
    }

    @Throws(IOException::class)
    fun getAllSubscriptions(): List<Subscription>? = try {
        PrivateDatabase.subscriptionDao.list()
    } catch (ex: SQLiteCantOpenDatabaseException) {
        throw IOException(ex)
    } catch (ex: SQLException) {
        printLog(ex)
        null
    }
}
//endregion
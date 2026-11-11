package com.lagradost.cloudstream3

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

/**
 * Stub ContentProvider for authority "androidx.car.app.connection".
 * Resolves the CarConnection query made by Media3/media sessions on devices or emulators
 * where Android Auto is not installed, eliminating "Failed to find provider info for androidx.car.app.connection".
 */
class CarConnectionProvider : ContentProvider() {
    companion object {
        const val COLUMN_CAR_CONNECTION_STATE = "CarConnectionState"
        const val CONNECTION_TYPE_NOT_CONNECTED = 0
    }

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val cursor = MatrixCursor(arrayOf(COLUMN_CAR_CONNECTION_STATE))
        cursor.addRow(arrayOf(CONNECTION_TYPE_NOT_CONNECTED))
        return cursor
    }

    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0
}

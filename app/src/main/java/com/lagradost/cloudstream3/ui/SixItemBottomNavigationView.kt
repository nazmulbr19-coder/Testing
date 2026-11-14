package com.lagradost.cloudstream3.ui

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Allows up to 7 destinations in BottomNavigationView so the offline Tetris
 * button sits directly next to Downloads before Settings on phones too.
 */
class SixItemBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.bottomNavigationStyle,
    defStyleRes: Int = com.google.android.material.R.style.Widget_Design_BottomNavigationView
) : BottomNavigationView(context, attrs, defStyleAttr, defStyleRes) {

    override fun getMaxItemCount(): Int = 7

    override fun getCollapsedMaxItemCount(): Int = 7
}

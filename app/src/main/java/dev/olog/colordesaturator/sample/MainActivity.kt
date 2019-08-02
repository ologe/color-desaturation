package dev.olog.colordesaturator.sample

import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import dev.olog.lib.DarkDesaturatedResources
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        @JvmStatic
        var isDarkMode = false
    }

    private var customResources: Resources? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setDarkMode()

        darkModeFab?.setOnClickListener {
            isDarkMode = !isDarkMode
            setDarkMode()
        }
    }

    private fun setDarkMode() {
        val flag = if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
        else AppCompatDelegate.MODE_NIGHT_NO

        AppCompatDelegate.setDefaultNightMode(flag)
    }

    override fun getResources(): Resources {
        val res = super.getResources()
        if (customResources == null) {
            val isDarkMode = res.getBoolean(R.bool.is_dark_mode)
            customResources = DarkDesaturatedResources(
                isDarkMode, .5f, .5f,
                res.assets, res.displayMetrics, res.configuration
            )
        }
        return customResources!!
    }

}

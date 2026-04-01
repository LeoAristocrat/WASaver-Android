package com.leo.wasaver

import android.app.Application

class WASaverApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: WASaverApplication
            private set
    }
}


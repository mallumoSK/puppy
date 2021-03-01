package tk.mallumo.puppy

import android.app.Application

val app: MainApplication get() = MainApplication.instance

class MainApplication : Application() {

    companion object {
        lateinit var instance: MainApplication
    }

    override fun onCreate() {
        instance = this
        super.onCreate()
    }
}
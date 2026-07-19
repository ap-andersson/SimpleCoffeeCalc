package se.andynet.simplecoffeecalc

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class CoffeeApp : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}

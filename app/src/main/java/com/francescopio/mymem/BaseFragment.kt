package com.francescopio.mymem

import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/*
Si inserisce all'interno della BaseFragment che fa da base a tutti i nostri Fragment, la CoroutineScope(li estende anche).
Le Coroutine aiutano a gestire attività di lunga durata che potrebbero bloccare il Thread principale ed
impedire la risposta dell'app. CoroutineScope ha il compito di generare nuovi "scopi" per nuove coroutine.
 */

abstract class BaseFragment:Fragment(),CoroutineScope{

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
    get() = job + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
    }

    override fun onDestroy(){
        super.onDestroy()
        job.cancel()
    }
}
package dev.sasikanth.pinnit.di

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.work.ListenableWorker

interface ComponentProvider {
  val component: AppComponent
}

val Context.injector get() = (applicationContext as ComponentProvider).component
val FragmentActivity.injector get() = (application as ComponentProvider).component
val Fragment.injector get() = (requireActivity().application as ComponentProvider).component
val ListenableWorker.injector get() = (applicationContext as ComponentProvider).component

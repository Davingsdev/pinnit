package dev.sasikanth.pinnit.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dev.sasikanth.pinnit.notifications.NotificationsRepositoryAndroidTest
import dev.sasikanth.pinnit.worker.ScheduleWorkerTest

@AppScope
@Component(modules = [TestAppModule::class])
interface TestAppComponent : AppComponent {
  @Component.Factory
  interface Factory {
    fun create(@BindsInstance application: Application): TestAppComponent
  }

  fun inject(target: NotificationsRepositoryAndroidTest)
  fun inject(target: ScheduleWorkerTest)
}

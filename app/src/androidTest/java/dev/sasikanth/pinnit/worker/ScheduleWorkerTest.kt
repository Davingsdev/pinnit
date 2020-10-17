package dev.sasikanth.pinnit.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import dev.sasikanth.pinnit.TestData
import dev.sasikanth.pinnit.TestPinnitApp
import dev.sasikanth.pinnit.data.AppDatabase
import dev.sasikanth.pinnit.data.ScheduleType
import dev.sasikanth.pinnit.notifications.NotificationRepository
import dev.sasikanth.pinnit.utils.TestUserClock
import dev.sasikanth.pinnit.utils.TestUtcClock
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class ScheduleWorkerTest {

  @Inject
  lateinit var repository: NotificationRepository

  @Inject
  lateinit var userClock: TestUserClock

  @Inject
  lateinit var utcClock: TestUtcClock

  @Inject
  lateinit var appDatabase: AppDatabase

  private lateinit var context: Context

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    (context as TestPinnitApp).component.inject(this)

    userClock.setDate(LocalDate.parse("2018-01-01"))
    utcClock.setDate(LocalDate.parse("2018-01-02"))
  }

  @After
  fun tearDown() {
    appDatabase.clearAllTables()
  }

  @Test
  fun test_schedule_with_repeat_set() = runBlocking {
    // given
    val scheduleDate = LocalDate.parse("2020-01-02")
    val scheduleTime = LocalTime.parse("09:00:00")

    val notificationUuid = UUID.fromString("b64fdba2-6b0b-4e83-bc06-aabc992d13cb")
    val inputData = workDataOf(
      ScheduleWorker.INPUT_NOTIFICATION_UUID to notificationUuid.toString()
    )
    val worker = TestListenableWorkerBuilder<ScheduleWorker>(
      context = context,
      inputData = inputData
    ).build()

    val notification = TestData.notification(
      uuid = notificationUuid,
      createdAt = Instant.now(utcClock),
      updatedAt = Instant.now(utcClock),
      deletedAt = null,
      isPinned = true,
      scheduleDate = scheduleDate,
      scheduleTime = scheduleTime,
      scheduleType = ScheduleType.Daily
    )

    repository.save(listOf(notification))

    // when

    // Advance to next day before doing work to check if it's rescheduling on success result
    userClock.advanceBy(Duration.ofDays(1L))
    utcClock.advanceBy(Duration.ofDays(1L))

    val result = worker.doWork()

    // then
    val expectedSchedule = notification.schedule?.copy(
      scheduleDate = LocalDate.parse("2020-01-03"),
      scheduleTime = LocalTime.parse("09:00:00")
    )

    assertThat(result).isEqualTo(ListenableWorker.Result.success())
    assertThat(repository.notification(notificationUuid)).isEqualTo(
      notification.copy(
        updatedAt = Instant.parse("2018-01-03T00:00:00Z"),
        schedule = expectedSchedule
      )
    )
  }

  @Test
  fun test_schedule_with_repeat_not_set() = runBlocking {
    // given
    val scheduleDate = LocalDate.parse("2020-01-01")
    val scheduleTime = LocalTime.parse("09:00:00")

    val notificationUuid = UUID.fromString("59b815da-ae11-4ef3-99b3-efbc2ff99bad")
    val inputData = workDataOf(
      ScheduleWorker.INPUT_NOTIFICATION_UUID to notificationUuid.toString()
    )
    val worker = TestListenableWorkerBuilder<ScheduleWorker>(
      context = context,
      inputData = inputData
    ).build()

    val notification = TestData.notification(
      uuid = notificationUuid,
      createdAt = Instant.now(utcClock),
      updatedAt = Instant.now(utcClock),
      deletedAt = null,
      isPinned = true,
      scheduleDate = scheduleDate,
      scheduleTime = scheduleTime,
      scheduleType = null
    )

    repository.save(listOf(notification))

    // when

    // Advance to next day before doing work to check if it's rescheduling on success result
    userClock.advanceBy(Duration.ofDays(1L))
    utcClock.advanceBy(Duration.ofDays(1L))

    val result = worker.doWork()

    // then
    assertThat(result).isEqualTo(ListenableWorker.Result.success())
  }

  @Test
  fun test_schedule_worker_fail() = runBlocking {
    // given
    val inputData = workDataOf(
      ScheduleWorker.INPUT_NOTIFICATION_UUID to "wrong-uuid-string"
    )
    val worker = TestListenableWorkerBuilder<ScheduleWorker>(
      context = context,
      inputData = inputData
    ).build()

    // when
    val result = worker.doWork()

    // then
    assertThat(result).isEqualTo(ListenableWorker.Result.failure())
  }
}

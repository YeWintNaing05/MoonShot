package com.haroldadmin.moonshot_repository.launches

import com.haroldadmin.cnradapter.NetworkResponse
import com.haroldadmin.moonshot.core.Resource
import com.haroldadmin.moonshot.database.launch.LaunchDao
import com.haroldadmin.moonshot.database.launch.rocket.RocketSummaryDao
import com.haroldadmin.moonshot.database.launch.rocket.first_stage.FirstStageSummaryDao
import com.haroldadmin.moonshot.database.launch.rocket.second_stage.SecondStageSummaryDao
import com.haroldadmin.moonshot_repository.launch.LaunchesRepository
import com.haroldadmin.moonshot_repository.mappers.toDbLaunch
import com.haroldadmin.moonshot_repository.mappers.toDbRocketSummary
import com.haroldadmin.spacex_api_wrapper.common.ErrorResponse
import com.haroldadmin.spacex_api_wrapper.launches.*
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.DescribeSpec
import io.mockk.*
import kotlinx.coroutines.CompletableDeferred
import java.io.IOException
import com.haroldadmin.moonshot.models.launch.Launch as DbLaunch
import com.haroldadmin.moonshot.models.launch.rocket.RocketSummary as DbRocketSummary

internal class LaunchesRepositoryTest : DescribeSpec() {

    private val sampleData = listOf<DbLaunch>()
    private val launchDao: LaunchDao = spyk(FakeLaunchesDao())
    private val rocketSummaryDao: RocketSummaryDao = spyk(FakeRocketSummariesDao())
    private val firstStageSummaryDao: FirstStageSummaryDao = spyk(FakeFirstStageSummaryDao())
    private val secondStageSummaryDao: SecondStageSummaryDao = spyk(FakeSecondStageSummaryDao())
    private val launchesService = mockk<LaunchesService>()
    private val repository =
        LaunchesRepository(launchDao, rocketSummaryDao, firstStageSummaryDao, secondStageSummaryDao, launchesService)

    init {

        describe("Launches Repository") {
            context("Get all launches successfully") {

                every { launchesService.getAllLaunches() } returns CompletableDeferred(
                    NetworkResponse.Success(listOf(), null)
                )

                val launches = repository.getAllLaunches()

                it("Should call the api launchesService") {
                    verify { launchesService.getAllLaunches() }
                }

                it("Should call the launchDao") {
                    coVerify {
                        launchDao.getAllLaunches()
                    }
                }

                it("Should return Resource.Success") {
                    launches.shouldBeTypeOf<Resource.Success<List<DbLaunch>>>()
                }

                it("Should return $sampleData") {
                    (launches as Resource.Success<List<DbLaunch>>).data shouldHaveSize 0
                }
            }

            context("Get all launches with network error") {

                every { launchesService.getAllLaunches() } returns CompletableDeferred(
                    NetworkResponse.NetworkError(IOException())
                )

                val launches = repository.getAllLaunches()

                it("Should call the api launchesService") {
                    verify { launchesService.getAllLaunches() }
                }

                it("Should call the launchDao") {
                    coVerify { launchDao.getAllLaunches() }
                }

                it("Should return Resource.Error") {
                    launches.shouldBeTypeOf<Resource.Error<List<DbLaunch>, ErrorResponse>>()
                }

                it("Should have an $sampleData as data") {
                    (launches as Resource.Error<List<DbLaunch>, *>).data shouldNotBe null
                }

                it("Should have the same error as was generated by the network response") {
                    (launches as Resource.Error<List<DbLaunch>, *>).error.shouldBeTypeOf<IOException>()
                }
            }

            context("Get all launches with server error") {

                val errorBody = null
                val responseCode = 404
                every { launchesService.getAllLaunches() } returns CompletableDeferred(
                    NetworkResponse.ServerError(errorBody, responseCode)
                )

                val launches = repository.getAllLaunches()

                it("Should call the api launchesService") {
                    verify { launchesService.getAllLaunches() }
                }

                it("Should call the launchDao") {
                    coVerify { launchDao.getAllLaunches() }
                }

                it("Should return Resource.Error") {
                    launches.shouldBeTypeOf<Resource.Error<List<DbLaunch>, ErrorResponse>>()
                }

                it("Should have $sampleData as data") {
                    with(launches as Resource.Error<List<DbLaunch>, *>) {
                        data shouldBe sampleData
                        error shouldBe null
                    }
                }
            }

            context("Get next launch with server error") {
                val errorBody = null
                val responseCode = 404

                every { launchesService.getNextLaunch() } returns CompletableDeferred(
                    NetworkResponse.ServerError(errorBody, responseCode)
                )

                val launch = repository.getNextLaunch(0L)

                it("Should call the api launchesService") {
                    verify { launchesService.getNextLaunch() }
                }

                it("Should return Resource.Error") {
                    launch.shouldBeTypeOf<Resource.Error<DbLaunch, ErrorResponse>>()
                }
            }
        }
    }
}
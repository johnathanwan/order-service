package com.polarbookshop.orderservice.order.domain

import com.polarbookshop.orderservice.config.*
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.*
import org.springframework.boot.test.autoconfigure.data.r2dbc.*
import org.springframework.context.annotation.*
import org.springframework.test.context.*
import org.testcontainers.containers.*
import org.testcontainers.junit.jupiter.*
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.*
import reactor.test.*

@DataR2dbcTest
@Import(DataConfig::class)
@Testcontainers
class OrderRepositoryR2dbcTests {

    @Autowired
    lateinit var orderRepository: OrderRepository

    companion object {
        @Container
        val postgresql = PostgreSQLContainer(DockerImageName.parse("postgres:15.1"))

        @DynamicPropertySource
        fun postgresqlProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.r2dbc.url", OrderRepositoryR2dbcTests::r2dbcUrl)
            registry.add("spring.r2dbc.username", postgresql::getUsername)
            registry.add("spring.r2dbc.password", postgresql::getPassword)
            registry.add("spring.flyway.url", postgresql::getJdbcUrl)
        }

        private fun r2dbcUrl() = String.format(
            "r2dbc:postgresql://%s:%s/%s",
            postgresql.host,
            postgresql.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
            postgresql.databaseName
        )
    }

    @Test
    fun createRejectedOrder() {
        val rejectedOrder = OrderService.buildRejectedOrder("1234567890", 3)
        StepVerifier.create(orderRepository.save(rejectedOrder)).expectNextMatches { it.status == OrderStatus.REJECTED }
            .verifyComplete()
    }

}
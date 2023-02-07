package com.polarbookshop.orderservice.order.domain

import com.polarbookshop.orderservice.config.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.*
import reactor.test.StepVerifier

@DataR2dbcTest
@Import(DataConfig::class)
@Testcontainers
class OrderRepositoryR2dbcTests(@Autowired val orderRepository: OrderRepository) {
    companion object {
        @Container
        val postgresql = PostgreSQLContainer(DockerImageName.parse("postgres:14.4"))

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
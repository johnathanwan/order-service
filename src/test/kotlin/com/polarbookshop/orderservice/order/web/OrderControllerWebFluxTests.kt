package com.polarbookshop.orderservice.order.web

import com.polarbookshop.orderservice.order.domain.*
import com.polarbookshop.orderservice.order.domain.Order
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.*
import org.mockito.BDDMockito.*
import org.springframework.beans.factory.annotation.*
import org.springframework.boot.test.autoconfigure.web.reactive.*
import org.springframework.boot.test.mock.mockito.*
import org.springframework.test.web.reactive.server.*
import reactor.core.publisher.*

@WebFluxTest(OrderController::class)
class OrderControllerWebFluxTests(@Autowired val webTestClient: WebTestClient) {

    @MockBean
    private lateinit var orderService: OrderService

    @Test
    fun `when book not available then reject order`() {
        val orderRequest = OrderRequest("1234567890", 3)
        val expectedOrder = OrderService.buildRejectedOrder(orderRequest.isbn, orderRequest.quantity)
        given(orderService.submitOrder(orderRequest.isbn, orderRequest.quantity)).willReturn(Mono.just(expectedOrder))


        webTestClient.post().uri("/orders").bodyValue(orderRequest).exchange().expectStatus().is2xxSuccessful
            .expectBody(Order::class.java)
            .value {
                assertThat(it).isNotNull
                assertThat(it.status).isEqualTo(OrderStatus.REJECTED)
            }
    }
}
package com.polarbookshop.orderservice.order.event

import com.polarbookshop.orderservice.order.domain.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.*
import java.util.function.*

@Configuration
class OrderFunctions {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(OrderFunctions::class.java)
    }

    @Bean
    fun dispatchOrder(orderService: OrderService): Consumer<Flux<OrderDispatchedMessage>> = Consumer {
        orderService.consumeOrderDispatchedEvent(it)
            .doOnNext { order -> log.info("The order with id ${order.id} is dispatched") }.subscribe()

    }
}
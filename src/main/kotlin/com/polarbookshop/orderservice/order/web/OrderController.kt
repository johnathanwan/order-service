package com.polarbookshop.orderservice.order.web

import com.polarbookshop.orderservice.order.domain.*
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("orders")
class OrderController(val orderService: OrderService) {

    @GetMapping
    fun getAllOrders(): Flux<Order> = orderService.getAllOrders()

    @PostMapping
    fun submitOrder(@RequestBody @Valid orderRequest: OrderRequest): Mono<Order> =
        orderService.submitOrder(orderRequest.isbn, orderRequest.quantity)
}
package com.polarbookshop.orderservice.order.domain

import com.polarbookshop.orderservice.book.*
import org.springframework.stereotype.*
import reactor.core.publisher.*

@Service
class OrderService(val orderRepository: OrderRepository, val bookClient: BookClient) {

    fun getAllOrders(): Flux<Order> = orderRepository.findAll()

    fun submitOrder(isbn: String, quantity: Int): Mono<Order> =
        bookClient.getBookByIsbn(isbn).map { buildAcceptedOrder(it, quantity) }
            .defaultIfEmpty(buildRejectedOrder(isbn, quantity)).flatMap(orderRepository::save)

    companion object {
        fun buildAcceptedOrder(book: Book, quantity: Int) =
            Order(book.isbn, "${book.title} - ${book.author}", book.price, quantity, OrderStatus.ACCEPTED)

        fun buildRejectedOrder(bookIsbn: String, quantity: Int): Order =
            Order(bookIsbn, null, null, quantity, OrderStatus.REJECTED)
    }
}


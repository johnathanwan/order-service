package com.polarbookshop.orderservice.order.domain

import com.polarbookshop.orderservice.book.*
import com.polarbookshop.orderservice.order.event.*
import org.slf4j.*
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.stereotype.*
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.*

@Service
class OrderService(val orderRepository: OrderRepository, val bookClient: BookClient, val streamBridge: StreamBridge) {

    fun getAllOrders(): Flux<Order> = orderRepository.findAll()

    @Transactional
    fun submitOrder(isbn: String, quantity: Int): Mono<Order> =
        bookClient.getBookByIsbn(isbn).map { buildAcceptedOrder(it, quantity) }
            .defaultIfEmpty(buildRejectedOrder(isbn, quantity)).flatMap(orderRepository::save)
            .doOnNext(::publishOrderAcceptedEvent)

    fun consumeOrderDispatchedEvent(flux: Flux<OrderDispatchedMessage>): Flux<Order> =
        flux.flatMap { message -> orderRepository.findById(message.orderId) }
            .map(::buildDispatchedOrder).flatMap(orderRepository::save)

    private fun buildDispatchedOrder(existingOrder: Order): Order =
        Order(
            existingOrder.bookIsbn,
            existingOrder.bookName,
            existingOrder.bookPrice,
            existingOrder.quantity,
            OrderStatus.DISPATCHED,
            existingOrder.createdDate,
            existingOrder.lastModifiedDate,
            existingOrder.version,
            existingOrder.id
        )

    private fun publishOrderAcceptedEvent(order: Order) {
        if (order.status != OrderStatus.ACCEPTED) return
        val orderAcceptedMessage = OrderAcceptedMessage(order.id!!)
        log.info("Sending order accepted event with id: ${order.id}")
        val result = streamBridge.send("acceptOrder-out-0", orderAcceptedMessage)
        log.info("Result of sending data for order with id ${order.id}: $result")
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(OrderService::class.java)

        @JvmStatic
        fun buildAcceptedOrder(book: Book, quantity: Int) =
            Order(book.isbn, "${book.title} - ${book.author}", book.price, quantity, OrderStatus.ACCEPTED)

        @JvmStatic
        fun buildRejectedOrder(bookIsbn: String, quantity: Int): Order =
            Order(bookIsbn, null, null, quantity, OrderStatus.REJECTED)
    }


}


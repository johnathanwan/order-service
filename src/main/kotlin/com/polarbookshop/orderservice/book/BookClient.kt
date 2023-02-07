package com.polarbookshop.orderservice.book

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.*
import reactor.util.retry.*
import java.time.*

@Component
class BookClient(val webClient: WebClient) {

    fun getBookByIsbn(isbn: String): Mono<Book> =
        webClient.get().uri(BOOKS_ROOT_API + isbn).retrieve().bodyToMono(Book::class.java)
            .timeout(Duration.ofSeconds(3), Mono.empty())
            .onErrorResume(WebClientResponseException.NotFound::class.java) { Mono.empty() }
            .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
            .onErrorResume(Exception::class.java) { Mono.empty() }

    companion object {
        private const val BOOKS_ROOT_API = "/books/"
    }
}
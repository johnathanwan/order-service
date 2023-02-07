package com.polarbookshop.orderservice.book

import okhttp3.mockwebserver.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.http.*
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@TestMethodOrder(MethodOrderer.Random::class)
class BookClientTests {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var bookClient: BookClient

    @BeforeEach
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val webClient = WebClient.builder().baseUrl(mockWebServer.url("/").toUri().toString()).build()
        bookClient = BookClient(webClient)
    }

    @AfterEach
    fun clean() = mockWebServer.shutdown()

    @Test
    fun `when book exists then return book`() {
        val bookIsbn = "1234567890"
        val mockResponse = MockResponse().addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody("""
                {
                "isbn": $bookIsbn,
                "title": "Title",
                "author": "Author",
                "price": 9.90,
                "publisher": "Polarsophia"
                }
            """.trimIndent())

        mockWebServer.enqueue(mockResponse)

        val book: Mono<Book> = bookClient.getBookByIsbn(bookIsbn)

        StepVerifier.create(book).expectNextMatches { it.isbn == bookIsbn }.verifyComplete()
    }

}
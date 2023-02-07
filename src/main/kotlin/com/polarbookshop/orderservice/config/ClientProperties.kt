package com.polarbookshop.orderservice.config

import jakarta.validation.constraints.NotNull
import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.*

@ConfigurationProperties(prefix = "polar")
data class ClientProperties(@NotNull val catalogServiceUri: URI)

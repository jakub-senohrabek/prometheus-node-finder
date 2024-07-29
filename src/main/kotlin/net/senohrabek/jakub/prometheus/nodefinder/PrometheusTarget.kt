package net.senohrabek.jakub.prometheus.nodefinder

import kotlinx.serialization.Serializable

@Serializable
data class PrometheusTarget(
    val targets: List<String>,
    val labels: Map<String, String>
)
package net.senohrabek.jakub.prometheus.nodefinder

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

class PrometheusTargetsFile(
    val path: File,
    val json: Json = Json {
        ignoreUnknownKeys = true
    }
) {
    fun updateWith(jobNodes: Map<String, List<Node>>) {
        val targets = mutableListOf<PrometheusTarget>()
        for ((job, nodes) in jobNodes) {
            targets.add(
                PrometheusTarget(
                    targets = nodes.map { it.ips.first() }.sorted(),
                    labels = mapOf(
                        "job" to job
                    )
                )
            )
        }
        path.writeText(Json.encodeToString(serializer, targets))
    }

    companion object {
        private val serializer = ListSerializer(PrometheusTarget.serializer())
    }
}
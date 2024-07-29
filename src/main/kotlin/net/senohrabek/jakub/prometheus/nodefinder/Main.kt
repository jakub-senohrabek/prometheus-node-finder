package net.senohrabek.jakub.prometheus.nodefinder

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.senohrabek.jakub.prometheus.nodefinder.fargate.FargateNodeFinder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.time.Duration.Companion.seconds

private val logger: Logger = LoggerFactory.getLogger("main")

@Serializable
data class DiscoveryConfig(
    val outputFile: String,
    val checkEverySeconds: Long,
    val jobs: Map<String, ServiceDiscovery>
)

@Serializable
sealed interface ServiceDiscovery {

}

@Serializable
@SerialName("fargate")
data class FargateServiceDiscovery(
    val cluster: String,
    val service: String,
    val targetPort: Int
): ServiceDiscovery

fun main() {
    val rawConfig = System.getenv("DISCOVERY_CONFIG")
        ?: error("Provide configuration via env variable DISCOVERY_CONFIG")
    val prometheusConfig = System.getenv("PROMETHEUS_CONFIG")
    if (prometheusConfig != null) {
        File("/etc/prometheus/prometheus.yml").writeText(prometheusConfig)
    }

    val config = Json.decodeFromString(DiscoveryConfig.serializer(), rawConfig)
    val file = PrometheusTargetsFile(File(config.outputFile))
    val finders = config.jobs.map { (job, discovery) ->
        if (discovery is FargateServiceDiscovery) {
            job to FargateNodeFinder(discovery.cluster, discovery.service, discovery.targetPort)
        } else error("Unknown discovery type ${discovery::class.simpleName}")
    }.toMap()

    runBlocking {
        while(isActive) {
            try {
                val targets = finders.mapValues { (_, finder) ->
                    finder.findNodes()
                }
                file.updateWith(targets)
            } catch (ex: Exception) {
                logger.error("Failed to update targets to ${File(config.outputFile).absolutePath}", ex)
            }
            delay(config.checkEverySeconds.seconds)
        }
    }
}
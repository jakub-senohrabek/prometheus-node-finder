package net.senohrabek.jakub.prometheus.nodefinder.fargate

import aws.sdk.kotlin.services.ecs.EcsClient
import aws.sdk.kotlin.services.ecs.describeTasks
import aws.sdk.kotlin.services.ecs.model.ListTasksRequest
import kotlinx.coroutines.runBlocking
import net.senohrabek.jakub.prometheus.nodefinder.Node
import net.senohrabek.jakub.prometheus.nodefinder.NodeFinder

class FargateNodeFinder(
    private val cluster: String,
    private val service: String,
    private val port: Int,
    private val ecs: EcsClient = runBlocking { EcsClient.fromEnvironment() },
): NodeFinder {
    override suspend fun findNodes(): List<Node> {
        val response = ecs.listTasks(ListTasksRequest {
            cluster = this@FargateNodeFinder.cluster
            serviceName = service
        })
        val tasks = ecs.describeTasks {
            cluster = this@FargateNodeFinder.cluster
            tasks = response.taskArns
        }.tasks!!
        return tasks.mapNotNull { task ->
            task.attachments?.mapNotNull attachment@{ attachment ->
                if (attachment.status == "ATTACHED") {
                    attachment.details?.firstOrNull { it.name == "privateIPv4Address" }?.value
                } else null
            }?.let { ips ->
                FargateNode(
                    ips.map {
                        "${it}:${port}"
                    }
                )
            }
        }
    }

}
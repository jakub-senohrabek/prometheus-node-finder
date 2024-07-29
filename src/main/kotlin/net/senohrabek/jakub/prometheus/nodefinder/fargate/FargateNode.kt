package net.senohrabek.jakub.prometheus.nodefinder.fargate

import net.senohrabek.jakub.prometheus.nodefinder.Node

class FargateNode(override val ips: List<String>) : Node
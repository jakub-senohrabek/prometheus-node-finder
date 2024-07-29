package net.senohrabek.jakub.prometheus.nodefinder

interface NodeFinder {
    suspend fun findNodes(): List<Node>
}
package io.github.andrebeat.kadfun

import com.github.nscala_time.time.Imports._
import java.net.InetAddress
import scala.collection.immutable.SortedSet

case class Node(nodeId: Id, address: InetAddress, port: Int) {
  def distance(that: Node) = this.nodeId.distance(that.nodeId)
}
case class Contact(node: Node, timestamp: DateTime = DateTime.now)

object Contact {
  implicit object LeastRecentlySeenOrdering extends Ordering[Contact] {
    def compare(x: Contact, y: Contact) =
      x.timestamp.compare(y.timestamp)
  }

  case class ClosestToNodeOrdering(node: Node) extends Ordering[Contact] {
    def compare(x: Contact, y: Contact) =
      implicitly[Ordering[Distance]].compare(node.distance(x.node), node.distance(y.node))
  }
}

case class Bucket(capacity: Int = Bucket.CAPACITY, entries: SortedSet[Contact] = SortedSet[Contact]()) {
  def size: Int = entries.size

  def nodes: Set[Node] = entries.map(_.node)

  def +(node: Node): Bucket =
    entries.find(_.node == node) match {
      case Some(old) => copy(entries = (entries - old + Contact(node)))
      case _ if size < capacity => copy(entries = (entries + Contact(node)))
      case _ => this
        // TODO: ping least-recently seen to decide what to do.
    }

  def -(node: Node): Bucket =
    entries.find(_.node == node) match {
      case Some(c) => copy(entries = (entries - c))
      case _ => this
    }
}

object Bucket {
  val CAPACITY = 20
}

case class RoutingTable(self: Id, buckets: Vector[Bucket] = Vector(Bucket())) {
  import Bits._

  private[this] def bucket(id: Id): Bucket = {
    val b = self.distance(id).bytes

    def loop(bit: Int = 0): Int =
      if (bit == Id.SIZE_BITS)
        throw new IllegalArgumentException("A node must never put its own nodeId into a bucket as a contact")
      else if (b.testBit(bit)) Id.SIZE_BITS - bit - 1
      else loop(bit + 1)

    buckets(loop())
  }
}

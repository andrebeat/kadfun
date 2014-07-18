package io.github.andrebeat.kadfun

import java.net.InetAddress
import java.security.MessageDigest

object Bits {
  implicit class PimpedByteArray(val bs: Array[Byte]) extends AnyVal {
    def testBit(bit: Int) = {
      require(bit / 8 < bs.size)
      bs(bit / 8).testBit(bit % 8)
    }

    def ^(that: Array[Byte]) = {
      val ret = new Array[Byte](bs.size)

      def loop(i: Int = 0): Unit =
        if (i < ret.size) {
          ret(i) = (this.bs(i) ^ that.bs(i)).toByte
          loop(i + 1)
        }
      loop()

      ret
    }

    def toBitString =
      bs.map(_.toBitString).mkString(" ")
  }

  implicit class PimpedByte(val b: Byte) extends AnyVal {
    def testBit(bit: Int) = (b & (1L << (7 - bit))) != 0
    def toBitString =
      String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0')
  }
}

class Distance(val bytes: Array[Byte]) extends AnyVal
object Distance {
  implicit object DistanceOrdering extends Ordering[Distance] {
    def compare(x: Distance, y: Distance) = {
      def loop(i: Int = 0): Int =
        if (x.bytes(i) != y.bytes(i))
          if (x.bytes(i) < y.bytes(i)) -1 else 1
        else
          if (i + 1 < x.bytes.size) loop(i + 1) else 0

      loop()
    }
  }
}

class Id(val bytes: Array[Byte]) extends AnyVal {
  import Bits._
  def distance(that: Id): Distance =
    new Distance(this.bytes ^ that.bytes)
}

object Id {
  private[this] val sha1 = MessageDigest.getInstance("SHA-1")
  val SIZE_BITS = 160

  def apply(data: Array[Byte]): Id = {
    require(data.length == Id.SIZE_BITS / 8, s"Ids must be ${Id.SIZE_BITS}-bit long")
    new Id(data)
  }

  def apply(string: String): Id =
    Id(sha1.digest(string.getBytes("UTF-8")))

  def apply(address: InetAddress, port: Int): Id =
    Id(s"${address.getHostAddress}:$port")
}

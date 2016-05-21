package japgolly.scalajs.react.test

import org.scalajs.dom.{document, html}
import scala.io.AnsiColor._
import scalaz.Equal
import scalaz.syntax.equal._
import japgolly.scalajs.react.raw

object TestUtil extends TestUtil

trait TestUtil
  extends scalaz.std.StringInstances
     with scalaz.std.StreamInstances
     with scalaz.std.VectorInstances
     with scalaz.std.SetInstances
     with scalaz.std.TupleInstances
     with scalaz.std.OptionInstances
     with scalaz.std.AnyValInstances
     with scalaz.std.ListInstances {

  // TODO erm... not really. Only allow in raw testing
  implicit val equalReactNodeList: Equal[raw.ReactNodeList] = Equal.equalA
  implicit val equalRawKey       : Equal[raw.Key          ] = Equal.equalA
  implicit val equalRawRef       : Equal[raw.Ref          ] = Equal.equalA
  implicit val equalState        : Equal[raw.State        ] = Equal.equalA

  def assertEq[A: Equal](actual: A, expect: A): Unit =
    assertEq(null, actual, expect)

  def assertEq[A: Equal](name: => String, actual: A, expect: A): Unit =
    if (actual ≠ expect) {
      println()
      Option(name).foreach(n => println(s">>>>>>> $n"))

      val toString: Any => String = {
        case s: Stream[_] => s.force.toString() // SI-9266
        case a            => a.toString
      }

      var as = toString(actual)
      var es = toString(expect)
      var pre = "["
      var post = "]"
      if ((as + es) contains "\n") {
        pre = "↙[\n"
      }
      println(s"expect: $pre$BOLD$BLUE$es$RESET$post")
      println(s"actual: $pre$BOLD$RED$as$RESET$post")
      println()
      assert(false)
    }

  def fail(msg: String, clearStackTrace: Boolean = true): Nothing = {
    val t = new AssertionError(msg)
    if (clearStackTrace)
      t.setStackTrace(Array.empty)
    throw t
  }

  final type TopNode = org.scalajs.dom.Element

  def assertOuterHTML(node: TopNode, expect: String): Unit =
    assertOuterHTML(null, node, expect)

  def assertOuterHTML(name: => String, node: TopNode, expect: String): Unit =
    assertEq(name, scrubReactHtml(node.outerHTML), expect)

  private val reactRubbish = """\s+data-react\S*?\s*?=\s*?".*?"|<!--(?:.|[\r\n])*?-->""".r

  def scrubReactHtml(html: String): String =
    reactRubbish.replaceAllIn(html, "")

  def newBodyContainer(): html.Element = {
    val cont = document.createElement("div")
    document.body.appendChild(cont)
    cont.asInstanceOf[html.Element]
  }

  def withBodyContainer[A](f: html.Element => A): A = {
    val cont = newBodyContainer()
    try
      f(cont)
    finally {
      raw.ReactDOM unmountComponentAtNode cont // Doesn't matter if no component mounted here
      document.body.removeChild(cont)
    }
  }

}


package circumflex

import org.junit.Test
import java.net.URL
import kotlin.test.assertEquals

class LinkEvaluatorTest {
    @Test
    fun testSimpleReplacement() {
        val lines = listOf(
                "x", "y", "z"
        )

        val urls = LinkEvaluator.evaluateURLs("^", lines)

        for ((line: String, outUrl: URL) in lines.zip(urls)) {
            assertEquals(line, outUrl.path!!, "Failed to convert simple URL")
        }
    }

    @Test
    fun testTwoSimpleReplacements() {
        val lines = listOf("a b", "1 232")

        val url = "^/^"

        val expectedValues = listOf("a/b", "1/232")

        doTest(url, lines, expectedValues)
    }

    @Test(expected = RuntimeException::class)
    fun testFailBadInput() {
        val lines = listOf("a b c", "1 2")
        val url = "^/^"
        val expectedValues = listOf("a/b", "1/2")
        doTest(url, lines, expectedValues)
    }

    @Test(expected = RuntimeException::class)
    fun testFailBadUrl() {
        val lines = listOf("a b", "1 2")
        val url = "^^^"
        val expectedValues = listOf("ab", "1/2")
        doTest(url, lines, expectedValues)
    }

    @Test(expected = RuntimeException::class)
    fun testEmptyLineInput() {
        val lines = listOf("   ", "1 2")
        val url = "^/^"
        val expectedValues = listOf(" / ", "1/2")
        doTest(url, lines, expectedValues)
    }

    private fun doTest(url: String, lines: List<String>, expectedValues: List<String>) {
        for ((expected, output: URL) in expectedValues.zip(LinkEvaluator.evaluateURLs(url, lines))) {
            assertEquals(expected, output.path!!)
        }
    }
}
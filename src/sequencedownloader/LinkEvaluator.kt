package sequencedownloader

import java.net.URL

class LinkEvaluator private constructor() {

    companion object {
        fun evaluateURLs(url: String, substitutions: List<String>, replacementChar: String = "^"): List<URL> {
            if (!verifyInput(url, substitutions, replacementChar)) {
                throw RuntimeException("Some of the input file entries don't " +
                        "match the number of requested substitutions. Make sure the number" +
                        "of entries in each substitution line is equal to the number of \"^\" in the link")
            }

            val result = mutableListOf<String>()

            for (substitutionLine in substitutions) {
                // Replace the mark char with its substitution
                val subs = substitutionLine
                        .split(" ", "\t")
                        .map { it.trim().replace("\n", "") }

                result.add(replaceMultiple(url, subs, replacementChar))
            }
            return listOf()
        }

        /**
         * The input is verified by making sure that every entry in [substitutions] has a number
         * of strings that match the number or the char to be replaced.
         */
        private fun verifyInput(url: String, substitutions: List<String>,
                                replacementChar: String = "^"): Boolean {

            val replaceCount = url.count { it.toString() == replacementChar }

            for (entry in substitutions) {
                val substitutionCount = entry.split(" ", "\t").count()

                if (replaceCount != substitutionCount) {
                    return false
                }
            }

            return true
        }

        /**
         * Keep replacing the first occurrence of [replaceChar]
         * in [inputString] sequentially by each entry of [values]
         */
        private fun replaceMultiple(inputString: String,
                                    values: Collection<String>,
                                    replaceChar: String = "^"): String {

            var result = inputString
            for (value in values) {
                result = result.replaceFirst(replaceChar, value)
            }

            return result
        }
    }
}

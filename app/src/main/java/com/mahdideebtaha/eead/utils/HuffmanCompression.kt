import com.mahdideebtaha.eead.model.HuffmanNode
import java.util.PriorityQueue

object HuffmanCompression {

    fun compress(data: ByteArray): Pair<String, Map<Byte, String>> {
        // حساب التكرار لكل بايت
        val freqMap = mutableMapOf<Byte, Int>()
        for (byte in data) {
            freqMap[byte] = freqMap.getOrDefault(byte, 0) + 1
        }

        val queue = PriorityQueue(compareBy<HuffmanNode> { it.freq })
        freqMap.forEach { (byte, freq) ->
            queue.add(HuffmanNode(byte, freq))
        }

        while (queue.size > 1) {
            val left = queue.poll()
            val right = queue.poll()
            queue.add(HuffmanNode(null, left.freq + right.freq, left, right))
        }

        val root = queue.poll() ?: return Pair("", emptyMap())

        val codes = mutableMapOf<Byte, String>()
        buildCodeTable(root, "", codes)

        val compressed = data.map { codes[it] ?: "" }.joinToString("")

        return Pair(compressed, codes)
    }

    private fun buildCodeTable(node: HuffmanNode?, code: String, map: MutableMap<Byte, String>) {
        if (node == null) return
        if (node.byte != null) map[node.byte] = code
        buildCodeTable(node.left, code + "0", map)
        buildCodeTable(node.right, code + "1", map)
    }
}

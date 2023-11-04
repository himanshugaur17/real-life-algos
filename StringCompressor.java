import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

public class StringCompressor {
    private static class Node {
        char c;
        Map<Character, Node> children;
        int count;
        boolean isLeafNode;

        public Node(char c) {
            this.c = c;
            this.children = new LinkedHashMap<>(); // to preserve the order of insertion
            this.count = 0;
            this.isLeafNode = false;
        }

        public void addChild(Node node) {
            this.children.put(node.c, node);
        }
    }

    private Node createTrie(List<String> data) {
        Node trie = new Node('0');
        Node currNode = trie;
        for (String x : data) {
            currNode = trie;
            for (char lc : x.toCharArray()) {
                if (!currNode.children.containsKey(lc))
                    currNode.addChild(new Node(lc));
                currNode = currNode.children.get(lc);
            }
            currNode.isLeafNode = true;
            currNode.count = currNode.count + 1;
        }
        return trie;
    }

    private String serializeTrie(Node trie) {
        Queue<Node> queue = new LinkedList<>();
        queue.add(trie);
        StringBuilder sb = new StringBuilder();
        while (!queue.isEmpty()) {
            Node currNode = queue.poll();
            for (Node node : currNode.children.values()) {
                sb.append(node.c);
                if (node.isLeafNode)
                    sb.append("[" + Integer.toString(node.count) + "]");
                queue.add(node);
            }

            sb.append(';');
        }
        return sb.toString();
    }

    public String encodeStringToTransferOverNetwork(List<String> data) {
        Node trie = createTrie(data);
        String serializedTrie = serializeTrie(trie);
        return serializedTrie;
    }

    private Node createTrieFromSerializedString(String encodedData) {
        Node trie = new Node('0');
        char[] edArray = encodedData.toCharArray();
        Queue<Node> queue = new LinkedList<>();
        int i = 0, n = edArray.length;
        queue.add(trie);
        while (!queue.isEmpty()) {
            Node currNode = queue.poll();
            while (i < n && edArray[i] != ';') {
                Node childNode = new Node(edArray[i++]);
                currNode.addChild(childNode);
                if (i < n && Character.isDigit(edArray[i])) {
                    StringBuilder count = new StringBuilder();
                    while (i < n && Character.isDigit(edArray[i]))
                        count.append(edArray[i++]);
                    childNode.isLeafNode = true;
                    childNode.count = Integer.valueOf(count.toString());
                }
                queue.add(childNode);
            }
            i++;
        }
        return trie;
    }

    private List<String> traverseTrie(Node node, final StringBuilder sb) {
        sb.append(node.c);
        List<String> localList = new ArrayList<>();
        if (node.isLeafNode) {
            for (int i = 0; i < node.count; i++)
                localList.add(new String(sb));

        } else {
            localList = node.children.values().stream()
                    .map(child -> traverseTrie(child, sb))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
        sb.deleteCharAt(sb.length() - 1);
        return localList;
    }

    public List<String> decodeCompressedString(String compressedData) {
        Node trie = createTrieFromSerializedString(compressedData);
        return traverseTrie(trie, new StringBuilder());
    }
}
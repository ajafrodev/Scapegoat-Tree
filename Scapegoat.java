import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.lang.Math;

public class Scapegoat {

    // tree root
    private Node root;

    // max nodes added (needed to rebuild tree when deleting)
    private int MaxNodeCount = 0;

    // total nodes in tree
    private int NodeCount = 0;

    // alpha threshold needed to check for balancing
    private static final double threshold = 0.57;

    /**
     *  Node class
     */
    public class Node {
        T data;
        Node parent;
        Node left;
        Node right;
        public Node (T data, Node parent, Node left, Node right) {
            this.data = data;
            this.parent = parent;
            this.left = left;
            this.right = right;
        }
        public String toString() {
            return "[data="+data+"]";
        }
    }

    /**
     *  Constructor
     */
    public Scapegoat() {
        root = null;
    }

    /**
     *  Constructor
     */
    public Scapegoat(T data) {
        root = new Node(data, null, null, null);
        NodeCount ++;
    }

    /**
     *  Return root of tree.
     */
    public Node root(){
        return this.root;
    }

    /**
     *  Finds the first scapegoat node.
     */
    private Node scapegoatNode(Node node) {
        Node traverse = node.parent;
        while (size(traverse) <= (threshold * size(traverse.parent))) {
            traverse = traverse.parent;
        }
        if (traverse == root) {
            return traverse;
        }
        return traverse.parent;
    }

    /**
     *  Re-build the tree rooted at node into a perfectly balanced tree.
     */
    public Node rebuild(Node node) {
        List<Node> node_list = inorder(node);
        Node new_root = inorder_trees(node_list, 0, node_list.size());
        if (new_root == null) {
            return null;
        }
        new_root.parent = null;
        return new_root;
    }

    /**
     *  Helper function for rebuild(). Recursively build balanced tree.
     */
    private Node inorder_trees(List<Node> nodes, int start, int end) {
        if (end == 0) {
            return null;
        }
        int middle = end / 2;
        int index = start + middle;
        Node change = nodes.get(index);
        change.left = inorder_trees(nodes, start, middle);
        if (change.left != null) {
            change.left.parent = change;
        }
        change.right = inorder_trees(nodes, index + 1, end - middle - 1);
        if (change.right != null) {
            change.right.parent = change;
        }
        return change;
    }

    /**
     *  Add node to scapegoat tree. Rebuild to a balanced tree if
     *  tree is not a-weight balanced.
     */
    public void add(T data) {
        System.out.println("add " + data);
        if (find(data) != null) {
            return;
        }
        NodeCount++;
        MaxNodeCount = Math.max(MaxNodeCount, NodeCount);
        if (root == null) {
            root = new Node(data, null, null, null);
        } else {
            Node insert_node = new Node(data, null, null, null);
            Node current = root;
            boolean right = true;
            Node parent = null;
            while(current != null) {
                if (insert_node.data.compareTo(current.data) == -1) {
                    parent = current;
                    current = current.left;
                    right = false;
                } else {
                    parent = current;
                    current = current.right;
                    right = true;
                }
            }
            if (right) {
                parent.right = insert_node;
            } else {
                parent.left = insert_node;
            }
            insert_node.parent = parent;
//            System.out.println("height: " + height(root));
//            System.out.println("weight: " + -1 * Math.log10(NodeCount) / Math.log10(threshold));
            if (height(root) > (-1 * Math.log10(NodeCount) / Math.log10(threshold))) {
//                System.out.println("Rebuilding");
                Node scapegoat = scapegoatNode(insert_node);
                Node sg_parent = scapegoat.parent;
                Node rebuilt = rebuild(scapegoat);
                if (sg_parent == null) {
                    root = rebuilt;
                    rebuilt.parent = null;
                    MaxNodeCount = NodeCount;
                } else {
                    if (sg_parent.left == scapegoat) {
                        sg_parent.left = rebuilt;
                    } else {
                        sg_parent.right = rebuilt;
                    }
                    rebuilt.parent = sg_parent;
                }

            }
        }
//        System.out.println("preorder: " + preorder(root));
//        System.out.println("inorder: " + inorder(root));
    }

    /**
     *  Return height of the tree.
     */
    private int height(Node node) {
        if (node == null) {
            return -1;
        }
        if (height(node.left) < height(node.right)) {
            return 1 + height(node.right);
        } else {
            return 1 + height(node.left);
        }
    }

    /**
     *  Remove a node from tree. If the node count
     *  is less than the alpha * max node count, rebuild the tree.
     */
    public void remove(T data) {
        System.out.println("remove: " + data);
        Node remove_node = find(data);
        if (remove_node != null) {
//            System.out.println("bfs: " + breadthFirstSearch());
//            System.out.println("parent: " + remove_node.parent);
//            System.out.println("left: " + remove_node.left);
//            System.out.println("right: " + remove_node.right);
            if (remove_node.right == null && remove_node.left == null) {
                if (remove_node == root) {
                    root = null;
                } else if (remove_node.parent.left == remove_node) {
                    remove_node.parent.left = null;
                } else {
                    remove_node.parent.right = null;
                }
            } else if (remove_node.left == null) {
                if (remove_node == root) {
                    root = remove_node.right;
                    root.parent = null;
                } else if (remove_node.parent.left == remove_node) {
                    remove_node.parent.left = remove_node.right;
                    remove_node.right.parent = remove_node.parent;
                } else {
                    remove_node.parent.right = remove_node.right;
                    remove_node.right.parent = remove_node.parent;
                }
            } else if (remove_node.right == null) {
                if (remove_node == root) {
                    root = remove_node.left;
                    root.parent = null;
                } else if (remove_node.parent.left == remove_node) {
                    remove_node.parent.left = remove_node.left;
                    System.out.println(remove_node.parent);
                    remove_node.left.parent = remove_node.parent;
                } else {
                    remove_node.parent.right = remove_node.left;
                    remove_node.left.parent = remove_node.parent;
                }
            } else {
                Node succ = succ(remove_node);
                Node succ_parent = succ.parent;
                if (remove_node == root) {
                    System.out.println(root.right);
                    root = succ;
                    root.parent = null;
                    if (succ_parent == remove_node) {
                        remove_node.left.parent = root;
                    } else {
                        Node right = succ.right;
                        if (succ.right == null) {
                            root.right = remove_node.right;
                            remove_node.left.parent = root;
                            remove_node.right.parent = root;
                            succ_parent.left = null;
                        } else {
                            root.right = remove_node.right;
                            remove_node.left.parent = root;
                            remove_node.right.parent = root;
                            succ_parent.left = right;
                        }

                    }
                } else if (remove_node.parent.left == remove_node) {
                    if (succ_parent == remove_node) {
                        remove_node.parent.left = succ;
                        succ.parent = remove_node.parent;
                        remove_node.left.parent = succ;
                    } else {
                        succ_parent.left = null;
                        remove_node.parent.left = succ;
                        succ.parent = remove_node.parent;
                        remove_node.left.parent = succ;
                        remove_node.right.parent = succ;
                        succ.right = remove_node.right;
                    }
                } else {
                    if (succ_parent == remove_node) {
                        remove_node.parent.right = succ;
                        succ.parent = remove_node.parent;
                        remove_node.left.parent = succ;
                    } else {
                        succ_parent.left = null;
                        remove_node.parent.right = succ;
                        succ.parent = remove_node.parent;
                        remove_node.left.parent = succ;
                        remove_node.right.parent = succ;
                        succ.right = remove_node.right;
                    }
                }
                succ.left = remove_node.left;
            }
            NodeCount--;
        }
        if (NodeCount <= (threshold * MaxNodeCount)) {
            root = rebuild(root);
        }
//        System.out.println("preorder: " + preorder(root));
//        System.out.println("inorder: " + inorder(root));
    }

    /**
     *  Successor of a given node.
     */
    private Node succ(Node node) {
        node = node.right;
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    /**
     *  Preorder traversal of tree.
     */
    public List<Node> preorder(Node node) {
        List<Node> nodes = new ArrayList<Node>();
        nodes.add(node);
        if(node.left != null){
            nodes.addAll(preorder(node.left));
        }
        if(node.right != null){
            nodes.addAll(preorder(node.right));
        }
        return nodes;
    }

    /**
     *  Inorder traversal of tree.
     */
    public List<Node> inorder(Node node) {
        List<Node> nodes = new ArrayList<Node>();
        if(node.left != null){
            nodes.addAll(inorder(node.left));
        }
        nodes.add(node);
        if(node.right != null){
            nodes.addAll(inorder(node.right));
        }
        return nodes;
    }

    /**
     *  Printing either preorder or inorder traversal
     */
    public void print() {
        List<Node> nodes = inorder(root);
        for (Node node : nodes) {
            System.out.println(node.toString());
        }
    }

    /**
     *  Find node given data.
     */
    public Node find(T data) {
        Node current = root;
        int result;
        while(current != null){
            result = data.compareTo(current.data);
            if (result == 0) {
                return current;
            } else if (result > 0) {
                current = current.right;
            } else {
                current = current.left;
            }
        }
        return null;
    }

    /**
     *  Return size of the tree.
     */
    private int size (Node node) {
        if (node == null)
            return 0;
        return 1 + size(node.left) + size(node.right);
    }

    /**
     *  BFS of tree.
     */
    public List<Node> breadthFirstSearch() {
        Node node = root;
        List<Node> nodes = new ArrayList<Node>();
        Deque<Node> deque = new ArrayDeque<Node>();
        if(node != null){
            deque.offer(node);
        }
        while(!deque.isEmpty()){
            Node first = deque.poll();
            nodes.add(first);
            if(first.left != null){
                deque.offer(first.left);
            }
            if(first.right != null){
                deque.offer(first.right);
            }
        }
        return nodes;
    }

    /**
     *  Test the tree.
     */
    public static void main(String[] args) {
        Scapegoat tree = new Scapegoat();
//        tree.add(new T(40));
//        tree.add(new T(10));
//        tree.remove(new T(40));
//        tree.add(new T(8));
//        tree.add(new T(12));
//        tree.add(new T(7));
//        tree.add(new T(9));
//        tree.add(new T(11));
//        tree.add(new T(14));
//        tree.add(new T(16));
//        tree.add(new T(18));
//        tree.remove(new T(14));
//        tree.remove(new T(16));
//        tree.remove(new T(12));
//        tree.remove(new T(18));
    }


}



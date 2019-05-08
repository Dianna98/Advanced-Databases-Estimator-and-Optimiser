package sjdb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Optimiser implements PlanVisitor {

    Catalogue cat;
    Set<Scan> scans = new HashSet<>();
    Set<Project> projects = new HashSet<>();
    Set<Select> selects = new HashSet<>();
    Set<Product> products = new HashSet<>();
    Set<Node> tree = new HashSet<>();

    Optimiser(Catalogue cat){
        this.cat = cat;
    }

    public Operator optimise(Operator plan){
        plan.accept(this);
        //Node n = new Node();
        addProjects(projects);
        addSelect(selects);
        addProducts(products);
        addScans(scans);
        System.out.println("*****************************");
        for (Node n : tree){
            System.out.println(n.getNode().toString());
            if (n.getLeft()==null){
                System.out.println("NULL");
            }else
            System.out.println("Left:   "+n.getLeft().getNode().toString());
            if (n.getRight()==null){
                System.out.println("NULL");
            }else
            System.out.println("Right:  "+n.getRight().getNode().toString());
            System.out.println("--------------------------------------");
        }

     return new Scan(new NamedRelation("Diana",10));
    }

    @Override
    public void visit(Scan op) {
        scans.add(op);
        System.out.println(scans.toString());
    }

    @Override
    public void visit(Project op) {
        projects.add(op);
        System.out.println(projects.toString());

    }

    @Override
    public void visit(Select op) {
        selects.add(op);
        System.out.println(selects.toString());
    }

    @Override
    public void visit(Product op) {
        products.add(op);
        System.out.println(products.toString());

    }

    @Override
    public void visit(Join op) {
    }

    public Set<Node> addProducts(Set<Product> products){
        for (Product op: products) {
            tree.add(new Node(op,new Node(op.getLeft()),new Node(op.getRight())));
        }
        return tree;
    }

    public Set<Node> addSelect(Set<Select> selects){
        for (Select op: selects) {
            tree.add(new Node(op,new Node(op.getInput())));
        }
        return tree;
    }

    public Set<Node> addScans(Set<Scan> scans){
        for (Scan op: scans) {
            tree.add(new Node(op));
        }
        return tree;
    }

    public Set<Node> addProjects(Set<Project> projects){
        for (Project op: projects) {
            tree.add(new Node(op,new Node(op.getInput())));
        }
        return tree;
    }
}

class Node {

    Node left = null,right = null;
    Operator node = null;
    Set<Node> tree = new HashSet<>();

    Node(Operator node, Node left, Node right){
        this.node = node;
        this.left = left;
        this.right = right;
        tree.add(this);
    }

    Node(Operator node, Node left){
        this.node = node;
        this.left = left;
        this.right = null;
        tree.add(this);
    }

    Node(Operator node){
        this.node = node;
        this.left = null;
        this.right = null;
    }

    Node(){}

    public Operator getNode() {
        return node;
    }

    public void addLeft(Node node){

        this.left = node;
    }

    public void addRight(Node node){
        this.right = node;
    }

    public Node getLeft() {
        return left;
    }

    public Node getRight() {
        return right;
    }

    public boolean isLeft(Operator node){
        return left.getNode().equals(node);
    }

    public boolean isRight(Operator node){
        return right.getNode().equals(node);
    }

    public Node getNode(Set<Node> tree, Operator op){
        for (Node n : tree){
            if (n.getNode().equals(op)){
                return n;
            }
        }
        return null;
    }

    public Node getParent(Set<Node> tree, Operator op){
        for (Node n: tree){
            if (n.getLeft().getNode().equals(op) || n.getRight().getNode().equals(op)){
                return n;
            }
        }

        return null;
    }

    public Set<Node> getTree() {
        return tree;
    }

    public Set<Node> addProducts(Set<Product> products){
        for (Product op: products) {
            new Node(op,new Node(op.getLeft()),new Node(op.getRight()));
        }
        return tree;
    }

    public Set<Node> addSelect(Set<Select> selects){
        for (Select op: selects) {
            new Node(op,new Node(op.getInput()));
        }
        return tree;
    }

    public Set<Node> addScans(Set<Scan> scans){
        for (Scan op: scans) {
            new Node(op,new Node(op));
        }
        return tree;
    }

    public Set<Node> addProjects(Set<Project> projects){
        for (Project op: projects) {
            new Node(op,new Node(op.getInput()));
        }
        return tree;
    }

    public Node findRoot(Set<Node> tree){
        for (Node n: tree){

        }
    }

    public Set<Node> getBranch(Set<Node> branch ,Set<Node>tree){

        Set<Node> rest = new HashSet<>();

        for (Node n : tree){
            if 
        }

        while (!tree.isEmpty() || ){
            for
        }
    }

    public boolean checkRoot(Set<Node> tree){
        Set<Node> branch = new HashSet<>();
        Node node = this;
        while ((getLeft() !=null) || (getRight() !=null)){
            if (getLeft()!=null){
                branch.add(node.getLeft());
            }
            if (getRight()!=null){

            }
        }

    }
}


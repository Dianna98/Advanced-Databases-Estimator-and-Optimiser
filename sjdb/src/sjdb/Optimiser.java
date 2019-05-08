package sjdb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Optimiser implements PlanVisitor {

    public Catalogue cat;
    public final Estimator estimator = new Estimator();
    public Set<Attribute> attributes = new HashSet<>();
    public Set<Predicate> predicates = new HashSet<>();
    public Set<Scan> scans = new HashSet<>();
    public int minCost = Integer.MAX_VALUE;

    public Optimiser(Catalogue cat) {
        this.cat = cat;
    }

    public void visit(Scan op) {
        scans.add(new Scan((NamedRelation)op.getRelation()));
    }

    public void visit(Project op) {
        attributes.addAll(op.getAttributes());
    }

    public void visit(Product op) {}

    public void visit(Join op) {}

    public void visit(Select op) {
        Predicate p = op.getPredicate();
        predicates.add(p);
        attributes.add(p.getLeftAttribute());
        if(!p.equalsValue()) attributes.add(p.getRightAttribute());
    }

    // this method returns the optimised query plan
    public Operator optimise(Operator plan) {

        plan.accept(this);

        // move SELECTs and PROJECTS down the tree
        List<Operator> operations = moveDown(plan,scans, predicates);

        // choose the cheapest plan from the generated permutations and return it
        return getOptimisedPlan(plan,predicates, operations);
    }

    // this method moves down the tree SELECT and PRODUCT operators
    public List<Operator> moveDown(Operator root, Set<Scan> scans, Set<Predicate> predicates) {

        List<Operator> operators = new ArrayList<>(scans.size());

        for (Scan s: scans){
            Operator o = createTopSelect(s, predicates);
            List<Predicate> pred = new ArrayList<>(predicates);
            operators.add(createTopProject(o, getAttributes(root,pred)));
        }

        return operators;
    }

    // this method builds on the top level SELECT operators and returns the one that is closest to the root
    public Operator createTopSelect(Operator op, Set<Predicate> predicates){

        Operator result = op;
        List<Attribute> attributes = result.getOutput().getAttributes();
        List<Predicate> preds = new ArrayList<>(predicates);

        for (Predicate p : preds){

            if(result.getOutput() == null) {
                result.accept(estimator);
            }

            if ((p.equalsValue() && attributes.contains(p.getLeftAttribute())) || (!p.equalsValue() && attributes.contains(p.getLeftAttribute()) && attributes.contains(p.getRightAttribute()))) {
                result = new Select(result, p);
                predicates.remove(p);
            }

        }

        return result;
    }

    // this method projects only the desired attributes and returns the PROJECT operator set as a root if not all attributes are wanted, or the old root operator otherwise
    public Operator createTopProject(Operator op, Set<Attribute> attributes){

        if(op.getOutput() == null) {
            op.accept(estimator);
        }

        List<Attribute> attrs = new ArrayList<>(attributes);
        attrs.retainAll(op.getOutput().getAttributes());

        if (!attrs.isEmpty()) {
            Operator o = new Project(op, attrs);
            o.accept(estimator);
            return o;
        } else {
            return op;
        }
    }

    // this method returns the set of attributes desired, given an operator and a list of predicates
    public Set<Attribute> getAttributes(Operator op, List<Predicate> predicates){

        Set<Attribute> attributes = new HashSet<>();

        for(Predicate p : predicates){
            attributes.add(p.getLeftAttribute());
            if (!p.equalsValue()) {
                attributes.add(p.getRightAttribute());
            }
        }

        if (op instanceof Project) attributes.addAll(((Project) op).getAttributes());

        return attributes;
    }

    /* this method builds operators depending on the number of operators found for each predicate:
     * if 0 operators found     -> builds a PRODUCT
     * if 1 operator found      -> builds a SELECT
     * if 2 operators found     -> builds a JOIN
     * if more operators found  -> builds more PRODUCTs
     */
    public Operator build(Operator op, List<Operator> operators, List<Predicate> predicates){

        Operator result = null;
        List<Predicate> preds = new ArrayList<>(predicates);

        if (operators.size() == 1){
            result = operators.get(0);
            if (result.getOutput() == null) {
                result.accept(estimator);
            }
            return result;
        }
        for(Predicate p : preds){
            Operator left = getOperator(operators, p.getLeftAttribute());
            Operator right = getOperator(operators, p.getRightAttribute());

            // build SELECT
            if(left == null ^ right == null){
                result = new Select(left != null? left : right, p);
                predicates.remove(p);
            }

            // build JOIN
            if(left != null && right != null){
                result = new Join(left, right, p);
                predicates.remove(p);
            }

            if (result.getOutput() == null) {
                result.accept(estimator);
            }

            Set<Attribute> neededAttrs = getAttributes(op,predicates);

            List<Attribute> attributes = result.getOutput().getAttributes();

            // build PROJECT
            if (neededAttrs.size() == attributes.size() && attributes.containsAll(neededAttrs)){
                operators.add(result);
            }else{
                List<Attribute> attrs = new ArrayList<>();
                for (Attribute a : attributes){
                    if (neededAttrs.contains(a)){
                        attrs.add(a);
                    }
                }

                if (attrs.isEmpty()) {
                    operators.add(result);
                } else {
                    Project project = new Project(result, attrs);
                    project.accept(estimator);
                    operators.add(project);
                }
            }
        }

        // build more PRODUCTs
        while(operators.size()>1) {
            Operator o1 = operators.get(0);
            Operator o2 = operators.get(1);
            Operator product = new Product(o1, o2);
            product.accept(estimator);

            operators.remove(o1);
            operators.remove(o2);
            operators.add(product);
        }

        // return the root
        result = operators.get(0);

        return result;
    }

    // this method returns the first operator in the list which contains the given attribute
    public Operator getOperator(List<Operator> operators, Attribute attribute){

        List<Operator> ops = new ArrayList<>(operators);

        for (Operator o : ops){
            if (o.getOutput().getAttributes().contains(attribute)){
                operators.remove(o);
                return o;
            }
        }
        return null;
    }

    // this method finds the cheapest query plan from all n! permutations
    public Operator getOptimisedPlan(Operator op, Set<Predicate> predicates, List<Operator> operators){

        Operator cheapest = null;
        List<Predicate> preds = new ArrayList<>(predicates);
        List<List<Predicate>> permutations = permutations(preds);

        for (List<Predicate> p : permutations) {
            List<Operator> ops = new ArrayList<>(operators);

            Operator plan = build(op,ops, p);

            int cost = estimator.getCost(plan);
            if (cost<minCost){
                minCost = cost;
                cheapest = plan;
            }
        }

        printCost();

        return cheapest;
    }

    private void printCost() {
        System.out.println("\n---------------------------------------------");
        System.out.println("The cost of the optimal query plan is:  "+minCost);
        System.out.println("---------------------------------------------\n");
    }

    // this method generates a list with all permutations possible, given a list of predicates
    public List<List<Predicate>> permutations(List<Predicate> predicates) {

        if (predicates.isEmpty()) {
            List<List<Predicate>> result = new ArrayList<>();
            result.add(new ArrayList<>());
            return result;
        }

        Predicate fst = predicates.remove(0);
        List<List<Predicate>> list = new ArrayList<>();
        List<List<Predicate>> permutations = permutations(predicates);

        for (List<Predicate> partial : permutations) {
            for (int i=0; i <= partial.size(); i++) {
                List<Predicate> part = new ArrayList<>(partial);
                part.add(i, fst);
                list.add(part);
            }
        }

        return list;
    }

}

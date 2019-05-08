package sjdb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Optimiser implements PlanVisitor{

    public Catalogue cat;
    public Estimator estimator = new Estimator();
    public ArrayList<Attribute> attributes = new ArrayList<>();
    public Set<Predicate> predicates = new HashSet<>();
    public Set<Scan> scans = new HashSet<>();

    public Optimiser(Catalogue cat) {
        this.cat = cat;
    }

    public Operator optimise(Operator plan) {
        System.out.println("Optimised:");
        plan.accept(this);
        List<Operator> operations = moveDown(plan,scans,predicates);
        Operator optimisedPlan = optimalPlan(plan, operations, predicates);
        return optimisedPlan;
    }

    @Override
    public void visit(Scan op) {
        scans.add(op);
    }

    @Override
    public void visit(Project op) {
        attributes.addAll(op.getAttributes());
    }

    @Override
    public void visit(Select op) {
        predicates.add(op.getPredicate());
        attributes.add(op.getPredicate().getLeftAttribute());
        if (!op.getPredicate().equalsValue()){
            attributes.add(op.getPredicate().getRightAttribute());
        }
    }

    @Override
    public void visit(Product op) {

    }

    @Override
    public void visit(Join op) {

    }

    public Operator optimalPlan(Operator op, List<Operator> operators, Set<Predicate> predicates){
        Operator plan = null;
        Integer minCost = Integer.MAX_VALUE;

        List<Predicate> preds = new ArrayList<>(predicates);
        List<List<Predicate>> permutations = permutations(preds);

        for (List<Predicate> p : permutations){
            List<Operator> ops = new ArrayList<>(operators);
            Operator o = buildJoin(op,ops,p);

            int cost = estimator.getCost(o);
            if (cost<minCost){
                minCost = cost;
                plan = o;
            }
        }

        System.out.println("The cost of the optimal solution is:    " +minCost );

        return plan;
    }

    private List<List<Predicate>> permutations(List<Predicate> predicates) {
        List<List<Predicate>> result = new ArrayList<>();

        if (predicates.isEmpty()){
            return  new ArrayList<>();
        } else {
            Predicate fst = predicates.get(0);
            predicates.remove(0);
            List<List<Predicate>> perm = permutations(predicates);

            for (List<Predicate> p : perm){
                for (int i=0; i<p.size();i++) {
                    ArrayList<Predicate> x = new ArrayList<>(p);
                    x.add(i, fst);
                    result.add(x);
                }
            }
        }
        return result;
    }

    private Operator buildJoin(Operator op, List<Operator> operators, List<Predicate> predicates) {
        Operator result = null;

        if (operators.size() == 1){
            result = operators.get(0);
            if (result.getOutput() == null){
                result.accept(estimator);
                return result;
            }
        }

        for (Predicate p : predicates) {
            Operator left = getOperator(operators, p.getLeftAttribute());
            Operator right = getOperator(operators, p.getRightAttribute());

            if ((left == null) ^ (right == null)) {
                result = new Select(left != null ? left : right, p);
                predicates.remove(p);
            }

            if (left != null && right != null) {
                result = new Join(left, right, p);
                predicates.remove(p);

            }


            if (result.getOutput() == null) {
                result.accept(estimator);
            }

            Set<Attribute> attrs = getAttributes(op, predicates);

            List<Attribute> available = result.getOutput().getAttributes();

            if (attrs.size() == available.size() && available.containsAll(attrs)) {
                operators.add(result);
            } else {
                List<Attribute> a = available.stream().filter(attrs::contains).collect(Collectors.toList());
                if (a.size() == 0) {
                    operators.add(result);
                } else {
                    Project proj = new Project(result, a);
                    proj.accept(estimator);
                    operators.add(proj);
                }
            }
        }
        
        while (operators.size()>1){
            Operator b1 = operators.get(0);
            Operator b2 = operators.get(1);
            Operator product = new Product(b1, b2);
            product.accept(estimator);
            operators.remove(b1);
            operators.remove(b2);
            operators.add(product);
        }

        result = operators.get(0);

        return result;
    }

    private Operator getOperator(List<Operator> operators, Attribute attribute) {

        for (Operator op : operators){
            if (op.getOutput().getAttributes().contains(attribute)){
                operators.remove(op);
                return op;
            }
        }
        return null;
    }

    public List<Operator> moveDown(Operator root, Set<Scan> scans, Set<Predicate> predicates){
        List<Operator> plan = new ArrayList<>(scans.size());

        for (Scan s : scans){
            Operator op = selectTop(s,predicates);

            List<Predicate> p = new ArrayList<>(predicates);
            plan.add(projectTop(op, getAttributes(root,p)));
        }

        return plan;
    }

    private Set<Attribute> getAttributes(Operator op, List<Predicate> predicates) {
        Set<Attribute> attributes = new HashSet<>();

        for (Predicate p : predicates) {
            attributes.add(p.getLeftAttribute());
            if (!p.equalsValue()) {
                attributes.add(p.getRightAttribute());
            }
        }

        if (op instanceof Project){
            attributes.addAll(((Project) op).getAttributes());
        }

        return attributes;
    }

    private Operator selectTop(Operator op, Set<Predicate> predicates) {
        Operator result = op;
        //Relation rel = op.getOutput();
        //List<Attribute> attributes = rel.getAttributes();

        List<Attribute> attributes = result.getOutput().getAttributes();

        for (Predicate p : predicates){
            if (result.getOutput() == null){
                result.accept(estimator);
            }

            if (((!p.equalsValue() && attributes.contains(p.getLeftAttribute())) && attributes.contains(p.getRightAttribute())) || (attributes.contains(p.getLeftAttribute()) && p.equalsValue())){
                result = new Select(result,p);
                predicates.remove(p);
            }
        }
        return result;
    }

    private Operator projectTop(Operator op, Set<Attribute> attributes) {

        //Relation rel = op.getOutput();
        List<Attribute> attrs = new ArrayList<>(attributes);

        if (op.getOutput()==null){
       // if (rel == null){
            op.accept(estimator);
        }

        attrs.retainAll(op.getOutput().getAttributes());

        if (!attrs.isEmpty()){
            Operator o = new Project(op, attrs);
            o.accept(estimator);
            return o;
        }

        return op;
    }

}

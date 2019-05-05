package sjdb;

import java.util.List;

public class Optimiser implements PlanVisitor{

    protected Catalogue cat;
    protected Estimator estimator = new Estimator();

    public Optimiser(Catalogue cat) {
        this.cat = cat;
    }

    public Operator optimise(Operator plan) {
        plan.accept(this);
        Operator optimisedPlan = null;
        return optimisedPlan;
    }

    @Override
    public void visit(Scan op) {

    }

    @Override
    public void visit(Project op) {

    }

    @Override
    public void visit(Select op) {

    }

    @Override
    public void visit(Product op) {

    }

    @Override
    public void visit(Join op) {

    }
}

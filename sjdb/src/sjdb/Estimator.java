package sjdb;

import org.w3c.dom.Attr;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class  Estimator implements PlanVisitor {


	public Estimator() {
		// empty constructor
	}

	/* 
	 * Create output relation on Scan operator
	 *
	 * Example implementation of visit method for Scan operators.
	 */
	public void visit(Scan op) {
		Relation input = op.getRelation();
		Relation output = new Relation(input.getTupleCount());
		
		Iterator<Attribute> iter = input.getAttributes().iterator();
		while (iter.hasNext()) {
			output.addAttribute(new Attribute(iter.next()));
		}
		
		op.setOutput(output);
	}

	public void visit(Project op) {
		Relation input = op.getInput().getOutput();
		Relation output = new Relation(input.getTupleCount());

		Iterator<Attribute> it = op.getAttributes().iterator();
		while (it.hasNext()){
			output.addAttribute(new Attribute(it.next()));
		}

		op.setOutput(output);
	}
	
	public void visit(Select op) {
		Relation input = op.getInput().getOutput();
		Relation output;

		Predicate p = op.getPredicate();
		Attribute attribute = p.getLeftAttribute();
		int val,total = input.getTupleCount();

		if(p.getRightAttribute().equals(null)){
			val = input.getAttribute(attribute).getValueCount();
		} else {
			val = Math.max(input.getAttribute(p.getLeftAttribute()).getValueCount(),input.getAttribute(p.getRightAttribute()).getValueCount());
		}

		output = new Relation(total/val);

		Iterator<Attribute> it = input.getAttributes().iterator();
		while (it.hasNext()){
			output.addAttribute(new Attribute(it.next()));
		}
		op.setOutput(output);
	}
	
	public void visit(Product op) {
		Relation input1 = op.getLeft().getOutput();
		Relation input2 = op.getRight().getOutput();
		Relation output = new Relation(input1.getTupleCount()*input2.getTupleCount());

		Iterator<Attribute> it1 = input1.getAttributes().iterator();
		Iterator<Attribute> it2 = input2.getAttributes().iterator();

		while (it1.hasNext()){
			output.addAttribute(new Attribute(it1.next()));
		}

		while (it2.hasNext()){
			output.addAttribute(new Attribute(it2.next()));
		}

		op.setOutput(output);

	}
	
	public void visit(Join op) {
		Relation input1 = op.getLeft().getOutput();
		Relation input2 = op.getRight().getOutput();
		Relation output;

		Iterator<Attribute> it1 = input1.getAttributes().iterator();
		Iterator<Attribute> it2 = input2.getAttributes().iterator();

		Predicate p = op.getPredicate();
		int total = input1.getTupleCount()*input2.getTupleCount();
		int val = Math.max(input1.getAttribute(p.getLeftAttribute()).getValueCount(),input2.getAttribute(p.getRightAttribute()).getValueCount());

		output = new Relation(total/val);

		while (it1.hasNext()){
			output.addAttribute(new Attribute(it1.next()));
		}

		while (it2.hasNext()){
			if (!it1.next().equals(p.getRightAttribute())){
				output.addAttribute(new Attribute(it2.next()));
			}
		}

		op.setOutput(output);
	}
}

package sjdb;

import org.w3c.dom.Attr;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class  Estimator implements PlanVisitor {

	protected int cost;

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
		cost+=output.getTupleCount();
		//System.out.println(cost);
	}

	public void visit(Project op) {
		Relation input = op.getInput().getOutput();
		Relation output = new Relation(input.getTupleCount());

		Iterator<Attribute> it = op.getAttributes().iterator();

		while (it.hasNext()){
			Attribute a = it.next();
			Iterator<Attribute> itRel = input.getAttributes().iterator();
			while (itRel.hasNext()){
				Attribute attr = itRel.next();
				if (a.equals(attr)){
					output.addAttribute(new Attribute(attr));
				}
			}
		}

		op.setOutput(output);
		cost+=output.getTupleCount();
		//System.out.println(cost);

	}
	
	public void visit(Select op) {
		Relation input = op.getInput().getOutput();
		Relation output = null;

		Predicate p = op.getPredicate();
		Attribute leftAttr = input.getAttribute(p.getLeftAttribute());

		int val,total = input.getTupleCount();

		if(p.equalsValue()){
			// attr = val
			val = leftAttr.getValueCount();

			output = new Relation(total/val);

			Iterator<Attribute> it = input.getAttributes().iterator();
			while (it.hasNext()){
				Attribute attr = it.next();
				if (!attr.equals(leftAttr)){
					output.addAttribute(new Attribute(attr));
				}
			}
			
			output.addAttribute(new Attribute(leftAttr.getName(),Math.min(1,total)));
		} else {
			// attr = attr
			Attribute rightAttr = input.getAttribute(p.getRightAttribute());

			val = Math.max(leftAttr.getValueCount(),rightAttr.getValueCount());

			output = new Relation(total/val);

			Iterator<Attribute> it = input.getAttributes().iterator();
			while (it.hasNext()){
				Attribute attr = it.next();
				if (!attr.equals(leftAttr) && !attr.equals(rightAttr)){
					output.addAttribute(new Attribute(attr));
				}
			}

			int size = Math.min(output.getTupleCount(),Math.min(leftAttr.getValueCount(),rightAttr.getValueCount()));
			output.addAttribute(new Attribute(leftAttr.getName(),size));
			output.addAttribute(new Attribute(rightAttr.getName(),size));
		}

		op.setOutput(output);
		cost+=output.getTupleCount();
		//System.out.println(cost);

	}
	
	public void visit(Product op) {
		Relation inLeft = op.getLeft().getOutput();
		Relation inRight = op.getRight().getOutput();
		Relation output = new Relation(inLeft.getTupleCount()*inRight.getTupleCount());

		Iterator<Attribute> itLeft = inLeft.getAttributes().iterator();
		Iterator<Attribute> itRight = inRight.getAttributes().iterator();

		while (itLeft.hasNext()){
			output.addAttribute(new Attribute(itLeft.next()));
		}

		while (itRight.hasNext()){
			output.addAttribute(new Attribute(itRight.next()));
		}

		op.setOutput(output);
		cost+=output.getTupleCount();
		//System.out.println(cost);

	}
	
	public void visit(Join op) {
		Relation inLeft = op.getLeft().getOutput();
		Relation inRight = op.getRight().getOutput();
		Relation output;

		Predicate p = op.getPredicate();
		Attribute leftAttr = inLeft.getAttribute(p.getLeftAttribute());
		Attribute rigthAttr = inRight.getAttribute(p.getRightAttribute());

		int total = inLeft.getTupleCount()*inRight.getTupleCount();
		int val = Math.max(inLeft.getAttribute(p.getLeftAttribute()).getValueCount(),inRight.getAttribute(p.getRightAttribute()).getValueCount());

		output = new Relation(total/val);

		int size = Math.min(leftAttr.getValueCount(),rigthAttr.getValueCount());

		output.addAttribute(new Attribute(leftAttr.getName(),size));
		output.addAttribute(new Attribute(rigthAttr.getName(),size));

		Iterator<Attribute> it1 = inLeft.getAttributes().iterator();
		while (it1.hasNext()){
			Attribute attr = it1.next();
			if (!attr.equals(leftAttr)) {
				output.addAttribute(new Attribute(attr));
			}
		}

		Iterator<Attribute> it2 = inRight.getAttributes().iterator();
		while (it2.hasNext()){
			Attribute attr = it2.next();
			if (!attr.equals(rigthAttr)){
				output.addAttribute(new Attribute(attr));
			}
		}

		op.setOutput(output);
		cost+=output.getTupleCount();
		//System.out.println(cost);

	}

	protected int getCost(Operator o){
		cost=0;
		o.accept(this);
		return cost;
	}
}

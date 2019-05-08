package sjdb;

import org.w3c.dom.Attr;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class  Estimator implements PlanVisitor {

	protected int cost;

	public Estimator() {
	}

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

	// T(PROJECT_[A](R)) = T(R)
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

	// attr = val
	// T(SELECT_[A=C](R)) = T(R)/V(R,A)
	// V(SELECT_[A=C](R), A) = 1
	//
	// attr = attr
	// T(SELECT_[A=B](R)) = T(R)/max(V(R,A),V(R,B))
	// V(SELECT_[A=B](R), A) = V(SELECT_[A=B](R), B) = min(V(R, A), V(R, B)
	public void visit(Select op) {
		Relation input = op.getInput().getOutput();
		Relation output;

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

	// T(R * S) = T(R)T(S)
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

	//T(R JOIN_[A=B] S) = T(R)T(S)/max(V(R,A),V(S,B))
	// V(R JOIN_[A=B] S, A) = V(R JOIN_[A=B] S, B) = min(V(R, A), V(S, B))
	// V(R JOIN_[A=B] S, C) = V(R, C)
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

	// this method prints the estimate cost
	protected int getCost(Operator o){
		cost=0;
		o.accept(this);
		return cost;
	}
}

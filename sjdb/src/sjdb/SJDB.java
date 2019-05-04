/**
 * 
 */
package sjdb;
import java.io.*;

/**
 * @author nmg
 *
 */
public class SJDB {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// read serialised catalogue from file and parse
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		//String catFile = args[0];
		String catFile = in.readLine().trim();
		Catalogue cat = new Catalogue();
		System.out.println(1);
		CatalogueParser catParser = new CatalogueParser(catFile, cat);
		System.out.println(2);
		catParser.parse();
		System.out.println(3);
		
		// read stdin, parse, and build canonical query plan
		QueryParser queryParser = new QueryParser(cat, new InputStreamReader(System.in));
		System.out.println(4);
		Operator plan = queryParser.parse();
		System.out.println(5);
				
		// create estimator visitor and apply it to canonical plan
		//Estimator est = new Estimator();
		//plan.accept(est);
		
		// create optimised plan
		//Optimiser opt = new Optimiser(cat);
		//Operator optPlan = opt.optimise(plan);
	}

}

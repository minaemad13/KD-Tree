//Itai Epstein
//260896705
import java.util.ArrayList;
import java.util.Iterator;
public class KDTree implements Iterable<Datum>{ 

	KDNode 		rootNode;
	int    		k; 
	int			numLeaves;
	
	// constructor

	public KDTree(ArrayList<Datum> datalist) throws Exception {

		Datum[]  dataListArray  = new Datum[ datalist.size() ]; 

		if (datalist.size() == 0) {
			throw new Exception("Trying to create a KD tree with no data");
		}
		else
			this.k = datalist.get(0).x.length;

		int ct=0;
		for (Datum d :  datalist) {
			dataListArray[ct] = datalist.get(ct);
			ct++;
		}
		
	//   Construct a KDNode that is the root node of the KDTree.

		rootNode = new KDNode(dataListArray);
	}
	
	//   KDTree methods
	
	public Datum nearestPoint(Datum queryPoint) {
		return rootNode.nearestPointInNode(queryPoint);
	}
	

	public int height() {
		return this.rootNode.height();	
	}

	public int countNodes() {
		return this.rootNode.countNodes();	
	}
	
	public int size() {
		return this.numLeaves;	
	}

	//-------------------  helper methods for KDTree   ------------------------------

	public static long distSquared(Datum d1, Datum d2) {

		long result = 0;
		for (int dim = 0; dim < d1.x.length; dim++) {
			result +=  (d1.x[dim] - d2.x[dim])*((long) (d1.x[dim] - d2.x[dim]));
		}
		// if the Datum coordinate values are large then we can easily exceed the limit of 'int'.
		return result;
	}

	public double meanDepth(){
		int[] sumdepths_numLeaves =  this.rootNode.sumDepths_numLeaves();
		return 1.0 * sumdepths_numLeaves[0] / sumdepths_numLeaves[1];
	}
	
	class KDNode { 

		boolean leaf;
		Datum leafDatum;           //  only stores Datum if this is a leaf
		
		//  the next two variables are only defined if node is not a leaf

		int splitDim;      // the dimension we will split on
		int splitValue;    // datum is in low if value in splitDim <= splitValue, and high if value in splitDim > splitValue  

		KDNode lowChild, highChild;   //  the low and high child of a particular node (null if leaf)
		  //  You may think of them as "left" and "right" instead of "low" and "high", respectively

		KDNode(Datum[] datalist) throws Exception{

			/*
			 *  This method takes in an array of Datum and returns 
			 *  the calling KDNode object as the root of a sub-tree containing  
			 *  the above fields.
			 */

			//   ADD YOUR CODE BELOW HERE			
			if(datalist.length==1 || !findSplitValues(datalist)){
				lowChild=null;
				highChild=null;
				leaf = true;
				leafDatum=datalist[0];
				numLeaves+=1;
			}
			else{
				leaf = false;
				ArrayList<Datum> low = new ArrayList<Datum>();
				ArrayList<Datum> high = new ArrayList<Datum>();
				for (Datum datum : datalist) {
					if (datum.x[splitDim] <= splitValue) {
						low.add(datum);
					}
					else {
						high.add(datum);
					}
				}

				lowChild = new KDNode(low.toArray(Datum[]::new));
				highChild = new KDNode(high.toArray(Datum[]::new));
			}
			//   ADD YOUR CODE ABOVE HERE

		}

		public Datum nearestPointInNode(Datum queryPoint) {
			Datum nearestPoint, nearestPoint_otherSide;
		
			//   ADD YOUR CODE BELOW HERE
			if(this.leaf){
				return this.leafDatum;
			}
			KDNode goodSide, badSide;
			if(queryPoint.x[splitDim]<this.splitValue){
				goodSide=this.lowChild;
				badSide=this.highChild;
			}
			else{
				goodSide=this.highChild;
				badSide=this.lowChild;
			}
			nearestPoint=goodSide.nearestPointInNode(queryPoint);
			if(distSquared(nearestPoint,queryPoint)>Math.pow((Math.abs(queryPoint.x[splitDim]-splitValue)),2)){
				nearestPoint_otherSide=badSide.nearestPointInNode(queryPoint);
				if(distSquared(nearestPoint_otherSide,queryPoint)<distSquared(nearestPoint,queryPoint))
					nearestPoint=nearestPoint_otherSide;
			}
			return nearestPoint;
			//   ADD YOUR CODE ABOVE HERE

		}
		
		// -----------------  KDNode helper methods (might be useful for debugging) -------------------

		public boolean findSplitValues(Datum[] datalist){
			int realMax=0; int realMin=0; int realRange=0; int realDim=0;

			for(int i=0;i<k;i++) {
				int min = datalist[0].x[i];
				int max = datalist[0].x[i];
				for (int j = 1; j < datalist.length; j++) {
					int tmp = datalist[j].x[i];
					if (tmp > max) {
						max=tmp;
					} else if (tmp < min) {
						min = tmp;
					}
				}
				int range=max-min;
				if(range > realRange){
					realRange=range;
					realMax=max;
					realMin=min;
					realDim=i;
				}
			}
			if(realRange == 0){
				return false;
			}

			splitDim=realDim;
			int avg = (realMin + realMax) / 2;
			splitValue = avg;

			if(avg <= 0 && (realMax + realMin)%2 !=0 && (realMax + realMin) / 2.0 < 0) {
				splitValue--;
			}
			return true;
		}

		public int height() {
			if (this.leaf) 	
				return 0;
			else {
				return 1 + Math.max( this.lowChild.height(), this.highChild.height());
			}
		}

		public int countNodes() {
			if (this.leaf)
				return 1;
			else
				return 1 + this.lowChild.countNodes() + this.highChild.countNodes();
		}
		
		/*  
		 * Returns a 2D array of ints.  The first element is the sum of the depths of leaves
		 * of the subtree rooted at this KDNode.   The second element is the number of leaves
		 * this subtree.    Hence,  I call the variables  sumDepth_size_*  where sumDepth refers
		 * to element 0 and size refers to element 1.
		 */
				
		public int[] sumDepths_numLeaves(){
			int[] sumDepths_numLeaves_low, sumDepths_numLeaves_high;
			int[] return_sumDepths_numLeaves = new int[2];
			
			/*     
			 *  The sum of the depths of the leaves is the sum of the depth of the leaves of the subtrees, 
			 *  plus the number of leaves (size) since each leaf defines a path and the depth of each leaf 
			 *  is one greater than the depth of each leaf in the subtree.
			 */
			
			if (this.leaf) {  // base case
				return_sumDepths_numLeaves[0] = 0;
				return_sumDepths_numLeaves[1] = 1;
			}
			else {
				sumDepths_numLeaves_low  = this.lowChild.sumDepths_numLeaves();
				sumDepths_numLeaves_high = this.highChild.sumDepths_numLeaves();
				return_sumDepths_numLeaves[0] = sumDepths_numLeaves_low[0] + sumDepths_numLeaves_high[0] + sumDepths_numLeaves_low[1] + sumDepths_numLeaves_high[1];
				return_sumDepths_numLeaves[1] = sumDepths_numLeaves_low[1] + sumDepths_numLeaves_high[1];
			}	
			return return_sumDepths_numLeaves;
		}
		
	}

	public Iterator<Datum> iterator() {
		return new KDTreeIterator();
	}
	
	private class KDTreeIterator implements Iterator<Datum> {
		
		//   ADD YOUR CODE BELOW HERE
		ArrayList<Datum> leaves = new ArrayList<Datum>();

		private KDTreeIterator(){
			inOrderBT(rootNode);
		}

		private void inOrderBT(KDNode root){
			if(root==null){
				return;
			}
			else{
				inOrderBT(root.lowChild);
				if(root.leaf)
					leaves.add(root.leafDatum);
				inOrderBT(root.highChild);
			}

		}
		private int i=0;
		@Override
		public boolean hasNext() {
			return i<leaves.size() && leaves.get(i)!=null;
		}

		@Override
		public Datum next() {
			return leaves.get(i++);
		}
		//   ADD YOUR CODE ABOVE HERE

	}

}


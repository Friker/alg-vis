package algvis.bst;

import java.awt.Color;
import java.awt.Graphics;

import algvis.core.DataStructure;
import algvis.core.ExtremeNodePair;
import algvis.core.Node;
import algvis.core.View;

public class BSTNode extends Node {
	public int leftw, rightw;
	public BSTNode left = null, right = null, parent = null;

	// statistics
	public int size = 1, height = 1, sumh = 1;

	public int offset;	// distance to each son
	public boolean thread;	// if this is a leaf, contains a thread to another vertex?
	// for extreme nodes
	public int offpar = 0; // offset from a root of subtree
	public int level = -1;	// tree level
	
	public BSTNode(DataStructure D, int key, int x, int y) {
		super(D, key, x, y);
	}

	public BSTNode(DataStructure D, int key) {
		super(D, key);
	}

	public boolean isRoot() {
		return parent == null;
	}

	public boolean isLeaf() {
		return left == null && right == null;
	}

	public boolean isLeft() {
		return parent.left == this;
	}

	public void linkleft(BSTNode v) {
		left = v;
		if (v != null) {
			v.parent = this;
		}
	}

	public void linkright(BSTNode v) {
		right = v;
		if (v != null) {
			v.parent = this;
		}
	}

	public void isolate() {
		left = right = parent = null;
	}

	/**
	 * Calculate the height, size, and sum of heights of this node,
	 * assuming that this was already calculated for its children.
	 */
	public void calc() {
		int ls = 0, rs = 0, lh = 0, rh = 0, lsh = 0, rsh = 0;
		if (left != null) {
			ls = left.size;
			lh = left.height;
			lsh = left.sumh;
		}
		if (right != null) {
			rs = right.size;
			rh = right.height;
			rsh = right.sumh;
		}
		size = ls + rs + 1;
		height = Math.max(lh, rh) + 1;
		sumh = lsh + rsh + size;
	}

	/**
	 * Calculate the height, size, and sum of heights for all the nodes in this subtree
	 * (recursively bottom-up).
	 */
	public void calcTree() {
		if (left != null) {
			left.calcTree();
		}
		if (right != null) {
			right.calcTree();
		}
		calc();
	}

	public void setArc() {
		setArc(parent);
	}

	public void drawTree(Graphics g, View v) {
		if (left != null) {
			g.setColor(Color.black);
			v.drawLine(g, x, y, left.x, left.y);
			left.drawTree(g, v);
		}
		if (right != null) {
			g.setColor(Color.black);
			v.drawLine(g, x, y, right.x, right.y);
			right.drawTree(g, v);
		}
		draw(g, v);
	}

	public void moveTree() {
		if (left != null) {
			left.moveTree();
		}
		if (right != null) {
			right.moveTree();
		}
		move();
	}

	/**
	 * Create an (imaginary) box around the subtree rooted at this node.
	 * Calculate the width from the node to the left side (leftw)
	 * and the width from the node to the right side (rightw).
	 * Assumption: this box has already been created for both children. 
	 */
	public void rebox() {
		// if there is a left child, leftw = width of the box enclosing the whole left subtree,
		// i.e., leftw+rightw; otherwise the width is the node radius plus some additional
		// space called xspan
		leftw = (left == null) ? D.xspan + D.radius : left.leftw + left.rightw;
		// rightw is computed analogically
		rightw = (right == null) ? D.xspan + D.radius : right.leftw
				+ right.rightw;
	}

	/**
	 * Rebox the whole subtree calculating the widths recursively bottom-up.
	 */
	public void reboxTree() {
		if (left != null) {
			left.reboxTree();
		}
		if (right != null) {
			right.reboxTree();
		}
		rebox();
	}

	/**
	 * Calculate the coordinates of each node from the widths of boxes
	 * around them and direct the nodes to their new positions. 
	 */
	private void repos() {
		if (isRoot()) {
			goToRoot();
			D.x1 = -leftw;
			D.x2 = rightw;
			D.y2 = this.toy;
			// System.out.println ("r" + key + " " +leftw +"  "+ rightw);
		}
		if (this.toy > D.y2) {
			D.y2 = this.toy;
		}
		if (left != null) {
			left.goTo(this.tox - left.rightw, this.toy + 2 * D.radius
							+ D.yspan);
			left.repos();
		}
		if (right != null) {
			right.goTo(this.tox + right.leftw, this.toy + 2 * D.radius
					+ D.yspan);
			right.repos();
		}
	}

	public ExtremeNodePair reposition() {
//		reboxTree();
//		repos();
		ExtremeNodePair EP = setup(this, 0);
		petrify(this, 0);
		return EP;
	}
	
	/**
	 * Find the node at coordinates (x,y).
	 * This is used to identify the node that has been clicked by user.
	 */
	public BSTNode find(int x, int y) {
		if (inside(x,y)) return this;
		if (left != null) {
			BSTNode tmp = left.find(x, y);
			if (tmp != null) return tmp;
		}
		if (right != null) {
			return right.find(x, y);
		}
		return null;
	}
	
	public int abs(int x) {
		return x > 0 ? x : -x;
	}
	
	public  ExtremeNodePair setup(BSTNode T,	/* "this" is a root of subtree */
			int level		/* current overall level */
			) /* return extreme descendants */ {
	
		/* this procedure implements algorithm TR, assigning relative
		 * positionings to all nodes in the tree pointed to by parameter TT.
		 */
		BSTNode L, R;	// left & right son
		ExtremeNodePair LPair = null, RPair = null;
		int cursep;		// separation on current level
		int rootsep = 0;	// current separation
		int loffsum, roffsum;	// offset from L & R to T
		
		ExtremeNodePair result = new ExtremeNodePair();
		
		if (this == null) {	// avoid selecting as extreme
			result.left.level = -1;
			result.right.level = -1;
		} else {
			int minsep = 20;
			T.goTo(T.tox, level*D.yspan*5); 
			//this.y = level*D.yspan;
			L =  this.left;		// follows contour of left subtree
			R =  this.right;		// follows contour of right subtree
			if (L != null)	LPair = L.setup( L, level+1 );	// position subtrees recursively
			if (R != null)  RPair = R.setup( R, level+1 );
			if ((R == null) && (L == null)) {	// leaf
				result.right.addr =  this;		// a leaf is both the leftmost
				result.left.addr =  this;		// and rightmost node on the
				result.right.level = level;	// lowest level of the subtree
				result.left.level = level;	// consisting of itself
				result.right.offset = 0;
				result.left.offset = 0;
			} else {		// T is not a leaf
				/* set up for subtree pushing
				 * place roots of subtrees minimum distance apart
				 */
				
				cursep = minsep;
				rootsep = minsep;
				loffsum = 0;
				roffsum = 0;
				
				/* now consider each level in turn until one
				 * subtree is exhausted, pushing the subtrees
				 * apart when necessary
				 */
				
				while ((L != null) && (R != null)) {
					if (cursep < minsep) {
						rootsep += minsep-cursep;
						cursep = minsep;
					}
					
					// advance L & R
					if (L.right != null) {
						loffsum += L.offset;
						cursep -= L.offset;
						L =  L.right;
					} else {
						loffsum -= L.offset;
						cursep += L.offset;
						L =  L.left;
					}
					if (R.left != null) {
						roffsum -= R.offset;
						cursep -= R.offset;
						R =  R.left;
					} else {
						roffsum += R.offset;
						cursep += R.offset;
						R =  R.right;
					}
				}
				
				/* set the offset in node T, and include it
				 * in accumulated offsets for L and R
				 */
				
				this.offset = (rootsep+10)/2;
				loffsum -= this.offset;
				roffsum += this.offset;
				
				/* update extreme descendant information
				 */
				
				if (RPair == null) {
					RPair = new ExtremeNodePair();
				}
				
				if (LPair == null) {
					LPair = new ExtremeNodePair();
				}
				
				if ((RPair.left.level > LPair.left.level) || (this.left == null)) {
					result.left = RPair.left;
					result.left.offset += this.offset;
				} else {
					result.left = LPair.left;
					result.left.offset -= this.offset;
				}
				if ((LPair.right.level > RPair.right.level) || (this.right == null)) {
					result.right = LPair.right;
					result.right.offset -= this.offset;
				} else {
					result.right = RPair.right;
					result.right.offset += this.offset;
				}
				
				/* if subtrees of T were of uneven heights,
				 * check to see if threading is necessary.
				 * at most one thread needs to be inserted
				 */
				
				if ((L != null) && (L != this.left)) {
					RPair.right.addr.thread = true;
					RPair.right.addr.offset = abs( (RPair.right.offset + this.offset) - loffsum);
					if ((loffsum - this.offset) <= RPair.right.offset) {
						RPair.right.addr.left = L;
					} else {
						RPair.right.addr.right = L;
					}
				} else if ((R != null) && (R != this.right)) {
					LPair.left.addr.thread = true;
					LPair.left.addr.offset = abs( (LPair.left.offset - this.offset) - roffsum);
					if ((roffsum + this.offset) >= LPair.left.offset) {
						LPair.left.addr.right = R;
					} else {
						LPair.left.addr.left = R;
					}
				}
			}
		}
		return result;
	}
	
	public void petrify(BSTNode T, int column) {
		/* this procedure performs a preorder traversal of the tree,
		 * converting the relative offsets to absolute coordinates.
		 */
		
		if ( T != null ) {
			//T.x = column;
			T.goTo(column, T.toy); /*level*D.yspan*5);*/
			if ( T.thread ) {
				T.thread = false;
				T.right = null;	// threaded node must be a leaf
				T.left = null;
			}
			petrify(T.left, column - T.offset);
			petrify(T.right, column + T.offset);
		}
	}

}

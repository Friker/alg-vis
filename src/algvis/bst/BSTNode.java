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

	public int offset; // distance to each son
	public boolean thread; // if this is a leaf, contains a thread to another
							// vertex?
	// for extreme nodes
	public int offpar = 0; // offset from a root of subtree
	public int level = -1; // tree level

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
	 * Calculate the height, size, and sum of heights of this node, assuming
	 * that this was already calculated for its children.
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
	 * Calculate the height, size, and sum of heights for all the nodes in this
	 * subtree (recursively bottom-up).
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
	 * Calculate the width from the node to the left side (leftw) and the width
	 * from the node to the right side (rightw). Assumption: this box has
	 * already been created for both children.
	 */
	public void rebox() {
		// if there is a left child, leftw = width of the box enclosing the
		// whole left subtree,
		// i.e., leftw+rightw; otherwise the width is the node radius plus some
		// additional
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
	 * Calculate the coordinates of each node from the widths of boxes around
	 * them and direct the nodes to their new positions.
	 */
	@SuppressWarnings("unused")
	private void repos() {
		if (isRoot()) {
			goToRoot();
			D.x1 = -leftw;
			D.x2 = rightw;
			D.y2 = toy;
			// System.out.println ("r" + key + " " +leftw +"  "+ rightw);
		}
		if (toy > D.y2) {
			D.y2 = toy;
		}
		if (left != null) {
			left.goTo(tox - left.rightw, toy + 2 * D.radius + D.yspan);
			left.repos();
		}
		if (right != null) {
			right.goTo(tox + right.leftw, toy + 2 * D.radius + D.yspan);
			right.repos();
		}
	}

	public ExtremeNodePair reposition() {
		// reboxTree();
		// repos();
		ExtremeNodePair EP = setup(0);
		petrify(0);
		return EP;
	}

	/**
	 * Find the node at coordinates (x,y). This is used to identify the node
	 * that has been clicked by user.
	 */
	public BSTNode find(int x, int y) {
		if (inside(x, y))
			return this;
		if (left != null) {
			BSTNode tmp = left.find(x, y);
			if (tmp != null)
				return tmp;
		}
		if (right != null) {
			return right.find(x, y);
		}
		return null;
	}

	/**
	 * This procedure implements algorithm TR, assigning relative positionings
	 * to all nodes in the tree rooted by "this" node.
	 */
	public ExtremeNodePair setup(int level /* current overall level */) {

		ExtremeNodePair LPair = new ExtremeNodePair(), RPair = new ExtremeNodePair();
		ExtremeNodePair result = new ExtremeNodePair();

		goTo(tox, level * 29 /* D.yspan * 5 */); // D.yspan is not enough

		// follows contour of left/right subtree
		BSTNode L = left, R = right;
		// position subtrees recursively
		if (L != null)
			LPair = L.setup(level + 1);
		if (R != null)
			RPair = R.setup(level + 1);
		// extreme descendants are set, threads are set
		if (isLeaf()) { // "this" is a leaf
			result.right.addr = this; // a leaf is both the leftmost
			result.left.addr = this; // and rightmost node on the
			result.right.level = level; // lowest level of the subtree
			result.left.level = level; // consisting of itself
			result.right.off = 0;
			result.left.off = 0;
		} else { // "this" is not a leaf
			/*
			 * set up for subtree pushing place roots of subtrees minimum
			 * distance apart
			 */

			// D.xspan is not enough
			int minsep = /* D.radius * 2 + */2 * D.xspan;
			// separation on current level = against its brother
			int cursep = minsep;
			// current separation at "this" node, accumulates when 2 nodes of
			// same levels are interfering
			int rootsep = minsep;
			// offset from L & R to "this"
			int loffsum = 0, roffsum = 0;

			/*
			 * now consider each level in turn until one subtree is exhausted,
			 * pushing the subtrees apart when necessary
			 */

			while ((L != null) && (R != null)) {
				if (cursep < minsep) {
					rootsep += minsep - cursep;
					cursep = minsep;
				}

				/*
				 * advance L & R right contour of L subtree is closer to left
				 * contour of R subtree and vice versa.
				 */
				if (L.right != null) {
					loffsum += L.offset;
					cursep -= L.offset;
					L = L.right;
				} else {
					loffsum -= L.offset;
					cursep += L.offset;
					L = L.left;
				}
				// if (L != null)
				// System.out.print(L.offset + "\n");
				if (R.left != null) {
					roffsum -= R.offset;
					cursep -= R.offset;
					R = R.left;
				} else {
					roffsum += R.offset;
					cursep += R.offset;
					R = R.right;
				}
				// if (R != null)
				// System.out.print(R.offset + "\n");

			}

			/*
			 * set the offset in node "this", and include it in accumulated
			 * offsets for L and R
			 */

			offset = (rootsep + D.xspan) / 2;
			loffsum -= offset;
			roffsum += offset;

			/*
			 * update extreme descendant information
			 */

			if ((RPair.left.level > LPair.left.level) || (left == null)) {
				result.left = RPair.left;
				result.left.off += offset;
			} else {
				result.left = LPair.left;
				result.left.off -= offset;
			}
			if ((LPair.right.level > RPair.right.level) || (right == null)) {
				result.right = LPair.right;
				result.right.off -= offset;
			} else {
				result.right = RPair.right;
				result.right.off += offset;
			}

			/*
			 * if subtrees of T were of uneven heights, check to see if
			 * threading is necessary. at most one thread needs to be inserted
			 */

			if ((L != null) && (L != left)) {
				RPair.right.addr.thread = true;
				RPair.right.addr.offset = Math.abs((RPair.right.off + offset)
						- loffsum);
				if ((loffsum - offset) <= RPair.right.off) {
					RPair.right.addr.left = L;
				} else {
					RPair.right.addr.right = L;
				}
			} else if ((R != null) && (R != right)) {
				LPair.left.addr.thread = true;
				LPair.left.addr.offset = Math.abs((LPair.left.off - offset)
						- roffsum);
				if ((roffsum + offset) >= LPair.left.off) {
					LPair.left.addr.right = R;
				} else {
					LPair.left.addr.left = R;
				}
			}
		}
		return result;
	}

	/**
	 * this procedure performs a preorder traversal of the tree, converting the
	 * relative offsets to absolute coordinates, deletes threads and sets bounds
	 * of data structure
	 */
	public void petrify(int column) {

		if (isRoot()) {
			D.x1 = column;
			D.x2 = column;
			D.y1 = toy;
			D.y2 = toy;
		} else {
			if (column < D.x1) D.x1 = column;
			if (column > D.x2) D.x2 = column;
			if (toy > D.y2) D.y2 = toy;
			if (toy < D.y1) D.y1 = toy;
		}
		goTo(column, toy); /* level*D.yspan*5); */
		if (thread) {
			thread = false;
			right = null; // threaded node must be a leaf
			left = null;
		}
		if (left != null)
			left.petrify(column - offset);
		if (right != null)
			right.petrify(column + offset);
	}

}

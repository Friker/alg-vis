package algvis.core;

import algvis.bst.BSTNode;


public class ExtremeNodeData {
	public BSTNode addr;	// adress
	public int offset;		// offset from root
	public int level;		// tree level
	public ExtremeNodeData() {
		addr = null;
		offset = 0;
		level = 0;
	}

}

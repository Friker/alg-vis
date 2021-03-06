/*******************************************************************************
 * Copyright (c) 2012 Jakub Kováč, Katarína Kotrlová, Pavol Lukča, Viktor Tomkovič, Tatiana Tóthová
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package algvis.ds.suffixtree;

import algvis.core.DataStructure;
import algvis.core.Settings;
import algvis.ui.VisPanel;

public class SuffixTreePanel extends VisPanel {
	private static final long serialVersionUID = -8652425842838569507L;
	public static Class<? extends DataStructure> DS = SuffixTree.class;

	public SuffixTreePanel(Settings S) {
		super(S);
	}

	@Override
	public void initDS() {
		D = new SuffixTree(this);
		scene.add(D);
		buttons = new SuffixTreeButtons(this);
	}

	@Override
	public void start() {
		super.start();
		final boolean p = pauses;
		pauses = false;
		((SuffixTree) D).insert("BANANA$");
		D.start(new Runnable() {
			@Override
			public void run() {
				pauses = p;
			}
		});
		screen.V.miny = -70;
		screen.V.resetView();
	}
}

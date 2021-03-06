/**
* Copyright (c) 2009-2012, Regents of the University of Colorado
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
* Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
*/
package com.googlecode.clearnlp.conversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.googlecode.clearnlp.constituent.CTLib;
import com.googlecode.clearnlp.constituent.CTNode;
import com.googlecode.clearnlp.constituent.CTTree;
import com.googlecode.clearnlp.dependency.DEPNode;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.headrule.HeadRule;
import com.googlecode.clearnlp.headrule.HeadRuleMap;
import com.googlecode.clearnlp.headrule.HeadTagSet;
import com.googlecode.clearnlp.morphology.MPLib;
import com.googlecode.clearnlp.reader.AbstractColumnReader;

/**
 * Abstract constituent to dependency converter.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code choijd@colorado.edu})
 */
abstract public class AbstractC2DConverter
{
	protected HeadRuleMap m_headrules;
	
	public AbstractC2DConverter(HeadRuleMap headrules)
	{
		m_headrules = headrules;
	}
	
	/**
	 * Sets the head of the specific node and all its sub-nodes.
	 * Calls {@link AbstractC2DConverter#findHeads(CTNode)}.
	 * @param curr the node to process.
	 */
	protected void setHeads(CTNode curr)
	{
		// terminal nodes become the heads of themselves
		if (!curr.isPhrase())
		{
			curr.c2d = new C2DInfo(curr);
			return;
		}
		
		// set the heads of all children
		for (CTNode child : curr.getChildren())
			setHeads(child);
		
		// stop traversing if it is the top node
		if (curr.isPTag(CTLib.PTAG_TOP))
			return;
		
		// only one child
		if (curr.getChildrenSize() == 1)
		{
			curr.c2d = new C2DInfo(curr.getChild(0));
			return;
		}
		
		// find the headrule of the current node
		HeadRule rule = m_headrules.get(curr.pTag);
				
		if (rule == null)
		{
			System.err.println("Error: headrules not found for \""+curr.pTag+"\"");
			rule = m_headrules.get(CTLib.PTAG_X);
		}			
				
		setHeadsAux(rule, curr);
	}
	
	/**
	 * Returns the head of the specific node list according to the specific headrule.
	 * Every other node in the list becomes the dependent of the head node.
	 * @param rule the headrule to be consulted.
	 * @param nodes the list of nodes.
	 * @param flagSize the number of head flags.
	 * @return the head of the specific node list according to the specific headrule.
	 */
	protected CTNode getHead(HeadRule rule, List<CTNode> nodes, int flagSize)
	{
		nodes = new ArrayList<CTNode>(nodes);
		if (rule.isRightToLeft())	Collections.reverse(nodes);
		
		int i, size = nodes.size(), flag;
		int[] flags = new int[size];
		
		for (i=0; i<size; i++)
			flags[i] = getHeadFlag(nodes.get(i));
		
		CTNode head = null, child;
		
		outer: for (flag=0; flag<flagSize; flag++)
		{
			for (HeadTagSet tagset : rule.getHeadTags())
			{
				for (i=0; i<size; i++)
				{
					child = nodes.get(i);
					
					if (flags[i] == flag && tagset.matches(child))
					{
						head = child;
						break outer;
					}
				}
			}
		}

		if (head == null)
		{
			System.err.println("Error: head not found.");
			System.exit(1);
		}
		
		CTNode parent = head.getParent();
		
		for (CTNode node : nodes)
		{
			if (node != head && !node.c2d.hasHead())
				node.c2d.setHead(head, getDEPLabel(node, parent, head));
		}
		
		return head;
	}
	
	/** @return the dependency tree converted from the specific constituent tree without head information. */
	protected DEPTree initDEPTree(CTTree cTree)
	{
		DEPTree dTree = new DEPTree();
		String form, lemma, pos;
		DEPNode dNode;
		int id;
		
		for (CTNode node : cTree.getTokens())
		{
			id    = node.getTokenId() + 1;
			form  = MPLib.revertBracket(node.form);
			lemma = AbstractColumnReader.BLANK_COLUMN;
			pos   = node.pTag;
			
			dNode = new DEPNode(id, form, lemma, pos, node.c2d.d_feats);
			dTree.add(dNode);
		}
		
		dTree.initXHeads();
		return dTree;
	}
	
	/**
	 * Sets the head of the specific phrase node.
	 * This is a helper method of {@link AbstractC2DConverter#setHeads(CTNode)}.
	 * @param rule the headrule to the specific node.
	 * @param curr the phrase node.
	 */
	abstract protected void setHeadsAux(HeadRule rule, CTNode curr);
	
	/**
	 * Returns the head flag of the specific constituent node.
	 * @param child the constituent node.
	 * @return the head flag of the specific constituent node.
	 */
	abstract protected int getHeadFlag(CTNode child);
	
	/**
	 * Returns a dependency label given the specific phrase structure.
	 * @param C the current node.
	 * @param P the parent of {@code C}.
	 * @param p the head of {@code P}.
	 * @return a dependency label given the specific phrase structure.
	 */
	abstract protected String getDEPLabel(CTNode C, CTNode P, CTNode p);
	
	/**
	 * Returns the dependency tree converted from the specific constituent tree.
	 * If the constituent tree contains only empty categories, returns {@code null}.
	 * @param cTree the constituent tree to convert.
	 * @return the dependency tree converted from the specific constituent tree.
	 */
	abstract public DEPTree toDEPTree(CTTree cTree);
}

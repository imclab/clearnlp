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
package com.googlecode.clearnlp.dependency.srl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.googlecode.clearnlp.util.pair.StringIntPair;


/**
 * Compare two dependency-based semantic role labeling outputs.
 * @author Jinho D. Choi
 * <b>Last update:</b> 4/19/2011
 */
public class SRLEval
{
	private final String HLINE = "------------------------------------------------------------";
	
	private final int IDX_CORRECT = 0;
	private final int IDX_AUTO    = 1;
	private final int IDX_GOLD    = 2;
	
	static public final String UAS  = "UAS";
	static public final String LAS  = "LAS";
	static public final String ARGN = "ARGN";
	static public final String ARGM = "ARGM";
	
	private HashMap<String,int[]> m_counts;
	
	public SRLEval()
	{
		init();
	}
	
	public void init()
	{
		m_counts = new HashMap<String, int[]>();
		
		m_counts.put(UAS , new int[3]);
		m_counts.put(LAS , new int[3]);
		m_counts.put(ARGN, new int[3]);
		m_counts.put(ARGM, new int[3]);
	}

	public void evaluate(StringIntPair[][] gold, StringIntPair[][] auto)
	{
		StringIntPair[] gHeads, aHeads;
		int i, size = gold.length;
		
		for (i=1; i<size; i++)
		{
			gHeads = gold[i];
			aHeads = auto[i];
			
			evaluate(gHeads, aHeads);
		}
	}
	
	public void evaluate(StringIntPair[] gold, StringIntPair[] auto)
	{
		int[] uas  = m_counts.get(UAS);
		int[] las  = m_counts.get(LAS);
		int[] argn = m_counts.get(ARGN);
		int[] argm = m_counts.get(ARGM);
		int[] arg;
		
		boolean isArgn;
		
		for (StringIntPair gHead : gold)
		{
			isArgn = SRLLib.isNumberedArgument(gHead.s);
			if (isArgn)	argn[IDX_GOLD]++;
			else		argm[IDX_GOLD]++;
			
			arg = getArray(gHead.s);
			arg[IDX_GOLD]++;
			
			for (StringIntPair aHead : auto)
			{
				if (gHead.i == aHead.i)
				{
					uas[IDX_CORRECT]++;
					
					if (gHead.s.equals(aHead.s))
					{
						las[IDX_CORRECT]++;
						arg[IDX_CORRECT]++;
						
						if (isArgn)	argn[IDX_CORRECT]++;
						else		argm[IDX_CORRECT]++;
					}
				}
			}
		}
		
		for (StringIntPair aHead : auto)
		{
			isArgn = SRLLib.isNumberedArgument(aHead.s);
			if (isArgn)	argn[IDX_AUTO]++;
			else		argm[IDX_AUTO]++;
			
			arg = getArray(aHead.s);
			arg[IDX_AUTO]++;
		}

		uas[IDX_AUTO] += auto.length;
		uas[IDX_GOLD] += gold.length;
		las[IDX_AUTO] += auto.length;
		las[IDX_GOLD] += gold.length;
	}
	
	private int[] getArray(String label)
	{
		if (label.startsWith("C-"))
			label = label.substring(2);
		
		if (m_counts.containsKey(label))
		{
			return m_counts.get(label);
		}
		else
		{
			int[] counts = new int[3];
			m_counts.put(label, counts);
			
			return counts;
		}
	}
	
	public void print()
	{
		printOverall();
		
		ArrayList<String> labels = new ArrayList<String>(m_counts.keySet());
		Collections.sort(labels);

		int total = getTotalCount();
		
		for (String label : labels)
		{
			if (!(label.equals(UAS) || label.equals(LAS) || label.equals(ARGN) || label.equals(ARGM)))
				printLabel(label, total);
		}
		
		System.out.println(HLINE);
	}
	
	public void printOverall()
	{
		int total = getTotalCount();
		
		System.out.println(HLINE);
		System.out.printf("%10s%10s%10s%10s%10s%10s\n", "Label", "Count", "Dist.", "P", "R", "F1");
		
		System.out.println(HLINE);
		printLabel(UAS, total);
		printLabel(LAS, total);
		
		System.out.println(HLINE);
		printLabel(ARGN, total);
		printLabel(ARGM, total);
		
		System.out.println(HLINE);
	}
	
	private void printLabel(String label, int total)
	{
		int[] counts = m_counts.get(label);
		int     auto = counts[IDX_AUTO];
		int     gold = counts[IDX_GOLD];
		double  dist = 100d * gold / total;
		
		double precision = (auto == 0) ? 0 : 100d * counts[IDX_CORRECT] / auto;
		double recall    = (gold == 0) ? 0 : 100d * counts[IDX_CORRECT] / gold;
		double f1        = getF1(precision, recall);
		
		System.out.printf("%10s%10d%10.2f%10.2f%10.2f%10.2f\n", label, gold, dist, precision, recall, f1);
	}
	
	private int getTotalCount()
	{
		return m_counts.get(UAS)[IDX_GOLD];
	}
	
	public double getF1(String label)
	{
		int[] counts = m_counts.get(label);
		int     auto = counts[IDX_AUTO];
		int     gold = counts[IDX_GOLD];
		
		double precision = (auto == 0) ? 0 : 100d * counts[IDX_CORRECT] / auto;
		double recall    = (gold == 0) ? 0 : 100d * counts[IDX_CORRECT] / gold;
		
		return getF1(precision, recall);
	}
	
	static public double getF1(double precision, double recall)
	{
		return (precision + recall == 0) ? 0 : 2 * (precision * recall) / (precision + recall);
	}
}

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
package com.googlecode.clearnlp.classification.train;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.googlecode.clearnlp.classification.model.StringModel;
import com.googlecode.clearnlp.classification.vector.SparseFeatureVector;
import com.googlecode.clearnlp.classification.vector.StringFeatureVector;
import com.googlecode.clearnlp.util.pair.Pair;


/**
 * Train space containing string vectors.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code choijd@colorado.edu})
 */
public class StringTrainSpace extends AbstractTrainSpace
{
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());
	
	/** Casted from {@likn AbstractTrainSpace#m_model}. */
	private StringModel s_model;
	/** The label count cutoff (exclusive). */
	private int l_cutoff;
	/** The feature count cutoff (exclusive). */
	private int f_cutoff;
	/** The list of all training instances. */
	private List<Pair<String,StringFeatureVector>> s_instances;
	/** The map between labels and their counts. */
	private ObjectIntOpenHashMap<String> m_labels;
	/** The map between features and their counts. */
	private Map<String,ObjectIntOpenHashMap<String>> m_features;
	
	/**
	 * Constructs a train space containing string vectors.
	 * @param hasWeight {@code true} if features are assigned with different weights.
	 * @param labelCutoff the label count cutoff (exclusive).
	 * @param featureCutoff the feature count cutoff (exclusive).
	 */
	public StringTrainSpace(boolean hasWeight, int labelCutoff, int featureCutoff)
	{
		super(new StringModel(), hasWeight);
		
		s_model     = (StringModel)m_model;
		l_cutoff    = labelCutoff;
		f_cutoff    = featureCutoff;
		s_instances = new ArrayList<Pair<String,StringFeatureVector>>();
		m_labels    = new ObjectIntOpenHashMap<String>();
		m_features  = new HashMap<String, ObjectIntOpenHashMap<String>>();
	}
	
	public void printInstances(PrintStream fout)
	{
		int i, size = s_instances.size();
		String[] instances = new String[size];
		Pair<String,StringFeatureVector> p;
		
		for (i=0; i<size; i++)
		{
			p = s_instances.get(i);
			instances[i] = p.o1+DELIM_COL+p.o2.toString();	
		}
		
		Arrays.sort(instances);
		
		for (String instance : instances)
			fout.println(instance);
	}
	
	/**
	 * Adds a training instance to this space.
	 * @param label the label to be added.
	 * @param vector the feature vector to be added.
	 */
	public void addInstance(String label, StringFeatureVector vector)
	{
		addLexica(label, vector);
		s_instances.add(new Pair<String, StringFeatureVector>(label, vector));
	}
	
	/**
	 * Adds a training instance to this space.
	 * @param line {@code <label>}{@link AbstractTrainSpace#DELIM_COL}{@link StringFeatureVector#toString()}.
	 */
	public void addInstance(String line)
	{
		Pair<String,StringFeatureVector> instance = toInstance(line, b_weight);
		addInstance(instance.o1, instance.o2);
	}
	
	public void appendSpace(StringTrainSpace space)
	{
		appendSpaceLabels(space);
		appendSpaceFeatures(space);
		appendSpaceInstances(space);
	}
	
	private void appendSpaceLabels(StringTrainSpace space)
	{
		ObjectIntOpenHashMap<String> mLabels = space.m_labels;
		String label;
		
		for (ObjectCursor<String> cur : mLabels.keys())
		{
			label = cur.value;
			m_labels.put(label, m_labels.get(label) + mLabels.get(label));
		}
	}
	
	private void appendSpaceFeatures(StringTrainSpace space)
	{
		Map<String,ObjectIntOpenHashMap<String>> mFeatures = space.m_features;
		ObjectIntOpenHashMap<String> tMap, sMap;
		String value;
		
		for (String type : mFeatures.keySet())
		{
			sMap = mFeatures.get(type);
			
			if (m_features.containsKey(type))
			{
				tMap = m_features.get(type);
				
				for (ObjectCursor<String> cur : sMap.keys())
				{
					value = cur.value;
					tMap.put(value, tMap.get(value) + sMap.get(value));
				}
			}
			else
				m_features.put(type, sMap);
		}
	}
	
	private void appendSpaceInstances(StringTrainSpace space)
	{
		s_instances.addAll(space.s_instances);
	}
	
	public void clear()
	{
		s_instances.clear();
		m_labels   .clear();
		m_features .clear();
	}
	
	/** 
	 * Called by {@link StringTrainSpace#addInstance(String, StringFeatureVector)}.
	 * Called by {@link StringTrainSpace#addInstance(String)}.
	 */
	private void addLexica(String label, StringFeatureVector vector)
	{
		addLexicaLabel(label);
		addLexicaFeatures(vector);
	}
	
	private void addLexicaLabel(String label)
	{
		m_labels.put(label, m_labels.get(label)+1);
	}
	
	private void addLexicaFeatures(StringFeatureVector vector)
	{
		ObjectIntOpenHashMap<String> map;
		int i, size = vector.size();
		String type, value;
		
		for (i=0; i<size; i++)
		{
			type  = vector.getType(i);
			value = vector.getValue(i);
			
			if (m_features.containsKey(type))
			{
				map = m_features.get(type);
				map.put(value, map.get(value)+1);
			}
			else
			{
				map = new ObjectIntOpenHashMap<String>();
				map.put(value, 1);
				m_features.put(type, map);
			}
		}
	}
	
	@Override
	public void build(boolean clearInstances)
	{
		LOG.info("Building:\n");
		initModelMaps();
		
		Pair<String,StringFeatureVector> instance;
		int y, i, size = s_instances.size();
		SparseFeatureVector x;
		
		for (i=0; i<size; i++)
		{
			instance = s_instances.get(i);
			
			if ((y = s_model.getLabelIndex(instance.o1)) < 0)
				continue;
			
			x = s_model.toSparseFeatureVector(instance.o2);
			
			a_ys.add(y);
			a_xs.add(x.getIndices());
			if (b_weight)	a_vs.add(x.getWeights());
		}
		
		a_ys.trimToSize();
		a_xs.trimToSize();
		if (b_weight)	a_vs.trimToSize();
		
		LOG.info("- # of labels   : "+s_model.getLabelSize()+"\n");
		LOG.info("- # of features : "+s_model.getFeatureSize()+"\n");
		LOG.info("- # of instances: "+a_ys.size()+"\n");
		
		if (clearInstances)	s_instances.clear();
	}
	
	@Override
	public void build()
	{
		build(true);
	}
	
	/** Called by {@link StringTrainSpace#build()}. */
	private void initModelMaps()
	{
		// initialize label map
		String label;
		
		for (ObjectCursor<String> cur : m_labels.keys())
		{
			label = cur.value;

			if (m_labels.get(label) > l_cutoff)
				s_model.addLabel(label);
		}
		
		s_model.initLabelArray();
		
		// initialize feature map
		ObjectIntOpenHashMap<String> map;
		String value;
		
		for (String type : m_features.keySet())
		{
			map = m_features.get(type);
			
			for (ObjectCursor<String> cur : map.keys())
			{
				value = cur.value;
				
				if (map.get(value) > f_cutoff)
					s_model.addFeature(type, value);
			}
		}
		
	/*	for (String label : UTHppc.getSortedKeys(m_labels))
		{
			if (m_labels.get(label) > l_cutoff)
				s_model.addLabel(label);
		}
		
		s_model.initLabelArray();
		
		// initialize feature map
		List<String> types = new ArrayList<String>(m_features.keySet());
		ObjectIntOpenHashMap<String> map;
		Collections.sort(types);
		
		for (String type : types)
		{
			map = m_features.get(type);
			
			for (String value : UTHppc.getSortedKeys(map))
			{
				if (map.get(value) > f_cutoff)
					s_model.addFeature(type, value);
			}
		}*/
	}
	
	/** Pair of label and feature vector. */
	static public Pair<String,StringFeatureVector> toInstance(String line, boolean hasWeight)
	{
		String[] tmp = line.split(DELIM_COL);
		String label = tmp[0];
		
		StringFeatureVector vector = new StringFeatureVector(hasWeight);
		int i, size = tmp.length;
		
		for (i=1; i<size; i++)
			vector.addFeature(tmp[i]);
		
		return new Pair<String,StringFeatureVector>(label, vector);
	}
}

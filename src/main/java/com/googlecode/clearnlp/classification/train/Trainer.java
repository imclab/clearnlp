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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.clearnlp.classification.algorithm.AbstractAlgorithm;
import com.googlecode.clearnlp.classification.model.AbstractModel;


/**
 * Trainer.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code choijd@colorado.edu})
 */
public class Trainer
{
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());
	
	private AbstractTrainSpace     t_space;
	private AbstractAlgorithm      a_algorithm;
	volatile private AbstractModel m_model;
	
	public Trainer(AbstractTrainSpace space, AbstractAlgorithm algorithm, int numThreads)
	{
		t_space     = space;
		a_algorithm = algorithm;
		m_model     = space.getModel();
		
		LOG.info("Training:\n");
		m_model.initWeightVector();
		
		if (space.isBinaryLabel())
			trainBinary();
		else
			trainMulti(numThreads);
	}
	
	private void trainBinary()
	{
		double[] weights = a_algorithm.getWeight(t_space, 0);
		m_model.copyWeightVector(weights);
	}
	
	private void trainMulti(int numThreads)
	{
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		
		int currLabel, size = t_space.getLabelSize();
		
		for (currLabel=0; currLabel<size; currLabel++)
			executor.execute(new TrainTask(currLabel));
		
		executor.shutdown();
		
		try
		{
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		}
		catch (InterruptedException e) {e.printStackTrace();}
	}
	
	class TrainTask implements Runnable
	{
		/** The current label to train */
		int curr_label;
		
		/**
		 * Trains one-vs-all model.
		 * @param currLabel the current label to train.
		 */
		public TrainTask(int currLabel)
		{
			curr_label = currLabel;
		}
		
		public void run()
		{
			double[] weights = a_algorithm.getWeight(t_space, curr_label);
			m_model.copyWeightVector(curr_label, weights);
		}
    }
}

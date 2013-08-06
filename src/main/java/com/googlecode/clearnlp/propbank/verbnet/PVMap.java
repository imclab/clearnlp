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
package com.googlecode.clearnlp.propbank.verbnet;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.googlecode.clearnlp.io.FileExtFilter;
import com.googlecode.clearnlp.util.UTXml;


@SuppressWarnings("serial")
public class PVMap extends HashMap<String,PVVerb>
{
	static public final String VERB_EXT 	= "-v.xml";
	static public final String E_PBVNMAP	= "pbvnmap";
	static public final String E_FRAMESET	= "frameset";
	static public final String E_VERB		= "verb";
	static public final String E_ROLESET	= "roleset";
	static public final String E_ROLES		= "roles";
	static public final String E_ROLE		= "role";
	static public final String E_VNROLE		= "vnrole";
	
	/**
	 * Constructs a PropBank to VerbNet map from the specific input-stream.
	 * @param in the input-stream from a PB2VN mapping file.
	 */
	public PVMap(InputStream in)
	{
		try
		{
			DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dFactory.newDocumentBuilder();
			Document doc = builder.parse(in);
			
			Element ePVMap = (Element)doc.getElementsByTagName(PVMap.E_PBVNMAP).item(0);
			NodeList list  = ePVMap.getElementsByTagName(E_VERB);
			int i, size = list.getLength();
			Element eVerb;
			String  lemma;
			PVVerb  pvVerb;
			
			for (i=0; i<size; i++)
			{
				eVerb  = (Element)list.item(i);
				lemma  = UTXml.getTrimmedAttribute(eVerb, PVVerb.ATTR_LEMMA);
				pvVerb = new PVVerb(eVerb, lemma, true);
				
				if (!pvVerb.isEmpty())	put(lemma, pvVerb);
			}			
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	/**
	 * Constructs a PropBank to VerbNet map from the specific directory containing PropBank frameset files.
	 * @param framesetDir the directory containing PropBank frameset files.
	 */
	public PVMap(String framesetDir)
	{
		try
		{
			String[] filelist = new File(framesetDir).list(new FileExtFilter(VERB_EXT));
			DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dFactory.newDocumentBuilder();
			Document doc;
			
			Element eFrameset;
			String  lemma;
			PVVerb  pvVerb;
			
			for (String framesetFile : filelist)
			{
				doc       = builder.parse(new FileInputStream(framesetDir+"/"+framesetFile));
				eFrameset = (Element)doc.getElementsByTagName(PVMap.E_FRAMESET).item(0);
				lemma     = framesetFile.substring(0, framesetFile.length()-VERB_EXT.length());
				pvVerb    = new PVVerb(eFrameset, lemma, false);
				
				if (!pvVerb.isEmpty())	put(lemma, pvVerb);
			}
		}
		catch (Exception e) {e.printStackTrace();}		
	}
	
	public PVRoleset getRoleset(String rolesetId)
	{
		String lemma  = rolesetId.substring(0, rolesetId.lastIndexOf("."));
		PVVerb pvVerb = get(lemma);
		
		return (pvVerb != null)	? pvVerb.get(rolesetId) : null;
	}
	
	public void print(PrintStream out)
	{
		out.println(UTXml.startsElement(false, PVMap.E_PBVNMAP));
		
		List<String> lemmas = new ArrayList<String>(keySet());
		Collections.sort(lemmas);
		
		for (String lemma : lemmas)
			out.println(get(lemma));
		
		out.println(UTXml.endsElement(PVMap.E_PBVNMAP));
	}
}

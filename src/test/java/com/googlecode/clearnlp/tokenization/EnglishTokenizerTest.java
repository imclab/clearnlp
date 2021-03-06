/**
* Copyright 2012-2013 University of Massachusetts Amherst
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.googlecode.clearnlp.tokenization;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.zip.ZipInputStream;

/**
 * @since 1.1.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class EnglishTokenizerTest
{
//	@Test
	public void testTokenize() throws FileNotFoundException
	{
		EnglishTokenizer tok = new EnglishTokenizer(new ZipInputStream(new FileInputStream("src/main/resources/model/dictionary-1.2.0.zip")));
		String src, trg;
		
		// spaces
		src = "a b  c\n d \t\n\r\fe";
		trg = "[a, b, c, d, e]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		// emoticons
		src = ":-))))))) :------------) (____) :( :-) :--)";
		trg = "[:-))))))), :------------), (____), :(, :-), :, --, )]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		// URLs
		src = "|http://www.google.com|www.google.com|mailto:somebody@google.com|some-body@google+.com|";
		trg = "[|, http://www.google.com, |, www.google.com, |, mailto:somebody@google.com, |, some-body@google+.com, |]";
		assertEquals(tok.getTokens(src).toString(), trg);
	
		src = "google.com index.html a.b.htm ab-cd.shtml";
		trg = "[google.com, index.html, a.b.htm, ab-cd.shtml]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		// abbreviations
		src = "prof. ph.d. a. a.b. a.b a.b.c. ab.cd";
		trg = "[prof., ph.d., a., a.b., a.b, a.b.c., ab, ., cd]";
		assertEquals(tok.getTokens(src).toString(), trg);
				
		// consecutive punctuation
		src = "A..B!!C??D.!?E.!?.!?F..!!??";
		trg = "[A, .., B, !!, C, ??, D, .!?, E, .!?.!?, F, ..!!??]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = ",,A---C*D**E~~~~F==";
		trg = "[,,, A, ---, C*D, **, E, ~~~~, F, ==]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		// dots in numbers
		src = ".1 a.1 2.3 4,5 6:7 8-9 0/1 '2 3's 3'4 5'b a'6 a'b";
		trg = "[.1, a.1, 2.3, 4,5, 6:7, 8-9, 0/1, '2, 3's, 3'4, 5'b, a'6, a'b]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = ".a a.3 4,a a:a a8-9 0/1a";
		trg = "[., a, a.3, 4, ,, a, a, :, a, a8-9, 0/1a]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		// hyphens
		src = "dis-able cross-validation o-kay art-o-torium s-e-e art-work";
		trg = "[dis-able, cross-validation, o-kay, art-o-torium, s-e-e, art, -, work]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		// apostrophies
		src = "he's we'd I'm you'll they're I've didn't did'nt";
		trg = "[he, 's, we, 'd, I, 'm, you, 'll, they, 're, I, 've, did, n't, did, 'nt]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = "he'S DON'T gue'ss";
		trg = "[he, 'S, DO, N'T, gue'ss]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = "aint cannot don'cha d'ye i'mma dunno";
		trg = "[ai, nt, can, not, do, n', cha, d', ye, i, 'm, ma, du, n, no]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = "$1 E2 L3 USD1 2KPW ||$1 USD1..";
		trg = "[$, 1, E2, L3, USD, 1, 2, KPW, |, |, $, 1, USD, 1, ..]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = "1m 2mm 3kg 4oz";
		trg = "[1, m, 2, mm, 3, kg, 4, oz]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = "1D 2nM 3CM 4LB";
		trg = "[1, D, 2, nM, 3, CM, 4, LB]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = "(1){2}[3]<4>";
		trg = "[(, 1, ), {, 2, }, [, 3, ], <, 4, >]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = "`a'b,c:d;e-f/g\"h'";
		trg = "[`, a'b, ,, c, :, d, ;, e, -, f, /, g, \", h, ']";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = "`a'b,c:d;e-f/g\"h'";
		trg = "[`, a'b, ,, c, :, d, ;, e, -, f, /, g, \", h, ']";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = "a@b #c$d%e&f|g";
		trg = "[a@b, #, c, $, d, %, e, &, f, |, g]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = "e.g., i.e, (e.g.,";
		trg = "[e.g., ,, i.e, ,, (, e.g., ,]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = " \n \t";
		trg = "[]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = "\"John & Mary's dog,\" Jane thought (to herself).\n" + "\"What a #$%!\n" + "a- ``I like AT&T''.\"";
		trg = "[\", John, &, Mary, 's, dog, ,, \", Jane, thought, (, to, herself, ), ., \", What, a, #, $, %, !, a, -, ``, I, like, AT&T, '', ., \"]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = "I said at 4:45pm.";
		trg = "[I, said, at, 4:45, pm, .]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = "I can't believe they wanna keep 40% of that.\"``Whatcha think?''\"I don't --- think so...,\"";
		trg = "[I, ca, n't, believe, they, wan, na, keep, 40, %, of, that, ., \", ``, What, cha, think, ?, '', \", I, do, n't, ---, think, so, ..., ,, \"]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = "You `paid' US$170,000?!\nYou should've paid only$16.75.";
		trg = "[You, `, paid, ', US$, 170,000, ?!, You, should, 've, paid, only, $, 16.75, .]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
		src = " 1. Buy a new Chevrolet (37%-owned in the U.S..) . 15%";
		trg = "[1, ., Buy, a, new, Chevrolet, (, 37, %, -, owned, in, the, U.S., ., ), ., 15, %]";
		assertEquals(tok.getTokens(src).toString(), trg);
		
	//	System.out.println(tok.getTokens(src).toString());
	//	src = "He said, \"I'd like to know Mr. Choi.\" He's the owner of ClearNLP.";
	//	for (String t : tok.getTokens(src))	System.out.println(t);
	}
}

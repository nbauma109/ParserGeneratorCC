/**
 * Copyright 2017-2018 Philip Helger, pgcc@helger.com
 *
 * Copyright 2011 Google Inc. All Rights Reserved.
 * Author: sreeni@google.com (Sreeni Viswanadha)
 *
 * Copyright (c) 2006, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
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
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.helger.pgcc.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.helger.commons.string.StringHelper;
import com.helger.pgcc.PGVersion;
import com.helger.pgcc.output.UnsupportedOutputLanguageException;

/**
 * This package contains data created as a result of parsing and semanticizing a
 * JavaCC input file. This data is what is used by the back-ends of JavaCC as
 * well as any other back-end of JavaCC related tools such as JJTree.
 */
public final class JavaCCGlobals
{
  /**
   * String that identifies the JavaCC generated files.
   */
  public static final String s_toolName = "ParserGeneratorCC";

  /**
   * The name of the grammar file being processed.
   */
  public static String s_fileName;

  /**
   * The name of the original file (before processing by JJTree). Currently this
   * is the same as fileName.
   */
  public static String s_origFileName;

  /**
   * Set to true if this file has been processed by JJTree.
   */
  public static boolean s_jjtreeGenerated;

  /**
   * The list of tools that have participated in generating the input grammar
   * file.
   */
  public static List <String> s_toolNames;

  /**
   * This prints the banner line when the various tools are invoked. This takes
   * as argument the tool's full name and its version.
   */
  public static void bannerLine (final String fullName, final String ver)
  {
    System.out.print ("ParserGenerator Version " + PGVersion.s_versionNumber + " (" + fullName);
    if (StringHelper.hasText (ver))
      System.out.print (" Version " + ver);
    System.out.println (")");
  }

  /**
   * The name of the parser class (what appears in PARSER_BEGIN and PARSER_END).
   */
  public static String s_cu_name;

  /**
   * This is a list of tokens that appear after "PARSER_BEGIN(name)" all the way
   * until (but not including) the opening brace "{" of the class "name".
   */
  public static final List <Token> s_cu_to_insertion_point_1 = new ArrayList <> ();

  /**
   * This is the list of all tokens that appear after the tokens in
   * "cu_to_insertion_point_1" and until (but not including) the closing brace
   * "}" of the class "name".
   */
  public static final List <Token> s_cu_to_insertion_point_2 = new ArrayList <> ();

  /**
   * This is the list of all tokens that appear after the tokens in
   * "cu_to_insertion_point_2" and until "PARSER_END(name)".
   */
  public static final List <Token> s_cu_from_insertion_point_2 = new ArrayList <> ();

  /**
   * A list of all grammar productions - normal and JAVACODE - in the order they
   * appear in the input file. Each entry here will be a subclass of
   * "NormalProduction".
   */
  public static final List <NormalProduction> s_bnfproductions = new ArrayList <> ();

  /**
   * A symbol table of all grammar productions - normal and JAVACODE. The symbol
   * table is indexed by the name of the left hand side non-terminal. Its
   * contents are of type "NormalProduction".
   */
  public static final Map <String, NormalProduction> s_production_table = new HashMap <> ();

  /**
   * A mapping of lexical state strings to their integer internal
   * representation. Integers are stored as java.lang.Integer's.
   */
  public static final Map <String, Integer> s_lexstate_S2I = new HashMap <> ();

  /**
   * A mapping of the internal integer representations of lexical states to
   * their strings. Integers are stored as java.lang.Integer's.
   */
  public static final Map <Integer, String> s_lexstate_I2S = new HashMap <> ();

  /**
   * The declarations to be inserted into the TokenManager class.
   */
  public static List <Token> s_token_mgr_decls;

  /**
   * The list of all TokenProductions from the input file. This list includes
   * implicit TokenProductions that are created for uses of regular expressions
   * within BNF productions.
   */
  public static final List <TokenProduction> s_rexprlist = new ArrayList <> ();

  /**
   * The total number of distinct tokens. This is therefore one more than the
   * largest assigned token ordinal.
   */
  public static int s_tokenCount;

  /**
   * This is a symbol table that contains all named tokens (those that are
   * defined with a label). The index to the table is the image of the label and
   * the contents of the table are of type "RegularExpression".
   */
  public static final Map <String, RegularExpression> s_named_tokens_table = new HashMap <> ();

  /**
   * Contains the same entries as "named_tokens_table", but this is an ordered
   * list which is ordered by the order of appearance in the input file.
   */
  public static final List <RegularExpression> s_ordered_named_tokens = new ArrayList <> ();

  /**
   * A mapping of ordinal values (represented as objects of type "Integer") to
   * the corresponding labels (of type "String"). An entry exists for an ordinal
   * value only if there is a labeled token corresponding to this entry. If
   * there are multiple labels representing the same ordinal value, then only
   * one label is stored.
   */
  public static final Map <Integer, String> s_names_of_tokens = new HashMap <> ();

  /**
   * A mapping of ordinal values (represented as objects of type "Integer") to
   * the corresponding RegularExpression's.
   */
  public static final Map <Integer, RegularExpression> rexps_of_tokens = new HashMap <> ();

  /**
   * This is a three-level symbol table that contains all simple tokens (those
   * that are defined using a single string (with or without a label). The index
   * to the first level table is a lexical state which maps to a second level
   * hashtable. The index to the second level hashtable is the string of the
   * simple token converted to upper case, and this maps to a third level
   * hashtable. This third level hashtable contains the actual string of the
   * simple token and maps it to its RegularExpression.
   */
  public static final Map <String, Map <String, Map <String, RegularExpression>>> s_simple_tokens_table = new HashMap <> ();

  /**
   * maskindex, jj2index, maskVals are variables that are shared between
   * ParseEngine and ParseGen.
   */
  protected static int s_maskindex = 0;
  protected static int s_jj2index = 0;
  public static boolean s_lookaheadNeeded;
  protected static final List <int []> s_maskVals = new ArrayList <> ();

  static Action s_actForEof;
  static String s_nextStateForEof;
  static Token s_otherLanguageDeclTokenBeg;
  static Token s_otherLanguageDeclTokenEnd;

  // Some general purpose utilities follow.

  /**
   * Returns the identifying string for the file name, given a toolname used to
   * generate it.
   */
  public static String getIdString (final String toolName, final String fileName)
  {
    final List <String> toolNames = new ArrayList <> ();
    toolNames.add (toolName);
    return getIdString (toolNames, fileName);
  }

  /**
   * Returns the identifying string for the file name, given a set of tool names
   * that are used to generate it.
   */
  public static String getIdString (final List <String> toolNames, final String fileName)
  {
    final String toolNamePrefix = "Generated by: " + StringHelper.getImploded ('&', toolNames) + ":";

    if (toolNamePrefix.length () > 200)
    {
      System.out.println ("Tool names too long.");
      throw new IllegalStateException ("Tool names too long: " + toolNamePrefix);
    }

    return toolNamePrefix + " Do not edit this line. " + addUnicodeEscapes (fileName);
  }

  /**
   * Returns true if tool name passed is one of the tool names returned by
   * getToolNames(fileName).
   */
  public static boolean isGeneratedBy (final String toolName, final String fileName)
  {
    final List <String> v = getToolNames (fileName);

    for (int i = 0; i < v.size (); i++)
      if (toolName.equals (v.get (i)))
        return true;

    return false;
  }

  private static List <String> _makeToolNameList (final String str)
  {
    final List <String> retVal = new ArrayList <> ();

    int limit1 = str.indexOf ('\n');
    if (limit1 == -1)
      limit1 = 1000;
    int limit2 = str.indexOf ('\r');
    if (limit2 == -1)
      limit2 = 1000;
    final int limit = (limit1 < limit2) ? limit1 : limit2;

    String tmp;
    if (limit == 1000)
    {
      tmp = str;
    }
    else
    {
      tmp = str.substring (0, limit);
    }

    if (tmp.indexOf (':') == -1)
      return retVal;

    tmp = tmp.substring (tmp.indexOf (':') + 1);

    if (tmp.indexOf (':') == -1)
      return retVal;

    tmp = tmp.substring (0, tmp.indexOf (':'));

    int i = 0, j = 0;

    while (j < tmp.length () && (i = tmp.indexOf ('&', j)) != -1)
    {
      retVal.add (tmp.substring (j, i));
      j = i + 1;
    }

    if (j < tmp.length ())
      retVal.add (tmp.substring (j));

    return retVal;
  }

  /**
   * Returns a List of names of the tools that have been used to generate the
   * given file.
   */
  public static List <String> getToolNames (final String fileName)
  {
    final char [] buf = new char [256];
    int total = 0;

    try (final FileReader stream = new FileReader (fileName))
    {
      int read;
      while (true)
        if ((read = stream.read (buf, total, buf.length - total)) != -1)
        {
          total += read;
          if (total == buf.length)
            break;
        }
        else
          break;

      return _makeToolNameList (new String (buf, 0, total));
    }
    catch (final FileNotFoundException e1)
    {}
    catch (final IOException e2)
    {
      if (total > 0)
        return _makeToolNameList (new String (buf, 0, total));
    }

    return new ArrayList <> ();
  }

  public static void createOutputDir (final File outputDir)
  {
    if (!outputDir.exists ())
    {
      JavaCCErrors.warning ("Output directory \"" + outputDir + "\" does not exist. Creating the directory.");

      if (!outputDir.mkdirs ())
      {
        JavaCCErrors.semantic_error ("Cannot create the output directory : " + outputDir);
        return;
      }
    }

    if (!outputDir.isDirectory ())
    {
      JavaCCErrors.semantic_error ("\"" + outputDir + " is not a valid output directory.");
      return;
    }

    if (!outputDir.canWrite ())
    {
      JavaCCErrors.semantic_error ("Cannot write to the output output directory : \"" + outputDir + "\"");
      return;
    }
  }

  public static String staticOpt ()
  {
    if (Options.isStatic ())
    {
      return "static ";
    }
    return "";
  }

  @Nonnull
  public static String addEscapes (@Nonnull final String str)
  {
    final StringBuilder retval = new StringBuilder (str.length () * 2);
    for (final char ch : str.toCharArray ())
    {
      if (ch == '\b')
      {
        retval.append ("\\b");
      }
      else
        if (ch == '\t')
        {
          retval.append ("\\t");
        }
        else
          if (ch == '\n')
          {
            retval.append ("\\n");
          }
          else
            if (ch == '\f')
            {
              retval.append ("\\f");
            }
            else
              if (ch == '\r')
              {
                retval.append ("\\r");
              }
              else
                if (ch == '\"')
                {
                  retval.append ("\\\"");
                }
                else
                  if (ch == '\'')
                  {
                    retval.append ("\\\'");
                  }
                  else
                    if (ch == '\\')
                    {
                      retval.append ("\\\\");
                    }
                    else
                      if (ch < 0x20 || ch > 0x7e)
                      {
                        final String s = "0000" + Integer.toString (ch, 16);
                        retval.append ("\\u").append (s.substring (s.length () - 4, s.length ()));
                      }
                      else
                      {
                        retval.append (ch);
                      }
    }
    return retval.toString ();
  }

  public static String addUnicodeEscapes (final String str)
  {
    switch (Options.getOutputLanguage ())
    {
      case JAVA:
      {
        final StringBuilder retval = new StringBuilder (str.length () * 2);
        for (final char ch : str.toCharArray ())
        {
          if (ch < 0x20 ||
              ch > 0x7e /* || ch == '\\' -- cba commented out 20140305 */ )
          {
            final String s = "0000" + Integer.toString (ch, 16);
            retval.append ("\\u").append (s.substring (s.length () - 4, s.length ()));
          }
          else
          {
            retval.append (ch);
          }
        }
        return retval.toString ();
      }
      case CPP:
        return str;
      default:
        throw new UnsupportedOutputLanguageException (Options.getOutputLanguage ());
    }
  }

  public static int s_cline;
  public static int s_ccol;

  protected static void printTokenSetup (final Token t)
  {
    Token tt = t;
    while (tt.specialToken != null)
      tt = tt.specialToken;
    s_cline = tt.beginLine;
    s_ccol = tt.beginColumn;
  }

  protected static void printTokenOnly (final Token t, final PrintWriter ostr)
  {
    for (; s_cline < t.beginLine; s_cline++)
    {
      ostr.println ("");
      s_ccol = 1;
    }
    for (; s_ccol < t.beginColumn; s_ccol++)
    {
      ostr.print (" ");
    }
    if (t.kind == JavaCCParserConstants.STRING_LITERAL || t.kind == JavaCCParserConstants.CHARACTER_LITERAL)
      ostr.print (addUnicodeEscapes (t.image));
    else
      ostr.print (t.image);
    s_cline = t.endLine;
    s_ccol = t.endColumn + 1;
    final char last = t.image.charAt (t.image.length () - 1);
    if (last == '\n' || last == '\r')
    {
      s_cline++;
      s_ccol = 1;
    }
  }

  public static void printToken (final Token t, final PrintWriter ostr)
  {
    Token tt = t.specialToken;
    if (tt != null)
    {
      while (tt.specialToken != null)
        tt = tt.specialToken;
      while (tt != null)
      {
        printTokenOnly (tt, ostr);
        tt = tt.next;
      }
    }
    printTokenOnly (t, ostr);
  }

  protected static void printTokenList (final List <Token> list, final PrintWriter ostr)
  {
    Token t = null;
    for (final Iterator <Token> it = list.iterator (); it.hasNext ();)
    {
      t = it.next ();
      printToken (t, ostr);
    }

    if (t != null)
      printTrailingComments (t);
  }

  protected static void printLeadingComments (final Token t, final PrintWriter ostr)
  {
    if (t.specialToken == null)
      return;
    Token tt = t.specialToken;
    while (tt.specialToken != null)
      tt = tt.specialToken;
    while (tt != null)
    {
      printTokenOnly (tt, ostr);
      tt = tt.next;
    }
    if (s_ccol != 1 && s_cline != t.beginLine)
    {
      ostr.println ("");
      s_cline++;
      s_ccol = 1;
    }
  }

  public static String printTokenOnly (final Token t)
  {
    String retval = "";
    for (; s_cline < t.beginLine; s_cline++)
    {
      retval += "\n";
      s_ccol = 1;
    }
    for (; s_ccol < t.beginColumn; s_ccol++)
    {
      retval += " ";
    }
    if (t.kind == JavaCCParserConstants.STRING_LITERAL || t.kind == JavaCCParserConstants.CHARACTER_LITERAL)
      retval += addUnicodeEscapes (t.image);
    else
      retval += t.image;
    s_cline = t.endLine;
    s_ccol = t.endColumn + 1;
    final char last = t.image.charAt (t.image.length () - 1);
    if (last == '\n' || last == '\r')
    {
      s_cline++;
      s_ccol = 1;
    }
    return retval;
  }

  public static String printToken (final Token t)
  {
    String ret = "";
    Token tt = t.specialToken;
    if (tt != null)
    {
      while (tt.specialToken != null)
        tt = tt.specialToken;
      while (tt != null)
      {
        ret += printTokenOnly (tt);
        tt = tt.next;
      }
    }
    ret += printTokenOnly (t);
    return ret;
  }

  protected static String printLeadingComments (final Token t)
  {
    String retval = "";
    if (t.specialToken == null)
      return retval;
    Token tt = t.specialToken;
    while (tt.specialToken != null)
      tt = tt.specialToken;
    while (tt != null)
    {
      retval += printTokenOnly (tt);
      tt = tt.next;
    }
    if (s_ccol != 1 && s_cline != t.beginLine)
    {
      retval += "\n";
      s_cline++;
      s_ccol = 1;
    }
    return retval;
  }

  protected static String printTrailingComments (final Token t)
  {
    if (t.next == null)
      return "";
    return printLeadingComments (t.next);
  }

  public static void reInitStatic ()
  {
    s_fileName = null;
    s_origFileName = null;
    s_jjtreeGenerated = false;
    s_toolNames = null;
    s_cu_name = null;
    s_cu_to_insertion_point_1.clear ();
    s_cu_to_insertion_point_2.clear ();
    s_cu_from_insertion_point_2.clear ();
    s_bnfproductions.clear ();
    s_production_table.clear ();
    s_lexstate_S2I.clear ();
    s_lexstate_I2S.clear ();
    s_token_mgr_decls = null;
    s_rexprlist.clear ();
    s_tokenCount = 0;
    s_named_tokens_table.clear ();
    s_ordered_named_tokens.clear ();
    s_names_of_tokens.clear ();
    rexps_of_tokens.clear ();
    s_simple_tokens_table.clear ();
    s_maskindex = 0;
    s_jj2index = 0;
    s_maskVals.clear ();
    s_cline = 0;
    s_ccol = 0;
    s_actForEof = null;
    s_nextStateForEof = null;
  }

  static String getFileExtension ()
  {
    switch (Options.getOutputLanguage ())
    {
      case JAVA:
        return ".java";
      case CPP:
        return ".cc";
      default:
        throw new UnsupportedOutputLanguageException (Options.getOutputLanguage ());
    }
  }

  /**
   * Replaces all backslahes with double backslashes.
   */
  public static String replaceBackslash (final String str)
  {
    if (str.indexOf ('\\') < 0)
    {
      // No backslash found.
      return str;
    }

    final StringBuilder b = new StringBuilder (str.length () * 2);
    for (final char c : str.toCharArray ())
      if (c == '\\')
        b.append ("\\\\");
      else
        b.append (c);

    return b.toString ();
  }
}

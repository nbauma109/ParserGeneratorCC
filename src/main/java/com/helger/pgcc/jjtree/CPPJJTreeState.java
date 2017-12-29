// Copyright 2011 Google Inc. All Rights Reserved.
// Author: sreeni@google.com (Sreeni Viswanadha)

package com.helger.pgcc.jjtree;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.helger.pgcc.Version;
import com.helger.pgcc.parser.Options;
import com.helger.pgcc.parser.OutputFile;

/**
 * Generate the State of a tree.
 */
final class CPPJJTreeState
{
  static final String JJTStateVersion = Version.majorDotMinor;

  private CPPJJTreeState ()
  {}

  static void generateTreeState () throws IOException
  {
    final Map <String, Object> options = new HashMap <> (Options.getOptions ());
    options.put (Options.NONUSER_OPTION__PARSER_NAME, JJTreeGlobals.parserName);

    final String filePrefix = new File (JJTreeOptions.getJJTreeOutputDirectory (),
                                        "JJT" + JJTreeGlobals.parserName + "State").getAbsolutePath ();

    OutputFile outputFile = new OutputFile (new File (filePrefix + ".h"), JJTStateVersion, new String [0]);
    CPPNodeFiles.generateFile (outputFile, "/templates/cpp/JJTTreeState.h.template", options, true);

    outputFile = new OutputFile (new File (filePrefix + ".cc"), JJTStateVersion, new String [0]);
    CPPNodeFiles.generateFile (outputFile, "/templates/cpp/JJTTreeState.cc.template", options, true);
  }
}

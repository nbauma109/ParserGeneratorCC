// Copyright 2011 Google Inc. All Rights Reserved.
// Author: sreeni@google.com (Sreeni Viswanadha)

package com.helger.pgcc.jjtree;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import com.helger.pgcc.parser.JavaCCGlobals;
import com.helger.pgcc.parser.Options;

public class CPPCodeGenerator extends DefaultJJTreeVisitor
{
  @Override
  public Object defaultVisit (final SimpleNode node, final Object data)
  {
    visit ((JJTreeNode) node, data);
    return null;
  }

  @Override
  public Object visit (final ASTGrammar node, final Object data)
  {
    final IO io = (IO) data;
    io.println ("/*@bgen(jjtree) " +
                JavaCCGlobals.getIdString (JJTreeGlobals.toolList, new File (io.getOutputFileName ()).getName ()) +
                (Options.booleanValue (Options.USEROPTION__CPP_IGNORE_ACTIONS) ? "" : " */"));
    io.print ((Options.booleanValue (Options.USEROPTION__CPP_IGNORE_ACTIONS) ? "" : "/*") + "@egen*/");

    return node.childrenAccept (this, io);
  }

  @Override
  public Object visit (final ASTBNFAction node, final Object data)
  {
    final IO io = (IO) data;
    /*
     * Assume that this action requires an early node close, and then try to
     * decide whether this assumption is false. Do this by looking outwards
     * through the enclosing expansion units. If we ever find that we are
     * enclosed in a unit which is not the final unit in a sequence we know that
     * an early close is not required.
     */

    final NodeScope ns = NodeScope.getEnclosingNodeScope (node);
    if (ns != null && !ns.isVoid ())
    {
      boolean needClose = true;
      final Node sp = node.getScopingParent (ns);

      JJTreeNode n = node;
      while (true)
      {
        final Node p = n.jjtGetParent ();
        if (p instanceof ASTBNFSequence || p instanceof ASTBNFTryBlock)
        {
          if (n.getOrdinal () != p.jjtGetNumChildren () - 1)
          {
            /* We're not the final unit in the sequence. */
            needClose = false;
            break;
          }
        }
        else
          if (p instanceof ASTBNFZeroOrOne || p instanceof ASTBNFZeroOrMore || p instanceof ASTBNFOneOrMore)
          {
            needClose = false;
            break;
          }
        if (p == sp)
        {
          /* No more parents to look at. */
          break;
        }
        n = (JJTreeNode) p;
      }
      if (needClose)
      {
        openJJTreeComment (io, null);
        io.println ();
        insertCloseNodeAction (ns, io, getIndentation (node));
        closeJJTreeComment (io);
      }
    }

    return visit ((JJTreeNode) node, io);
  }

  @Override
  public Object visit (final ASTBNFDeclaration node, final Object data)
  {
    final IO io = (IO) data;
    if (!node.node_scope.isVoid ())
    {
      String indent = "";
      if (TokenUtils.hasTokens (node))
      {
        for (int i = 1; i < node.getFirstToken ().beginColumn; ++i)
        {
          indent += " ";
        }
      }
      else
      {
        indent = "  ";
      }

      openJJTreeComment (io, node.node_scope.getNodeDescriptorText ());
      io.println ();
      insertOpenNodeCode (node.node_scope, io, indent);
      closeJJTreeComment (io);
    }

    return visit ((JJTreeNode) node, io);
  }

  @Override
  public Object visit (final ASTBNFNodeScope node, final Object data)
  {
    final IO io = (IO) data;
    if (node.node_scope.isVoid ())
    {
      return visit ((JJTreeNode) node, io);
    }

    final String indent = getIndentation (node.expansion_unit);

    openJJTreeComment (io, node.node_scope.getNodeDescriptor ().getDescriptor ());
    io.println ();
    tryExpansionUnit (node.node_scope, io, indent, node.expansion_unit);
    return null;
  }

  @Override
  public Object visit (final ASTCompilationUnit node, final Object data)
  {
    final IO io = (IO) data;
    Token t = node.getFirstToken ();
    while (true)
    {
      node.print (t, io);
      if (t == node.getLastToken ())
        break;
      if (t.kind == JJTreeParserConstants._PARSER_BEGIN)
      {
        // eat PARSER_BEGIN "(" <ID> ")"
        node.print (t.next, io);
        node.print (t.next.next, io);
        node.print (t = t.next.next.next, io);
      }

      t = t.next;
    }
    return null;
  }

  @Override
  public Object visit (final ASTExpansionNodeScope node, final Object data)
  {
    final IO io = (IO) data;
    final String indent = getIndentation (node.expansion_unit);
    openJJTreeComment (io, node.node_scope.getNodeDescriptor ().getDescriptor ());
    io.println ();
    insertOpenNodeAction (node.node_scope, io, indent);
    tryExpansionUnit (node.node_scope, io, indent, node.expansion_unit);

    // Print the "whiteOut" equivalent of the Node descriptor to preserve
    // line numbers in the generated file.
    ((ASTNodeDescriptor) node.jjtGetChild (1)).jjtAccept (this, io);
    return null;
  }

  @Override
  public Object visit (final ASTJavacodeBody node, final Object data)
  {
    final IO io = (IO) data;
    if (node.node_scope.isVoid ())
    {
      return visit ((JJTreeNode) node, io);
    }

    final Token first = node.getFirstToken ();

    String indent = "";
    for (int i = 4; i < first.beginColumn; ++i)
    {
      indent += " ";
    }

    openJJTreeComment (io, node.node_scope.getNodeDescriptorText ());
    io.println ();
    insertOpenNodeCode (node.node_scope, io, indent);
    tryTokenSequence (node.node_scope, io, indent, first, node.getLastToken ());
    return null;
  }

  public Object visit (final ASTLHS node, final Object data)
  {
    final IO io = (IO) data;
    final NodeScope ns = NodeScope.getEnclosingNodeScope (node);

    /*
     * Print out all the tokens, converting all references to `jjtThis' into the
     * current node variable.
     */
    final Token first = node.getFirstToken ();
    final Token last = node.getLastToken ();
    for (Token t = first; t != last.next; t = t.next)
    {
      TokenUtils.print (t, io, "jjtThis", ns.getNodeVariable ());
    }

    return null;
  }

  /*
   * This method prints the tokens corresponding to this node recursively
   * calling the print methods of its children. Overriding this print method in
   * appropriate nodes gives the output the added stuff not in the input.
   */

  public Object visit (final JJTreeNode node, final Object data)
  {
    final IO io = (IO) data;
    /*
     * Some productions do not consume any tokens. In that case their first and
     * last tokens are a bit strange.
     */
    if (node.getLastToken ().next == node.getFirstToken ())
    {
      return null;
    }

    final Token t1 = node.getFirstToken ();
    Token t = new Token ();
    t.next = t1;
    JJTreeNode n;
    for (int ord = 0; ord < node.jjtGetNumChildren (); ord++)
    {
      n = (JJTreeNode) node.jjtGetChild (ord);
      while (true)
      {
        t = t.next;
        if (t == n.getFirstToken ())
          break;
        node.print (t, io);
      }
      n.jjtAccept (this, io);
      t = n.getLastToken ();
    }
    while (t != node.getLastToken ())
    {
      t = t.next;
      node.print (t, io);
    }

    return null;
  }

  static void openJJTreeComment (final IO io, final String arg)
  {
    if (arg != null)
    {
      io.print ("/*@bgen(jjtree) " +
                arg +
                (Options.booleanValue (Options.USEROPTION__CPP_IGNORE_ACTIONS) ? "" : " */"));
    }
    else
    {
      io.print ("/*@bgen(jjtree)" + (Options.booleanValue (Options.USEROPTION__CPP_IGNORE_ACTIONS) ? "" : "*/"));
    }
  }

  static void closeJJTreeComment (final IO io)
  {
    io.print ((Options.booleanValue (Options.USEROPTION__CPP_IGNORE_ACTIONS) ? "" : "/*") + "@egen*/");
  }

  String getIndentation (final JJTreeNode n)
  {
    return getIndentation (n, 0);
  }

  String getIndentation (final JJTreeNode n, final int offset)
  {
    String s = "";
    for (int i = offset + 1; i < n.getFirstToken ().beginColumn; ++i)
    {
      s += " ";
    }
    return s;
  }

  void insertOpenNodeDeclaration (final NodeScope ns, final IO io, final String indent)
  {
    insertOpenNodeCode (ns, io, indent);
  }

  void insertOpenNodeCode (final NodeScope ns, final IO io, final String indent)
  {
    final String type = ns.node_descriptor.getNodeType ();
    final String nodeClass;
    if (JJTreeOptions.getNodeClass ().length () > 0 && !JJTreeOptions.getMulti ())
    {
      nodeClass = JJTreeOptions.getNodeClass ();
    }
    else
    {
      nodeClass = type;
    }

    CPPNodeFiles.addType (type);

    io.print (indent + nodeClass + " *" + ns.nodeVar + " = ");
    final String p = Options.getStatic () ? "null" : "this";
    final String parserArg = JJTreeOptions.getNodeUsesParser () ? (p + ", ") : "";

    if (JJTreeOptions.getNodeFactory ().equals ("*"))
    {
      // Old-style multiple-implementations.
      io.println ("(" +
                  nodeClass +
                  "*)" +
                  nodeClass +
                  "::jjtCreate(" +
                  parserArg +
                  ns.node_descriptor.getNodeId () +
                  ");");
    }
    else
      if (JJTreeOptions.getNodeFactory ().length () > 0)
      {
        io.println ("(" + nodeClass + "*)nodeFactory->jjtCreate(" + parserArg + ns.node_descriptor.getNodeId () + ");");
      }
      else
      {
        io.println ("new " + nodeClass + "(" + parserArg + ns.node_descriptor.getNodeId () + ");");
      }

    if (ns.usesCloseNodeVar ())
    {
      io.println (indent + "bool " + ns.closedVar + " = true;");
    }
    io.println (indent + ns.node_descriptor.openNode (ns.nodeVar));
    if (JJTreeOptions.getNodeScopeHook ())
    {
      io.println (indent + "jjtreeOpenNodeScope(" + ns.nodeVar + ");");
    }

    if (JJTreeOptions.getTrackTokens ())
    {
      io.println (indent + ns.nodeVar + "->jjtSetFirstToken(getToken(1));");
    }
  }

  void insertCloseNodeCode (final NodeScope ns, final IO io, final String indent, final boolean isFinal)
  {
    final String closeNode = ns.node_descriptor.closeNode (ns.nodeVar);
    io.println (indent + closeNode);
    if (ns.usesCloseNodeVar () && !isFinal)
    {
      io.println (indent + ns.closedVar + " = false;");
    }
    if (JJTreeOptions.getNodeScopeHook ())
    {
      io.println (indent + "if (jjtree.nodeCreated()) {");
      io.println (indent + " jjtreeCloseNodeScope(" + ns.nodeVar + ");");
      io.println (indent + "}");
    }

    if (JJTreeOptions.getTrackTokens ())
    {
      io.println (indent + ns.nodeVar + "->jjtSetLastToken(getToken(0));");
    }
  }

  void insertOpenNodeAction (final NodeScope ns, final IO io, final String indent)
  {
    io.println (indent + "{");
    insertOpenNodeCode (ns, io, indent + "  ");
    io.println (indent + "}");
  }

  void insertCloseNodeAction (final NodeScope ns, final IO io, final String indent)
  {
    io.println (indent + "{");
    insertCloseNodeCode (ns, io, indent + "  ", false);
    io.println (indent + "}");
  }

  private void insertCatchBlocks (final NodeScope ns, final IO io, final String indent)
  {
    // if (thrown_names.hasMoreElements()) {
    io.println (indent + "} catch (...) {"); // " + ns.exceptionVar + ") {");

    if (ns.usesCloseNodeVar ())
    {
      io.println (indent + "  if (" + ns.closedVar + ") {");
      io.println (indent + "    jjtree.clearNodeScope(" + ns.nodeVar + ");");
      io.println (indent + "    " + ns.closedVar + " = false;");
      io.println (indent + "  } else {");
      io.println (indent + "    jjtree.popNode();");
      io.println (indent + "  }");
    }
    // }

  }

  void tryTokenSequence (final NodeScope ns, final IO io, final String indent, final Token first, final Token last)
  {
    io.println (indent + "try {");
    closeJJTreeComment (io);

    /*
     * Print out all the tokens, converting all references to `jjtThis' into the
     * current node variable.
     */
    for (Token t = first; t != last.next; t = t.next)
    {
      TokenUtils.print (t, io, "jjtThis", ns.nodeVar);
    }

    openJJTreeComment (io, null);
    io.println ();

    insertCatchBlocks (ns, io, indent);

    io.println (indent + "} {");
    if (ns.usesCloseNodeVar ())
    {
      io.println (indent + "  if (" + ns.closedVar + ") {");
      insertCloseNodeCode (ns, io, indent + "    ", true);
      io.println (indent + "  }");
    }
    io.println (indent + "}");
    closeJJTreeComment (io);
  }

  private static void findThrown (final NodeScope ns,
                                  final Map <String, String> thrown_set,
                                  final JJTreeNode expansion_unit)
  {
    if (expansion_unit instanceof ASTBNFNonTerminal)
    {
      /*
       * Should really make the nonterminal explicitly maintain its name.
       */
      final String nt = expansion_unit.getFirstToken ().image;
      final ASTProduction prod = JJTreeGlobals.productions.get (nt);
      if (prod != null)
      {
        for (final String t : prod.throws_list)
          thrown_set.put (t, t);
      }
    }
    for (int i = 0; i < expansion_unit.jjtGetNumChildren (); ++i)
    {
      final JJTreeNode n = (JJTreeNode) expansion_unit.jjtGetChild (i);
      findThrown (ns, thrown_set, n);
    }
  }

  void tryExpansionUnit (final NodeScope ns, final IO io, final String indent, final JJTreeNode expansion_unit)
  {
    io.println (indent + "try {");
    closeJJTreeComment (io);

    expansion_unit.jjtAccept (this, io);

    openJJTreeComment (io, null);
    io.println ();

    // Maintain order
    final Map <String, String> thrown_set = new LinkedHashMap <> ();
    findThrown (ns, thrown_set, expansion_unit);
    insertCatchBlocks (ns, io, indent);

    io.println (indent + "} {");
    if (ns.usesCloseNodeVar ())
    {
      io.println (indent + "  if (" + ns.closedVar + ") {");
      insertCloseNodeCode (ns, io, indent + "    ", true);
      io.println (indent + "  }");
    }
    io.println (indent + "}");
    closeJJTreeComment (io);
  }

}

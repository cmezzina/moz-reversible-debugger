/* Generated By:JavaCC: Do not edit this line. mozParser.java */
package parser;

import java.io.*;
import language.statement.*;
import language.value.*;

import java.util.*;

public class mozParser implements mozParserConstants {
        static mozParser parser = null;

 public static IStatement parse(InputStream pgm) throws ParseException {
                // create a parser (this object)
                 parser = new mozParser(pgm);
                System.out.println("started parsing ....");
                IStatement program = mozParser.parse();
                return program;
                }

  static final public IStatement parse() throws ParseException {
  IStatement pgm = null;
    pgm = stm_list();
    jj_consume_token(0);
        {if (true) return pgm;}
    throw new Error("Missing return statement in function");
  }
  
//sequential composition of statements
  static final public IStatement stm_list() throws ParseException {
  IStatement sx = null;
  IStatement dx = null;
    sx = stm();
    label_1:
    while (true) {
      if (jj_2_1(2)) {
        ;
      } else {
        break label_1;
      }
      jj_consume_token(SC);
      dx = stm_list();
    }
          if(dx != null)
                  {if (true) return new Sequence(sx,dx);}
         {if (true) return sx;}
    throw new Error("Missing return statement in function");
  }

  static final public IStatement stm() throws ParseException {
  Token t;
  IValue val;
  IStatement sx = null;
  IStatement dx = null;
  ArrayList< String> args;
    if (jj_2_5(2)) {
      jj_consume_token(SK);
    {if (true) return new Skip();}
    } else if (jj_2_6(2)) {
      jj_consume_token(IF);
      t = jj_consume_token(ID);
      jj_consume_token(THEN);
      sx = stm_list();
      jj_consume_token(ELSE);
      dx = stm_list();
      jj_consume_token(END);
          {if (true) return new Conditional(t.toString(),sx,dx);}
    } else if (jj_2_7(2)) {
      jj_consume_token(LET);
      t = jj_consume_token(ID);
      jj_consume_token(EQ);
      if (jj_2_2(2)) {
        val = value();
      } else if (jj_2_3(2)) {
        val = receive_value();
      } else {
        jj_consume_token(-1);
        throw new ParseException();
      }
      jj_consume_token(IN);
      sx = stm_list();
      jj_consume_token(END);
         // System.out.println("Scope con "+t);
          {if (true) return new Assignment(t.toString(), val, sx);}
    } else if (jj_2_8(2)) {
      //thread creation 
              sx = spawn();
          {if (true) return sx;}
    } else if (jj_2_9(2)) {
      jj_consume_token(CL);
      if (jj_2_4(2)) {
        sx = send();
      } else {
        args = var_list();
                                //the first element of the list is the invoked procedure
                                if(args.size() >0)
                                {
                             String proc_name = args.remove(0);
                             sx = new Invoke(proc_name, args);

                           }
      }
      jj_consume_token(CR);
          {if (true) return sx;}
    } else {
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

//creation new thread
  static final public IStatement spawn() throws ParseException {
  IStatement body;
    jj_consume_token(THR);
    body = stm_list();
    jj_consume_token(END);
    {if (true) return new ThreadStm(body);}
    throw new Error("Missing return statement in function");
  }

  static final public IStatement send() throws ParseException {
  Token chan,value;
    jj_consume_token(SEND);
    chan = jj_consume_token(ID);
    value = jj_consume_token(ID);
    {if (true) return new Send(chan.toString(), value.toString());}
    throw new Error("Missing return statement in function");
  }

  static final public IValue receive_value() throws ParseException {
  Token id;
    jj_consume_token(CL);
    jj_consume_token(REC);
    id = jj_consume_token(ID);
    jj_consume_token(CR);
                {if (true) return new Receive(id.toString());}
    throw new Error("Missing return statement in function");
  }

  static final public BoolValue if_value() throws ParseException {
  boolean e;
    jj_consume_token(PL);
    e = bool_val();
    jj_consume_token(PR);
        {if (true) return new BoolValue(e);}
    throw new Error("Missing return statement in function");
  }

  static final public boolean bool_val() throws ParseException {
    if (jj_2_10(2)) {
      jj_consume_token(TRUE);
    {if (true) return true;}
    } else if (jj_2_11(2)) {
      jj_consume_token(FALSE);
        {if (true) return false;}
    } else {
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  static final public IValue value() throws ParseException {
  ArrayList< String> var= null;
  IStatement stmlist = null;
  boolean bol;
    if (jj_2_12(2)) {
      jj_consume_token(PROC);
      var = proc_var();
      stmlist = stm_list();
      jj_consume_token(END);
   {if (true) return new Procedure(var, stmlist);}
    } else if (jj_2_13(2)) {
      bol = bool_val();
          {if (true) return new BoolValue(bol);}
    } else if (jj_2_14(2)) {
      jj_consume_token(PORT);
          {if (true) return new PortCreation();}
    } else {
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  static final public ArrayList< String> proc_var() throws ParseException {
  ArrayList< String> ret;
    jj_consume_token(CL);
    ret = var_list();
    jj_consume_token(CR);
          {if (true) return ret;}
    throw new Error("Missing return statement in function");
  }

  static final public ArrayList< String> var_list() throws ParseException {
  Token id;
  ArrayList< String> ret = new ArrayList< String>();
    label_2:
    while (true) {
      if (jj_2_15(2)) {
        ;
      } else {
        break label_2;
      }
      id = jj_consume_token(ID);
          ret.add(id.toString());
    }
          {if (true) return ret;}
    throw new Error("Missing return statement in function");
  }

  static private boolean jj_2_1(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_1(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(0, xla); }
  }

  static private boolean jj_2_2(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_2(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(1, xla); }
  }

  static private boolean jj_2_3(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_3(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(2, xla); }
  }

  static private boolean jj_2_4(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_4(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(3, xla); }
  }

  static private boolean jj_2_5(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_5(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(4, xla); }
  }

  static private boolean jj_2_6(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_6(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(5, xla); }
  }

  static private boolean jj_2_7(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_7(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(6, xla); }
  }

  static private boolean jj_2_8(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_8(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(7, xla); }
  }

  static private boolean jj_2_9(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_9(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(8, xla); }
  }

  static private boolean jj_2_10(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_10(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(9, xla); }
  }

  static private boolean jj_2_11(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_11(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(10, xla); }
  }

  static private boolean jj_2_12(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_12(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(11, xla); }
  }

  static private boolean jj_2_13(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_13(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(12, xla); }
  }

  static private boolean jj_2_14(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_14(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(13, xla); }
  }

  static private boolean jj_2_15(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_15(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(14, xla); }
  }

  static private boolean jj_3R_11() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3_5()) {
    jj_scanpos = xsp;
    if (jj_3_6()) {
    jj_scanpos = xsp;
    if (jj_3_7()) {
    jj_scanpos = xsp;
    if (jj_3_8()) {
    jj_scanpos = xsp;
    if (jj_3_9()) return true;
    }
    }
    }
    }
    return false;
  }

  static private boolean jj_3_5() {
    if (jj_scan_token(SK)) return true;
    return false;
  }

  static private boolean jj_3_14() {
    if (jj_scan_token(PORT)) return true;
    return false;
  }

  static private boolean jj_3_1() {
    if (jj_scan_token(SC)) return true;
    if (jj_3R_3()) return true;
    return false;
  }

  static private boolean jj_3_2() {
    if (jj_3R_4()) return true;
    return false;
  }

  static private boolean jj_3_9() {
    if (jj_scan_token(CL)) return true;
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3_4()) {
    jj_scanpos = xsp;
    if (jj_3R_8()) return true;
    }
    if (jj_scan_token(CR)) return true;
    return false;
  }

  static private boolean jj_3_13() {
    if (jj_3R_10()) return true;
    return false;
  }

  static private boolean jj_3R_5() {
    if (jj_scan_token(CL)) return true;
    if (jj_scan_token(REC)) return true;
    return false;
  }

  static private boolean jj_3R_3() {
    if (jj_3R_11()) return true;
    return false;
  }

  static private boolean jj_3_8() {
    if (jj_3R_7()) return true;
    return false;
  }

  static private boolean jj_3_12() {
    if (jj_scan_token(PROC)) return true;
    if (jj_3R_9()) return true;
    return false;
  }

  static private boolean jj_3R_4() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3_12()) {
    jj_scanpos = xsp;
    if (jj_3_13()) {
    jj_scanpos = xsp;
    if (jj_3_14()) return true;
    }
    }
    return false;
  }

  static private boolean jj_3R_6() {
    if (jj_scan_token(SEND)) return true;
    if (jj_scan_token(ID)) return true;
    return false;
  }

  static private boolean jj_3_15() {
    if (jj_scan_token(ID)) return true;
    return false;
  }

  static private boolean jj_3R_12() {
    Token xsp;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3_15()) { jj_scanpos = xsp; break; }
    }
    return false;
  }

  static private boolean jj_3_7() {
    if (jj_scan_token(LET)) return true;
    if (jj_scan_token(ID)) return true;
    return false;
  }

  static private boolean jj_3_11() {
    if (jj_scan_token(FALSE)) return true;
    return false;
  }

  static private boolean jj_3R_7() {
    if (jj_scan_token(THR)) return true;
    if (jj_3R_3()) return true;
    return false;
  }

  static private boolean jj_3_6() {
    if (jj_scan_token(IF)) return true;
    if (jj_scan_token(ID)) return true;
    return false;
  }

  static private boolean jj_3_3() {
    if (jj_3R_5()) return true;
    return false;
  }

  static private boolean jj_3R_10() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3_10()) {
    jj_scanpos = xsp;
    if (jj_3_11()) return true;
    }
    return false;
  }

  static private boolean jj_3R_8() {
    if (jj_3R_12()) return true;
    return false;
  }

  static private boolean jj_3_10() {
    if (jj_scan_token(TRUE)) return true;
    return false;
  }

  static private boolean jj_3_4() {
    if (jj_3R_6()) return true;
    return false;
  }

  static private boolean jj_3R_9() {
    if (jj_scan_token(CL)) return true;
    return false;
  }

  static private boolean jj_initialized_once = false;
  /** Generated Token Manager. */
  static public mozParserTokenManager token_source;
  static SimpleCharStream jj_input_stream;
  /** Current token. */
  static public Token token;
  /** Next token. */
  static public Token jj_nt;
  static private int jj_ntk;
  static private Token jj_scanpos, jj_lastpos;
  static private int jj_la;
  static private int jj_gen;
  static final private int[] jj_la1 = new int[0];
  static private int[] jj_la1_0;
  static private int[] jj_la1_1;
  static {
      jj_la1_init_0();
      jj_la1_init_1();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {};
   }
   private static void jj_la1_init_1() {
      jj_la1_1 = new int[] {};
   }
  static final private JJCalls[] jj_2_rtns = new JJCalls[15];
  static private boolean jj_rescan = false;
  static private int jj_gc = 0;

  /** Constructor with InputStream. */
  public mozParser(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public mozParser(java.io.InputStream stream, String encoding) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser.  ");
      System.out.println("       You must either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new mozParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  static public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  static public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor. */
  public mozParser(java.io.Reader stream) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser. ");
      System.out.println("       You must either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new mozParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  static public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor with generated Token Manager. */
  public mozParser(mozParserTokenManager tm) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser. ");
      System.out.println("       You must either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(mozParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  static private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static private final class LookaheadSuccess extends java.lang.Error { }
  static final private LookaheadSuccess jj_ls = new LookaheadSuccess();
  static private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0; Token tok = token;
      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
      if (tok != null) jj_add_error_token(kind, i);
    }
    if (jj_scanpos.kind != kind) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
    return false;
  }


/** Get the next Token. */
  static final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  static final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  static private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  static private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  static private int[] jj_expentry;
  static private int jj_kind = -1;
  static private int[] jj_lasttokens = new int[100];
  static private int jj_endpos;

  static private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) return;
    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];
      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }
      jj_entries_loop: for (java.util.Iterator<?> it = jj_expentries.iterator(); it.hasNext();) {
        int[] oldentry = (int[])(it.next());
        if (oldentry.length == jj_expentry.length) {
          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              continue jj_entries_loop;
            }
          }
          jj_expentries.add(jj_expentry);
          break jj_entries_loop;
        }
      }
      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
    }
  }

  /** Generate ParseException. */
  static public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[33];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 0; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1<<j)) != 0) {
            la1tokens[32+j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 33; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  static final public void enable_tracing() {
  }

  /** Disable tracing. */
  static final public void disable_tracing() {
  }

  static private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 15; i++) {
    try {
      JJCalls p = jj_2_rtns[i];
      do {
        if (p.gen > jj_gen) {
          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
          switch (i) {
            case 0: jj_3_1(); break;
            case 1: jj_3_2(); break;
            case 2: jj_3_3(); break;
            case 3: jj_3_4(); break;
            case 4: jj_3_5(); break;
            case 5: jj_3_6(); break;
            case 6: jj_3_7(); break;
            case 7: jj_3_8(); break;
            case 8: jj_3_9(); break;
            case 9: jj_3_10(); break;
            case 10: jj_3_11(); break;
            case 11: jj_3_12(); break;
            case 12: jj_3_13(); break;
            case 13: jj_3_14(); break;
            case 14: jj_3_15(); break;
          }
        }
        p = p.next;
      } while (p != null);
      } catch(LookaheadSuccess ls) { }
    }
    jj_rescan = false;
  }

  static private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) { p = p.next = new JJCalls(); break; }
      p = p.next;
    }
    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
  }

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }

}
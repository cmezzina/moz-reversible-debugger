/*******************************************************************************
 * Copyright (c) 2012 Claudio Antares Mezzina.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Claudio Antares Mezzina - initial API and implementation
 *     Ivan Lanese - implementation
 ******************************************************************************/
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import language.history.HistoryEsc;
import language.history.HistoryIf;
import language.history.HistoryInvoke;
import language.history.HistoryPort;
import language.history.HistoryProc;
import language.history.HistoryReceive;
import language.history.HistorySend;
import language.history.HistorySkip;
import language.history.HistoryThread;
import language.history.HistoryType;
import language.history.HistoryVar;
import language.history.IHistory;
import language.statement.Assignment;
import language.statement.Conditional;
import language.statement.Esc;
import language.statement.IStatement;
import language.statement.Invoke;
import language.statement.Nil;
import language.statement.Send;
import language.statement.Sequence;
import language.statement.Skip;
import language.statement.StatementType;
import language.statement.ThreadStm;
import language.util.Channel;
import language.util.Tuple;
import language.value.BoolValue;
import language.value.IValue;
import language.value.PortCreation;
import language.value.Procedure;
import language.value.Receive;
import language.value.SimpleId;
import language.value.ValueType;
import parser.ParseException;
import parser.mozParser;
import expection.ChildMissingException;
import expection.WrongElementChannel;
public class Debugger {

	static String path="src\\pgm.txt";
	
	/* counters */
	static int chan_count =0;
	static int proc_count =0;
	static int thread_count =0;
	static int var_count=0;
	static int pc=1;
	
	static IStatement program;
	static String last_com="";
	
	/*stores*/
	//variables store
	static HashMap<String, IValue> store= new HashMap<String, IValue>();
	//channel/port store
	static HashMap<String, Channel> chans = new HashMap<String, Channel>();
	//procedure store
	static HashMap<String,IValue> procs = new HashMap<String,IValue>();
	//thread pool
	static HashMap<String,IStatement> threadlist = new HashMap<String, IStatement>();
	
	static HashMap<String , ArrayList<IHistory>> history = new HashMap<String, ArrayList<IHistory>>();
	
	/***prompt messages ***/
	static String warning="\n+++";
	static String error="\n***";
	static String done = "...done";
	
	public static void main(String arg[])
	{
		BufferedReader cons = new BufferedReader(new InputStreamReader(System.in));
		
		if(arg.length >0)
			path = arg[0];
		System.out.println("reading  file  ... "+path);
		try {
			program = mozParser.parse(new FileInputStream(path));
			//program represents the first configuration
			
			String initial = generateThreadId();
			threadlist.put(initial, program);
			history.put(initial, new ArrayList<IHistory>());
			System.out.println("generated initial configuration "+ initial +"\n\n");
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		System.out.println(warning+" type help to see all the commands \n\n");

				
		String command;
		try {
				
			while( true)
			{
				System.out.print("Insert command : ");
				command = cons.readLine();
				
				if(command.equals("") && !last_com.equals(""))
					command = last_com;
				
				last_com = command;
				String[] cmd = command.split(" ");
				if(cmd[0].equals("quit") || cmd[0].equals("q"))
				{
					System.out.println(error+"quitting debugging");
					return;
				}

				if(cmd[0].equals("help") || cmd[0].equals("c"))
				{
					showHelp();
					continue;
				}
				if (cmd[0].equals("back") || cmd[0].equals("undo") ||  cmd[0].equals("roll") || cmd[0].equals("forth") ||cmd[0].equals("f") ||
						cmd[0].equals("b") || cmd[0].equals("u") || cmd[0].equals("r") )
				{
					if(cmd.length > 2 && !threadlist.containsKey(cmd[1]))
					{
						System.out.println(warning+"invilid thread identifier "+ cmd[1]);
						System.out.println();
						continue;
					}
					
					if(cmd.length < 2)
					{
						System.out.println(warning+"missing parameter ");
						System.out.println();
						continue;
					}
				}
				if(cmd[0].equals("store") || cmd[0].equals("s"))
				{ 
					if(store.size() == 0)
					{
						System.out.println(warning +"empty store");
						System.out.println();
					}
					else
						System.out.println("Stored ids :"+store.keySet());		
				}
				else 
					if(cmd[0].equals("print") || cmd[0].equals("p"))
					{
						
						if(threadlist.containsKey(cmd[1]))
						{
							System.out.println(threadlist.get(cmd[1]));
							continue;
						}
						String toprint = printId(cmd[1]);
						if(toprint!=null)
						{
							System.out.println(cmd[1]+ " = "+toprint);
						}
					}
					else
						if(cmd[0].equals("list") || cmd[0].equals("l"))
						{
							System.out.println("Available threads : "+threadlist.keySet());
						}
					else
						if( cmd[0].equals("forth") || cmd[0].equals("f"))
						{
							IStatement body = threadlist.get(cmd[1]);
							if(body != null)
							{
								if(body.getType() != StatementType.NIL )
								{
									body = execute(body, cmd[1]);
									if(body == null)
									{
										//should not be possible to reach this point ...
										break;
									}
									threadlist.put(cmd[1], body);
								}
								else
								{
									System.out.println("thread "+cmd[1] + " has terminated");
								}
							}
						//	else
							//{
							//	System.out.println(error+"invalid thread name "+ cmd[1]+ "\n");
						//	}
						}
						else if(cmd[0].equals("back") || cmd[0].equals("b"))
						{
							/*if(!threadlist.containsKey(cmd[1]))
							{
								System.out.println(warning +"invalid thread identifier "+cmd[1]);
								continue;
							}*/
							
							if(!history.containsKey(cmd[1]))
							{
								System.out.println(warning +"invalid memory for thread "+cmd[1]);
								continue;
							}
								try {
									if(stepBack(cmd[1])>= 0)
										System.out.println(done);

								} catch (WrongElementChannel e) {
									System.out.println(warning+ e.getMsg());
								} catch (ChildMissingException e) {
									System.out.println(warning+ e.getMsg());								
								}
						}
				
						else if(cmd[0].equals("undo") || cmd[0].equals("u"))
						{
							try{
							/*	if(!threadlist.containsKey(cmd[1]))
								{
									System.out.println(warning + " invalid thread id "+cmd[1]);
									continue;
								}
							*/
								if(rollNsteps(cmd[1], Integer.parseInt(cmd[2])))
									System.out.println(done);
								else
									System.out.println(warning + "nothing to undo");
							}
							catch(NumberFormatException e)
							{
								System.out.println(warning + "invalid number");
							}
						}

						else if(cmd[0].equals("roll") || cmd[0].equals("r"))
						{
							/*if(!threadlist.containsKey(cmd[1]))
							{
								System.out.println(warning + " invalid thread id "+cmd[1]);
								continue;
							}*/
							rollEnd(cmd[1]);
							System.out.println(done);
						}
						else if(cmd[0].equals("story") || cmd[0].equals("h"))
						{
							printHistory(cmd[1]);
						}
						else
						{
							last_com = "";
							System.out.println(error+"invalid command "+ cmd[0]+ "\n");
						}
						
			}

			//System.out.println("..............");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("exiting from debugging ");


	}
	
	//logs and executes all the esc in a sequence at once. Stops when there is a statement different from esc
	private static IStatement normalize(IStatement stm, String thread_name)
	{
		if(stm.getType() == StatementType.ESC)
		{
			ArrayList<IHistory> h = history.get(thread_name);
			h.add(new HistoryEsc());
			history.put(thread_name, h);
			return new Nil();

		}
		
		
		if(stm.getType() == StatementType.SEQUENCE)
		{
			Sequence seq= (Sequence)stm;
			IStatement sx = normalize(seq.getSx(), thread_name);
			if(sx.getType() !=StatementType.NIL)
			{
				return new Sequence(sx, seq.getDx());
			}
			else return normalize(seq.getDx(), thread_name);
		}
		return stm;
	}

	//executes one step forward of a given thread
	private static  IStatement execute(IStatement stm, String thread_name)
	{
		StatementType type = stm.getType();
		ArrayList<IHistory> h = history.get(thread_name);

		switch(type)
		{
			case SEQUENCE: 
			{
				Sequence seq = (Sequence)stm;

				//executing left member of the sequence
				IStatement sx= execute(seq.getSx(), thread_name);
				//error --> quit
				if(sx == null)
					return null;
				
				//if the left element has finished = Nil then we have to normalize the right one
				if(sx.getType() == StatementType.NIL)
				{
					return normalize(seq.getDx(), thread_name);
				}
				seq.setSx(sx);
				return seq;
			}
			case SPAWN:
			{
				ThreadStm th = (ThreadStm)stm;
	
				String tid = generateThreadId();
				threadlist.put(tid, th.getBody());
				history.put(tid, new ArrayList<IHistory>());
				h.add(new HistoryThread(tid));
				history.put(thread_name, h);
				System.out.println("Generating thread "+ tid);
				return new Nil();
			}
			case LET: 
			{
				//should return a sequence with a trailing ESC
				
				Assignment let = (Assignment)stm;
				IValue val = let.getV();
				String old_id = let.getId();
				
				//renaming variable
				String new_id = generateVarId(old_id);
				
				ValueType valuetype = val.getType();
				switch(valuetype)
				{
					case RECEIVE:
					{
						Receive rec = (Receive) val;
						String from = rec.getFrom();
						
						if(isChan(from))
						{
							IValue chan=store.get(from);
							//consuming message
							String chanid = ((SimpleId)chan).getId();
							Channel ch = chans.get(chanid);
							if(ch.isEmpty())
							{
								System.out.println("empty channel "+from);
								return stm;
							}
							System.out.println("receiving from "+from +" in "+new_id);
							int gamma= pc++;
							IValue received =ch.receive(thread_name, gamma);
							store.put(new_id, received);
							h.add(new HistoryReceive(from, new_id,gamma));
						}
						else
						{
							System.out.println(error + "unrecognized channel "+from);
							System.out.println();
							return null;
						}
						break;
					}
					case BOOLEAN:
							{
								System.out.println("putting in store variable "+new_id);
								store.put(new_id, val);
								h.add(new HistoryVar(new_id));
								break;
							}
					case PORT:
							{
								String xi = generateChanId();
								System.out.println("generating channel "+ new_id +" --> "+xi);
								store.put(new_id, new SimpleId(xi));
								chans.put(xi, new Channel());
								h.add(new HistoryPort(new_id));
								break;
							}
					case PROCEDURE:
							{
								//Procedure prc = (Procedure)val;
								String lambda = generateProcId();
								System.out.println("generating procedure "+ new_id +" --> "+lambda);
								val.rename(old_id, new_id);
								store.put(new_id, new SimpleId(lambda));
								procs.put(lambda, val);
								h.add(new HistoryProc(new_id));
								break;
							}
					//			execute(let.getStm());
				}
				//renaming the body of a let statement
				let.getStm().rename(old_id, new_id);
			
				//logging the action
				history.put(thread_name, h);
				
				//putting a trailing ESC to delimit the let scope
				return new Sequence(let.getStm(), new Esc());			
			}
			case IF:
			{
				Conditional cond = (Conditional) stm;
				String guard = cond.getGuard();
				IValue val = store.get(guard);
				IStatement ret =null;
				if(val != null && val.getType() == ValueType.BOOLEAN)
				{
					BoolValue e = (BoolValue)val;
					if(e.getValue())
					{
						System.out.println("reducing to then (left) branch");
						h.add(new HistoryIf(guard, cond.getRight(), true));
						ret= cond.getLeft();
					}
					else
					{
						System.out.println("reducing to else (right) branch");
						h.add(new HistoryIf(guard, cond.getLeft(), false));
						ret =cond.getRight();
					}
				}
				else
				{
					if(val == null)
					{
						System.out.println(error+" undefined variable "+guard);
					}
					else
					{
						System.out.println(error+" non boolean value for "+guard);
					}
					return null;
				}
				history.put(thread_name, h);
				return new Sequence(ret,new Esc());
			}
			case SEND:
			{
				Send snd = (Send)stm;
				String to = snd.getObj();
				IValue chan = store.get(to);
		/*		if(chan == null)
				{
					System.out.println(error+to + " is not recognized as channel");
					return null;
				}
		*/		
				if(isChan(to))
				{
					String id = ((SimpleId)chan).getId();
					//chans.put(id, chans.get(id).add(e))
	
					//IValue tosend = store.get(snd.getSub());
					IValue tosend = new SimpleId(snd.getSub());
					int gamma=pc++;
					Channel tmp = chans.get(id);
					tmp.send(tosend, thread_name,gamma);
					//chans.put(id, tmp );
					System.out.println("sending to channel "+to);
					h.add(new HistorySend(to,gamma));
					history.put(thread_name, h);
					return new Nil();
				}	
				else
				{
					System.out.println(error+to +" is not a channel");
					System.out.println();
					return null; 
				}
			}
			case INVOKE:
			{
				Invoke call = (Invoke)stm;
				String call_id = call.getCallee();
				SimpleId real_name = (SimpleId) store.get(call_id);
				if(real_name != null && real_name.getType() == ValueType.ID)
				{
					System.out.println("should execute procedure "+ real_name.getId());
					Procedure proc_def = (Procedure) procs.get(real_name.getId());
					
					h.add(new HistoryInvoke(call_id, call.getParams()));
					history.put(thread_name, h);
					if(call.getParams().isEmpty())
					{
						return new Sequence(proc_def.getBody(), new Esc());
					}
					else
					{
						List<String> param = proc_def.getParams();
						List<String> actual_param = call.getParams();
						if(param.size() == actual_param.size())
						{
							//cloning the procedure body in order to rename it and to give it to the thread
							IStatement body= proc_def.getBody().clone();							
							for(int i=0; i < param.size(); i++)
							{
								body.rename(param.get(i), actual_param.get(i));
							
							}
							return new Sequence(body, new Esc());
						}
						else
						{
							System.out.println(warning + " size mismatch on invocation of procedure "+ call_id );
						}
					}
				}
				
				break;
			}
			case SKIP:
			{
				System.out.println("skip");
				h.add(new HistorySkip());
				history.put(thread_name, h);
				return new Nil();
			}
			case ESC:
			{
				h.add(new HistoryEsc());
				history.put(thread_name, h);
				return new Nil();
			}
			
			case NIL:	return stm;
		
		}
			return null;
	}
	
	//tries to execute one step back of a given thread
	//returns -1 if it cannot perform the step, 
	// 0 for an internal back step and the gamma of a send/receive 
	private  static int stepBack(String thread_id) throws WrongElementChannel, ChildMissingException
	{
		ArrayList<IHistory> lst; 
		IStatement body = threadlist.get(thread_id);
		int ret =0;
		//thread next action after a step backward
		IStatement new_body = null;
		//the rest of the body
		IStatement next=null ;
		
		//checks about thread id and history are done in the callee
		lst = history.get(thread_id);
		
		if(lst.size() == 0)
		{
			System.out.println(warning + "empty history for thread "+thread_id);
			return -1;
		}
		
		int index = lst.size()-1;
		IHistory action = lst.get(index);
		
		switch(action.getType())
		{
			case SKIP: {
				if(body.getType() == StatementType.NIL)
					new_body = new Skip();
				else{
					new_body = new Sequence(new Skip(), body);
				}
				break;
			}
			case IF :{
				HistoryIf log = (HistoryIf)action;
				if(log.isLeft())
				{
					next = afterEsc(body);
					new_body = beforeEsc(body);
					new_body = new Conditional(log.getGuard(), new_body, log.getBody());
				}
				break;
			}
			case VAR:
			{
				//var creation of a boolean value
				HistoryVar log = (HistoryVar)action;
				
				if(store.containsKey(log.getId()))
				{
					IValue val =store.remove(log.getId());
					
					//probably this check is useless since it there is always an esc delimiter of the scope
					if(body.getType() == StatementType.SEQUENCE)
					{
							next = afterEsc(body);
							new_body = beforeEsc(body);
							new_body = new Assignment(log.getId(), val, new_body);
					}
				}
				break;
			}
			case PROCEDURE:
			{
				HistoryProc log = (HistoryProc)action;
				if(store.containsKey(log.getId()))
				{
					IValue val =store.remove(log.getId());
					if(val.getType() == ValueType.ID)
					{
						String xi = ((SimpleId)val).getId();
						IValue proc = null;
						if( (proc = procs.get(xi)) != null)
						{
							next = afterEsc(body);
							new_body = beforeEsc(body);
							new_body = new  Assignment(log.getId(), proc, new_body);
							//removing from store variale and procedure
							store.remove(log.getId());
							procs.remove(xi);
						}
					}
					
				}
				break;
				
				
			}
			case PORT:
			{
				HistoryPort log = (HistoryPort)action;
				//to reverse a port creation the port should be empty
				if(store.containsKey(log.getPort_name()))
				{
					IValue val =store.remove(log.getPort_name());
					if(val.getType() == ValueType.ID)
					{
						String xi = ((SimpleId)val).getId();
						Channel chan = null;
						if( (chan = chans.get(xi)) != null)
						{
							if(chan.isEmpty())
							{

								next = afterEsc(body);
								new_body = beforeEsc(body);
							
								new_body = new  Assignment(log.getPort_name(), new PortCreation(), new_body);
							//removing from store variable and procedure
								store.remove(log.getPort_name());
								procs.remove(xi);
							
							}
							else
							{
								System.out.println(warning +" cannot revert port creation of "+log.getPort_name() +" since it is not empty");
								return -1;
							}
						}
						
					}
					
				}
				
				
				break;
				
			}
			case THREAD:{

				HistoryThread log = (HistoryThread)action;
				//to reverse a port creation the port should be empty
				String xi = log.getThread_id();
				if(threadlist.containsKey(xi))
				{
					ArrayList<IHistory> child_story = null;

					if( (child_story = history.get(xi)) != null)
					{
						//if the child has not executed (or has been fully reversed)
						if(child_story.size() == 0)
						{
							IStatement thread_body = threadlist.get(xi);
							if(thread_body == null)
							{
								//this cannot happen
							}
							new_body = new ThreadStm(thread_body);
							next = body;
							//removing from store variable and procedure
							threadlist.remove(xi);
							history.remove(xi);
						}
						else
						{	//if the child has still some story = is not in its initial form
							throw new ChildMissingException("cannot revert thread creation of "+log.getThread_id() +" since it has not empty history \n", log.getThread_id());
						}
					}
				}
				break;
			}
			case SEND:{
				HistorySend log = (HistorySend)action;
				String id = log.getChan();
				ret = log.getInstruction();
				SimpleId tmp = (SimpleId) store.get(id);
				Channel ch = chans.get(tmp.getId());
				if(ch !=null)
				{
					if(ch.isEmpty())
						//different kind of exception ... should reverse who read the msg ..
						throw new WrongElementChannel("value on channel "+tmp.getId() +" does not belong to thread "+thread_id+"\n", ch.getReaders(thread_id));
					IValue val =ch.reverseSend(thread_id);
					//if val =  null means that on the channel there is something that does not belong to the thread
					if(val == null)
					{
						//System.out.println(ch.beforeThread(thread_id));
						throw new WrongElementChannel("value on channel "+tmp.getId() +" does not belong to thread "+thread_id+"\n", ch.getSenders(thread_id));
						//System.out.println(warning +"value on channel "+tmp.getId() +" does not belong to thread "+thread_id);
						//return;
					}
					//next = afterEsc(body);
					//new_body = beforeEsc(body);
				
					if(val.getType() == ValueType.ID){
						new_body = new  Send(log.getChan(), ((SimpleId)val).getId());
						next=body;
					}
				}
				break;
			}
			case RECEIVE:
			{
				HistoryReceive log = (HistoryReceive)action;
				String id = log.getFrom();
				ret = log.getInstruction();

				//lookup
				String xi = ((SimpleId)store.get(id)).getId();
				Channel ch = chans.get(xi);
				//putting back msg
				ch.reverseReceive(thread_id);
				
				//receive is an assigment
				next = afterEsc(body);
				new_body = beforeEsc(body);
				new_body = new  Assignment(log.getVar(), new Receive(log.getFrom()), new_body);

				//erasing variables from store
				store.remove(log.getClass());
				
				break;
			}
			case INVOKE:{
				HistoryInvoke log = (HistoryInvoke)action;
				
//				String xi = log.getProc_name();
				next= afterEsc(body);
				new_body = new Invoke(log.getProc_name(), log.getParam());
				break;
			}
			case ESC:
			{
				new_body = new Esc();
				next = body;
				
				if(next != null && next.getType() != StatementType.NIL)
					new_body = new Sequence(new_body,next);
				
				lst.remove(index);
				history.put(thread_id, lst);
				threadlist.put(thread_id, new_body);
				 return stepBack(thread_id);
				 //return ret;
			}
		}
	
		if(next != null && next.getType() != StatementType.NIL)
			new_body = new Sequence(new_body,next);
		
		lst.remove(index);
		history.put(thread_id, lst);
		threadlist.put(thread_id, new_body);
		return ret;
	}

	
	
	//forces backward the execution till a certain action
	private static void rollTill(HashMap<String, Integer> map)
	{
		Iterator<String> it =  map.keySet().iterator();
		while(it.hasNext())
		{
			String id = it.next();
			int gamma = map.get(id);
			
			while(true)
			{
				try {
					int nro = stepBack(id);
					System.out.println("... reversing thread "+id +" of one step");
					if(nro == gamma)
						break;
				} catch (WrongElementChannel e) {
					rollTill(e.getDependencies());
				} catch (ChildMissingException e) {
					rollEnd(e.getChild());
				}
				
			}
		
		}
	}
	
	//forces backward a thread to the beginning causing the failure of its caused actions
	private static void rollEnd(String thread_id)
	{

		while(history.get(thread_id).size() != 0)
		{
			try {
				stepBack(thread_id);
	//			System.out.println(nro);
			} catch (WrongElementChannel e) {
			
	//			System.out.println("roll till");
				rollTill(e.getDependencies());
				}
			 catch (ChildMissingException e) {
				System.out.println(warning +" reversing child thread "+e.getChild() +"\n");
				rollEnd(e.getChild());
			}
		}
	}
	
	private static boolean rollNsteps(String thread_id, int steps)
	{
		boolean flag = false;
		
		while(history.get(thread_id).size() > 0 && steps >0)
		{
			try {
				stepBack(thread_id);
				steps--;
				flag = true;
			//	System.out.println(nro);
			} catch (WrongElementChannel e) {
			
	//			System.out.println("roll till");
				rollTill(e.getDependencies());
				}
			 catch (ChildMissingException e) {
				System.out.println(warning +" reversing child thread "+e.getChild());
				rollEnd(e.getChild());
			}
		}
		return flag;
	}
	
	private static String generateChanId()
	{
		return "chan_"+(chan_count++);

	}
	private static String generateProcId()
	{
		String ret= null;
		
		while(true)
		{
			ret="proc_"+(proc_count++);
			if(!store.containsKey(ret));
				break;
		}
		return ret;

	}
	
	private static String generateThreadId()
	{
		String ret= null;
		
		while(true)
		{
			ret="t_"+(thread_count++);
			if(!store.containsKey(ret));
				break;
		}
		return ret;

	}
	private static String generateVarId(String id)
	{
		String ret;
		
		while(true)
		{
			ret = id+var_count++;
			if(!store.containsKey(ret))
				break;
		}
		return ret;
	}

	private static void showHelp()
	{
		System.out.println("\nCommands : \n\t forth (f) thread_name (executes forward one step of thread_name)");
		System.out.println("\t back (b)  thread_name (tries to execute backward one step of thread_name)");
		System.out.println("\t undo (u)  thread_name  n (forces backward the execution of n steps of thread_name)");
		System.out.println("\t roll (r) thread_name (rollsback a thread at its starting point)");
		System.out.println("\t list (l) (displays all the available threads)");
		System.out.println("\t print (p) id (shows the state of a thread, channel, or variable)");
		System.out.println("\t story (h) id (shows thread/channel computational history)");
		System.out.println("\t store  (s) (displays all the ids contained in the store)");
		System.out.println("\t help  (c) (displays all commands)");
		System.out.println("\t quit (q)\n");
	}

	/*prints the status of an id, including threads*/
	private static String printId(String id)
	{
		IValue val = store.get(id);
		ValueType type;
		if(val!=null)
		{
			type = val.getType();
			switch(type)
			{
				case BOOLEAN:
					//System.out.println(id + " = "+val);
					return val.toString();
				
				case ID:
					//if ID it can be either a channel or a procedure or a variable
					String xi = ((SimpleId)val).getId();
					Channel chan = chans.get(xi);
					//channel
					if(chan !=null)
						//System.out.println(id +" = " + printChan(chan.getValues()));
						return printChan(chan.getValues());
					else
					{
						//procedure
						IValue proc = procs.get(xi);
						if(proc != null)
							return proc.toString();
						else
						{
							return printId(xi);
						}
					}
						
			}
		}
		else
		{
			System.out.println("\n**invalid identifier: "+ id);
		}
		return null;
		
		
	}
	
	
	private static String printChan(List<Tuple<IValue,String>> queue)
	{
		String ret= " [";
		for (Tuple<IValue,String> ith : queue) {
			ValueType type = ith.getFirst().getType();
			if(type == ValueType.ID)
			{
				String xi = ((SimpleId)ith.getFirst()).getId();
				//variable
				if(store.get(xi)!= null)
				{
					ret+= " (" + store.get(xi).toString()+" , "+ith.getSecond() +")";
					continue;
				}
				List<Tuple<IValue,String>> lst = chans.get(xi).getValues();
				if(lst != null)
					ret += printChan(lst)+" ";
			}
			else
				ret+= " (" +ith.getFirst().toString() + " , "+ith.getSecond()+")";
		}
//		ret= ret.substring(0, ret.length());
		return ret+=" ]";
		
	}
	
	static private boolean isChan(String id)
	{
		IValue val = null;
		if ((val = store.get(id)) !=null)  
		{	
			if(val.getType() == ValueType.ID) 
			{
				String xi = ((SimpleId)val).getId();
				return chans.containsKey(xi);
			}
		}
		return false;
	}
	
	static void printHistory(String id)
	{
		if(!history.containsKey(id) && !isChan(id))
		{
			System.out.println(warning + " no history for identifier "+ id);
			return;
		
		}
		ArrayList<IHistory> h = history.get(id);
	
		if(h != null)
		{	
			if(h.size() == 0)
			{
				System.out.println(warning +" empty history for thread "+id);
			}
			for (IHistory log : h) {
				if(log.getType() != HistoryType.ESC)
					System.out.println(log.toString());
			}
		}
		else
		{
			//prints the history of a channel (if any)
			String xi = ((SimpleId)store.get(id)).getId();
			Channel ch = chans.get( xi);
			if(ch.getStory().isEmpty())
				System.out.println(warning +" empty history for channel "+id);
			else printChanHistory(xi);
		}
	}
	
	static void printChanHistory(String chan_id)
	{
		Channel ch = chans.get(chan_id);
		if(ch == null)
		{	
			System.out.println(warning + " no history for channel "+ chan_id);
			return;
		}
		
		LinkedList<Tuple<Tuple<IValue, String>, String>> h = ch.getStory();
		Iterator<Tuple<Tuple<IValue, String>, String>> it = h.descendingIterator();
		while(it.hasNext())
		{
			Tuple<Tuple<IValue, String>, String> ith = it.next();
			IValue val = ith.getFirst().getFirst();
			String tostring;
			if(val.getType() == ValueType.ID)
			{
				tostring = printId( ((SimpleId)val).getId() );
			}
			else tostring= ith.getFirst().getFirst().toString();
			System.out.println( "("+ tostring +" , " +ith.getFirst().getSecond() + " , "+ith.getSecond() +")");

			
		}
		
	}
	
	//there should be a better recursive implementation of this
	
	//return the rest of a statement sequence after the FIRST ESC
	private static IStatement afterEsc(IStatement stm)
	{
		ArrayList<IStatement> queue = new ArrayList<IStatement>();
		queue.add(stm);
		while(true)
		{
			IStatement el = queue.remove(0);
			//exits the loop as soon as the first esc has been found
			if(el.getType() == StatementType.ESC)
				break;
			if(el.getType()== StatementType.SEQUENCE)
			{
				Sequence seq = (Sequence) el;
				queue.add(0,seq.getDx());
				queue.add(0,seq.getSx());
			}
		}
		return build(queue);
	}

	//return the all the construct before the FIRST ESC

	private static IStatement beforeEsc(IStatement stm)
	{
		ArrayList<IStatement> queue = new ArrayList<IStatement>();
		ArrayList<IStatement> rest = new ArrayList<IStatement>();
		queue.add(stm);
		
		while(true)
		{
			IStatement el = queue.remove(0);
			//exits the loop as soon as the first esc has been found
			if(el.getType() == StatementType.ESC)
				break;
			if(el.getType() != StatementType.SEQUENCE)
				rest.add(el);
			if(el.getType()== StatementType.SEQUENCE)
			{
				Sequence seq = (Sequence) el;
				queue.add(0,seq.getDx());
				queue.add(0,seq.getSx());
			}
		}
		return build(rest);
	}
	
	//builds back a statement (simple of sequence) from a  list
	private static IStatement build(ArrayList<IStatement> queue)
	{
		if(queue.size() == 0)
			return null;
		if(queue.size() == 1)
			return queue.remove(0);
		else return new Sequence (queue.remove(0), build(queue));
			
	}
}

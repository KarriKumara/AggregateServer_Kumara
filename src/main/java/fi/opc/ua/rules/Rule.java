package fi.opc.ua.rules;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Rule {

    public Rule(String lhs, String rhs, String type) {
        /*
         * History handling is whatever is the aggregate server default.
         * 
         */
        this.LHS = lhs;//.replaceAll("\\s","");
        this.RHS = rhs;//.replaceAll("\\s","");
        this.Type = type;
    }

    public Rule(String lhs, String rhs, String type, String history) throws Exception {
        /*
         * History handling is as given, but parameters for local history, if any, are the server default.
         * 
         */
        this.LHS = lhs;//.replaceAll("\\s","");
        this.RHS = rhs;//.replaceAll("\\s","");
        this.Type = type;
        if(Rule.HistoryHandlingOptions.contains(history.toLowerCase()))
        {
            this.History = history.toLowerCase();
        } else throw new Exception("No such history handling method");
        
    }
    
    public Rule(String lhs, String rhs, String type, String history, Double minTime, Double maxTime) throws Exception {
        /*
         * History handling is as given, and parameters for local history, if any, are as given.
         * 
         */
        this.LHS = lhs;//.replaceAll("\\s","");
        this.RHS = rhs;//.replaceAll("\\s","");
        this.Type = type;
        if(Rule.HistoryHandlingOptions.contains(history.toLowerCase()))
        {
            this.History = history.toLowerCase();
        } else throw new Exception("No such history handling method");
        
        if(minTime > 0.0 && maxTime > 0.0 && minTime < maxTime)
        {
            this.MinTimeInterval = minTime;
            this.MaxTimeInterval = maxTime;
        } else throw new Exception("Invalid values for minimum and maximum time intervals");

        
    }

    public String LHS = null;
    public String RHS = null;
    public String Type = null;
    public String NodeClass = null;
    
    // These are the system's default values for history management.
    public String History = "none";
    public Double MinTimeInterval = 10.0;
    public Double MaxTimeInterval = 20.0;
    
    
    private static final Set<String> HistoryHandlingOptions = new HashSet<String>(Arrays.asList(
            new String[] {"local", "relay", "hybrid", "localcustom", "none"}
       ));
}

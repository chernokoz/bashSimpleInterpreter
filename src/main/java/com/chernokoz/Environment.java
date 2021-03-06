package com.chernokoz;

import java.io.File;
import java.util.HashMap;

/**
 * environment for interpreter
 */
public class Environment {

    /**
     * global vars in interpreter
     */
    private final HashMap<String, String> vars = new HashMap();

    /**
     * current directory value
     */
    private final String currentDirectory = new File("").getAbsolutePath();

    /**
     * getter for currentDirectory
     * @return currentDirectory
     */
    public String getCurrentDirectory() {
        return currentDirectory;
    }

    /**
     * method for check keys
     * @param identifier key
     * @return true if vars contain ind, false if not
     */
    public boolean isVar(String identifier) {
        return vars.containsKey(identifier);
    }

    /**
     * method for put var in global vars
     * @param identifier key
     * @param value value
     */
    public void putVar(String identifier, String value) {
        vars.put(identifier, value);
    }

    /**
     * method for get var from global vars
     * @param identifier key
     * @return value
     */
    public String getVar(String identifier) {
        return vars.get(identifier);
    }

}

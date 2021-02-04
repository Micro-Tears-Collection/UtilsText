package code.utils;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class IniFile {

    final Hashtable hashtable;

    public static IniFile createFromResource(String file) {
        return createFromResource(file, false);
    }
    
    public static IniFile createFromResource(String file, boolean keys) {
        file = StringTools.getStringFromResource(file);
        return new IniFile(file, keys);
    }
    
    public static Object[] createGroups(String file) {
        file = StringTools.getStringFromResource(file);
        String[] lines = StringTools.cutOnStrings(file, '\n');
        return createGroups(lines);
    }
    
    public static Object[] createGroups(String[] lines) {
        Vector groupsNames = new Vector();
        Vector allGroups = new Vector();
        
        Hashtable currentGroup = null;
        
        for(int i=0; i<lines.length; i++) {
            String line = lines[i];
            if(line.length() <= 0) continue;
            if(line.startsWith("#") || lines[i].startsWith(";")) continue;
            
            int charIndex;
            if(line.charAt(0) == '[') {
                //Group
                groupsNames.addElement( line.substring(1, line.length() - 1) );
                currentGroup = new Hashtable();
                allGroups.addElement(new IniFile(currentGroup));
            } else if ((charIndex = line.indexOf('=')) >= 0) {
                //Value
                String key = line.substring(0, charIndex).trim();
                String val = line.substring(charIndex + 1).trim();
                
                currentGroup.put(key, val);
            }
        }
        
        String[] namesM = new String[groupsNames.size()];
        IniFile[] groupsM = new IniFile[namesM.length];
        
        for(int i=0; i<namesM.length; i++) {
            namesM[i] = (String)groupsNames.elementAt(i);
            groupsM[i] = (IniFile)allGroups.elementAt(i);
        }
        
        return new Object[]{namesM, groupsM};
    }
    
    public static void save(Hashtable hashtable, PrintStream stream) throws IOException {
        Enumeration keys = hashtable.keys();
        Enumeration els = hashtable.elements();
        
        while(keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            Object obj = els.nextElement();
            
            if(obj instanceof Hashtable) {
                stream.print('[');
                stream.print(key);
                stream.print("]\n");
                
                save((Hashtable)obj, stream);
            } else if(obj instanceof String) {
                stream.print(key);
                stream.print('=');
                stream.print(obj);
                stream.print('\n');
            }
        }
    }
   
    public IniFile(Hashtable hash) {
        hashtable = hash;
    }
    
    public IniFile(String str, boolean keys) {
        hashtable = new Hashtable();
        String[] lines = StringTools.cutOnStrings(str, '\n');
        set(lines, keys);
    }
    
    public IniFile(String[] lines, boolean keys) {
        hashtable = new Hashtable();
        set(lines, keys);
    }
    
    public void set(String[] lines, boolean useGroups) {
        Hashtable currentGroup = hashtable;
        
        for(int i=0; i<lines.length; i++) {
            String line = lines[i];
            if(line.length() <= 0) continue;
            if(line.startsWith("#") || line.startsWith(";")) continue;
            
            int charIndex;
            if(line.charAt(0) == '[' && useGroups) {
                //Group
                String group = line.substring(1, line.length() - 1);
                currentGroup = new Hashtable();
                
                hashtable.put(group, currentGroup);
            } else if ((charIndex = line.indexOf('=')) >= 0) {
                //Value
                String key = line.substring(0, charIndex).trim();
                String val = line.substring(charIndex + 1).trim();
                
                currentGroup.put(key, val);
            }
        }
    }
    
    public void save(PrintStream ps) throws IOException {
        IniFile.save(hashtable, ps);
    }

    //Stuff
    
    public String[] keys() {
        String[] out = new String[hashtable.size()];
        Enumeration keys = hashtable.keys();
        
        for(int i=0; i<out.length; i++) {
            out[i] = (String)keys.nextElement();
        }
        
        return out;
    }
    
    public Hashtable[] hashtables() {
        Hashtable[] out = new Hashtable[hashtable.size()];
        Enumeration hashtables = hashtable.elements();
        
        for(int i=0; i<out.length; i++) {
            out[i] = (Hashtable)hashtables.nextElement();
        }
        
        return out;
    }
    
    public boolean groupExists(String group) {
        return hashtable.get(group) != null;
    }
    
    public void put(String group, String key, String value) {
        Object val = hashtable.get(group);
        
        if(val instanceof Hashtable) ((Hashtable)val).put(key, value);
        else {
            Hashtable n = new Hashtable();
            n.put(key, value);
            hashtable.put(group, n);
        }
    }
    
    public void put(String key, String value) {
        hashtable.put(key, value);
    }
    
    //Groups and keys
    
    public String get(String group, String key) {
        Object val = hashtable.get(group);
        if(val!=null && val instanceof Hashtable) return (String)((Hashtable)val).get(key);
        
        return null;
    }
    
    public String getDef(String group, String key, String defaultValue) {
        String val = null;
        Object hash = hashtable.get(group);
        if(hash!=null && hash instanceof Hashtable) val = (String)((Hashtable)hash).get(key);
        
        return val == null ? defaultValue : val;
    }

    public byte getByte(String group, String key) {
        return StringTools.parseByte(get(group, key));
    }
    
    public float getFloat(String group, String key) {
        return StringTools.parseFloat(get(group, key));
    }
    
    public float getFloat(String group, String key, float defaultValue) {
        String tmp = get(group, key);
        return tmp == null ? defaultValue : StringTools.parseFloat(tmp);
    }

    public int getInt(String group, String key) {
        return StringTools.parseInt(get(group, key));
    }

    public int getInt(String group, String key, int defaultValue) {
        String tmp = get(group, key);
        return tmp == null ? defaultValue : StringTools.parseInt(tmp);
    }
    
    public long getLong(String group, String key) {
        return StringTools.parseLong(get(group, key));
    }
    
    //Only keys
    public String get(String key) {
        Object val = hashtable.get(key);
        return (val != null && val instanceof String) ? (String)val : null;
    }
    
    public String getDef(String key, String def) {
        Object val = hashtable.get(key);
        return (val != null && val instanceof String) ? (String)val : def;
    }

    public byte getByte(String key) {
        return StringTools.parseByte(get(key));
    }
    
    public float getFloat(String key) {
        return StringTools.parseFloat(get(key));
    }
    
    public float getFloat(String key, float def) {
        String tmp = get(key);
        return tmp == null ? def : StringTools.parseFloat(tmp);
    }
    
    public int getInt(String key) {
        return StringTools.parseInt(get(key));
    }

    public int getInt(String key, int def) {
        String tmp = get(key);
        return tmp == null ? def : StringTools.parseInt(tmp);
    }
    
    public long getLong(String key) {
        return StringTools.parseLong(get(key));
    }
}

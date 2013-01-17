package com.cloud.utils.xmlobject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.cloud.utils.exception.CloudRuntimeException;

import edu.emory.mathcs.backport.java.util.Collections;

public class XmlObject {
    private Map<String, Object> elements = new HashMap<String, Object>();
    private String text;
    private String tag;
    
    XmlObject() {
    }
    
    XmlObject putElement(String key, Object e) {
        Object old = elements.get(key);
        if (old == null) {
            System.out.println(String.format("no %s, add new", key));
            elements.put(key, e);
        } else {
            if (old instanceof List) {
                System.out.println(String.format("already list %s, add", key));
                ((List)old).add(e);
            } else {
                System.out.println(String.format("not list list %s, add list", key));
                List lst = new ArrayList();
                lst.add(old);
                lst.add(e);
                elements.put(key, lst);
            }
        }
        
        return this;
    }
    
    private Object recurGet(XmlObject obj, Iterator<String> it) {
        String key = it.next();
        Object e = obj.elements.get(key);
        if (!it.hasNext()) {
            return e;
        } else {
            if (!(e instanceof XmlObject)) {
                throw new CloudRuntimeException(String.format("%s doesn't reference to a XmlObject", it.next()));
            }
            return recurGet((XmlObject) e, it);
        }
    }
    
    public <T> T get(String elementStr) {
        String[] strs = elementStr.split("\\.");
        List<String> lst = new ArrayList<String>(strs.length);
        Collections.addAll(lst, strs);
        return (T)recurGet(this, lst.iterator());
    }
    
    public <T> List<T> getAsList(String elementStr) {
        Object e = get(elementStr);
        if (e instanceof List) {
            return (List<T>)e;
        }
        List lst = new ArrayList(1);
        lst.add(e);
        return lst;
    }
    
    public String getText() {
        return text;
    }

    void setText(String text) {
        this.text = text;
    }

    public String getTag() {
        return tag;
    }

    void setTag(String tag) {
        this.tag = tag;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("<" + tag);
        for (Map.Entry<String, Object> e : elements.entrySet()) {
            String key = e.getKey();
            Object value = e.getValue();
            if (!(value instanceof String)) {
                continue;
            }
            sb.append(String.format(" %s=\"%s\"", key, value.toString()));
        }
        
        if (text == null || "".equals(text.trim())) {
            sb.append(" />");
        } else {
            sb.append(">").append(text).append(String.format("</ %s>", tag));
        }
        return sb.toString();
    }
}
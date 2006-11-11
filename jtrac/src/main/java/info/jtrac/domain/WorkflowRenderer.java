/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.jtrac.domain;

import info.jtrac.util.XmlUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Class that is used to render a workflow to the GUI
 * currently designed to work with an HTML table in a JSP
 */
public class WorkflowRenderer implements Serializable {
    
    Map<String, Role> rolesMap;
    Map<String, Set<String>> transitionRoles;
    Map<Integer, Set<Integer>> stateTransitions;
    private Map<Integer, String> stateNames;
    Document document;
    
    public WorkflowRenderer(Map<String, Role> rolesMap, Map<Integer, String> stateNames) {
        this.rolesMap = rolesMap;
        this.stateNames = stateNames;
        init();
    }
    
    private void init() {
        // transitions <--> roleNames map
        // the key is a string concatenation <fromstate>_<tostate> for convenience
        transitionRoles = new TreeMap<String, Set<String>>();
        // for each state <--> a union of transitions across all roles
        stateTransitions = new HashMap<Integer, Set<Integer>>();
        for(Role r : rolesMap.values()) {
            for(State s: r.getStates().values()) {
                Set<Integer> transitions = stateTransitions.get(s.getStatus());
                if (transitions == null) {
                    transitions = new HashSet<Integer>();
                    stateTransitions.put(s.getStatus(), transitions);                    
                }
                transitions.addAll(s.getTransitions());
                for (int i : s.getTransitions()) {
                    String transitionKey = s.getStatus() + "_" + i;
                    Set<String> roleNames = transitionRoles.get(transitionKey);
                    if (roleNames == null) {
                        roleNames = new HashSet<String>();
                        transitionRoles.put(transitionKey, roleNames);
                    }
                    roleNames.add(r.getName());
                }
            }
        }
        document = XmlUtils.getNewDocument("state");
        Element e = document.getRootElement();
        e.addAttribute("name", stateNames.get(State.NEW));
        e.addAttribute("key", State.NEW + "");
        addTransitions(e, State.NEW);
    }
    
    /* has the state already been added to the tree? */
    private boolean stateExists(int key) {
        return document.selectNodes("//state[@key='" + key + "']").size() > 0;
    }
    
    /* main recursive function */
    private void addTransitions(Element parent, int state) {
        Set<Integer> transitions = stateTransitions.get(state);
        if (transitions == null) {
            return;
        }
        for(int i : transitions) {
            boolean exists = stateExists(i);
            Element child = parent.addElement("state");
            child.addAttribute("name", stateNames.get(i));
            child.addAttribute("key", i + "");
            if (exists) {
                child.addAttribute("mirror", "true");
            } else {
                addTransitions(child, i);
            }            
        }        
    }

    public Document getDocument() {
        return document;
    }
    
    public String getAsString() {
        return XmlUtils.getAsPrettyXml(document);
    }
    
    private String getAsHtml(Element e) {
        StringBuffer sb = new StringBuffer();        
        List<Element> childElements = (List<Element>) e.elements();
        String stateClass = e.attributeValue("mirror") != null ? "mirror" : "state";
        sb.append("<table class='workflow'><tr><td rowspan='" + childElements.size() + "' class='" + stateClass + "'>");        
        sb.append(e.attributeValue("name"));        
        sb.append("</td>");
        boolean first = true;
        String fromState = e.attributeValue("key");
        for(Element child : childElements) {
            if (!first) {
                sb.append("<tr>");
            }
            String toState = child.attributeValue("key");
            sb.append("<td class='transitions'>");
            for(String roleKey : transitionRoles.get(fromState + "_" + toState)) {
                sb.append(roleKey).append("<br/>");
            }
            sb.append("</td><td>");
            sb.append(getAsHtml(child));
            sb.append("</td></tr>");
            if (first) {
              first = false;  
            } 
        }        
        sb.append("</table>");        
        return sb.toString();
    }
    
    public String getAsHtml() {
        return getAsHtml(document.getRootElement());
    }
}
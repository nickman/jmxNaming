/**
 * Helios, OpenSource Monitoring
 * Brought to you by the Helios Development Group
 *
 * Copyright 2007, Helios Development Group and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org. 
 *
 */
package org.helios.javax.naming.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.DynamicMBean;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;



/**
 * <p>Title: JMXHelperExtended</p>
 * <p>Description: Some extended JMX helper utilities not available in HeliosUtils. </p> 
 * <p>Company: Helios Development Group</p>
 * @author Whitehead (whitehead.nicholas@gmail.com)
 * @version $Revision$
 */
public class JMXHelper {
	
	/** The property name where the jmx default domain is referenced */
	public static final String JMX_DOMAIN_PROPERTY = "org.helios.jmx.domain";
	/** The default jmx default domain is referenced */
	public static final String JMX_DOMAIN_DEFAULT = System.getProperty(JMX_DOMAIN_PROPERTY, ManagementFactory.getPlatformMBeanServer().getDefaultDomain());
	/** Regex WildCard Support Pattern for ObjectName key values */
	public static final Pattern OBJECT_NAME_KP_WILDCARD = Pattern.compile("[:|,](\\S+?)~=\\[(\\S+?)\\]");
	
	/** An object name filter that maps to all registered MBeans */
	public static final ObjectName ALL_MBEANS_FILTER = objectName("*:*");

	
	/**
	 * Acquires the configured or default Helios target MBeanServer.
	 * @return An MBeanServer.
	 */
	public static MBeanServer getHeliosMBeanServer() {
		MBeanServer server = null;
		String jmxDomain = getEnvThenSystemProperty(JMX_DOMAIN_PROPERTY, null);
		if(jmxDomain!=null) {
			server = getLocalMBeanServer(jmxDomain, true);
		}
		if(server==null) {
			return ManagementFactory.getPlatformMBeanServer();
		}
		
		return server;
	}
	
	/**
	 * Looks up a property, first in the environment, then the system properties. 
	 * If not found in either, returns the supplied default.
	 * @param name The name of the key to look up.
	 * @param defaultValue The default to return if the name is not found.
	 * @param properties An array of properties to search in. If empty or null, will search system properties. The first located match will be returned.
	 * @return The located value or the default if it was not found.
	 */
	public static String getEnvThenSystemProperty(String name, String defaultValue, Properties...properties) {
		
		String value = System.getenv(name);
		if(value==null) {			
			value = mergeProperties(properties).getProperty(name);
		}
		if(value==null) {
			value=defaultValue;
		}
		return value;
	}

	/**
	 * Merges the passed array of properties with System properties and returns a new property map
	 * @param properties An optional array of properties to merge with System properties
	 * @return the merged property map
	 */
	public static Properties mergeProperties(Properties...properties) {
		Properties allProps = new Properties();
		if(properties==null || properties.length==0) {
			allProps.putAll(System.getProperties());
		} else {
			for(int i = properties.length-1; i>=0; i--) {
				if(properties[i] != null && properties[i].size() >0) {
					allProps.putAll(properties[i]);
				}
			}
		}
		return allProps;
	}
	
	
	/**
	 * Acquires the configured or default Helios target MBeanServer wrapped with a checked exception free interface.
	 * @return An MBeanServer.
	 */
	public static NamingMBeanServer getRuntimeHeliosMBeanServer() {
		return NamingMBeanServer.getInstance(getHeliosMBeanServer());
	}
	
	
	/**
	 * Returns an MBeanConnection for an in-vm MBeanServer that has the specified default domain.
	 * @param domain The default domain of the requested MBeanServer.
	 * @return The located MBeanServerConnection or null if one cannot be located. 
	 */
	public static MBeanServer getLocalMBeanServer(String domain) {
		return getLocalMBeanServer(domain, true);
	}
	
	/**
	 * Searches for a matching MBeanServer in the passed list of domains and returns the first located.
	 * If one cannot be located a null will be returned. 
	 * @param domains The default domain of the requested MBeanServer.
	 * @return The located MBeanServerConnection or null if one cannot be found.
	 */
	public static MBeanServer getLocalMBeanServer(String...domains) {
		return getLocalMBeanServer(true, domains);
	}
	
	/**
	 * Searches for a matching MBeanServer in the passed list of domains and returns the first located.
	 * If one cannot be located, returnNullIfNotFound will either cause a null to be returned, or a RuntimeException. 
	 * @param returnNullIfNotFound If true, returns a null if a matching MBeanServer cannot be found. Otherwise, throws a RuntimeException.
	 * @param domains The default domain of the requested MBeanServer.
	 * @return The located MBeanServerConnection or null if one cannot be found and returnNullIfNotFound is true.
	 */
	public static MBeanServer getLocalMBeanServer(boolean returnNullIfNotFound, String...domains) {
		MBeanServer server = null;
		StringBuilder buff = new StringBuilder();
		for(String domain: domains) {
			server = getLocalMBeanServer(domain);
			buff.append(domain).append(",");
			if(server!=null) return server;
		}
		if(returnNullIfNotFound) {
			return null;
		} else {
			throw new RuntimeException("No MBeanServer located for domains [" + buff.toString() + "]"); 
		}
	}
	
	
	/**
	 * Returns an MBeanConnection for an in-vm MBeanServer that has the specified default domain.
	 * @param domain The default domain of the requested MBeanServer.
	 * @param returnNullIfNotFound If true, returns a null if a matching MBeanServer cannot be found. Otherwise, throws a RuntimeException. 
	 * @return The located MBeanServerConnection or null if one cannot be found and returnNullIfNotFound is true. 
	 */
	public static MBeanServer getLocalMBeanServer(String domain, boolean returnNullIfNotFound) {
		if(domain==null || domain.equals("") || domain.equalsIgnoreCase("DefaultDomain") || domain.equalsIgnoreCase("Default")) {
			return ManagementFactory.getPlatformMBeanServer();
		}
		List<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);
		for(MBeanServer server: servers) {
			if(server.getDefaultDomain().equals(domain)) return server;
		}
		if(returnNullIfNotFound) {
			return null;
		}
		throw new RuntimeException("No MBeanServer located for domain [" + domain + "]");
	}
	
	/**
	 * Creates a new JMX object name.
	 * @param on A string type representing the ObjectName string.
	 * @return an ObjectName the created ObjectName
	 */
	public static ObjectName objectName(CharSequence on) {
		try {
			return new ObjectName(on.toString());
		} catch (Exception e) {
			throw new RuntimeException("Failed to create Object Name", e);
		}
	}
	
	/**
	 * Creates a new JMX object name by appending properties on the end of an existing name
	 * @param on An existing ObjectName
	 * @param props Appended properties in the for {@code key=value}
	 * @return an ObjectName the created ObjectName
	 */
	public static ObjectName objectName(ObjectName on, CharSequence...props) {
		StringBuilder b = new StringBuilder(on.toString());
		try {			
			if(props!=null) {
				for(CharSequence prop: props) {
					b.append(",").append(prop);
				}
			}
			return new ObjectName(b.toString());
		} catch (Exception e) {
			throw new RuntimeException("Failed to create Object Name from [" + b + "]", e);			 
		}
	}
	
	
	/**
	 * Creates a new JMX object name.
	 * @param domain A string type representing the ObjectName domain
	 * @param properties A hash table of the Object name's properties
	 * @return an ObjectName the created ObjectName
	 */
	public static ObjectName objectName(CharSequence domain, Hashtable<String, String> properties) {
		try {
			return new ObjectName(domain.toString(), properties);
		} catch (Exception e) {
			throw new RuntimeException("Failed to create Object Name", e);
		}
	}
	

	/**
	 * Creates a new JMX object name.
	 * @param domain The ObjectName domain
	 * @param nameValuePairs an (even lengthed) array of name value pairs making up the key properties
	 * @return an ObjectName the created ObjectName
	 */
	public static ObjectName objectName(CharSequence domain, CharSequence...nameValuePairs) {
		if(domain==null || domain.toString().length()<1) throw new IllegalArgumentException("Null or zero length domain name");
		if(nameValuePairs==null || nameValuePairs.length<1 || nameValuePairs.length%2!=0) {
			throw new IllegalArgumentException("Invalid number of namevaluepairs [" + (nameValuePairs==null ? 0 : nameValuePairs.length) + "]");
		}
		try {
			Hashtable<String, String> props = new Hashtable<String, String>();
			for(int i = 0; i < nameValuePairs.length; i++) {
				if(nameValuePairs[i]==null || nameValuePairs[i].toString().length()<1) {
					throw new IllegalArgumentException("Null or blank nameValuePair entry at index [" + i + "]");
				}
				String key = nameValuePairs[i].toString();
				i++;
				if(nameValuePairs[i]==null || nameValuePairs[i].toString().length()<1) {
					throw new IllegalArgumentException("Null or blank nameValuePair entry at index [" + i + "]");
				}				
				String value = nameValuePairs[i].toString();
				props.put(key, value);
			}
			return new ObjectName(domain.toString(), props);
		} catch (IllegalArgumentException iae) {
			throw iae;
		} catch (Exception e) {
			throw new RuntimeException("Failed to create Object Name", e);
		}
	}
	
	/**
	 * Registers an MBean
	 * @param server The MBeanServer to register in
	 * @param objectName The ObjectName of the MBean
	 * @param mbean The MBean object instance to register
	 */
	public static void registerMBean(MBeanServer server, ObjectName objectName, Object mbean) {
		try {
			server.registerMBean(mbean, objectName);
		} catch(Exception e) {
			throw new RuntimeException("Failed to register MBean [" + objectName + "]", e);
		}
	}
	
	
	/**
	 * Retrieves MBeanInfo on the specified object name.
	 * @param server
	 * @param on
	 * @return an MBeanInfo
	 */
	public static MBeanInfo mbeanInfo(MBeanServerConnection server, ObjectName on) {
		try {
			return server.getMBeanInfo(on);
		} catch (Exception e) {
			throw new RuntimeException("Failed to get MBeanInfo", e);
		}		
	}
	
	/**
	 * Sets an MBean attribute.
	 * @param on
	 * @param server
	 * @param name
	 * @param value
	 */
	public static void setAttribute(ObjectName on, MBeanServerConnection server, String name, Object value) {
		try {
			server.setAttribute(on, new Attribute(name, value));
		} catch (Exception e) {
			throw new RuntimeException("Failed to set Attribute", e);
		}				
	}
	
	
	/**
	 * Sets a list of MBean attributes. Throws no exceptions. Returns a list of successfully set values.
	 * @param on
	 * @param server
	 * @param attributes
	 * @return
	 */
	public static Map<String, Object> setAttributesWithRet(ObjectName on, MBeanServerConnection server, Object...attributes) {
		Map<String, Object> returnValues = new HashMap<String, Object>(attributes.length);		
		Collection<NVP> list = NVP.generate(attributes);
		for(NVP nvp: list) {
			try {
				setAttribute(on, server, nvp.getName(), nvp.getValue());
				returnValues.put(nvp.getName(), nvp.getValue());
			} catch (Exception e) {}
		}
		return returnValues;
	}
	
	/**
	 * Returns a String->Object Map of the named attributes from the Mbean.
	 * @param on The object name of the MBean.
	 * @param server The MBeanServerConnection the MBean is registered in.
	 * @param attributes An array of attribute names to retrieve.
	 * @return A name value map of the requested attributes.
	 */
	public static Map<String, Object> getAttributes(ObjectName on, MBeanServerConnection server, String...attributes) {
		try {
			Map<String, Object> attrs = new HashMap<String, Object>(attributes.length);
			AttributeList attributeList = server.getAttributes(on, attributes);
			
			
			for(int i = 0; i < attributeList.size(); i++) {
				Attribute at = (Attribute)attributeList.get(i);
				if(isIn(at.getName(), attributes)) {
					attrs.put(at.getName(), at.getValue());
				}
			}
			return attrs;
		} catch (Exception e) {
			throw new RuntimeException("Failed to getAttributes on [" + on + "]", e);
		}
	}
	
	/**
	 * Inspects the array to see if it contains the passed string.
	 * @param name
	 * @param array
	 * @return true if the array contains the passed string.
	 */
	public static boolean isIn(String name, String[] array) {
		if(array==null || name==null) return false;
		for(String s: array) {
			if(s.equals(name)) return true;
		}
		return false;
		
	}
	
	
	/**
	 * Sets a list of MBean attributes. Throws an exception on any failure. Returns a list of successfully set values.
	 * @param on
	 * @param server
	 * @param attributes
	 * @return
	 */
	public static Map<String, Object> setAttributes(ObjectName on, MBeanServerConnection server, Object...attributes) {
		Map<String, Object> returnValues = new HashMap<String, Object>(attributes.length);		
		Collection<NVP> list = NVP.generate(attributes);
		for(NVP nvp: list) {
			setAttribute(on, server, nvp.getName(), nvp.getValue());
			returnValues.put(nvp.getName(), nvp.getValue());
		}
		return returnValues;
	}
	
	/**
	 * Gets an attribute value from an mbean.
	 * @param on
	 * @param server
	 * @param name
	 * @return
	 */
	public static Object getAttribute(ObjectName on, MBeanServerConnection server, String name) {
		try {
			return server.getAttribute(on,name);
		} catch (Exception e) {
			throw new RuntimeException("Failed to get attribute", e);
		}
	}
	
	/**
	 * Invokes an operation on the mbean.
	 * @param on
	 * @param server
	 * @param action
	 * @param args
	 * @param signature
	 * @return
	 */
	public static Object invoke(ObjectName on, MBeanServerConnection server, String action, Object[] args, String[] signature) {
		try {
			return server.invoke(on, action, args, signature);
		} catch (Exception e) {
			throw new RuntimeException("Failed to invoke operation", e);
		}
	}
	
	
	//public static final Pattern OBJECT_NAME_KP_WILDCARD = Pattern.compile("[:|,](\\S+?)~=\\[(\\S+?)\\]");
	
	public Set<ObjectName> getMatchingObjectNames(CharSequence wildcardEquals, ObjectName wildcard, MBeanServerConnection conn) {
		final String wc = new StringBuilder("(").append(wildcardEquals).append("$)").toString();
		Set<ObjectName> names = new HashSet<ObjectName>();
		//Matcher m = OBJECT_NAME_KP_WILDCARD.matcher(wildcard.toString());
		
		// A map of regex patterns to match on, keyed by the actual property key
		Map<String, Pattern> wildcardQueryProps = new HashMap<String, Pattern>();
		// the original wildcard object's key properties
		Hashtable<String, String> wildcardProps = wildcard.getKeyPropertyList();
		// the non wildcarded property keys we will query the mbean server with
		Hashtable<String, String> queryProps = new Hashtable<String, String>();
		queryProps.putAll(wildcardProps);
		
		
//		while(m.find()) {
//			String key = m.group(1);
//			String valueRegex = m.group(2);
//			wildcardProps.put(key, valueRegex);			
//		}
		// Extract the wildcarded property keys, ie, where the key is KEY<wildcardEquals>
		for(Map.Entry<String, String> prop: wildcard.getKeyPropertyList().entrySet()) {
			if(prop.getKey().endsWith(wc)) {
				String actualKey = prop.getKey().replaceFirst(wc, "");
				wildcardQueryProps.put(actualKey, Pattern.compile(prop.getValue()));
				queryProps.remove(actualKey);
			}
		}
		// Build the lookup query
		StringBuilder b = new StringBuilder(wildcard.getDomain());
		b.append(":");
		// Append the non regex wildcarded properties
		for(Map.Entry<String, String> qp: queryProps.entrySet()) {
			b.append(qp.getKey()).append("=").append(qp.getValue()).append(",");
		}
		// Append the regex wildcarded property keys and "*" as the value
		for(String key: wildcardQueryProps.keySet()) {
			b.append(key).append("=*,");
		}
		// Append a property wild card if the wildcard objectName had:
		//	Property Pattern:true
		//	PropertyList Pattern:true
		//	PropertyValue Pattern:false
		if(wildcard.isPropertyPattern() && wildcard.isPropertyListPattern() && !wildcard.isPropertyValuePattern()) {
			b.append("*");
		}
		if(b.toString().endsWith(",")) {
			b.deleteCharAt(b.length()-1);
		}
		// Create the query object
		try {
			ObjectName queryObjectName = objectName(b);
			for(ObjectName qon: conn.queryNames(queryObjectName, null)) {
				boolean match = true;
				for(Map.Entry<String, Pattern> pattern: wildcardQueryProps.entrySet()) {
					match = pattern.getValue().matcher(qon.getKeyProperty(pattern.getKey())).matches();
					if(!match) break;
				}
				if(match) {
					names.add(qon);
				}
			}
		} catch (Exception e) {			
		}
		
		// Remove all the wildcarded properties from the wildcard objectname's props
		
		//ObjectName query = new ObjectName(wildcard.getDomain());
		
		
		return names;
	}
	
	/*
import java.util.regex.*;
import javax.management.*;

String value = "com.ecs.jms.destinations:service~=[A/B/C],type~=[Queue|Topic],*";
on =  new ObjectName(value);
println on.getKeyPropertyList();
Pattern p = Pattern.compile("[:|,](\\S+?)~=\\[(\\S+?)\\]");
Matcher m = p.matcher(value);
while(m.find()) {
    println "Group:${m.group(1)}";
    println "Group:${m.group(2)}";
}

	 */
	
	
	
	
	
	
	/**
	 * Sorts an array of objects that implement the comparable interface.
	 * @param args An array of objects.
	 * @return An array of objects sorted by their natural order.
	 */
	@SuppressWarnings("unchecked")
	public static Object[] sortArray(Object...args) {
		List<Comparable> list = new ArrayList<Comparable>(args.length);
		for(Object o: args) {list.add((Comparable)o);}
		Collections.sort(list);
		return list.toArray(new Object[list.size()]);
	}
	
	
	/**
	 * Retrieves maps of attribute values keyed by attribute name, in turn keyed by the ObjectName of the MBean.
	 * @param server An MBeanServerConnection
	 * @param objectName An ObjectName which can be absolute or a wildcard.
	 * @param delimeter The delimeter for composite type compound names
	 * @param attributeNames An array of absolute or compound attribute names.
	 * @return a map of results.
	 * TODO: TabularData
	 * TODO: Collections / Maps / Arrays --> ref by index
	 */
	public static Map<ObjectName, Map<String, Object>> getMBeanAttributeMap(MBeanServerConnection server, ObjectName objectName, String delimeter, String...attributeNames) {
		if(server==null) throw new RuntimeException("MBeanServerConnection was null", new Throwable());
		if(objectName==null) throw new RuntimeException("ObjectName was null", new Throwable());
		if(attributeNames==null || attributeNames.length<1) throw new RuntimeException("Attribute names array was null or zero length", new Throwable());
		String[] rootNames = new String[attributeNames.length];
		Map<String, String> compoundNames = new HashMap<String, String>();
		for(int i = 0; i < attributeNames.length; i++) {
			String rootKey = null;
			if(attributeNames[i].contains(delimeter)) {
				String[] fragments = attributeNames[i].split(Pattern.quote(delimeter));
				rootKey = fragments[0];
				compoundNames.put(rootKey, attributeNames[i]);
			} else {
				rootKey = attributeNames[i];
			}
			rootNames[i] = rootKey;
		}
		Map<ObjectName, Map<String, Object>> map = new HashMap<ObjectName, Map<String, Object>>();		
		try {
			for(ObjectName on: server.queryNames(objectName, null)) {
				AttributeList attrs = null;
				try {
					attrs = server.getAttributes(on, rootNames);
					if(attrs.size()<1) continue;
				} catch (Exception e) {
					continue;
				}
				Map<String, Object> attrMap = new HashMap<String, Object>();
				map.put(on, attrMap);
				for(Attribute attr: attrs.asList()) {
					Object value = attr.getValue();
					if(value==null) continue;
					String name = attr.getName();
					if(value instanceof CompositeData && compoundNames.containsKey(name)) {
						try {
							name = compoundNames.get(name);
							value = extractCompositeData((CompositeData)value, delimeter, name);
						} catch (Exception e) {
							continue;
						}
					}
					attrMap.put(name, value);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to acquire attribute names for ObjectName [" + objectName + "] for MBeanServer [" + server + "]", e);
		}
		
		return map;
	}
	
	/**
	 * Retrieves maps of attribute values keyed by attribute name, in turn keyed by the ObjectName of the MBean.
	 * @param server An MBeanServerConnection
	 * @param objectName An ObjectName which can be absolute or a wildcard.
	 * @param delimeter The delimeter for composite type compound names
	 * @param attributeNames An collection of absolute or compound attribute names.
	 * @return a map of results.
	 */
	public static Map<ObjectName, Map<String, Object>> getMBeanAttributeMap(MBeanServerConnection server, ObjectName objectName, String delimeter, Collection<String> attributeNames) {
		if(attributeNames==null || attributeNames.size()<1) throw new RuntimeException("Attribute names collection was null or zero size", new Throwable());
		return getMBeanAttributeMap(server, objectName, delimeter, attributeNames.toArray(new String[attributeNames.size()]));
	}
	
	/**
	 * Extracts a composite data field from a CompositeData instance using a compound name.
	 * @param cd The composite data instance
	 * @param name The compound attribute name
	 */
	public static Object extractCompositeData(final CompositeData cd, final String delimeter, final String name) {
		String[] fragments = name.split(Pattern.quote(delimeter));
		CompositeData ref = cd;
		Object value = null;
		for(int i = 1; i < fragments.length; i++) {
			value = ref.get(fragments[i]);
			if(value instanceof CompositeData) {
				ref = (CompositeData)value;
			} else {
				break;
			}
		}
		return value;
	}
	
	/**
	 * Returns a list of a pojo's <code>PropertyDescriptor</code>s minus the same for <code>java.lang.Object</code>.
	 * @param pojo
	 * @return A list of <code>PropertyDescriptor</code>s.
	 */
	public static List<PropertyDescriptor> getPropertyDescriptors(Object pojo) {
		List<PropertyDescriptor> list = getAllPropertyDescriptors(pojo);
		List<PropertyDescriptor> objList = getAllPropertyDescriptors(new Object());
		list.removeAll(objList);
		return list;
	}
	
	/**
	 * Returns a list of all of a pojo's <code>PropertyDescriptor</code>s
	 * @param pojo
	 * @return A list of <code>PropertyDescriptor</code>s.
	 */
	public static List<PropertyDescriptor> getAllPropertyDescriptors(Object pojo) {
		List<PropertyDescriptor> list = new ArrayList<PropertyDescriptor>();
		BeanInfo bi = getBeanInfo(pojo);
		for(PropertyDescriptor pd : bi.getPropertyDescriptors()) {
			list.add(pd);
		}
		return list;
	}
	
	/**
	 * An regex pattern to parse A[X/Y/Z...] 
	 */
	public static final Pattern OBJECT_NAME_ATTR_PATTERN = Pattern.compile("(\\S+)\\[(\\S+)\\]"); 
	
	/**
	 * Retrieves the named attribute from the MBean with the passed ObjectName in the passed MBeanServerConnection.
	 * If the retrieval results in an exception or a null, the default value is returned 
	 * @param conn The MBeanServerConnection to the MBeanServer where the target MBean is registered
	 * @param objectName The ObjectName  of the target MBean
	 * @param attributeName The attribute name
	 * @param defaultValue The default value
	 * @return The attribute value or the defaut value
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getAttribute(MBeanServerConnection conn, ObjectName objectName, String attributeName, T defaultValue) {
		try {
			T t = (T)conn.getAttribute(objectName, attributeName);
			return t==null ? defaultValue : t;
		} catch (Exception e) {
			return defaultValue;
		}
	}
	
	/**
	 * Retrieves an attribute from an MBeanServer connection. 
	 * The compound name is in the format <b><code>&lt;ObjectName&gt;[&lt;Fragment<i>1</i>&gt;/&lt;Fragment<i>2</i>&gt;/&lt;Fragment<i>n</i>&gt;]</code></b>. 
	 * The multiple fragment names represent support for nested fields in a composite type.
	 * To retrieve a standard "flat" attribute, simply supply one fragment. 
	 * @param conn The MBeanServer connection
	 * @param compoundName The compound name
	 * @return the attribute value or null.
	 */
	public static Object getAttribute(MBeanServerConnection conn, CharSequence compoundName) {
		try {
			Matcher m = OBJECT_NAME_ATTR_PATTERN.matcher(compoundName);
			if(m.find()) {
				String objName = m.group(1);
				String[] fragments = m.group(2).split("/");
				return getAttribute(conn, objName, fragments);
			} else {
				return null;
			}
		} catch (Exception e) { return null; }
	}
	
	/**
	 * Retrieves an attribute from an MBeanServer connection. 
	 * @param conn The MBeanServer connection
	 * @param objectName The ObjectName
	 * @param attrs the compound attribute name in the format <b><code>&lt;Fragment<i>1</i>&gt;/&lt;Fragment<i>2</i>&gt;/&lt;Fragment<i>n</i>&gt;</code></b>.
	 * @return the attribute value or null.
	 */
	public static Object getAttribute(MBeanServerConnection conn, String objectName, String...attrs) {
		try {
			if(objectName!=null && attrs!=null && attrs.length > 0) {
				ObjectName on = objectName(objectName);
				StringBuilder b = new StringBuilder();
				for(String s: attrs) {
					b.append(s).append("/");
				}
				b.deleteCharAt(b.length()-1);
				String key = b.toString();
				Map<ObjectName, Map<String, Object>> map = getMBeanAttributeMap(conn, on, "/", key);
				return map.get(on).get(key);
			}
		} catch (Exception e) {
		}
		return null;
	}
	
	
	
	/**
	 * Wrapped call to <code>java.beans.Introspector</code>.
	 * Impl. may be swapped out.
	 * @param pojo The object to get the bean info for.
	 * @return A BeanInfo instance.
	 */
	public static BeanInfo getBeanInfo(Object pojo) {
		try {
			return Introspector.getBeanInfo(pojo.getClass());
		} catch (Exception e) {
			throw new RuntimeException("Failed to create bean info", e);
		}
	}
	
	/**
	 * Reregisters mbeans from one MBeanServer to another.
	 * @param query An ObjectName mask.
	 * @param source The source MBeanServer
	 * @param target The target MBeanServer
	 * @return The number of MBeans susccessfully re-registered.
	 */
	public static int remapMBeans(ObjectName query, MBeanServer source, MBeanServer target) {
		int remaps = 0;
		Set<ObjectName> mbeans = target.queryNames(query, null);
		for(ObjectName on: mbeans) {
			try {
				Object proxy = MBeanServerInvocationHandler.newProxyInstance(source, on, DynamicMBean.class, true);
				target.registerMBean(proxy, on);
				remaps++;
			} catch (Exception e) {}
		}
		return remaps;
	}
	
	
}


	

class NVP {
	String name = null;
	Object value = null;
	
	public static Collection<NVP> generate(Object...args) {
		List<NVP> list = new ArrayList<NVP>(args.length);
		String name = null;		
		for(int i=0; i<args.length; i++) {
			if(i+1 < args.length) {
				name=args[i].toString();
				i++;
				list.add(new NVP(name, args[i]));
			}
		}
		return list;
	}
	
	/**
	 * @param name
	 * @param value
	 */
	public NVP(String name, Object value) {
		super();
		this.name = name;
		this.value = value;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}
}

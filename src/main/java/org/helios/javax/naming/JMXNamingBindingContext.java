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
package org.helios.javax.naming;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.naming.directory.InvalidAttributesException;

/**
 * <p>Title: JMXNamingBindingContext</p>
 * <p>Description: Implementation of a DynamicMBean that serves as a bindings container and manages bindings for one logical JMXNaming JNDI context.</p>
 * <p>The MBean attribute names and values represent the bound name value pairs for this JNDI context.
 * 
 * </p>
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * @version $LastChangedRevision$
 * <p><code>org.helios.javax.naming.JMXNamingBindingContext</code></p>
 * TODO: <ul>
 * 	<li>Add new attribute</li>
 *  <li>Remove attribute</li>
 *  <li>Set attribute value</li>
 *  <li>Rename attribute</li>
 *  <li>Get all attributes</li>
 *  <li>Remove all attributes</li>
 *  <li>JMX Notifications For:<ul>
 *  	<li>Attribute Added</li>
 *  	<li>Attribute Removed</li>
 *  	<li>Attribute Renamed</li>
 *  	<li>NamingException</li>
 *  	<li>Context Added</li>
 *  	<li>Context Removed</li>
 *  	<li>Context Renamed</li>
 *  </ul></li>
 *  <li>NotificationBroadcasterSupport and shared ThreadPool.</li>
 *  <li>Serialization write replace into a simple Context</li>
 *  <li>Referenceable support</li>
 *  <li>Link support</li>
 *  <li></li>
 *  <li></li>
 * </ul>
 */

public class JMXNamingBindingContext implements DynamicMBean, MBeanRegistration {
	/** A map of the context environment */
	protected final Map<Object, Object> environment = new ConcurrentHashMap<Object, Object>();	
	/** A map of context bindings keyed by the binding name */
	protected final Map<String, Object> bindings = new ConcurrentHashMap<String, Object>();
	/** The ObjectName of this DynamicMBean */
	protected ObjectName objectName = null;
	/** The MBeanServer where this MBean is registered */
	protected MBeanServer server = null;
	/** A map of AttributeInfos keyed by attribute name */
	protected final Map<String, MBeanAttributeInfo> attrInfos = new ConcurrentHashMap<String, MBeanAttributeInfo>();
	
	/**
	 * Adds a new attribute to this context
	 * @param name the name to bind; may not be empty
	 * @param value the object to bind; possibly null 
     * @throws	NameAlreadyBoundException if name is already bound
     * @throws	InvalidAttributesException if object did not supply all mandatory attributes
     * @throws	NamingException if a naming exception is encountered
	 */
	public void bind(String name, Object value) throws NameAlreadyBoundException, InvalidAttributesException, NamingException {
		if(name==null) throw new NamingException("Binding name was null");
		if(value==null) {
			value = new NullValueBinding();
		}
		if(!bindings.containsKey(name)) {
			synchronized(bindings) {
				if(!bindings.containsKey(name)) {
					bindings.put(name, value);
				} else {
					throw new NameAlreadyBoundException("The binding named [" + name + "] is already bound in context [" + objectName + "]");
				}
			}
		} else {
			throw new NameAlreadyBoundException("The binding named [" + name + "] is already bound in context [" + objectName + "]");
		}
	}
	
	/**
	 * <p>Title: NullValueBinding</p>
	 * <p>Description: Represents a null value binding</p> 
	 * <p>Company: Helios Development Group LLC</p>
	 * @author Whitehead (nwhitehead AT heliosdev DOT org)
	 * @version $LastChangedRevision$
	 * <p><code>org.helios.javax.naming.JMXNamingBindingContext.NullValueBinding</code></p>
	 */
	private static class NullValueBinding implements Externalizable {

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {			
		}
		
		@Override
		public String toString() {
			return "null";
		}
		
	}
	
	
    /**
     * Obtain the value of a specific attribute of the Dynamic MBean.
     *
     * @param attribute The name of the attribute to be retrieved
     *
     * @return  The value of the attribute retrieved.
     *
     * @exception AttributeNotFoundException thrown if the named attribute is not bound.
     * @exception MBeanException  Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's getter.
     * @exception ReflectionException  Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the getter. 
     *
     * @see #setAttribute
     */
    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
    	return null;
    }
    
    /**
     * Set the value of a specific attribute of the Dynamic MBean.
     *
     * @param attribute The identification of the attribute to
     * be set and  the value it is to be set to.
     *
     * @exception AttributeNotFoundException thrown if the named attribute is not bound.
     * @exception InvalidAttributeValueException  thrown if the attribute value is invalid.
     * @exception MBeanException Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's setter.
     * @exception ReflectionException Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the MBean's setter.
     *
     * @see #getAttribute
     */
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
    	
    }
        
    /**
     * Get the values of several attributes of the Dynamic MBean.
     *
     * @param attributes A list of the attributes to be retrieved.
     *
     * @return  The list of attributes retrieved.
     *
     * @see #setAttributes
     */
    public AttributeList getAttributes(String[] attributes) {
    	return null;
    }
        
    /**
     * Sets the values of several attributes of the Dynamic MBean.
     *
     * @param attributes A list of attributes: The identification of the
     * attributes to be set and  the values they are to be set to.
     *
     * @return  The list of attributes that were set, with their new values.
     *
     * @see #getAttributes
     */
    public AttributeList setAttributes(AttributeList attributes) {
    	return null;
    }
    
    /**
     * Allows an action to be invoked on the Dynamic MBean.
     *
     * @param actionName The name of the action to be invoked.
     * @param params An array containing the parameters to be set when the action is
     * invoked.
     * @param signature An array containing the signature of the action. The class objects will
     * be loaded through the same class loader as the one used for loading the
     * MBean on which the action is invoked.
     *
     * @return  The object returned by the action, which represents the result of
     * invoking the action on the MBean specified.
     *
     * @exception MBeanException  Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's invoked method.
     * @exception ReflectionException  Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the method
     */
    public Object invoke(String actionName, Object params[], String signature[]) throws MBeanException, ReflectionException {
    	return null;
    }
    
    /**
     * Provides the exposed attributes and actions of the Dynamic MBean using an MBeanInfo object.
     *
     * @return  An instance of <CODE>MBeanInfo</CODE> allowing all attributes and actions 
     * exposed by this Dynamic MBean to be retrieved.
     *
     */
    public MBeanInfo getMBeanInfo() {
    	return null;
    }

    // =================================================
    //		MBeanRegistration Callbacks
    // =================================================

    /**
     * Allows the MBean to perform any operations it needs before
     * being registered in the MBean server.  If the name of the MBean
     * is not specified, the MBean can provide a name for its
     * registration.  If any exception is raised, the MBean will not be
     * registered in the MBean server.
     *
     * @param server The MBean server in which the MBean will be registered.
     *
     * @param name The object name of the MBean.  This name is null if
     * the name parameter to one of the <code>createMBean</code> or
     * <code>registerMBean</code> methods in the {@link MBeanServer}
     * interface is null.  In that case, this method must return a
     * non-null ObjectName for the new MBean.
     *
     * @return The name under which the MBean is to be registered.
     * This value must not be null.  If the <code>name</code>
     * parameter is not null, it will usually but not necessarily be
     * the returned value.
     *
     * @exception java.lang.Exception This exception will be caught by
     * the MBean server and re-thrown as an {@link
     * MBeanRegistrationException}.
     */
    public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
    	this.server = server;
    	this.objectName = name;
    	return name;
    }

    /**
     * Allows the MBean to perform any operations needed after having been
     * registered in the MBean server or after the registration has failed.
     *
     * @param registrationDone Indicates whether or not the MBean has
     * been successfully registered in the MBean server. The value
     * false means that the registration phase has failed.
     */
    public void postRegister(Boolean registrationDone) {
    	
    }

    /**
     * Allows the MBean to perform any operations it needs before
     * being unregistered by the MBean server.
     *
     * @exception java.lang.Exception This exception will be caught by
     * the MBean server and re-thrown as an {@link
     * MBeanRegistrationException}.
     */
    public void preDeregister() throws Exception {
    	
    }

    /**
     * Allows the MBean to perform any operations needed after having been
     * unregistered in the MBean server.
     */
    public void postDeregister() {
    	
    }

}

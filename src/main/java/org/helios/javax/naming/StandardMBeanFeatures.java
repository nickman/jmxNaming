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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.naming.Context;

/**
 * <p>Title: StandardMBeanFeatures</p>
 * <p>Description: Defines the default MBean features for all {@link JMXNamingBindingContext}s.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * @version $LastChangedRevision$
 * <p><code>org.helios.javax.naming.StandardMBeanFeatures</code></p>
 */
public class StandardMBeanFeatures {
	/** A map of OperationInfos keyed by method signature hash */
	protected static final Map<Integer, MBeanOperationInfo> operationInfos = new HashMap<Integer, MBeanOperationInfo>();
	/** A map of methods underlying each OperationInfo keyed by the method signature hash */
	protected static final Map<Integer, Method> keyedMethods = new HashMap<Integer, Method>();
	
	/** A map of ConstructorInfos keyed by ctor signature hash */
	protected static final Map<Integer, MBeanConstructorInfo> ctorInfos = new HashMap<Integer, MBeanConstructorInfo>();
	/** A map of methods underlying each ConstructorInfo keyed by the constructor signature hash */
	protected static final Map<Integer, Constructor<JMXNamingBindingContext>> keyedCtors = new HashMap<Integer, Constructor<JMXNamingBindingContext>>();
	
	/** A map of NotificationInfos keyed by notification type */
	protected static final Vector<MBeanNotificationInfo> notifInfos = new Vector<MBeanNotificationInfo>();

	static {
		for(Method method: Context.class.getDeclaredMethods()) {
			int hash = hashMethod(method);
			keyedMethods.put(hash, method);
			Vector<MBeanParameterInfo> params = new Vector<MBeanParameterInfo>(method.getParameterTypes().length);
			int cnt = 0;
			for(Class<?> clazz: method.getParameterTypes()) {
				params.add(new MBeanParameterInfo(
						"p" + cnt, clazz.getName(), 
						"MBeanParameter"
				));
				cnt++;
			}
			operationInfos.put(hash, new MBeanOperationInfo(
					method.getName(), "javax.naming.Context Operation",					
					params.toArray(new MBeanParameterInfo[params.size()]),
					method.getReturnType().getName(),
					MBeanOperationInfo.UNKNOWN,
					new ImmutableDescriptor(new String[]{
							"method.signature", "method.hash"
					}, new Object[]{
							method.toGenericString(), hash
					})
					
			));
		}
		Constructor<JMXNamingBindingContext>[] ctors = (Constructor<JMXNamingBindingContext>[])JMXNamingBindingContext.class.getDeclaredConstructors();
		for(Constructor<JMXNamingBindingContext> ctor: ctors) {
			int hash = hashCtor(ctor);
			keyedCtors.put(hash, ctor);
			Vector<MBeanParameterInfo> params = new Vector<MBeanParameterInfo>(ctor.getParameterTypes().length);
			int cnt = 0;
			for(Class<?> clazz: ctor.getParameterTypes()) {
				params.add(new MBeanParameterInfo(
						"p" + cnt, clazz.getName(), 
						"MBeanParameter"
				));
				cnt++;
			}
			ctorInfos.put(hash, new MBeanConstructorInfo(
					ctor.getName(), "javax.naming.Context Constructor",					
					params.toArray(new MBeanParameterInfo[params.size()]),
					new ImmutableDescriptor(new String[]{
							"ctor.signature", "ctor.hash"
					}, new Object[]{
							ctor.toGenericString(), hash
					})
			));			
		}
		notifInfos.add(new MBeanNotificationInfo(new String[]{JMXNamingBindingContext.NOTIF_TYPE_NEW_BINDING}, Notification.class.getName(), "A notification emitted when a new name is bound to the context"));
		notifInfos.add(new MBeanNotificationInfo(new String[]{JMXNamingBindingContext.NOTIF_TYPE_REMOVED_BINDING}, Notification.class.getName(), "A notification emitted when a bound name is removed from the context"));
		notifInfos.add(new MBeanNotificationInfo(new String[]{JMXNamingBindingContext.NOTIF_TYPE_RENAMED_BINDING}, Notification.class.getName(), "A notification emitted when a bound name is renamed"));
	}
	
	/**
	 * Builds a new MBeanInfo instance for a JMXNamingBindingContext instance
	 * @param attrInfos An optionally empty or null collection of the context's attributes
	 * @param name The ObjectName of the context
	 * @param descriptor An optional descriptor. If null,  a new one will be created.
	 * @return an MBeanInfo
	 */
	public static MBeanInfo buildMBeanInfo(Collection<MBeanAttributeInfo> attrInfos, ObjectName name, Descriptor descriptor) {
		return new MBeanInfo(JMXNamingBindingContext.class.getName(), "A jmxNaming JNDI Context [" + name + "]",
				(attrInfos!=null && attrInfos.size()>0) ? attrInfos.toArray(new MBeanAttributeInfo[attrInfos.size()]) : new MBeanAttributeInfo[0], 
				ctorInfos.values().toArray(new MBeanConstructorInfo[ctorInfos.size()]),
				operationInfos.values().toArray(new MBeanOperationInfo[operationInfos.size()]),
				notifInfos.toArray(new MBeanNotificationInfo[notifInfos.size()]),
				descriptor != null ? descriptor : new ImmutableDescriptor(new String[]{
						
				}, new Object[]{
					
				})
		);
	}
	
	/**
	 * Generates a deterministic hash code for an MBeanOperationInfo's operation name and signature.
	 * Intended to provide a lookup of the target MBeanOperationInfo from a DynamicMBean's invoke method.
	 * @param opName The operation name
	 * @param signature The parameter signature of the operation
	 * @return the hash code
	 */
	public static int hashOperation(String opName, String...signature) {
		StringBuilder b = new StringBuilder();
		b.append(Context.class.getName());
		b.append(opName);
		if(signature!=null) {
			for(String s: signature) {
				b.append(s);
			}
		}
		return b.toString().hashCode();		
	}
	
	/**
	 * Returns a deterministic hash code for a method
	 * @param method The method
	 * @return the hash code
	 */
	public static int hashMethod(Method method) {
		StringBuilder b = new StringBuilder();
		b.append(method.getDeclaringClass().getName());
		b.append(method.getName());
		for(Class<?> clazz: method.getParameterTypes()) {
			b.append(clazz.getName());
		}
		return b.toString().hashCode();
	}
	
	/**
	 * Returns a deterministic hash code for a constructor
	 * @param method The constructor
	 * @return the hash code
	 */
	public static int hashCtor(Constructor<JMXNamingBindingContext> ctor) {
		StringBuilder b = new StringBuilder();
		b.append(ctor.getDeclaringClass().getName());		
		for(Class<?> clazz: ctor.getParameterTypes()) {
			b.append(clazz.getName());
		}
		return b.toString().hashCode();
	}
	
	/**
	 * Returns a deterministic hash code for a constructor
	 * @param className The class name
	 * @param signature The ctor signature
	 * @return the hash code
	 */
	public static int hashCtor(String className, String...signature) {
		StringBuilder b = new StringBuilder();
		b.append(className);
		if(signature!=null) {
			for(String s: signature) {
				b.append(s);
			}
		}
		return b.toString().hashCode();
	}
	
	
}

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

/**
 * <p>Title: NamingMBeanServerException</p>
 * <p>Description: A runtime exception implementation that represents a {@javax.naming.NamingException}</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * @version $LastChangedRevision$
 * <p><code>org.helios.javax.naming.util.RuntimeMBeanServerException</code></p>
 */

public class NamingMBeanServerException extends RuntimeException {

	/**
	 * Creates a new NamingMBeanServerException
	 */
	public NamingMBeanServerException() {
		super();
	}

	/**
	 * Creates a new NamingMBeanServerException
	 * @param message The exception message
	 */
	public NamingMBeanServerException(String message) {
		super(message);
	}

	/**
	 * Creates a new NamingMBeanServerException
	 * @param cause The exception cause
	 */
	public NamingMBeanServerException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new NamingMBeanServerException
	 * @param message The exception message
	 * @param cause The exception cause
	 */
	public NamingMBeanServerException(String message, Throwable cause) {
		super(message, cause);
	}

}

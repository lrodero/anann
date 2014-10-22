/*
 * Copyright 2013 Luis Rodero-Merino.
 * 
 * This file is part of Annan.
 * 
 * Annan is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Annan is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Annan.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package org.anann.core.events.dispatcher;

import java.util.Collection;

import org.anann.core.events.Event;

public interface EventsDispatcher {
    
    public void dispatch(Event event);
    
    /**
     * This method assumes that all events in the set
     * have the same firing time and priority, and thus
     * they can be run at the same time. Also, it assumes
     * the collection passed as parameter does not contain
     * {@code null} elements. 
     * If the dispatcher is serial (method {@link #isSerial()} returns {@code true}), then 
     * the events in the collection will be run sequentially, not in parallel.
     * @param events
     */
    public void dispatch(Collection<Event> events);
    
    /**
     * This method returns whether the dispatcher is serial or not.
     * @return
     */
    public boolean isSerial();
}

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

package org.anann.core.events.holder;

import java.util.NavigableSet;
import java.util.SortedSet;

import org.anann.core.events.Event;

import com.google.common.collect.TreeMultimap;

public class EHBasedOnGuavaTreeMultimap implements EventsHolder {
    TreeMultimap<Event, Event> eventsMap = TreeMultimap.create(Event.ORDERER, Event.ABSOLUT_ORDERER);
    NavigableSet<Event> keys = eventsMap.keySet();

    @Override
    public void add(Event event) {
        eventsMap.put(event, event);
    }

    @Override
    public boolean remove(Event event) {
        NavigableSet<Event> simultEvents = eventsMap.get(event);
        if(simultEvents == null)
            return false;
        boolean changed = simultEvents.remove(event);
        if(simultEvents.size() == 0)
            eventsMap.removeAll(event);
        return changed;
    }

    @Override
    public Event next() {
        NavigableSet<Event> simultEvents = simultEvents();
        if(simultEvents == null)
            return null;
        Event nextEvent = simultEvents.pollFirst();
        return nextEvent;
    }

    @Override
    public Event peek() {
        NavigableSet<Event> simultEvents = simultEvents();
        if(simultEvents == null)
            return null;
        return simultEvents.first();
    }
    
    @Override
    public int size() {
        return eventsMap.size();
    }

    @Override
    public void clear() {
        eventsMap.clear();
    }

    @Override
    public SortedSet<Event> nextSimultaneous() {
        if(keys.isEmpty())
            return null;
        SortedSet<Event> simultEvents = eventsMap.removeAll(keys.first());
        if(simultEvents.isEmpty())
            throw new IllegalStateException("Empty set of events in guava TreeMultimap associated to key event " + keys.first());
        return simultEvents;
    }
    
    protected NavigableSet<Event> simultEvents() {
        if(keys.isEmpty())
            return null;
        NavigableSet<Event> simultEvents = eventsMap.get(keys.first());
        if(simultEvents == null)
            return null;
        if(simultEvents.isEmpty())
            throw new IllegalStateException("Empty set of events in guava TreeMultimap associated to key event " + keys.first());
        return simultEvents;        
    }
}

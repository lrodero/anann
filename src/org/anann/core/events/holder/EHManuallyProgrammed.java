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

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.anann.core.events.Event;
import org.anann.core.events.EventHandler;

public class EHManuallyProgrammed implements EventsHolder {
    
    protected List<Event> events = new LinkedList<Event>();
    
    public static void main(String[] args) { // Small test
        EHManuallyProgrammed eh = new EHManuallyProgrammed();
        eh.add(new Event(10000, 0, new DumbEventHandler(), 0));
        eh.add(new Event(1000, 0, new DumbEventHandler(), 0));
        eh.add(new Event(100, 0, new DumbEventHandler(), 0));
        eh.add(new Event(10, 0, new DumbEventHandler(), 0));
        eh.add(new Event(1, 0, new DumbEventHandler(), 0));
        for(Event event: eh.events)
            System.out.println("time:priority -> " + event.getFiringTime() + ":" + event.getPriority());
    }
    
    static class DumbEventHandler implements EventHandler {
        public void newEvent(Event event) {
            System.out.println("kk");            
        }        
    }

    @Override
    public void add(Event eventToInsert) {
        
        // Empty?
        if(events.isEmpty()){
            events.add(eventToInsert);
            return;
        }

        // Where should we insert this event?
        int left = 0;
        int right = events.size() - 1;
        int middlePoint = 0;
        
        do{
            middlePoint = (left + right) / 2;
            
            Event middleEvent = events.get(middlePoint);
            
            if(beforeOrAtThan(middleEvent, eventToInsert)){
                // Ok, after that 'middle' event
                // Any element after?
                if(middlePoint < events.size() - 1) {
                    // Some elements after 'middle' event, must check
                    Event followingMiddleEvent = events.get(middlePoint + 1);
                    // That following event, goes after eventToInsert?
                    if(!beforeOrAtThan(followingMiddleEvent, eventToInsert)){
                        // Here it is
                        events.add(middlePoint + 1, eventToInsert);
                        return;
                    }
                    // Keep trying
                    left = middlePoint + 1;
                } else {
                    // Inserting at the end (is the last event!)
                    events.add(eventToInsert);
                    return;
                }
            } else {
                // Ok, before that 'middle' event
                // Any element before?
                if(middlePoint > 0){
                    // Some elements before 'middle' event, must check
                    Event previousMiddleEvent = events.get(middlePoint - 1);
                    // That previous element, goes before eventToInsert?
                    if(!beforeOrAtThan(eventToInsert, previousMiddleEvent)){
                        // Inserting here
                        events.add(middlePoint, eventToInsert);
                        return;
                    }
                    // Keep trying
                    right = middlePoint - 1;
                } else {                    
                    // Inserting at the beginning (is the first event!)
                    events.add(0,eventToInsert);            
                    return;
                }
            }
            
        } while(left <= right);        
        
        // Adding at the end
        events.add(eventToInsert);
    }

    @Override
    public boolean remove(Event event) {
        int index = eventIndex(event);
        if(index < 0)     
            return false;
        events.remove(index);
        return true;
    }

    @Override
    public Event next() {
        if(events.size() == 0)
            return null;
        Event event = events.remove(0);
        return event;
    }
    
    @Override
    public SortedSet<Event> nextSimultaneous() {
        if(events.size() == 0)
            return null;
        Event nextEvent = events.remove(0);
        SortedSet<Event> simultaneousEvents = new TreeSet<Event>(Event.ABSOLUT_ORDERER);
        simultaneousEvents.add(nextEvent);
        while(!events.isEmpty()) {
            if(nextEvent.isSimultaneous(events.get(0)))
                simultaneousEvents.add(events.remove(0));
            else
                break;
        }
        return simultaneousEvents;
    }

    @Override
    public Event peek() {
        if(events.size() == 0)
            return null;
        Event event = events.get(0);
        // Trimming is not needed as the events list has not changed 
        return event;
    }

    @Override
    public int size() {
        return events.size();
    }

    @Override
    public void clear() {
        events.clear();
    }
    
    // event1 must be fired before or at event2?
    protected boolean beforeOrAtThan(Event e1, Event e2){
        if(Event.ORDERER.compare(e1, e2) <= 0)
            return true;
        return false;
    }
    
    protected int eventIndex(Event eventToSearch){
        
        // First, trivial check
        if(events.isEmpty())
            return -1;
        
        // Looking for event position. First, let's look for some
        // event to be fired at the same time and with the same priority
        
        int left = 0;
        int right = events.size() - 1;
        int middlePoint = 0;
        Event middleEvent = null;
        boolean found = false;
        
        do{
            middlePoint = (left + right) / 2;
            
            middleEvent = events.get(middlePoint);
            
            if((middleEvent.getFiringTime() == eventToSearch.getFiringTime()) && (middleEvent.getPriority() == eventToSearch.getPriority())){
                found = true;
                break;
            }
            
            if(beforeOrAtThan(middleEvent, eventToSearch)){
                // Ok, after that 'middle' event
                // Any element after?
                if(middlePoint < events.size() - 1) {
                    // Keep trying
                    left = middlePoint + 1;                    
                } else {
                    // Could not find element!!
                    break;
                }
            } else {
                // Ok, before that 'middle' event
                // Any element before?
                if(middlePoint > 0){
                    // Keep trying
                    right = middlePoint - 1;
                } else {
                    // Could not find element!!
                    break;
                }
            }
        } while(left <= right);
        
        // Could not find any event with that time and priority
        if(!found)
            return -1;
        
        // So, we have found some event with the same firing time and priority. But maybe it is not the
        // event we are looking for!!.
        if(middleEvent == eventToSearch)
            // It is!!
            return middlePoint;
        
        // We must compare with previous and later events...
        
        // Comparing with previous events...
        int indexToCheck = middlePoint;
        Event eventToCheck = null;
        do{
            if(--indexToCheck < 0)
                break;
            eventToCheck = events.get(indexToCheck);
            if(eventToCheck == eventToSearch)
                return indexToCheck;
        } while ((eventToCheck.getFiringTime() == eventToSearch.getFiringTime()) &&
                 (eventToCheck.getPriority() == eventToSearch.getPriority()));
        
        // Comparing with later events....
        indexToCheck = middlePoint;
        eventToCheck = null;
        do{            
            if(++indexToCheck > events.size() -1)
                break;
            eventToCheck = events.get(indexToCheck);
            if(eventToCheck == eventToSearch)
                return indexToCheck;
        } while ((eventToCheck.getFiringTime() == eventToSearch.getFiringTime()) &&
                 (eventToCheck.getPriority() == eventToSearch.getPriority()));
        
        // Finally, not found :/
        return -1;
    }

}

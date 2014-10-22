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

package org.anann.core.events;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;

public class Event {

    public static final Comparator<Event> ORDERER = new EventsOrderer();
    public static final Comparator<Event> ABSOLUT_ORDERER = new AbsolutEventsOrderer();
    
    public static final int MINIMUM_EVENT_PRIORITY = Integer.MIN_VALUE;
    public static final int MAXIMUM_EVENT_PRIORITY = Integer.MAX_VALUE;
    public static final int RESERVED_EVENT_CODES_RANGE_MIN = 11100;
    public static final int RESERVED_EVENT_CODES_RANGE_MAX = RESERVED_EVENT_CODES_RANGE_MIN + 100;
    
    // Event firing time
    private long firingTime = 0;   

    // Event type, codified
    private int code = 0;
    
    // Event priority level. If two events are to be fired at the same time
    // the first to be fired is the one with bigger priority.
    private int priority = MINIMUM_EVENT_PRIORITY;
    
    // Events can be fired in two ways: by calling the handleEvent() method of the EventHandler instance,
    // or calling the methodToCall method of the methodTarget object.
    
    // Who will handle the event. If null, then the method in 'methodToInvoke' is called.
    private EventHandler eventHandler = null;
    
    // Method to invoke when the event is fired. Only used if eventHandler == null.
    private Method methodToInvoke = null;
    private Object objectToCall = null;
    
    public Event(long firingTime, int code, EventHandler eventHandler, int priority){
        if(firingTime < 0)
            throw new Error("Trying to create an event to be triggered at a negative time");
        if(eventHandler == null)
            throw new Error("Trying to create an event with a null Event Handler");
        this.firingTime = firingTime;
        this.code = code;
        this.eventHandler = eventHandler;
        this.priority = priority;
    }
    
    public Event(long firingTime, int code, Method methodToInvoke, Object objectToCall, int priority){
        if(firingTime < 0)
            throw new Error("Trying to create an event to be triggered at a negative time");
        if(methodToInvoke == null)
            throw new Error("Trying to create an event with a null Method to invoke");
        if(objectToCall == null)
            throw new Error("Trying to create an event with a null Object to call");
        this.firingTime = firingTime;
        this.methodToInvoke = methodToInvoke;
        this.objectToCall = objectToCall;
        this.priority = priority;
    }
    
    public void recycle(long firingTime, int code, EventHandler eventHandler, int priority){
        if(firingTime < 0)
            throw new Error("Trying to recicle an event to be triggered at a negative time");
        if(eventHandler == null)
            throw new Error("Trying to recicle an event with a null Event Handler");
        this.firingTime = firingTime;
        this.code = code;
        this.eventHandler = eventHandler;
        this.priority = priority;
    }
    
    public long getFiringTime(){
        return firingTime;
    }
    
    public int getCode() {
        return code;
    }
    
    public int getPriority(){
        return priority;
    }
    
    public EventHandler getEventHandler() {
        return eventHandler;
    }
    
    public boolean isSimultaneous(Event event) {
        return (ORDERER.compare(this, event) == 0);
    }
    
    public void fireEvent(){
        if(eventHandler != null)
            eventHandler.newEvent(this);
        else
            try {
                methodToInvoke.invoke(objectToCall, new Object[]{this});
            } catch (IllegalArgumentException exception) {
                throw new Error("IllegalArgumentException caught when invoking method " + methodToInvoke.getName() + ", the method must accept SimulationEvent as the first (and only) argument", exception);
            } catch (IllegalAccessException exception) {
                throw new Error("IllegalAccessException caught when invoking method " + methodToInvoke.getName() + ", the method can not be accessed by the simulator", exception);
            } catch (InvocationTargetException exception) {
                throw new Error("InvocationTargetException caught when invoking method " + methodToInvoke.getName() + ", the method thrown some exception", exception);
            }
    }

}

class EventsOrderer implements Comparator<Event> {
    @Override
    public int compare(Event e1, Event e2) {
        
        if(e1.getFiringTime() < e2.getFiringTime())
            return -1;
        
        if(e1.getFiringTime() > e2.getFiringTime())
            return 1;
        
        if(e1.getPriority() > e2.getPriority())
            return -1;
        
        if(e1.getPriority() < e2.getPriority())
            return 1;
        
        return 0;
    }
}

class AbsolutEventsOrderer implements Comparator<Event> {
    
    private static EventsOrderer eventsOrderer = new EventsOrderer();
    
    @Override
    public int compare(Event e1, Event e2) {
        
        int normalOrdering = eventsOrderer.compare(e1, e2);
        
        if(normalOrdering != 0)
            return normalOrdering;

        int h1 = e1.hashCode();
        int h2 = e2.hashCode();
        
        if(h1 < h2)
            return -1;
        
        if(h1 > h2)
            return 1;
        
        return 0;
    }
}


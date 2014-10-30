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

package org.anann.core;

import java.util.ArrayList;
import java.util.List;

import org.anann.core.events.Event;
import org.anann.core.events.EventHandler;

public abstract class Entity implements EventHandler {
    
    private static final int TASK_FINISHED_EVENT_CODE = Event.RESERVED_EVENT_CODES_RANGE_MIN;
    private static final int TASK_FINISHED_EVENT_PRIORITY = Integer.MAX_VALUE;
    
    // Events waiting to be processed by this entity
    // TODO: Check whether a LinkedList could be more efficient (probably will!)
    private List<Event> waitingEvents = new ArrayList<Event>(); 
    private boolean busy = false;
    private Event taskFinishedEvent = new Event(0, TASK_FINISHED_EVENT_CODE, this, TASK_FINISHED_EVENT_PRIORITY);

    // To be overrided
    protected String name = null;
    protected Simulator simulator = null;
    
    @Override
    public void newEvent(Event event) {
        
        if(event.getCode() == TASK_FINISHED_EVENT_CODE) {
            if(!busy)
                throw new IllegalStateException("A task finished event reached entity " + name + ", but it is not in busy state");
            if(event != taskFinishedEvent)
                throw new IllegalStateException("A task finished event reached entity " + name + ", but it is not the proper event");
            if(waitingEvents.isEmpty()) {
                busy = false;
                return;
            }
            long processingTime = processEvent(waitingEvents.remove(0));
            if(processingTime > 0) {
                taskFinishedEvent.recycle(simulator.time() + processingTime, taskFinishedEvent.getCode(), this, TASK_FINISHED_EVENT_PRIORITY);
                simulator.schedule(taskFinishedEvent);
            }
            return;
        }
        
        if(busy) {
            if(!waitingEvents.isEmpty()) {
                // Checking last enqueued event time is not greater than the new one
                // (for the sake of consistency).
                Event lastEvent = waitingEvents.get(waitingEvents.size()-1);
                if(lastEvent.getFiringTime() > event.getFiringTime())
                    throw new IllegalStateException("An event with time " + event.getFiringTime() + " reached entity " + name + ", but the last" +
                                                    " event enqueued for that entitiy was assigned a greater time " + lastEvent.getFiringTime());
            }
            // New event must be added at the end, unless last events
            // have the same firing time and a lesser priority.
            // (we assume that events, with high probability, will be scheduled for a time later than
            // the last event in the waiting events queue. If we didn't, we would be using a HashSet
            // or something like that)
            int insertAt = waitingEvents.size();
            while(insertAt > 0) {
                Event enqueuedEvent = waitingEvents.get(insertAt-1);
                if((enqueuedEvent.getFiringTime() == event.getFiringTime()) && (enqueuedEvent.getPriority() > event.getPriority()))
                    insertAt--;
                else
                    break;
            }
            waitingEvents.add(insertAt, event);
            return;
        }

        busy = true;
        long processingTime = processEvent(event);
        if(processingTime > 0) {
            taskFinishedEvent.recycle(simulator.time() + processingTime, taskFinishedEvent.getCode(), this, TASK_FINISHED_EVENT_PRIORITY);
            simulator.schedule(taskFinishedEvent);
        }
    }
    
    /**
     * @param event
     * @return The time to process the event (i.e. time the entity will be in 'busy' state,
     * forcing new events to be enqueued.
     */
    protected abstract long processEvent(Event event);

}
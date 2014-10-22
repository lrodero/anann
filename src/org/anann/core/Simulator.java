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

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import org.anann.core.events.Event;
import org.anann.core.events.dispatcher.EventsDispatcher;
import org.anann.core.events.dispatcher.ParallelEventsDispatcher;
import org.anann.core.events.dispatcher.SerialEventDispatcher;
import org.anann.core.events.holder.EventsHolder;

/**
 * This class assumes that only one thread will run the simulation at all times,
 * so no synchronization is required either here or in the events holder regarding
 * events. However, we synchronize some methods to avoid potential issues.
 * @author lrodero
 *
 */
public class Simulator {
    
    private Set<SimulationObserver> observers = new HashSet<SimulationObserver>();
    protected long maxSimDuration = -1;
    private long simTime = 0;
    private boolean keepRunning = true;
    protected boolean running = false;
    private EventsHolder eventsHolder = null;
    private EventsDispatcher eventsDispatcher = null;
    
    public Simulator(EventsHolder eventsHolder) {
        this(eventsHolder, true);
    }
    
    public Simulator(EventsHolder eventsHolder, boolean sequential) {
        if(eventsHolder == null)
            throw new IllegalArgumentException("Cannot create a simulator with a null events holder");
        this.eventsHolder = eventsHolder;
        if(sequential)
            this.eventsDispatcher = new SerialEventDispatcher();
        else
            this.eventsDispatcher = new ParallelEventsDispatcher();
    }

    public synchronized boolean registerSimObserver(SimulationObserver observer) {
        return observers.add(observer);
    }

    public synchronized boolean unregisterSimObserver(SimulationObserver observer) {
        return observers.remove(observer);
    }

    public long time() {
        return simTime;
    }
    
    public synchronized void reset() {
        if(running)
            throw new IllegalArgumentException("Cannot reset a simulator while it is running");
        observers.clear();
        maxSimDuration = -1;
        simTime = 0;
        keepRunning = true;
        eventsHolder.clear();
    }

    public void schedule(Event event) {
        if(event == null)
            throw new IllegalArgumentException("Cannot program null events in simulation");
        if(event.getFiringTime() < simTime)
            throw new IllegalArgumentException("Cannot program an event for the past! Event was to be fired at " +
                                               event.getFiringTime() + " but simulation time is " + simTime);
        eventsHolder.add(event);
    }

    public void cancel(Event event) {
        if(event == null)
            throw new IllegalArgumentException("Cannot cancel a null event");
        eventsHolder.remove(event);
    }
    
    public void start(long maxSimDuration) {
        synchronized(this) {
            if(running)
                throw new IllegalStateException("Simulator is already running");
            if(maxSimDuration == 0)
                throw new IllegalArgumentException("Cannot set max simulation duration to 0, it must be either positive or negative value");
            this.maxSimDuration = maxSimDuration;
            running = true;
            for(SimulationObserver observer: observers)
                observer.beforeSimulation();
        }
        run();
        synchronized(this) {
            running = false;
            for(SimulationObserver observer: observers)
                observer.afterSimulation();
        }
    }
    
    public void start() {
        start(-1);
    }

    public void stop() {
        keepRunning = false;
    }

    protected void run() {
        if(eventsDispatcher.isSerial())
            runSerial();
        else
            runParallel();
    }
    
    protected void runSerial() {
        while(keepRunning) {
            Event event = eventsHolder.next();
            if(event == null)
                return;
            if(event.getFiringTime() < simTime)
                throw new IllegalStateException("The events holder has returned an event in the past! Simulation time is " +
                                                simTime + ", event time is " + event.getFiringTime());
            simTime = event.getFiringTime();
            if((maxSimDuration > 0) && (simTime > maxSimDuration))
                return;                
            eventsDispatcher.dispatch(event);
        }
    }
    
    protected void runParallel() {
        while(keepRunning) {
            SortedSet<Event> simultaneousEvents = eventsHolder.nextSimultaneous();
            if(simultaneousEvents == null)
                return;
            if(simultaneousEvents.isEmpty())
                throw new IllegalStateException("The events holder has returned an empty list of simultaneous events");
            Event event = simultaneousEvents.first();
            if(event.getFiringTime() < simTime)
                throw new IllegalStateException("The events holder has returned an event in the past! Simulation time is " +
                                                simTime + ", event time is " + event.getFiringTime());
            simTime = event.getFiringTime();
            if((maxSimDuration > 0) && (simTime > maxSimDuration))
                return;                
            eventsDispatcher.dispatch(simultaneousEvents);
        }    
    }

}

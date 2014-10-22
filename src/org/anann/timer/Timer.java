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

package org.anann.timer;

import org.anann.core.Simulator;
import org.anann.core.events.Event;
import org.anann.core.events.EventHandler;

public class Timer implements EventHandler {
    
    protected TimerEvent timerEvent = null;
    protected TimerEventsWaiter eventsWaiter = null;
    protected long period = 0;
    protected boolean waitingForTimeEvent = false;
    protected Simulator simulator = null;
    
    public Timer(Simulator simulator) {
        if(simulator == null)
            throw new IllegalArgumentException("Cannot create a " + Timer.class.getName() + " instance with null simulator");
        this.simulator = simulator;
    }
    
    public void programPeriodicalTimeEvent(TimerEventsWaiter eventsWaiter, long startingTime, long period){
        programPeriodicalTimeEvent(eventsWaiter, startingTime, period, TimerEvent.DEFAULT_TIMER_EVENT_PRIORITY);
    }
    
    public void programPeriodicalTimeEvent(TimerEventsWaiter eventsWaiter, long startingTime, long period, int eventPriority){
        
        if(eventsWaiter == null)
            throw new IllegalArgumentException("Cannot program a time event with a null " + TimerEventsWaiter.class.getName() + " instance");
        
        if(period <= 0)
            throw new IllegalArgumentException("Cannot schedule periodical time events with a non-positive period");
        
        if(startingTime < simulator.time())
            throw new Error("Cannot program a time event at " + startingTime + ", present simulation time is already " + simulator.time());

        // Registering who waits for time events 
        this.eventsWaiter = eventsWaiter;
        
        // Suspending any already scheduled time event
        suspend();
        
        // Now, programming new Timer event         
        if(timerEvent == null)
            timerEvent = new TimerEvent(startingTime, this, eventPriority);
        else
            timerEvent.recycle(startingTime, timerEvent.getCode(), this, eventPriority);
        
        simulator.schedule(timerEvent);
        
        waitingForTimeEvent = true;
        
        this.period = period;
    }
    
    public boolean eventScheduled(){
        return waitingForTimeEvent;
    }
    
    public long timeUntilNextEvent(){
        return (!waitingForTimeEvent ? -1 : timerEvent.getFiringTime() - simulator.time());
    }
    
    public long period(){
        return period;
    }
    
    public void updatePeriod(long newPeriod){
        
        if(period <= 0)
            throw new IllegalStateException("Cannot update period in a timer where no previous period has been set");

        if(newPeriod <= 0)
            throw new IllegalArgumentException("Cannot schedule periodical time events with a non-positive period");
        
        period = newPeriod;
    }
    
    public void suspend(){
        
        // Suspending any already scheduled time event
        if(waitingForTimeEvent)
            simulator.cancel(timerEvent);
        waitingForTimeEvent = false;
        
        // Suspending periodical timing of events too. 
        period = 0;
    }

    public void newEvent(Event event) {

        if(!waitingForTimeEvent)
            throw new IllegalStateException("Timer was not expecting a time event");
        
        if(event != timerEvent)
            throw new IllegalStateException("A task finished event reached timer, but it is not the proper event");
        
        // If event is periodical, must be programmed again.
        if(period > 0){
            timerEvent.recycle(simulator.time() + period, timerEvent.getCode(), this, timerEvent.getPriority());
            simulator.schedule(timerEvent);
        } else         
            waitingForTimeEvent = false;
        
        eventsWaiter.timeExpired(simulator.time());
    }

}

class TimerEvent extends Event {
    
    public static final int DEFAULT_TIMER_EVENT_PRIORITY = MINIMUM_EVENT_PRIORITY;
    private static final int TIMER_EVENT_CODE = Event.RESERVED_EVENT_CODES_RANGE_MIN + 1; 
    
    public TimerEvent(long time, EventHandler eventHandler, int priority){
        super(time, TIMER_EVENT_CODE, eventHandler, priority);
    }

}
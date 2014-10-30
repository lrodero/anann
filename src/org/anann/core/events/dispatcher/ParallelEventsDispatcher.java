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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.anann.core.events.Event;

/**
 * This class, in contrast with {@link SerialEventDispatcher} can run
 * several events in parallel when calling to {{@link #dispatch(Set)}, using
 * an internal thread pool.
 * Calling to {@link #dispatch(Event)}, in contrast, has the same effect
 * than in {@link SerialEventDispatcher}, it just triggers the event
 * using the calling thread (this way code is simplified and thread switching
 * is avoided).
 * @author lrodero
 */
public class ParallelEventsDispatcher implements EventsDispatcher {
    
    private ExecutorService parallelEventsExecutor = null;
    
    // We use a custom thread factory to be able to set names to the threads
    // that will be instantiated by the thread pool executor
    private final static ThreadFactory thFactory = new ThreadFactory() {
        ThreadFactory defThFactory = Executors.defaultThreadFactory();        
        @Override
        public Thread newThread(Runnable r) {
            Thread t = defThFactory.newThread(r);
            t.setName("SIM-"+t.getId());
            return t;
        }        
    };
    
    public ParallelEventsDispatcher() {
        int processors = Runtime.getRuntime().availableProcessors();
        parallelEventsExecutor = new ThreadPoolExecutor(processors, processors, 1, TimeUnit.NANOSECONDS, new LinkedBlockingQueue<Runnable>(), thFactory);
        ((ThreadPoolExecutor)parallelEventsExecutor).allowCoreThreadTimeOut(true);
    }
    
    @Override
    public void dispatch(Event event) {
        event.fireEvent();
    }

    @Override
    public void dispatch(Collection<Event> events) {
        try {
            parallelEventsExecutor.invokeAll(EventCall.createCalls(events));
        } catch (InterruptedException e) {
            throw new IllegalStateException("Thread(s) in events dispatcher internal pool were interrupted (?)", e);
        }
    }
    
    @Override
    public boolean isSerial() {
        return false;
    }
    
    static private class EventCall implements Callable<Object> {
        
        private Event event = null;
        
        EventCall(Event event) {
            this.event = event;
        }

        @Override
        public Object call() throws Exception {
            event.fireEvent();
            return null;
        }
        
        static Collection<EventCall> createCalls(Collection<Event> events) {
            List<EventCall> calls = new ArrayList<EventCall>(events.size());
            for(Event event: events)
                calls.add(new EventCall(event));
            return calls;
        }
    }

}

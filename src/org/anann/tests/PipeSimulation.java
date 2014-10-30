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

package org.anann.tests;

import java.util.Random;

import org.anann.core.Entity;
import org.anann.core.Simulator;
import org.anann.core.events.Event;
import org.anann.core.events.holder.EHBasedOnPriorityQueue;

public class PipeSimulation {
    
    public static void main(String[] args) {
        new PipeSimulation().run();
    }
    
    protected void run() {
        
        Simulator sim = new Simulator(new EHBasedOnPriorityQueue());
        
        // Creating pipe
        Processor p3 = new Processor(sim, "p3", null);
        Processor p2 = new Processor(sim, "p2", p3);
        Processor p1 = new Processor(sim, "p1", p2); 
        Processor.sendMsg(p1, 1000);
        Processor.sendMsg(p1, 10000);
        
        // Running simulation
        System.out.println("Starting simulation");
        sim.start();
        System.out.println("Simulation over, it took " + sim.time() + " units of virtual time");
    }
    
}

class Processor extends Entity {
    
    private static final int MSG_RECV_EVENT_CODE = Event.RESERVED_EVENT_CODES_RANGE_MIN - 100;
    private static final int MSG_RECV_EVENT_PRIORITY = Integer.MIN_VALUE;

    private static Random random = new Random(); 
    private static final int MIN_PR_TIME = 1000; 
    private static final int MAX_PR_TIME = 10000;
    
    private Processor nextProc = null;
    
    
    public Processor (Simulator sim, String name, Processor nextProc) {
        super.simulator = sim;
        super.name = name;
        this.nextProc = nextProc;
    }

    @Override
    protected long processEvent(Event event) {
        if(event.getCode() != MSG_RECV_EVENT_CODE) // Should never happen!
            throw new InternalError(name + " received event of an unknown type code: " + event.getCode());
        long elapsedTime = MIN_PR_TIME + (random.nextInt(MAX_PR_TIME - MIN_PR_TIME) + 1);
        if(nextProc != null)
            sendMsg(nextProc, elapsedTime);
        return elapsedTime;
    }
    
    protected static void sendMsg(Processor p, long time) {
        p.simulator.schedule(new Event(p.simulator.time() + time, MSG_RECV_EVENT_CODE, p, MSG_RECV_EVENT_PRIORITY));        
    }    
}

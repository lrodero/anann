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

/**
 * This dispatcher just triggers events 'one by one' using the 
 * calling thread. No parallelism is implemented.
 * @author lrodero
 *
 */
public class SerialEventDispatcher implements EventsDispatcher {

    @Override
    public void dispatch(Event event) {
        event.fireEvent();
    }

    @Override
    public void dispatch(Collection<Event> events) {
        for(Event event: events)
            dispatch(event);
    }
    
    @Override
    public boolean isSerial() {
        return true;
    }

}

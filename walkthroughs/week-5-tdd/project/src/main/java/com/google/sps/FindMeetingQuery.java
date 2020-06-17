// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;

public final class FindMeetingQuery {

    /**
    * Finds list of available meeting times based on already planned events and the
    * requested meeting and takes optional events into consideration.
    * @param events The list of already scheduled events
    * @param request The MeetingRequest that is attempting to be scheduled
    * @return The list of all meeting times that will fit the request
    */
    public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
        if (request.getAttendees().isEmpty() && request.getOptionalAttendees().isEmpty()) {
            return Arrays.asList(TimeRange.WHOLE_DAY);
        }

        if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
            return Arrays.asList();
        }

        List<Event> eventListOptional = new ArrayList(events);
        List<Event> eventList = new ArrayList(events);
        eventList = removeIrrelevantEvents(eventList,request,false);
        eventListOptional = removeIrrelevantEvents(eventListOptional,request,true);
        List<TimeRange> availableTimes = new ArrayList();
        availableTimes = findMeetingTime(eventList, request);
        List<TimeRange> optionalTimes = new ArrayList();
        optionalTimes = findMeetingTime(eventListOptional, request);
        
        if (optionalTimes.isEmpty() && !request.getAttendees().isEmpty()) {
            return availableTimes;
        }
        return optionalTimes;
    }

    /**
    * Removes all events that are invalid or do not contain relevent attendees.
    * @param events The list of events
    * @param request The MeetingRequest that being used to compare attendees
    * @param includeOptionalEvents the conditional to consider optional attendees
    * @return The list with all irrelevant events removed
    */
    private List<Event> removeIrrelevantEvents(List<Event> events, MeetingRequest request, boolean includeOptionalEvents) {
        if (includeOptionalEvents) {
            events.removeIf( e -> (
                (Collections.disjoint(e.getAttendees(), request.getAttendees()) 
                && Collections.disjoint(e.getAttendees(), request.getOptionalAttendees()))
                || e.getWhen().duration() <= 0));
            Collections.sort(events, new Comparator<Event>() {
                public int compare (Event e1, Event e2) {
                    return TimeRange.ORDER_BY_START.compare(e1.getWhen(), e2.getWhen());
                }
            });
        } else {
          events.removeIf( e -> (
            (Collections.disjoint(e.getAttendees(), request.getAttendees()))
            || e.getWhen().duration() <= 0));
            Collections.sort(events, new Comparator<Event>() {
                public int compare (Event e1, Event e2) {
                    return TimeRange.ORDER_BY_START.compare(e1.getWhen(), e2.getWhen());
                }
            });
        }
        return events;
    }

    /**
    * Finds list of available meeting times based on already planned events and the
    * requested meeting.
    * @param eventList The list of already scheduled events
    * @param request The MeetingRequest that is attempting to be scheduled
    * @return The list of all meeting times that will fit the request
    */
    private List<TimeRange> findMeetingTime(List<Event> eventList, MeetingRequest request) {
        if (eventList.isEmpty()) {
            return Arrays.asList(TimeRange.WHOLE_DAY);
        }

        List<TimeRange> availableTimes = new ArrayList();
        if (eventList.get(0).getWhen().start() - TimeRange.START_OF_DAY > request.getDuration()) {
            availableTimes.add(TimeRange.fromStartEnd(TimeRange.START_OF_DAY,eventList.get(0).getWhen().start(),false));
        }
        for (int i = 0; i < eventList.size(); i++) {
            if (i != eventList.size() - 1) {
                TimeRange first = eventList.get(i).getWhen();
                TimeRange second = eventList.get(i + 1).getWhen();
                if (first.overlaps(second)) {
                    if (first.contains(second)) {
                        eventList.remove(i + 1);
                        i--;
                    }
                } else {
                    if (second.start() - first.end() >= request.getDuration()) {
                        availableTimes.add(TimeRange.fromStartEnd(first.end(),second.start(),false));
                    }
                }
            } else {
                if (TimeRange.END_OF_DAY - eventList.get(i).getWhen().end() > request.getDuration()) {
                    availableTimes.add(TimeRange.fromStartEnd(eventList.get(i).getWhen().end(),TimeRange.END_OF_DAY,true));
                }
            }
        }
        return availableTimes;
    }
}

package com.github.laxika.magicalvibes.model.rules;

import com.github.laxika.magicalvibes.model.Permanent;

import java.util.Objects;
import java.util.UUID;

/**
 * A Room's door became unlocked, as it entered or later (CR 709.5h), or a Room became fully unlocked (CR 709.5i): the
 * events "when you unlock this door" and "whenever you fully unlock a Room" abilities trigger on (CR 603.2). Each is
 * an occurrence of its own. The Room an event names is the live object, which the abilities read as they trigger; a
 * simulation copy of the event copies it.
 */
public sealed interface RoomEvent extends RulesEvent {

    /** The Room. */
    Permanent room();

    /** The player who controls the Room. */
    UUID controllerId();

    /** The door {@code doorIndex} (0 the left one, 1 the right one) of {@code room} became unlocked. */
    record DoorUnlocked(Permanent room, UUID controllerId, int doorIndex) implements RoomEvent {

        public DoorUnlocked {
            Objects.requireNonNull(room, "room");
            Objects.requireNonNull(controllerId, "controllerId");
        }

        @Override
        public DoorUnlocked deepCopy() {
            return new DoorUnlocked(new Permanent(room), controllerId, doorIndex);
        }
    }

    /** {@code controllerId} fully unlocked {@code room}. */
    record FullyUnlocked(Permanent room, UUID controllerId) implements RoomEvent {

        public FullyUnlocked {
            Objects.requireNonNull(room, "room");
            Objects.requireNonNull(controllerId, "controllerId");
        }

        @Override
        public FullyUnlocked deepCopy() {
            return new FullyUnlocked(new Permanent(room), controllerId);
        }
    }
}

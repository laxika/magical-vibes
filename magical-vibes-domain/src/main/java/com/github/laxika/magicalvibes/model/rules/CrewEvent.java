package com.github.laxika.magicalvibes.model.rules;

import com.github.laxika.magicalvibes.model.Permanent;

import java.util.Objects;
import java.util.UUID;

/**
 * A creature was tapped to pay a crew, saddle or station cost (CR 702.122a, 702.171a, 702.184a), or a Vehicle became
 * crewed or a Mount saddled: the events "whenever this creature crews a Vehicle", "whenever this creature saddles a
 * Mount or crews a Vehicle during your main phase", "whenever this Spacecraft becomes stationed" and "whenever this
 * Vehicle becomes crewed" abilities trigger on (CR 603.2). Each is an occurrence of its own. The permanents an event
 * names are the live objects, which the abilities read as they trigger; a simulation copy of the event copies them.
 */
public sealed interface CrewEvent extends RulesEvent {

    /**
     * {@code creature} was tapped by {@code activatingPlayerId} to pay a crew or saddle cost of {@code permanent} (the
     * Vehicle or Mount, or {@code null} when the ability has no source permanent).
     */
    record CreatureTappedToCrewOrSaddle(Permanent creature, Permanent permanent, UUID activatingPlayerId)
            implements CrewEvent {

        public CreatureTappedToCrewOrSaddle {
            Objects.requireNonNull(creature, "creature");
            Objects.requireNonNull(activatingPlayerId, "activatingPlayerId");
        }

        @Override
        public CreatureTappedToCrewOrSaddle deepCopy() {
            return new CreatureTappedToCrewOrSaddle(new Permanent(creature),
                    permanent == null ? null : new Permanent(permanent), activatingPlayerId);
        }
    }

    /** {@code creature} was tapped by {@code controllerId} to pay {@code spacecraft}'s station cost. */
    record CreatureStations(Permanent spacecraft, Permanent creature, UUID controllerId) implements CrewEvent {

        public CreatureStations {
            Objects.requireNonNull(spacecraft, "spacecraft");
            Objects.requireNonNull(creature, "creature");
            Objects.requireNonNull(controllerId, "controllerId");
        }

        @Override
        public CreatureStations deepCopy() {
            return new CreatureStations(new Permanent(spacecraft), new Permanent(creature), controllerId);
        }
    }

    /** A crew ability of {@code vehicle}, which {@code controllerId} controls, resolved: it became crewed. */
    record BecameCrewed(Permanent vehicle, UUID controllerId) implements CrewEvent {

        public BecameCrewed {
            Objects.requireNonNull(vehicle, "vehicle");
            Objects.requireNonNull(controllerId, "controllerId");
        }

        @Override
        public BecameCrewed deepCopy() {
            return new BecameCrewed(new Permanent(vehicle), controllerId);
        }
    }

    /** {@code mount}, which {@code controllerId} controls, became saddled. */
    record BecameSaddled(Permanent mount, UUID controllerId) implements CrewEvent {

        public BecameSaddled {
            Objects.requireNonNull(mount, "mount");
            Objects.requireNonNull(controllerId, "controllerId");
        }

        @Override
        public BecameSaddled deepCopy() {
            return new BecameSaddled(new Permanent(mount), controllerId);
        }
    }
}

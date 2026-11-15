package com.github.laxika.magicalvibes.model.rules;

import com.github.laxika.magicalvibes.model.Permanent;

import java.util.Objects;
import java.util.UUID;

/**
 * A permanent changed controllers: the events "when you gain control of this from another player", "when you lose
 * control of this" and "whenever an opponent gains control of a permanent you control" abilities trigger on (CR
 * 603.2). Each is an occurrence of its own, emitted as control changes. The permanent an event names is the live
 * object, which the abilities read as they trigger; a simulation copy of the event copies it.
 */
public sealed interface ControlChangeEvent extends RulesEvent {

    /** The permanent that changed controllers. */
    Permanent permanent();

    /**
     * {@code newControllerId} is about to gain control of {@code permanent} from {@code previousControllerId}, an
     * opponent of theirs; emitted while the permanent, and every other permanent changing control with it, is still
     * on its previous controller's battlefield.
     */
    record OpponentGains(Permanent permanent, UUID previousControllerId, UUID newControllerId)
            implements ControlChangeEvent {

        public OpponentGains {
            Objects.requireNonNull(permanent, "permanent");
            Objects.requireNonNull(previousControllerId, "previousControllerId");
            Objects.requireNonNull(newControllerId, "newControllerId");
        }

        @Override
        public OpponentGains deepCopy() {
            return new OpponentGains(new Permanent(permanent), previousControllerId, newControllerId);
        }
    }

    /** {@code controllerId} lost control of {@code permanent}, still on their battlefield as it is emitted. */
    record Lost(Permanent permanent, UUID controllerId) implements ControlChangeEvent {

        public Lost {
            Objects.requireNonNull(permanent, "permanent");
            Objects.requireNonNull(controllerId, "controllerId");
        }

        @Override
        public Lost deepCopy() {
            return new Lost(new Permanent(permanent), controllerId);
        }
    }

    /** {@code controllerId} gained control of {@code permanent} from another player. */
    record Gained(Permanent permanent, UUID controllerId) implements ControlChangeEvent {

        public Gained {
            Objects.requireNonNull(permanent, "permanent");
            Objects.requireNonNull(controllerId, "controllerId");
        }

        @Override
        public Gained deepCopy() {
            return new Gained(new Permanent(permanent), controllerId);
        }
    }
}

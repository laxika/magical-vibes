package com.github.laxika.magicalvibes.model.rules;

import com.github.laxika.magicalvibes.model.Permanent;

import java.util.Objects;
import java.util.UUID;

/**
 * An Aura or Equipment became attached to an object or player, or an Equipment became unattached from one: the events
 * "whenever an Aura becomes attached to this creature", "whenever this Equipment becomes attached to a creature" and
 * "whenever this Equipment becomes unattached from a permanent" abilities trigger on (CR 603.2). Each is an occurrence
 * of its own, emitted as the attachment changes. The permanents an event names are the live objects, which the
 * abilities read as they trigger; a simulation copy of the event copies them.
 */
public sealed interface AttachmentEvent extends RulesEvent {

    /** {@code aura} became attached to the object or player {@code attachedToId}. */
    record AuraAttached(Permanent aura, UUID attachedToId) implements AttachmentEvent {

        public AuraAttached {
            Objects.requireNonNull(aura, "aura");
        }

        @Override
        public AuraAttached deepCopy() {
            return new AuraAttached(new Permanent(aura), attachedToId);
        }
    }

    /**
     * {@code equipment}, which {@code controllerId} controls, became attached to the creature {@code equippedId}, after
     * its "as this Equipment becomes attached" abilities applied.
     */
    record EquipmentAttached(Permanent equipment, UUID equippedId, UUID controllerId) implements AttachmentEvent {

        public EquipmentAttached {
            Objects.requireNonNull(equipment, "equipment");
            Objects.requireNonNull(equippedId, "equippedId");
            Objects.requireNonNull(controllerId, "controllerId");
        }

        @Override
        public EquipmentAttached deepCopy() {
            return new EquipmentAttached(new Permanent(equipment), equippedId, controllerId);
        }
    }

    /**
     * {@code equipment}, which {@code controllerId} controls (or controlled, as it changes controllers), became
     * unattached from the permanent {@code unattachedFromId} (CR 701.3d).
     */
    record EquipmentUnattached(Permanent equipment, UUID unattachedFromId, UUID controllerId)
            implements AttachmentEvent {

        public EquipmentUnattached {
            Objects.requireNonNull(equipment, "equipment");
            Objects.requireNonNull(unattachedFromId, "unattachedFromId");
            Objects.requireNonNull(controllerId, "controllerId");
        }

        @Override
        public EquipmentUnattached deepCopy() {
            return new EquipmentUnattached(new Permanent(equipment), unattachedFromId, controllerId);
        }
    }
}

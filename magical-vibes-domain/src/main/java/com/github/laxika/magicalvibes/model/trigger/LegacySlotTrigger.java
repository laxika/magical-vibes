package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.EffectSlot;

import java.util.Objects;

/**
 * A trigger that is still an {@link EffectSlot}. The slot's collectors in the engine decide when
 * it triggers, how its registrations group into stack objects, and which wrapper effects filter or
 * limit it. Any slot is accepted, including the slots that still hold mana abilities, replacement
 * effects and special actions.
 *
 * @param slot the slot whose collectors decide when this trigger is met
 */
public record LegacySlotTrigger(EffectSlot slot) implements Trigger {

    public LegacySlotTrigger {
        Objects.requireNonNull(slot, "slot");
    }

    /**
     * The graveyard for a slot whose name starts with {@code GRAVEYARD_}, and the battlefield for any other.
     * The collectors of some unprefixed slots look in other zones as well; they decide where a
     * legacy slot works, not this value.
     */
    @Override
    public AbilityZones defaultZones() {
        if (slot.name().startsWith("GRAVEYARD_")) {
            return AbilityZones.GRAVEYARD;
        }
        return AbilityZones.BATTLEFIELD;
    }
}

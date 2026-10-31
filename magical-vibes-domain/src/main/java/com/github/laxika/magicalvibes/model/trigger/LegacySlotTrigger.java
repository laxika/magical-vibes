package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.EffectSlot;

import java.util.Objects;

/**
 * A trigger that is still an {@link EffectSlot}. The slot's collectors in the engine decide when
 * it triggers, how its registrations group into stack objects, and which wrapper effects filter or
 * limit it. Any slot is accepted except {@code STATIC} and {@code COMMAND_ZONE_STATIC}, which hold
 * static abilities; that includes the slots that still hold mana abilities, replacement effects and
 * special actions.
 *
 * @param slot the slot whose collectors decide when this trigger is met
 */
public record LegacySlotTrigger(EffectSlot slot) implements Trigger {

    public LegacySlotTrigger {
        Objects.requireNonNull(slot, "slot");
        switch (slot) {
            case STATIC, COMMAND_ZONE_STATIC ->
                    throw new IllegalArgumentException(slot + " holds static abilities, not a trigger");
            default -> {
            }
        }
    }

    /**
     * The zone that the slot's name starts with ({@code GRAVEYARD_}, {@code COMMAND_ZONE_},
     * {@code EXILE_}, {@code EXILED_} or {@code HAND_}), and the battlefield for a slot without one.
     * The collectors of some unprefixed slots look in other zones as well; they decide where a
     * legacy slot works, not this value.
     */
    @Override
    public AbilityZones defaultZones() {
        String name = slot.name();
        if (name.startsWith("GRAVEYARD_")) {
            return AbilityZones.GRAVEYARD;
        }
        if (name.startsWith("COMMAND_ZONE_")) {
            return AbilityZones.COMMAND;
        }
        if (name.startsWith("EXILE_") || name.startsWith("EXILED_")) {
            return AbilityZones.EXILE;
        }
        if (name.startsWith("HAND_")) {
            return AbilityZones.HAND;
        }
        return AbilityZones.BATTLEFIELD;
    }
}

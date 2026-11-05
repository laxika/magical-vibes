package com.github.laxika.magicalvibes.model.trigger;

import java.util.Objects;

/**
 * An Aura or Equipment becomes attached to a permanent: "whenever an Aura becomes attached to this creature",
 * "whenever this Equipment becomes attached to a creature". Build it through {@link Triggers}.
 *
 * @param event which attachment becomes attached to what, relative to the ability's source and controller
 */
public record AttachTrigger(AttachEvent event) implements Trigger {

    public AttachTrigger {
        Objects.requireNonNull(event, "event");
    }

    /**
     * The attach collectors ({@code MiscWatch}) read this trigger's abilities themselves
     * ({@code MiscAbilityReader}), so it has no legacy view.
     */
    @Override
    public boolean collectedAsAbility() {
        return true;
    }

    /** Which attachment becomes attached to what. */
    public enum AttachEvent {
        /** Any Aura becomes attached to the source, whoever controls the Aura. */
        AURA_TO_THIS,
        /** An Aura its controller controls becomes attached to a creature they control. */
        AURA_YOU_CONTROL_TO_CREATURE_YOU_CONTROL,
        /**
         * An Aura its controller controls becomes attached to a nonland permanent an opponent controls whose mana value
         * is at most the Aura's.
         */
        AURA_YOU_CONTROL_TO_OPPONENT_NONLAND_PERMANENT,
        /** The source, an Equipment, becomes attached to a creature. */
        THIS_EQUIPMENT_TO_CREATURE
    }
}

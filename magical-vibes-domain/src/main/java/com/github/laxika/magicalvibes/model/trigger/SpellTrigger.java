package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;

import java.util.Objects;

/**
 * A spell is cast (CR 601.2i), copied (CR 707.10) or countered (CR 701.6a). Build it through {@link Triggers}.
 *
 * @param event  what happens to the spell
 * @param player the player who casts or copies the spell, counters it, or controls the countered spell
 */
public record SpellTrigger(SpellEvent event, PlayerRelation player) implements Trigger {

    public SpellTrigger {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(player, "player");
    }

    /** What happens to a spell that a {@link SpellTrigger} triggers on. */
    public enum SpellEvent {
        /** The player casts a spell: "whenever you cast a spell". */
        CAST,
        /** The player casts this spell: "when you cast this spell". */
        CAST_THIS,
        /** The player copies a spell: "whenever you copy a spell". */
        COPIED,
        /** The player counters a spell: "whenever you counter a spell". */
        COUNTERS,
        /** A spell the player controls is countered: "whenever a spell you control is countered". */
        COUNTERED,
        /** This spell is countered, or doesn't resolve because its targets are illegal. */
        THIS_COUNTERED_OR_FIZZLES
    }

    /** Whether the trigger is about this spell itself. */
    public boolean thisSpell() {
        return event == SpellEvent.CAST_THIS || event == SpellEvent.THIS_COUNTERED_OR_FIZZLES;
    }

    /**
     * A trigger about this spell functions from the stack, where the spell is as it becomes cast (CR 601.2i); any
     * other from the battlefield.
     */
    @Override
    public AbilityZones defaultZones() {
        return thisSpell() ? AbilityZones.STACK : AbilityZones.BATTLEFIELD;
    }
}

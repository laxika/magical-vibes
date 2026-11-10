package com.github.laxika.magicalvibes.model.effect;

/**
 * Puts "the same number and kind of counters" as the counter event the ability triggered on, on this effect's source.
 * It reads the event from the stack entry as it resolves: the kind is the entry's event counter type and the number
 * its event value.
 *
 * @param triggeringPlayerPuts {@code true} when the player who put the counters puts these too ("they put the same
 *                             number and kind of counters on this creature", the entry's triggering player);
 *                             {@code false} when the ability's controller does
 */
public record PutSameCountersOnSourceEffect(boolean triggeringPlayerPuts) implements CardEffect {

    /** "Put the same number and kind of counters on this": the ability's controller puts them. */
    public PutSameCountersOnSourceEffect() {
        this(false);
    }

    /** "They put the same number and kind of counters on this": the player who put the counters puts these. */
    public static PutSameCountersOnSourceEffect putByTriggeringPlayer() {
        return new PutSameCountersOnSourceEffect(true);
    }
}

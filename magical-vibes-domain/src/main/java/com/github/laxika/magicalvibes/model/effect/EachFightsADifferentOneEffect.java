package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Each of those tokens fights a different one of those creatures" (Ezuri's Predation): every creature {@code fighters}
 * names fights a different creature {@code opponents} names, none of them targeted. The controller chooses the pairs as
 * the effect resolves (CR 608.2d, the Ezuri's Predation ruling "you choose which Beast is fighting which creature"):
 * for each creature of the smaller side, which one of the other side's it fights, a choice that is asked only when it
 * changes something. The creatures left over on the larger side fight nothing (the ruling on extra tokens from Doubling
 * Season), and all the fights happen at the same time (CR 701.14a; "each of the fights happens at the same time"): every
 * creature's power is read before any damage is dealt, and the damage is one event.
 *
 * @param fighters  the creatures that each fight one ("those tokens": {@code Subjects.createdThisResolution()})
 * @param opponents the creatures they fight, a different one each ("those creatures")
 */
public record EachFightsADifferentOneEffect(PermanentSubject fighters, PermanentSubject opponents)
        implements CardEffect {

    public EachFightsADifferentOneEffect {
        Objects.requireNonNull(fighters, "fighters");
        Objects.requireNonNull(opponents, "opponents");
    }
}

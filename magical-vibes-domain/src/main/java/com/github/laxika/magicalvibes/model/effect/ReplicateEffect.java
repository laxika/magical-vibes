package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Trigger marker for a spell with replicate. The cast-time trigger collector counts the matching
 * repeatable additional-cost payments and creates one copy of the spell for each payment.
 *
 * @param manaCost the replicate payment that identifies the spell's repeatable additional cost,
 *                 or {@code null} for a non-mana tap replicate cost
 * @param tapFilter the permanents tapped for a non-mana replicate cost, or {@code null} for a
 *                  mana replicate cost
 * @param tokenCopy whether copies of a permanent spell enter the battlefield as tokens
 */
public record ReplicateEffect(String manaCost, PermanentPredicate tapFilter, boolean tokenCopy) implements CardEffect {

    public ReplicateEffect(String manaCost) {
        this(manaCost, null, false);
    }

    public ReplicateEffect(String manaCost, PermanentPredicate tapFilter) {
        this(manaCost, tapFilter, false);
    }

    public ReplicateEffect(String manaCost, boolean tokenCopy) {
        this(manaCost, null, tokenCopy);
    }

    public ReplicateEffect {
        if ((manaCost == null || manaCost.isBlank()) == (tapFilter == null)) {
            throw new IllegalArgumentException("ReplicateEffect must have exactly one cost mode");
        }
    }

    public static ReplicateEffect forTapCost(PermanentPredicate tapFilter) {
        return new ReplicateEffect(null, tapFilter, false);
    }
}

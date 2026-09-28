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
 */
public record ReplicateEffect(String manaCost, PermanentPredicate tapFilter) implements CardEffect {

    public ReplicateEffect(String manaCost) {
        this(manaCost, null);
    }

    public ReplicateEffect {
        if ((manaCost == null || manaCost.isBlank()) == (tapFilter == null)) {
            throw new IllegalArgumentException("ReplicateEffect must have exactly one cost mode");
        }
    }

    public static ReplicateEffect forTapCost(PermanentPredicate tapFilter) {
        return new ReplicateEffect(null, tapFilter);
    }
}

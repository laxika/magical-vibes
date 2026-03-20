package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect declaring that a spell has kicker — an optional additional cost
 * that can be paid when casting. (MTG Rule 702.32)
 *
 * <p>The kicker cost can be a mana cost, a sacrifice cost, or both.
 *
 * @param cost the mana cost string for the kicker (e.g. "{4}", "{1}{G}"), or null if kicker has no mana cost
 * @param sacrificePredicate if non-null, the kicker requires sacrificing a permanent matching this predicate
 * @param sacrificeDescription human-readable description of the sacrifice cost (e.g. "an artifact or Goblin")
 */
public record KickerEffect(String cost, PermanentPredicate sacrificePredicate, String sacrificeDescription) implements CardEffect {

    /** Convenience constructor for mana-only kicker costs. */
    public KickerEffect(String cost) {
        this(cost, null, null);
    }

    /** Convenience constructor for sacrifice-only kicker costs (no mana). */
    public KickerEffect(PermanentPredicate sacrificePredicate, String sacrificeDescription) {
        this(null, sacrificePredicate, sacrificeDescription);
    }

    public boolean hasSacrificeCost() {
        return sacrificePredicate != null;
    }

    public boolean hasManaCost() {
        return cost != null && !cost.isEmpty();
    }
}

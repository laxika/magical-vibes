package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect declaring that a spell has kicker — an optional additional cost
 * that can be paid when casting.
 *
 * <p>The kicker cost can be a mana cost, a sacrifice cost, or a tap cost.
 *
 * @param cost the mana cost string for the kicker (e.g. "{4}", "{1}{G}"), or null if kicker has no mana cost
 * @param sacrificePredicate if non-null, the kicker requires sacrificing a permanent matching this predicate
 * @param sacrificeDescription human-readable description of the sacrifice cost (e.g. "an artifact or Goblin")
 * @param tapPredicate if non-null, the kicker requires tapping an untapped permanent matching this predicate
 * @param tapDescription human-readable description of the tap cost (e.g. "an untapped Vampire")
 */
public record KickerEffect(String cost, PermanentPredicate sacrificePredicate, String sacrificeDescription,
                           PermanentPredicate tapPredicate, String tapDescription) implements CardEffect {

    /** Convenience constructor for mana-only kicker costs. */
    public KickerEffect(String cost) {
        this(cost, null, null, null, null);
    }

    /** Convenience constructor for sacrifice-only kicker costs (no mana). */
    public KickerEffect(PermanentPredicate sacrificePredicate, String sacrificeDescription) {
        this(null, sacrificePredicate, sacrificeDescription, null, null);
    }

    /** Convenience constructor for mana and sacrifice kicker costs. */
    public KickerEffect(String cost, PermanentPredicate sacrificePredicate, String sacrificeDescription) {
        this(cost, sacrificePredicate, sacrificeDescription, null, null);
    }

    /** Convenience constructor for a tap-only kicker cost (no mana). */
    public static KickerEffect tap(PermanentPredicate tapPredicate, String tapDescription) {
        return new KickerEffect(null, null, null, tapPredicate, tapDescription);
    }

    public boolean hasSacrificeCost() {
        return sacrificePredicate != null;
    }

    public boolean hasManaCost() {
        return cost != null && !cost.isEmpty();
    }

    public boolean hasTapCost() {
        return tapPredicate != null;
    }
}

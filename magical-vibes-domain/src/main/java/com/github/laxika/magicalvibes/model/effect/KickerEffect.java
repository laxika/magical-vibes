package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static effect declaring that a spell has kicker — an optional additional cost
 * that can be paid when casting.
 *
 * <p>The kicker cost can be a mana cost, a life payment, a sacrifice cost, a tap cost, or a
 * return-to-hand cost.
 *
 * @param cost the mana cost string for the kicker (e.g. "{4}", "{1}{G}"), or null if kicker has no mana cost
 * @param sacrificePredicate if non-null, the kicker requires sacrificing a permanent matching this predicate
 * @param sacrificeDescription human-readable description of the sacrifice cost (e.g. "an artifact or Goblin")
 * @param tapPredicate if non-null, the kicker requires tapping an untapped permanent matching this predicate
 * @param tapDescription human-readable description of the tap cost (e.g. "an untapped Vampire")
 * @param returnPredicate if non-null, the kicker requires returning a permanent matching this predicate
 * @param returnDescription human-readable description of the return cost (e.g. "a creature")
 * @param discardPredicate if non-null, the kicker requires discarding a card matching this predicate
 * @param discardDescription human-readable description of the discard cost (e.g. "a creature")
 * @param lifeCost the life payment for a life-only kicker, or null when the kicker does not pay life
 */
public record KickerEffect(String cost, PermanentPredicate sacrificePredicate, String sacrificeDescription,
                           PermanentPredicate tapPredicate, String tapDescription,
                           PermanentPredicate returnPredicate, String returnDescription,
                           CardPredicate discardPredicate, String discardDescription,
                           int sacrificeCount, PayLifeCost lifeCost) implements CardEffect {

    /** Compatibility constructor for kicker costs without a return-to-hand component. */
    public KickerEffect(String cost, PermanentPredicate sacrificePredicate, String sacrificeDescription,
                        PermanentPredicate tapPredicate, String tapDescription) {
        this(cost, sacrificePredicate, sacrificeDescription, tapPredicate, tapDescription, null, null, null, null,
                sacrificePredicate == null ? 0 : 1, null);
    }

    /** Convenience constructor for mana-only kicker costs. */
    public KickerEffect(String cost) {
        this(cost, null, null, null, null, null, null, null, null, 0, null);
    }

    /** Convenience constructor for sacrifice-only kicker costs (no mana). */
    public KickerEffect(PermanentPredicate sacrificePredicate, String sacrificeDescription) {
        this(null, sacrificePredicate, sacrificeDescription, null, null, null, null, null, null,
                sacrificePredicate == null ? 0 : 1, null);
    }

    /** Convenience constructor for mana and sacrifice kicker costs. */
    public KickerEffect(String cost, PermanentPredicate sacrificePredicate, String sacrificeDescription) {
        this(cost, sacrificePredicate, sacrificeDescription, null, null, null, null, null, null,
                sacrificePredicate == null ? 0 : 1, null);
    }

    /** Convenience constructor for a fixed-count sacrifice kicker. */
    public KickerEffect(int sacrificeCount, PermanentPredicate sacrificePredicate, String sacrificeDescription) {
        this(null, sacrificePredicate, sacrificeDescription, null, null, null, null, null, null, sacrificeCount, null);
    }

    /** Convenience constructor for a mana and fixed-count sacrifice kicker. */
    public KickerEffect(String cost, int sacrificeCount, PermanentPredicate sacrificePredicate,
                        String sacrificeDescription) {
        this(cost, sacrificePredicate, sacrificeDescription, null, null, null, null, null, null, sacrificeCount, null);
    }

    /** Convenience constructor for a life-only kicker cost. */
    public KickerEffect(PayLifeCost lifeCost) {
        this(null, null, null, null, null, null, null, null, null, 0, lifeCost);
    }

    /** Convenience constructor for a tap-only kicker cost (no mana). */
    public static KickerEffect tap(PermanentPredicate tapPredicate, String tapDescription) {
        return new KickerEffect(null, null, null, tapPredicate, tapDescription, null, null, null, null, 0, null);
    }

    /** Convenience factory for a kicker that returns a matching permanent to its owner's hand. */
    public static KickerEffect returning(PermanentPredicate returnPredicate, String returnDescription) {
        return new KickerEffect(null, null, null, null, null, returnPredicate, returnDescription, null, null, 0, null);
    }

    /** Convenience factory for a kicker that discards a matching card. */
    public static KickerEffect discarding(String cost, CardPredicate discardPredicate, String discardDescription) {
        return new KickerEffect(cost, null, null, null, null, null, null, discardPredicate, discardDescription, 0, null);
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

    public boolean hasReturnCost() {
        return returnPredicate != null;
    }

    public boolean hasDiscardCost() {
        return discardPredicate != null;
    }

    public boolean hasLifeCost() {
        return lifeCost != null;
    }
}

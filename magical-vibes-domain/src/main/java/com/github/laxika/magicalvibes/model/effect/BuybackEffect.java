package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect declaring a spell's optional buyback cost.
 *
 * <p>If the buyback cost is paid, the spell is put into its owner's hand as it resolves instead
 * of into its owner's graveyard. The card pairs this declaration with a
 * {@code ConditionalEffect(new BuybackPaid(), ReturnToHandEffect.selfSpell())} in the SPELL
 * slot. The optional cost may contain mana, one or more permanent sacrifices, discarding cards, or
 * paying life.
 */
public record BuybackEffect(String cost, PermanentPredicate sacrificePredicate, int sacrificeCount,
                            String sacrificeDescription,
                            int discardCount, boolean randomDiscard, PayLifeCost lifeCost)
        implements CardEffect {

    public BuybackEffect(String cost) {
        this(cost, null, 0, null, 0, false, null);
    }

    public BuybackEffect(PermanentPredicate sacrificePredicate, String sacrificeDescription) {
        this(null, sacrificePredicate, 1, sacrificeDescription, 0, false, null);
    }

    public BuybackEffect(int sacrificeCount, PermanentPredicate sacrificePredicate,
                         String sacrificeDescription) {
        this(null, sacrificePredicate, sacrificeCount, sacrificeDescription, 0, false, null);
    }

    public BuybackEffect(int discardCount) {
        this(null, null, 0, null, discardCount, false, null);
    }

    public BuybackEffect(int discardCount, boolean randomDiscard) {
        this(null, null, 0, null, discardCount, randomDiscard, null);
    }

    public BuybackEffect(PayLifeCost lifeCost) {
        this(null, null, 0, null, 0, false, lifeCost);
    }

    public BuybackEffect(PayLifeCost lifeCost, int discardCount, boolean randomDiscard) {
        this(null, null, 0, null, discardCount, randomDiscard, lifeCost);
    }

    public BuybackEffect(String cost, PermanentPredicate sacrificePredicate, String sacrificeDescription) {
        this(cost, sacrificePredicate, sacrificePredicate == null ? 0 : 1, sacrificeDescription,
                0, false, null);
    }

    public BuybackEffect {
        if (sacrificeCount < 0) {
            throw new IllegalArgumentException("sacrificeCount must be non-negative");
        }
        if (sacrificePredicate == null && sacrificeCount != 0) {
            throw new IllegalArgumentException("sacrificeCount requires a sacrifice predicate");
        }
        if (sacrificePredicate != null && sacrificeCount == 0) {
            throw new IllegalArgumentException("A sacrifice predicate requires a positive sacrificeCount");
        }
        if (discardCount < 0) {
            throw new IllegalArgumentException("discardCount must be non-negative");
        }
        if (randomDiscard && discardCount == 0) {
            throw new IllegalArgumentException("randomDiscard requires a discard cost");
        }
    }

    public boolean hasManaCost() {
        return cost != null && !cost.isEmpty();
    }

    public boolean hasSacrificeCost() {
        return sacrificePredicate != null;
    }

    public boolean hasDiscardCost() {
        return discardCount > 0;
    }

    public boolean hasRandomDiscardCost() {
        return randomDiscard && hasDiscardCost();
    }

    public boolean hasLifeCost() {
        return lifeCost != null;
    }
}

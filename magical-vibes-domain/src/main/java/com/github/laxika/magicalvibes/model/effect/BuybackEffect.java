package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect declaring a spell's optional buyback cost.
 *
 * <p>If the buyback cost is paid, the spell is put into its owner's hand as it resolves instead
 * of into its owner's graveyard. The card pairs this declaration with a
 * {@code ConditionalEffect(new BuybackPaid(), ReturnToHandEffect.selfSpell())} in the SPELL
 * slot. The optional cost may contain mana, a single permanent sacrifice, or both.
 */
public record BuybackEffect(String cost, PermanentPredicate sacrificePredicate, String sacrificeDescription)
        implements CardEffect {

    public BuybackEffect(String cost) {
        this(cost, null, null);
    }

    public BuybackEffect(PermanentPredicate sacrificePredicate, String sacrificeDescription) {
        this(null, sacrificePredicate, sacrificeDescription);
    }

    public boolean hasManaCost() {
        return cost != null && !cost.isEmpty();
    }

    public boolean hasSacrificeCost() {
        return sacrificePredicate != null;
    }
}

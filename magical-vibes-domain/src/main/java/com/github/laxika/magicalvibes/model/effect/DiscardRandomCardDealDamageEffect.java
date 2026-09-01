package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals one amount of damage to any target, or a larger amount when the card discarded as the
 * activation cost was multicolored.
 */
public record DiscardRandomCardDealDamageEffect(int damage, int multicoloredDamage) implements CardEffect {

    public DiscardRandomCardDealDamageEffect {
        if (damage < 0 || multicoloredDamage < 0) {
            throw new IllegalArgumentException("Damage amounts cannot be negative");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.anyTarget());
    }
}

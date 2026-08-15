package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the top card of the controller's library, then deals damage equal to that card's mana
 * value to the effect's any-target.
 */
public record RevealTopCardDealManaValueDamageToAnyTargetEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.anyTarget());
    }
}

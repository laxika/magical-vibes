package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles cards from the controller's library until a nonland card is exiled, then deals damage
 * equal to that card's mana value to the effect's any-target.
 */
public record ExileTopUntilNonlandDealManaValueDamageToAnyTargetEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.anyTarget());
    }
}

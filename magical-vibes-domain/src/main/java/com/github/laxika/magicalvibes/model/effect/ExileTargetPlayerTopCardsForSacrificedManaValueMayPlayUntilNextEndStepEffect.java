package com.github.laxika.magicalvibes.model.effect;

/**
 * Sacrifice trigger effect: exile as many cards from the top of the target player's library as the sacrificed
 * permanent's mana value and let the ability controller play them until their next end step, using mana of any type
 * for the exiled spells. The sacrificed permanent is the stack entry's triggering permanent, as it last existed on
 * the battlefield (CR 603.10a).
 */
public record ExileTargetPlayerTopCardsForSacrificedManaValueMayPlayUntilNextEndStepEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}

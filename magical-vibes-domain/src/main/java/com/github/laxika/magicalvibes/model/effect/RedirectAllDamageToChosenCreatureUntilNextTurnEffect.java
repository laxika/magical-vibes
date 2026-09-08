package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose a creature you control. Until your next turn, all damage that would be dealt to creatures
 * you control is dealt to that creature instead."
 */
public record RedirectAllDamageToChosenCreatureUntilNextTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}

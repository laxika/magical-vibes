package com.github.laxika.magicalvibes.model.effect;

/**
 * Schedule the targeted permanent to be returned to its owner's hand at end of combat.
 */
public record ReturnTargetPermanentToHandAtEndOfCombatEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}

package com.github.laxika.magicalvibes.model.effect;

/**
 * Any number of target creatures become a color chosen by the controller until end of turn.
 */
public record SetChosenColorForTargetCreaturesUntilEndOfTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}

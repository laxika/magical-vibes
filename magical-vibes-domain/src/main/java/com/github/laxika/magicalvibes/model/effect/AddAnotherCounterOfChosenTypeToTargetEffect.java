package com.github.laxika.magicalvibes.model.effect;

/** "Choose a counter on target permanent or player. Give that permanent or player another counter of that kind." */
public record AddAnotherCounterOfChosenTypeToTargetEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.playerOrPermanent());
    }
}

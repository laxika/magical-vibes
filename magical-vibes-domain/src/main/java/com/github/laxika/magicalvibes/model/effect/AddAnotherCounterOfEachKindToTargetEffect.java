package com.github.laxika.magicalvibes.model.effect;

/** "For each kind of counter on target permanent or player, give that permanent or player another counter of that kind." */
public record AddAnotherCounterOfEachKindToTargetEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.playerOrPermanent());
    }
}

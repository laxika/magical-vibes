package com.github.laxika.magicalvibes.model.effect;

/** Turns the target creature face up without paying a morph or disguise cost if it is face down. */
public record TurnTargetCreatureFaceUpIfFaceDownEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}

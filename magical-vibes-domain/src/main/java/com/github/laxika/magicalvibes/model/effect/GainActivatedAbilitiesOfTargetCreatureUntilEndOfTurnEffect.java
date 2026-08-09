package com.github.laxika.magicalvibes.model.effect;

/**
 * Gives the source permanent the target creature's current activated abilities until end of turn.
 */
public record GainActivatedAbilitiesOfTargetCreatureUntilEndOfTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}

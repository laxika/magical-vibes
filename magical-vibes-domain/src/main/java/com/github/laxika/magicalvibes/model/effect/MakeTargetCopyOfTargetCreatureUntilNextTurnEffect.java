package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes a target permanent (targetIds[0], typically a Shapeshifter) become a copy of a
 * target creature (targetIds[1]) until the controller's next turn. Used by Shapesharer.
 * The copy is a {@link ResolvedCopyEffect} floating effect on the permanent, {@code Duration.UNTIL_YOUR_NEXT_TURN}.
 */
public record MakeTargetCopyOfTargetCreatureUntilNextTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.playerOrPermanent());
    }
}

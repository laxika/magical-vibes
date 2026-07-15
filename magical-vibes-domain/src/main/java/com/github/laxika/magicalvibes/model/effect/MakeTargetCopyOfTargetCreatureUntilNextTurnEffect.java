package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes a target permanent (targetIds[0], typically a Shapeshifter) become a copy of a
 * target creature (targetIds[1]) until the controller's next turn.
 * At the beginning of the ability controller's next turn, the permanent reverts to its
 * pre-copy card. Used by Shapesharer.
 */
public record MakeTargetCopyOfTargetCreatureUntilNextTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER_OR_PERMANENT);
    }
}

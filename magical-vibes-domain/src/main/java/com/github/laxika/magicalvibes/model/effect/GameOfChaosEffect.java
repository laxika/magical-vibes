package com.github.laxika.magicalvibes.model.effect;

/**
 * Game of Chaos: "Flip a coin. If you win the flip, you gain 1 life and target opponent loses 1
 * life, and you decide whether to flip again. If you lose the flip, you lose 1 life and that
 * opponent gains 1 life, and that player decides whether to flip again. Double the life stakes with
 * each flip."
 *
 * <p>Resolves the first flip against the targeted opponent (stored on the stack entry's
 * {@code targetId}); the winner of each flip then chooses whether to continue via a repeating
 * "you may flip again" prompt carrying a {@link GameOfChaosFlipAgainEffect} with the doubled stake.
 */
public record GameOfChaosEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}

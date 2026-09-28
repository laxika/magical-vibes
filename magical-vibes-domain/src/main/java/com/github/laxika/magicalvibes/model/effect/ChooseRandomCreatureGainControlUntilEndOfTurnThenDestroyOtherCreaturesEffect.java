package com.github.laxika.magicalvibes.model.effect;

/**
 * Chooses a creature at random, gives the effect controller control of it until end of turn,
 * untaps it, gives it haste until end of turn, then destroys every other creature.
 */
public record ChooseRandomCreatureGainControlUntilEndOfTurnThenDestroyOtherCreaturesEffect()
        implements CardEffect, ControlStealingEffect, BoardWipeEffect {

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.END_OF_TURN;
    }

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}

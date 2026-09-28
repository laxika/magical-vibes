package com.github.laxika.magicalvibes.model.effect;

/** Returns the controller-owned cards exiled with the source and registers those exact cards for discard. */
public record PutAllCardsExiledWithSourceIntoOwnersHandsAndDiscardAtNextTurnEndStepEffect()
        implements CardEffect {
}

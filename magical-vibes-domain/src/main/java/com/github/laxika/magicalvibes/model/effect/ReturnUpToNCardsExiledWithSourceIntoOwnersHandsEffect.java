package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/** Returns up to the evaluated number of cards exiled with the source to their owners' hands. */
public record ReturnUpToNCardsExiledWithSourceIntoOwnersHandsEffect(DynamicAmount maxCount)
        implements CardEffect {
}

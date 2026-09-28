package com.github.laxika.magicalvibes.model.effect;

/** Removes one hit counter from a chosen eligible exiled card, then resolves the follow-up. */
public record RemoveHitCounterFromExiledCardEffect(CardEffect followUpEffect)
        implements CardEffect {
}

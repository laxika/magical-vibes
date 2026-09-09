package com.github.laxika.magicalvibes.model.effect;

/** Describes an as-enters choice that moves opponent-owned exiled cards to their owners' graveyards. */
public interface AsEntersOpponentExileToGraveyardEffect extends ReplacementEffect {

    int counterCount();
}

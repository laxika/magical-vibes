package com.github.laxika.magicalvibes.model.effect;

/**
 * Static self-effect: the source gains each watched keyword found on a creature card exiled with
 * it. Fixed protection abilities represented by {@link ProtectionGrantingEffect} are carried over
 * as granted effects as well.
 */
public record GainKeywordsOfCreatureCardsExiledWithSourceEffect() implements CardEffect {
}

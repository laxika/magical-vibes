package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns all cards exiled with the source permanent (tracked via
 * {@code GameData.exiledCards} by source permanent ID) to the battlefield under
 * their owners' control. Used as a death trigger by cards like Helvault whose
 * abilities accumulate exiled cards and release them when the source is put into
 * a graveyard from the battlefield.
 */
public record ReturnAllCardsExiledWithSourceEffect() implements CardEffect {
}

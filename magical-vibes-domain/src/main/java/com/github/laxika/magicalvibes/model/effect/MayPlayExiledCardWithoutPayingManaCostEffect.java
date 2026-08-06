package com.github.laxika.magicalvibes.model.effect;

/**
 * Internal marker effect used in a {@code PendingMayAbility} to route a "you may play the exiled
 * card without paying its mana cost" choice. Not placed on cards directly — queued by
 * {@code CounterSupport} for {@link ReplaceControlledCounterWithExileAndPlayEffect} (Guile) and by
 * {@link MayCastCardsExiledWithSourceEffect} (Spell Queller), then handled by the may-ability
 * dispatch. If declined, the card stays exiled.
 */
public record MayPlayExiledCardWithoutPayingManaCostEffect() implements CardEffect {
}

package com.github.laxika.magicalvibes.model.effect;

/**
 * Static layer-6 effect giving the source all activated and triggered abilities of cards exiled
 * with it. Tap abilities represented by {@code ON_TAP} effects are exposed as mana abilities.
 */
public record GainActivatedAndTriggeredAbilitiesOfExiledCardsEffect() implements CardEffect {
}

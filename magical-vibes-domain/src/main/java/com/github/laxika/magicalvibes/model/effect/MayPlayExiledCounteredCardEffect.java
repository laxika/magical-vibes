package com.github.laxika.magicalvibes.model.effect;

/**
 * Internal marker effect used in a {@code PendingMayAbility} to route the "you may play the exiled
 * spell without paying its mana cost" choice created by {@link ReplaceControlledCounterWithExileAndPlayEffect}
 * (Guile). Not placed on cards directly — queued by {@code CounterSupport} and handled by the may-ability
 * dispatch. If declined, the card stays exiled.
 */
public record MayPlayExiledCounteredCardEffect() implements CardEffect {
}

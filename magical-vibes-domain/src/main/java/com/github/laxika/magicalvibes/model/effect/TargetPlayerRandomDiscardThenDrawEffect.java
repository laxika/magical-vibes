package com.github.laxika.magicalvibes.model.effect;

/**
 * The target player discards a card at random, then draws a card if a card was discarded.
 *
 * <p>On a {@code Triggers.dealsCombatDamageToPlayer()} ability with no declared player target it is "that
 * player", the player dealt damage, which the collector records as the entry's non-targeting target.
 */
public record TargetPlayerRandomDiscardThenDrawEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}

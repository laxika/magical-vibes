package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for a static draw replacement that exiles the top library cards and grants their
 * controller permission to play them until end of turn.
 */
public interface ExileTopCardsMayPlayThisTurnDrawReplacementEffect extends CardEffect {

    com.github.laxika.magicalvibes.model.amount.DynamicAmount count();

    boolean withoutPayingManaCost();
}

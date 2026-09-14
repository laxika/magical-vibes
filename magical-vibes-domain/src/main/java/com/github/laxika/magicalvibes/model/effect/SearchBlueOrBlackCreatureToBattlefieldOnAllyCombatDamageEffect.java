package com.github.laxika.magicalvibes.model.effect;

/** Marker stored in an emblem for searching a blue or black creature onto the battlefield when
 * a creature its controller controls deals combat damage to a player. */
public interface SearchBlueOrBlackCreatureToBattlefieldOnAllyCombatDamageEffect extends CardEffect {

    record Marker() implements SearchBlueOrBlackCreatureToBattlefieldOnAllyCombatDamageEffect {
    }
}

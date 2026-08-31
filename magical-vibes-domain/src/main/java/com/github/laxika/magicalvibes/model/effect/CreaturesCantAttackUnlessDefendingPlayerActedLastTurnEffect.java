package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: creatures can't attack a player unless that player cast a spell or put a
 * nontoken permanent onto the battlefield during their own most recently completed turn.
 * This restriction does not apply to attacks against planeswalkers.
 */
public record CreaturesCantAttackUnlessDefendingPlayerActedLastTurnEffect() implements CardEffect {
}

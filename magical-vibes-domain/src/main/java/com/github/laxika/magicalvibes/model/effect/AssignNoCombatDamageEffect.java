package com.github.laxika.magicalvibes.model.effect;

/**
 * The stack entry's source permanent ({@code sourcePermanentId}) assigns no combat damage this
 * turn — added to {@code GameData.creaturesPreventedFromDealingCombatDamage}, cleared at turn
 * cleanup. Pair with damage (or similar) inside a {@link MayEffect}/{@link SequenceEffect} for
 * "you may [effect]. If you do, it assigns no combat damage this turn" (Gaze of Pain).
 */
public record AssignNoCombatDamageEffect() implements CardEffect {
}

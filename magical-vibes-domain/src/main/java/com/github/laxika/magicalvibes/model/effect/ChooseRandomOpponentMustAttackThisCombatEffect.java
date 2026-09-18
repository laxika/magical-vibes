package com.github.laxika.magicalvibes.model.effect;

/**
 * Chooses one opponent of the effect controller at random and makes the source creature attack
 * that player during this combat if able.
 */
public record ChooseRandomOpponentMustAttackThisCombatEffect() implements CardEffect {
}

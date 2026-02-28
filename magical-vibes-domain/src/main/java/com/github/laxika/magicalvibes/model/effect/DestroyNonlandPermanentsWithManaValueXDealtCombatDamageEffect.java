package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroy each nonland permanent with mana value equal to X whose controller
 * was dealt combat damage by the source permanent this turn.
 *
 * <p>The X value comes from the activated ability's X cost (stored in
 * {@code StackEntry.getXValue()}). The source permanent is identified via
 * {@code StackEntry.getSourcePermanentId()}, and the set of players it
 * dealt combat damage to this turn is looked up from
 * {@code GameData.combatDamageToPlayersThisTurn}.
 *
 * <p>Used by Steel Hellkite.
 */
public record DestroyNonlandPermanentsWithManaValueXDealtCombatDamageEffect() implements CardEffect {
}

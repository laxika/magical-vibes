package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: each opponent must attack this permanent's controller (or a planeswalker
 * the controller controls) with at least one creature each combat if able.
 * Per CR 508.1d, the player need not pay any optional attack costs to satisfy
 * the requirement. Unlike {@link MustAttackEffect}, this does NOT force every
 * creature to attack — only that at least one creature is declared as an attacker
 * directed at the controller.
 *
 * <p>Used by Trove of Temptation and similar enchantments.</p>
 */
public record OpponentsMustAttackControllerEffect() implements CardEffect {
}

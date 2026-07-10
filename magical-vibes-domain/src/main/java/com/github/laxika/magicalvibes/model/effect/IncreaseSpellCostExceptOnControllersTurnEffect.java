package com.github.laxika.magicalvibes.model.effect;

/**
 * Increases the casting cost of every spell by the given amount, except while it is the
 * spell's controller's turn (i.e. the spell is being cast during that player's own turn).
 * Symmetric — affects all players. E.g. Defense Grid ({3} more except on the caster's turn).
 */
public record IncreaseSpellCostExceptOnControllersTurnEffect(int amount) implements CardEffect {
}

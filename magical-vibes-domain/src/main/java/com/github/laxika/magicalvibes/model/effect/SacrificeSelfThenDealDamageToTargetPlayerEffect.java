package com.github.laxika.magicalvibes.model.effect;

/**
 * Sacrifices the source permanent; if it was successfully sacrificed ("if you do"), deals
 * {@code damage} to the player baked into the stack entry's {@code targetId}. Used by Booby Trap,
 * whose trigger deals 10 damage to the chosen player when they draw the named card.
 */
public record SacrificeSelfThenDealDamageToTargetPlayerEffect(int damage) implements CardEffect {
}

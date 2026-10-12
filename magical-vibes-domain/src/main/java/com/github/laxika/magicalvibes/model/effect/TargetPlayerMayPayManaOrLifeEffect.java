package com.github.laxika.magicalvibes.model.effect;

/**
 * "That player may pay {M} or N life. If the player does, [wrapped]."
 *
 * <p>The paying player is the one carried on the stack entry's {@code targetId} — for an
 * {@code EACH_UPKEEP_TRIGGERED} trigger that is the active player, so the prompt goes to the
 * player whose upkeep it is rather than to the source's controller (Emberwilde Djinn).
 *
 * <p>A player who can afford only one resource gets a yes/no choice and pays that resource. A
 * player who can afford both picks mana, life, or neither.
 */
public record TargetPlayerMayPayManaOrLifeEffect(String manaCost, int lifeCost, CardEffect wrapped,
                                                 String prompt) implements CardEffect {
}

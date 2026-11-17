package com.github.laxika.magicalvibes.model.effect;

/**
 * "You may have it assign no combat damage this turn. If you do, defending player discards a card
 * at random." (Cloak of Confusion.) Resolved as the wrapped effect of a {@link MayEffect} on an
 * unblocked-attack trigger: the attacking (enchanted) creature is the stack entry's
 * {@code sourcePermanentId} and the defending player is its {@code targetId}. When resolved, the
 * source creature assigns no combat damage this turn (a floating {@link AssignNoCombatDamageEffect} until end
 * of turn) and the defending player discards a card at random.
 */
public record AssignNoCombatDamageAndDefendingPlayerDiscardsEffect() implements CardEffect {
}

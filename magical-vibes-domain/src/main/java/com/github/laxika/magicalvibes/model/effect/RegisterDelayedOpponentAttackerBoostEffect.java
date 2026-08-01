package com.github.laxika.magicalvibes.model.effect;

/**
 * "Until your next turn, whenever a creature an opponent controls attacks, it gets
 * {@code power}/{@code toughness} until end of turn." (Jace, Architect of Thought's +1.)
 *
 * <p>Non-targeting. Resolution registers a {@code DelayedOpponentAttackerBoost} delayed action that
 * fires from the attack-declaration step for every attacker its registering player does not
 * control, and is dropped when that player's next turn begins.
 */
public record RegisterDelayedOpponentAttackerBoostEffect(int power, int toughness) implements CardEffect {
}

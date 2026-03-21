package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * "If a [color] source you control would deal damage to a permanent or player this turn,
 * it deals that much damage plus {@code bonus} to that permanent or player instead."
 *
 * <p>When resolved, sets a per-controller damage bonus on the game state that persists
 * until end of turn. All sources of the specified color controlled by the player receive
 * the additive bonus. Multiple instances stack additively.
 */
public record BoostColorSourceDamageThisTurnEffect(CardColor color, int bonus) implements CardEffect {
}

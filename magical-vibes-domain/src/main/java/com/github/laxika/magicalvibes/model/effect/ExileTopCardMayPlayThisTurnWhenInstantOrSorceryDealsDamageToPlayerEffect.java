package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger-only effect for exiling the top card and allowing it to be played this turn when a
 * controlled instant or sorcery deals damage to a player. The trigger collector creates one
 * trigger for each player damaged by the spell.
 */
public record ExileTopCardMayPlayThisTurnWhenInstantOrSorceryDealsDamageToPlayerEffect()
        implements CardEffect {
}

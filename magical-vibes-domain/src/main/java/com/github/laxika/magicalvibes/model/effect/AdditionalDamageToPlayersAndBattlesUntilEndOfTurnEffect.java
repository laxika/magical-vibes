package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a global damage bonus for damage dealt to players and battles until end of turn.
 */
public record AdditionalDamageToPlayersAndBattlesUntilEndOfTurnEffect(int amount)
        implements CardEffect {
}

package com.github.laxika.magicalvibes.model.effect;

/**
 * Council's dilemma: each player votes for past or present. Past votes mill cards and deal damage
 * based on their total mana value; present votes exile cards that may be played until the end of
 * the controller's next turn.
 */
public record FatefulTempestEffect() implements CardEffect {
}

package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: the resolving controller may cast spells this turn as though they had flash
 * (Alchemist's Refuge). Tracked in {@code GameData.playersWithFlashUntilEndOfTurn} and cleared at
 * end of turn. The static, permanent-sourced counterpart is {@link GrantFlashToCardTypeEffect}.
 */
public record GrantFlashToSpellsThisTurnEffect() implements CardEffect {
}

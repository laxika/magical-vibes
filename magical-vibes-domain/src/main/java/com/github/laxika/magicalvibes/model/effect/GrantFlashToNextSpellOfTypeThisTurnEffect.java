package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * One-shot effect: the next spell of {@code cardType} the resolving controller casts this turn can be
 * cast as though it had flash (Quicken). Tracked in {@code GameData.nextSpellFlashGrantsThisTurn},
 * consumed by the next matching spell cast and cleared at end of turn. The unlimited counterpart is
 * {@link GrantFlashToSpellsThisTurnEffect}.
 */
public record GrantFlashToNextSpellOfTypeThisTurnEffect(CardType cardType) implements CardEffect {
}

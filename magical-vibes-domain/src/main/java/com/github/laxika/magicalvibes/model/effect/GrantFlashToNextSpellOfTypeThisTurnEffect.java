package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * One-shot effect: the next spell of {@code cardType} the resolving controller casts this turn can be
 * cast as though it had flash (Quicken). Tracked in {@code GameData.nextSpellFlashGrantsThisTurn},
 * consumed by the next matching spell cast and cleared at end of turn. The unlimited counterpart is
 * {@link GrantFlashToSpellsThisTurnEffect}.
 *
 * <p>A {@code null} {@code cardType} matches any spell (Ride the Avalanche's "the next spell you cast").
 * Use {@link #nextSpell()} for that form.</p>
 *
 * @param cardType the card type the next spell must have, or {@code null} for any spell
 */
public record GrantFlashToNextSpellOfTypeThisTurnEffect(CardType cardType) implements CardEffect {

    /** The next spell of any type the resolving controller casts this turn can be cast with flash. */
    public static GrantFlashToNextSpellOfTypeThisTurnEffect nextSpell() {
        return new GrantFlashToNextSpellOfTypeThisTurnEffect(null);
    }
}

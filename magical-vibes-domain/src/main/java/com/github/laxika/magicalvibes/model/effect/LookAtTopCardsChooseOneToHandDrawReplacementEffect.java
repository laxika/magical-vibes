package com.github.laxika.magicalvibes.model.effect;

/**
 * Static draw replacement (CR 121.6): while a permanent carrying this effect is on the battlefield, if its controller
 * would draw a card, instead they look at the top {@code lookCount} cards of their library, put one of those cards
 * into their hand, and put the rest on the bottom of their library in any order, or into their graveyard with
 * {@code restToGraveyard}. The draw is fully replaced: the kept card is put into hand, not "drawn" (no draw triggers,
 * no empty-library loss when there is nothing to look at). Tomorrow, Azami's Familiar ({@code (3, false)}) and
 * Underrealm Lich ({@link #restIntoGraveyard}). Detected in {@code DrawService.resolveDrawCard}.
 *
 * @param lookCount       how many cards from the top are looked at
 * @param restToGraveyard whether the cards not put into hand go to the graveyard instead of the library bottom
 */
public record LookAtTopCardsChooseOneToHandDrawReplacementEffect(int lookCount, boolean restToGraveyard)
        implements CardEffect {

    public LookAtTopCardsChooseOneToHandDrawReplacementEffect {
        if (lookCount < 1) {
            throw new IllegalArgumentException("lookCount must be at least 1");
        }
    }

    /** "Look at the top N cards ... put one into your hand and the rest on the bottom of your library in any order." */
    public static LookAtTopCardsChooseOneToHandDrawReplacementEffect restOnBottom(int lookCount) {
        return new LookAtTopCardsChooseOneToHandDrawReplacementEffect(lookCount, false);
    }

    /** "Look at the top N cards ... put one into your hand and the rest into your graveyard." */
    public static LookAtTopCardsChooseOneToHandDrawReplacementEffect restIntoGraveyard(int lookCount) {
        return new LookAtTopCardsChooseOneToHandDrawReplacementEffect(lookCount, true);
    }
}

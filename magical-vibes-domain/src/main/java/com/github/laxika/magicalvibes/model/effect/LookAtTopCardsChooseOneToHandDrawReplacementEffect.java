package com.github.laxika.magicalvibes.model.effect;

/**
 * Static draw-replacement capability: while a permanent carrying this effect is on the battlefield, if
 * its controller would draw a card, instead they look at the top {@link #lookCount()} cards of their
 * library, put one of those cards into their hand, and put the rest in the destination selected by
 * {@link #restToGraveyard()}. The draw is fully replaced — the kept card is put into hand, not "drawn"
 * (no draw triggers, no empty-library loss when there is nothing to look at). Tomorrow, Azami's Familiar
 * ({@code lookCount() == 3}, rest to bottom) and Underrealm Lich ({@code lookCount() == 3}, rest to
 * graveyard). Detected in {@code DrawService.resolveDrawCard}; read as a fact, never {@code instanceof}-ed
 * on a concrete type.
 */
public interface LookAtTopCardsChooseOneToHandDrawReplacementEffect extends CardEffect {

    int lookCount();

    /** Whether the cards not chosen for hand go to the graveyard instead of the library bottom. */
    default boolean restToGraveyard() {
        return false;
    }
}

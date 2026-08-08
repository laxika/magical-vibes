package com.github.laxika.magicalvibes.model.effect;

/**
 * Static draw-replacement capability: while a permanent carrying this effect is on the battlefield, if
 * its controller would draw a card, instead they look at the top {@link #lookCount()} cards of their
 * library, put one of those cards into their hand, and put the rest on the bottom of their library in
 * any order. The draw is fully replaced — the kept card is put into hand, not "drawn" (no draw triggers,
 * no empty-library loss when there is nothing to look at). Tomorrow, Azami's Familiar
 * ({@code lookCount() == 3}). Detected in {@code DrawService.resolveDrawCard}; read as a fact, never
 * {@code instanceof}-ed on a concrete type.
 */
public interface LookAtTopCardsChooseOneToHandDrawReplacementEffect extends CardEffect {

    int lookCount();
}

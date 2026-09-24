package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/** Conjures a token-card copy of the supplied card into the current player target's hand. */
public record ConjureCardIntoTargetPlayerHandEffect(Card card) implements CardEffect {
}

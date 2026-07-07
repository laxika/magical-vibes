package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * Matches a card that has the given colour among its colours (multicolour-aware: uses the card's
 * full colour list rather than a single primary colour). Distinct from {@link CardColorPredicate},
 * which matches only cards whose single {@code getColor()} equals the colour. Used by Green Sun's
 * Zenith ("a green creature card"), where a multicoloured green creature must still qualify.
 */
public record CardHasColorPredicate(CardColor color) implements CardPredicate {
}
